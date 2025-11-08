package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.Entity
import com.retrivedmods.wclient.game.entity.EntityUnknown
import com.retrivedmods.wclient.game.entity.LocalPlayer
import com.retrivedmods.wclient.game.entity.MobList
import com.retrivedmods.wclient.game.entity.Player
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MoveEntityAbsolutePacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.sqrt

class PositionLoggerModule : Module("position_logger", ModuleCategory.Misc) {
    private val playersOnly by boolValue("closest_player", false)
    private val trackAllPlayers by boolValue("track_all", false)
    private val mobsOnly by boolValue("track_mobs", false)
    private val range by floatValue("range", 50f, 10f..500f)

    private var playerPosition = Vector3f.from(0f, 0f, 0f)
    private val entityPositions = mutableMapOf<Long, Vector3f>()

    private fun isValidTarget(entityId: Long): Boolean {
        val entity = session.level.entityMap[entityId] ?: return false

        return when (entity) {
            is LocalPlayer -> false
            is Player -> {
                if (mobsOnly) {
                    false
                } else if (playersOnly) {
                    !isBot(entity)
                } else {
                    !isBot(entity)
                }
            }
            is EntityUnknown -> {
                if (mobsOnly) {
                    entity.identifier in MobList.mobTypes
                } else if (playersOnly) {
                    false
                } else {
                    true
                }
            }
            else -> false
        }
    }

    private fun isBot(player: Player): Boolean {
        if (player is LocalPlayer) return false
        val playerList = session.level.playerMap[player.uuid] ?: return false // Changed: treat unknown players as real players
        return playerList.name.isBlank()
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            playerPosition = packet.position

            if (trackAllPlayers) {
                // Track all valid entities within range
                entityPositions.forEach { (entityId, entityPos) ->
                    val distance = calculateDistance(playerPosition, entityPos)
                    if (distance <= range && isValidTarget(entityId)) {
                        val entity = session.level.entityMap[entityId] ?: return@forEach
                        logEntityPosition(entity, entityPos, distance)
                    }
                }
            } else {
                // Original closest-only logic
                var closestEntityId: Long? = null
                var closestDistance = Float.MAX_VALUE
                var closestEntityPosition: Vector3f? = null

                entityPositions.forEach { (entityId, entityPos) ->
                    val distance = calculateDistance(playerPosition, entityPos)
                    if (distance <= range && distance < closestDistance && isValidTarget(entityId)) {
                        closestDistance = distance
                        closestEntityId = entityId
                        closestEntityPosition = entityPos
                    }
                }

                if (closestEntityId != null && closestEntityPosition != null) {
                    val entity = session.level.entityMap[closestEntityId] ?: return
                    logEntityPosition(entity, closestEntityPosition!!, closestDistance)
                }
            }
        }

        // Continue tracking entity positions
        if (packet is MoveEntityAbsolutePacket) {
            entityPositions[packet.runtimeEntityId] = packet.position
        }
    }

    private fun logEntityPosition(entity: Entity, position: Vector3f, distance: Float) {
        val roundedPosition = position.roundUpCoordinates()
        val roundedDistance = ceil(distance)
        val direction = getCompassDirection(playerPosition, position)

        when (entity) {
            is Player -> {
                val playerInfo = session.level.playerMap[entity.uuid]
                val playerName = playerInfo?.name ?: entity.username
                val xuid = playerInfo?.xuid ?: "Unknown"

                sendMessage("""
                    §l§b[Tracer]
                    §r§ePlayer: §a$playerName
                    §eXUID: §7$xuid 
                    §ePosition: §a$roundedPosition
                    §eDistance: §c$roundedDistance
                    §eDirection: §d$direction
                """.trimIndent())
            }
            is EntityUnknown -> {
                sendMessage("""
                    §l§b[Tracer]
                    §r§eEntity: §a${entity.identifier}
                    §ePosition: §a$roundedPosition
                    §eDistance: §c$roundedDistance
                    §eDirection: §d$direction
                """.trimIndent())
            }
        }
    }

    // Calculate Euclidean distance
    private fun calculateDistance(from: Vector3f, to: Vector3f): Float {
        val dx = from.x - to.x
        val dy = from.y - to.y
        val dz = from.z - to.z
        return sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
    }

    // Convert position to rounded-up string format
    private fun Vector3f.roundUpCoordinates(): String {
        val roundedX = ceil(this.x).toInt()
        val roundedY = ceil(this.y).toInt()
        val roundedZ = ceil(this.z).toInt()
        return "($roundedX, $roundedY, $roundedZ)"
    }

    // Get compass direction between two points
    private fun getCompassDirection(from: Vector3f, to: Vector3f): String {
        val dx = to.x - from.x
        val dz = to.z - from.z
        val angle = Math.toDegrees(atan2(dx, dz).toDouble()).let {
            ((it + 360) % 360)
        }

        return when {
            angle >= 337.5 || angle < 22.5 -> "N"
            angle >= 22.5 && angle < 67.5 -> "NE"
            angle >= 67.5 && angle < 112.5 -> "E"
            angle >= 112.5 && angle < 157.5 -> "SE"
            angle >= 157.5 && angle < 202.5 -> "S"
            angle >= 202.5 && angle < 247.5 -> "SW"
            angle >= 247.5 && angle < 292.5 -> "W"
            else -> "NW"
        }
    }

    // Send message in chat with Minecraft Bedrock colors
    private fun sendMessage(msg: String) {
        val textPacket = TextPacket().apply {
            type = TextPacket.Type.RAW
            isNeedsTranslation = false
            message = msg
            xuid = ""
            sourceName = ""
        }
        session.clientBound(textPacket)
    }
}