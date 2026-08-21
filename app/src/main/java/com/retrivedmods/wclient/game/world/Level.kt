package com.retrivedmods.wclient.game.world

import com.retrivedmods.wclient.game.GameSession
import com.retrivedmods.wclient.game.entity.Entity
import com.retrivedmods.wclient.game.entity.EntityUnknown
import com.retrivedmods.wclient.game.entity.Item
import com.retrivedmods.wclient.game.entity.Player
import com.retrivedmods.wclient.game.registry.BlockDefinition
import com.retrivedmods.wclient.game.registry.UnknownBlockDefinition
import com.retrivedmods.wclient.game.world.chunk.Chunk
import org.cloudburstmc.protocol.bedrock.packet.AddEntityPacket
import org.cloudburstmc.protocol.bedrock.packet.AddItemEntityPacket
import org.cloudburstmc.protocol.bedrock.packet.AddPlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.ChangeDimensionPacket
import org.cloudburstmc.protocol.bedrock.packet.ChunkRadiusUpdatedPacket
import org.cloudburstmc.protocol.bedrock.packet.LevelChunkPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerListPacket
import org.cloudburstmc.protocol.bedrock.packet.RemoveEntityPacket
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket
import org.cloudburstmc.protocol.bedrock.packet.TakeItemEntityPacket
import org.cloudburstmc.protocol.bedrock.packet.UpdateBlockPacket
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.math.vector.Vector3i
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.pow

@Suppress("MemberVisibilityCanBePrivate")
class Level(val session: GameSession) {

    val entityMap = ConcurrentHashMap<Long, Entity>()

    val playerMap = ConcurrentHashMap<UUID, PlayerListPacket.Entry>()

    // --- world/chunk block tracking -----------------------------------------------------------
    // Ported (with real changes, see Chunk/ChunkSection/BlockStorage) from ProtoHax. Only the
    // "normal" full LevelChunkPacket path is handled - servers using blob (disk) caching or the
    // newer per-subchunk request system (SubChunkPacket) won't have their blocks tracked here,
    // since that needs a blob cache we don't implement. UpdateBlockPacket (individual block
    // changes) IS handled, so blocks placed/broken after a chunk loads stay accurate.

    val chunks = ConcurrentHashMap<Long, Chunk>()

    var is384WorldSupported = false
        private set

    var viewDistance = -1
        private set

    fun onDisconnect() {
        entityMap.clear()
        playerMap.clear()
        chunks.clear()
    }

    fun onPacketBound(packet: BedrockPacket) {
        when (packet) {
            is StartGamePacket -> {
                entityMap.clear()
                playerMap.clear()
                chunks.clear()

                is384WorldSupported = try {
                    // 384 height world was introduced in Minecraft 1.18
                    val parts = packet.vanillaVersion.split(".")
                    parts.size >= 2 && parts[0] == "1" && (parts[1].toIntOrNull() ?: 0) >= 18
                } catch (e: Exception) {
                    true
                }
            }

            is LevelChunkPacket -> {
                if (!session.isBlockMappingInitialized) {
                    // shouldn't normally happen (StartGamePacket sets blockMapping before Level
                    // sees it), but guard anyway since a missing mapping would crash chunk parsing
                    return
                }

                if (packet.isCachingEnabled || packet.isRequestSubChunks) {
                    // blob-cache / newer subchunk-request chunk loading isn't supported, see note above
                    return
                }

                val chunk = Chunk(packet.chunkX, packet.chunkZ, is384WorldSupported, session.blockMapping)
                try {
                    // duplicate() gives us an independent reader index over the same underlying
                    // memory (refcount shared with the original packet), so parsing here can never
                    // disturb packet.data's own reader index / the relay's forwarding of the real
                    // packet to the client.
                    val buf = packet.data.duplicate()
                    chunk.read(buf, packet.subChunksLength)
                    chunks[chunk.hash] = chunk
                } catch (e: Exception) {
                    // malformed/unexpected chunk data for this protocol version - skip it rather
                    // than crash the relay
                }
            }

            is UpdateBlockPacket -> {
                if (packet.dataLayer == 0) {
                    setBlockIdAt(
                        packet.blockPosition.x,
                        packet.blockPosition.y,
                        packet.blockPosition.z,
                        packet.definition.runtimeId
                    )
                }
            }

            is ChunkRadiusUpdatedPacket -> {
                viewDistance = packet.radius
            }

            is ChangeDimensionPacket -> {
                chunks.clear()
            }

            is AddEntityPacket -> {
                val entity = EntityUnknown(
                    packet.runtimeEntityId,
                    packet.uniqueEntityId,
                    packet.identifier
                ).apply {
                    move(packet.position)
                    rotate(packet.rotation)
                    handleSetData(packet.metadata)
                    handleSetAttribute(packet.attributes)
                }
                entityMap[packet.runtimeEntityId] = entity
            }

            is AddItemEntityPacket -> {
                val entity = Item(packet.runtimeEntityId, packet.uniqueEntityId).apply {
                    move(packet.position)
                    handleSetData(packet.metadata)
                }
                entityMap[packet.runtimeEntityId] = entity
            }

            is AddPlayerPacket -> {
                val entity = Player(
                    packet.runtimeEntityId,
                    packet.uniqueEntityId,
                    packet.uuid,
                    packet.username
                ).apply {
                    move(packet.position)
                    rotate(packet.rotation)
                    handleSetData(packet.metadata)
                }
                entityMap[packet.runtimeEntityId] = entity
            }

            is RemoveEntityPacket -> {
                val entityToRemove =
                    entityMap.values.find { it.uniqueEntityId == packet.uniqueEntityId } ?: return
                entityMap.remove(entityToRemove.runtimeEntityId)
            }

            is TakeItemEntityPacket -> {
                entityMap.remove(packet.itemRuntimeEntityId)
            }

            is PlayerListPacket -> {
                val add = packet.action == PlayerListPacket.Action.ADD
                packet.entries.forEach {
                    if (add) {
                        playerMap[it.uuid] = it
                    } else {
                        playerMap.remove(it.uuid)
                    }
                }
            }

            else -> {
                entityMap.values.forEach { entity ->
                    entity.onPacketBound(packet)
                }
            }
        }
    }

