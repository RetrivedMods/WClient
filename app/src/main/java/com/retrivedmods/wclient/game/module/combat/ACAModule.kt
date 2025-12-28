package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.*
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.math.cos
import kotlin.math.sin

class ACAModule : Module("ACA", ModuleCategory.Combat) {

    private var playersOnly by boolValue("players_only", true)
    private var mobsOnly by boolValue("mobs_only", true)
    private var tpAuraEnabled by boolValue("tp_aura", true)

    private var rangeValue by floatValue("range", 7.0f, 2f..10f)
    private var cpsValue by intValue("cps", 12, 5..20)

    private var tpSpeed by intValue("tp_speed", 150, 50..2000)
    private var tpYLevel by intValue("yOffset", 0, -10..10)

    private var distanceToKeep by floatValue("keep_distance", 2.0f, 1f..5f)

    private var lastAttackTime = 0L
    private var tpCooldown = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val currentTime = System.currentTimeMillis()
            val attackDelay = 1000L / cpsValue.coerceAtLeast(1)

            if ((currentTime - lastAttackTime) >= attackDelay) {
                val closestEntities = searchForClosestEntities()
                if (closestEntities.isEmpty()) return

                closestEntities.forEach { entity ->
                    if (tpAuraEnabled && (currentTime - tpCooldown) >= tpSpeed) {
                        teleportTo(entity, distanceToKeep, tpYLevel)
                        tpCooldown = currentTime
                    }

                    session.localPlayer.attack(entity)
                    lastAttackTime = currentTime
                }
            }
        }
    }

    private fun teleportTo(entity: Entity, distance: Float, yOffset: Int) {
        val targetPosition = entity.vec3Position
        val targetYaw = Math.toRadians(entity.vec3Rotation.y.toDouble()).toFloat()
        val direction = Vector3f.from(sin(targetYaw), 0f, -cos(targetYaw))
        val length = direction.length()
        val normalizedDirection = if (length != 0f) Vector3f.from(direction.x / length, 0f, direction.z / length) else direction

        val newPosition = Vector3f.from(
            targetPosition.x + normalizedDirection.x * distance,
            targetPosition.y + yOffset,
            targetPosition.z + normalizedDirection.z * distance
        )

        val movePlayerPacket = MovePlayerPacket().apply {
            runtimeEntityId = session.localPlayer.runtimeEntityId
            position = newPosition
            rotation = entity.vec3Rotation
            mode = MovePlayerPacket.Mode.NORMAL
            onGround = false
            tick = session.localPlayer.tickExists
        }

        session.clientBound(movePlayerPacket)
    }

    private fun Entity.isTarget(): Boolean {
        return when (this) {
            is LocalPlayer -> false
            is Player -> playersOnly && !this.isBot()
            is EntityUnknown -> mobsOnly && isMob()
            else -> false
        }
    }

    private fun EntityUnknown.isMob(): Boolean {
        return this.identifier in MobList.mobTypes
    }

    private fun Player.isBot(): Boolean {
        if (this is LocalPlayer) return false
        val playerList = session.level.playerMap[this.uuid] ?: return true
        return playerList.name.isBlank()
    }

    private fun searchForClosestEntities(): List<Entity> {
        return session.level.entityMap.values
            .filter { it.distance(session.localPlayer) < rangeValue && it.isTarget() }
            .sortedBy { it.distance(session.localPlayer) }
    }
}