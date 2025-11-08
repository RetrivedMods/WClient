package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.Entity
import com.retrivedmods.wclient.game.entity.EntityUnknown
import com.retrivedmods.wclient.game.entity.LocalPlayer
import com.retrivedmods.wclient.game.entity.MobList
import com.retrivedmods.wclient.game.entity.Player
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class KillauraModule : Module("killaura", ModuleCategory.Combat) {

    private var playersOnly by boolValue("players_only", true)
    private var mobsOnly by boolValue("mobs_only", false)
    private var tpAuraEnabled by boolValue("tp_aura", false)

    private var rangeValue by floatValue("range", 3.7f, 2f..12f)
    private var attackInterval by intValue("delay", 5, 1..20)
    private var cpsValue by intValue("cps", 10, 1..20)
    private var boost by intValue("packets", 1, 1..10)
    private var tpspeed by intValue("tp_speed", 1000, 100..2000)

    private var distanceToKeep by floatValue("keep_distance", 2.0f, 1f..12f)

    private var lastAttackTime = 0L
    private var tpCooldown = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val currentTime = System.currentTimeMillis()
            val minAttackDelay = 1000L / cpsValue

            if (packet.tick % attackInterval == 0L && (currentTime - lastAttackTime) >= minAttackDelay) {
                val closestEntities = searchForClosestEntities()
                if (closestEntities.isEmpty()) return

                closestEntities.forEach { entity ->
                    // Handle teleportation once when TP Aura is enabled
                    if (tpAuraEnabled && (currentTime - tpCooldown) >= tpspeed) {
                        teleportTo(entity, distanceToKeep)
                        tpCooldown = currentTime
                    }

                    repeat(boost) {
                        session.localPlayer.attack(entity)
                    }

                    lastAttackTime = currentTime
                }
            }
        }
    }

    private fun teleportTo(entity: Entity, distance: Float) {
        val targetPosition = entity.vec3Position
        val playerPosition = session.localPlayer.vec3Position

        val direction = Vector3f.from(
            targetPosition.x - playerPosition.x,
            0f,  // No modification to Y-axis
            targetPosition.z - playerPosition.z
        )

        val length = direction.length()
        val normalizedDirection = if (length != 0f) {
            Vector3f.from(direction.x / length, 0f, direction.z / length)
        } else {
            direction
        }

        val newPosition = Vector3f.from(
            targetPosition.x - normalizedDirection.x * distance,
            playerPosition.y,
            targetPosition.z - normalizedDirection.z * distance
        )

        val movePlayerPacket = MovePlayerPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            position = newPosition
            rotation = entity.vec3Rotation
            mode = MovePlayerPacket.Mode.NORMAL
            isOnGround = false
            ridingRuntimeEntityId = 0
            tick = session.localPlayer.tickExists
        }

        session.clientBound(movePlayerPacket)
    }



    private fun Entity.isTarget(): Boolean {
        return when (this) {
            is LocalPlayer -> false
            is Player -> {
                if (mobsOnly) {
                    false
                } else if (playersOnly) {
                    !this.isBot()
                } else {
                    !this.isBot()
                }
            }
            is EntityUnknown -> {
                if (mobsOnly) {
                    isMob()
                } else if (playersOnly) {
                    false
                } else {
                    true
                }
            }
            else -> false
        }
    }

    private fun EntityUnknown.isMob(): Boolean {
        return this.identifier in MobList.mobTypes
    }

    private fun Player.isBot(): Boolean {
        if (this is LocalPlayer) return false
        val playerList = session.level.playerMap[this.uuid] ?: return false // Changed: treat unknown players as real players
        return playerList.name.isBlank()
    }

    private fun searchForClosestEntities(): List<Entity> {
        return session.level.entityMap.values
            .filter { entity -> entity.distance(session.localPlayer) < rangeValue && entity.isTarget() }
    }
}