package com.retrivedmods.wclient.game.world

import com.retrivedmods.wclient.game.GameSession
import com.retrivedmods.wclient.game.entity.Entity
import com.retrivedmods.wclient.game.entity.EntityUnknown
import com.retrivedmods.wclient.game.entity.Item
import com.retrivedmods.wclient.game.entity.Player
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.AddEntityPacket
import org.cloudburstmc.protocol.bedrock.packet.AddItemEntityPacket
import org.cloudburstmc.protocol.bedrock.packet.AddPlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.ChangeDimensionPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerListPacket
import org.cloudburstmc.protocol.bedrock.packet.RemoveEntityPacket
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket
import org.cloudburstmc.protocol.bedrock.packet.TakeItemEntityPacket
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.pow

@Suppress("MemberVisibilityCanBePrivate")
class Level(val session: GameSession) {

    val entityMap = ConcurrentHashMap<Long, Entity>()

    val playerMap = ConcurrentHashMap<UUID, PlayerListPacket.Entry>()

    // Kept for compatibility with ProtoHax-style world/dimension handling.
    var dimension: Int = 0
        private set

    fun onDisconnect() {
        entityMap.clear()
        playerMap.clear()
    }

    fun onPacketBound(packet: BedrockPacket) {
        when (packet) {
            is StartGamePacket -> {
                entityMap.clear()
                playerMap.clear()
                dimension = packet.dimensionId
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
                val entity = Item(
                    packet.runtimeEntityId,
                    packet.uniqueEntityId
                ).apply {
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

            is ChangeDimensionPacket -> {
                dimension = packet.dimension
            }

            else -> {
                entityMap.values.forEach { entity ->
                    entity.onPacketBound(packet)
                }
            }
        }
    }

    /**
     * Removes an entity locally and sends the corresponding RemoveEntityPacket
     * to the server, matching ProtoHax's Level.removeEntity behavior.
     */
    fun removeEntity(entity: Entity) {
        if (entityMap.remove(entity.runtimeEntityId) == null) return

        session.serverBound(RemoveEntityPacket().apply {
            uniqueEntityId = entity.uniqueEntityId
        })
    }

    /**
     * Simulates vanilla-style explosion damage against tracked entities and
     * additional entities supplied by the caller.
     */
    fun simulateExplosionDamage(
        center: Vector3f,
        size: Float,
        extraEntities: List<Entity>,
        damageCallback: (Entity, Float) -> Unit
    ) {
        if (size <= 0f) return

        val explosionSearchSizeSq = (size * 2).pow(2)

        entityMap.values
            .filter { it.distanceSq(center) < explosionSearchSizeSq }
            .forEach { entity ->
                val distance = entity.distance(center) / size

                if (distance <= 1f) {
                    val impact = 1f - distance
                    val damage = ((impact * impact + impact) / 2f) * 8f * size + 1f
                    damageCallback(entity, damage)
                }
            }

        extraEntities
            .filter { it.distanceSq(center) < explosionSearchSizeSq }
            .forEach { entity ->
                val distance = entity.distance(center) / size

                if (distance <= 1f) {
                    val impact = 1f - distance
                    val damage = ((impact * impact + impact) / 2f) * 8f * size + 1f
                    damageCallback(entity, damage)
                }
            }
    }
}