    /**
     * Approximates vanilla explosion damage falloff for every tracked entity (and any [extraEntities]
     * not currently in [entityMap], e.g. the local player) around [center].
     *
     * This does NOT account for block occlusion/exposure (WClient has no local world/chunk state to
     * raycast against), so it always assumes full exposure (1.0). Real in-game damage will be lower
     * whenever blocks are between the explosion and the target. Treat the result as an upper-bound
     * estimate for target/placement selection, not an exact value.
     */
    fun simulateExplosionDamage(
        center: Vector3f,
        size: Float,
        extraEntities: List<Entity> = emptyList(),
        damageCallback: (Entity, Float) -> Unit
    ) {
        val searchRadiusSq = (size * 2).pow(2)

        fun evaluate(entity: Entity) {
            val distSq = entity.distanceSq(center)
            if (distSq >= searchRadiusSq) return

            val distance = entity.distance(center) / size
            if (distance <= 1f) {
                val impact = 1f - distance
                val damage = ((impact * impact + impact) / 2f) * 8f * size + 1f
                damageCallback(entity, damage)
            }
        }

        entityMap.values.forEach(::evaluate)
        extraEntities.forEach(::evaluate)
    }

    // --- block query/update helpers -----------------------------------------------------------

    fun getChunkAt(chunkX: Int, chunkZ: Int): Chunk? = chunks[Chunk.hash(chunkX, chunkZ)]

    fun isChunkLoaded(x: Int, z: Int): Boolean = chunks.containsKey(Chunk.hash(x shr 4, z shr 4))

    /**
     * Runtime id of the block at the given world coordinates, or the air runtime id if the
     * containing chunk hasn't been loaded/tracked (see the notes on the LevelChunkPacket handling
     * above for when that can happen).
     */
    fun getBlockIdAt(x: Int, y: Int, z: Int): Int {
        val chunk = getChunkAt(x shr 4, z shr 4)
            ?: return if (session.isBlockMappingInitialized) session.blockMapping.airId else 0
        return chunk.getBlockAt(x and 0x0f, y, z and 0x0f)
    }

    fun getBlockIdAt(pos: Vector3i): Int = getBlockIdAt(pos.x, pos.y, pos.z)

    fun getBlockAt(x: Int, y: Int, z: Int): BlockDefinition {
        if (!session.isBlockMappingInitialized) return UnknownBlockDefinition(0)
        return session.blockMapping.getDefinition(getBlockIdAt(x, y, z))
    }

    fun getBlockAt(pos: Vector3i): BlockDefinition = getBlockAt(pos.x, pos.y, pos.z)

    fun setBlockIdAt(x: Int, y: Int, z: Int, runtimeId: Int) {
        val chunk = getChunkAt(x shr 4, z shr 4) ?: return
        chunk.setBlockAt(x and 0x0f, y, z and 0x0f, runtimeId)
    }

    fun isAir(x: Int, y: Int, z: Int): Boolean = getBlockAt(x, y, z).identifier == "minecraft:air"

    fun isAir(pos: Vector3i): Boolean = isAir(pos.x, pos.y, pos.z)

    /**
     * Bedrock places a new block adjacent to whatever *existing* block you "click" - not directly
     * at the position you name - so block-placing modules (Surround, PistonCrystal, ...) need to
     * find a real, currently non-air neighbor of [pos] to click, and which face of that neighbor
     * points back at [pos]. Checks straight down first (the common "place on the ground" case),
     * then up, then the four horizontal neighbors.
     *
     * Returns null if [pos] itself isn't currently air (already occupied, or the chunk simply
     * isn't tracked/loaded yet - see the LevelChunkPacket handling notes above for when that
     * happens) or if none of its neighbors are known to be solid, e.g. floating in open air.
     *
     * @return (position of the block to click, Bedrock face index of that block to click:
     *   0=down,1=up,2=north,3=south,4=west,5=east) or null
     */
    fun findPlacementReference(pos: Vector3i): Pair<Vector3i, Int>? {
        if (!isAir(pos)) return null

        // (offset to the neighboring block, face of THAT block which points back at pos)
        val candidates = listOf(
            Vector3i.from(0, -1, 0) to 1, // below   -> click its UP face
            Vector3i.from(0, 1, 0) to 0,  // above   -> click its DOWN face
            Vector3i.from(1, 0, 0) to 4,  // east    -> click its WEST face
            Vector3i.from(-1, 0, 0) to 5, // west    -> click its EAST face
            Vector3i.from(0, 0, 1) to 2,  // south   -> click its NORTH face
            Vector3i.from(0, 0, -1) to 3  // north   -> click its SOUTH face
        )

        for ((offset, face) in candidates) {
            val neighborPos = Vector3i.from(pos.x + offset.x, pos.y + offset.y, pos.z + offset.z)
            if (!isAir(neighborPos)) {
                return neighborPos to face
            }
        }
        return null
    }

}