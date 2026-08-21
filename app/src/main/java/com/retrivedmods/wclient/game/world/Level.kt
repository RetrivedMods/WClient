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
import org.cloudburstmc.protocol.bedrock.packet.SubChunkPacket
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
                    // blob-cache chunk loading (isCachingEnabled) still isn't supported - servers
                    // using that will still leave us blind. But isRequestSubChunks (the modern,
                    // now near-universal loading path where LevelChunkPacket only carries heightmap/
                    // biome data and actual block data arrives later via individual SubChunkPacket
                    // responses) IS now handled below, in the SubChunkPacket branch - so don't
                    // bail out here for that case, just skip the (block-data-less) LevelChunkPacket
                    // itself.
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

            is SubChunkPacket -> {
                if (!session.isBlockMappingInitialized) return

                // This is the response to the (real, physical) client's own SubChunkRequestPacket,
                // which we don't send ourselves - we're just observing it pass through the relay.
                // Protocol semantics (best effort, not verified against a live packet capture):
                // `centerPosition` is the request's base (chunkX, sectionY, chunkZ) - sectionY as
                // a *section* index (worldBlockY shr 4), not a block Y - and each SubChunkData's
                // own `position` is a small relative offset from that base. In the overwhelmingly
                // common case (a single chunk column, vertical prefetch only) x/z offsets are 0
                // and only the y offset varies, but we honor x/z too in case a server ever sends
                // neighboring columns this way.
                for (subChunkData in packet.subChunks) {
                    val data = subChunkData.data ?: continue
                    if (data.readableBytes() <= 0) continue // no data = not found/failed, nothing to store

                    val offset = subChunkData.position
                    val chunkX = packet.centerPosition.x + offset.x
                    val chunkZ = packet.centerPosition.z + offset.z
                    val worldSectionY = packet.centerPosition.y + offset.y

                    val chunk = chunks.getOrPut(Chunk.hash(chunkX, chunkZ)) {
                        Chunk(chunkX, chunkZ, is384WorldSupported, session.blockMapping)
                    }
                    // Chunk.readSubChunk()/getBlockAt() index their sectionStorage array from 0,
                    // with 384-worlds offset by +4 sections (-64 world Y -> array index 0) - see
                    // Chunk.getBlockAt()'s `(yIn + 64) shr 4` for the equivalent block-Y-based math.
                    val sectionIndex = if (is384WorldSupported) worldSectionY + 4 else worldSectionY
                    try {
                        chunk.readSubChunk(sectionIndex, data.duplicate())
                    } catch (e: Exception) {
                        // malformed/unexpected subchunk data for this protocol version - skip it
                        // rather than crash the relay
                    }
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

}