package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.*
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.math.*

class AutoHvHModule : Module("Auto HVH", ModuleCategory.Combat) {

    private var maxRange by floatValue("Range", 500f, 50f..500f)
    private var baseSpeed by floatValue("Speed", 2.5f, 0.5f..20.0f)
    private var jitterPower by floatValue("Jitter", 0.1f, 0f..0.5f)
    private var strafeRadius by floatValue("StrafeRadius", 2f, 0f..10f)
    private var cps by intValue("CPS", 20, 1..30)
    private var packetsPerAttack by intValue("Packets", 3, 1..5)
    private var yOffset by floatValue("YOffset", 0.0f, -10.0f..10.0f)
    private var noClip by boolValue("NoClip", false)


    private var verticalRange by floatValue("VerticalRange", 12f, 0f..20f)
    private var verticalSpeed by floatValue("VerticalSpeed", 2f, 0.1f..10f)
    private var enableVerticalMotion by boolValue("AutoHvH Motion", true)

    private var lastMoveTime = 0L
    private var lastAttackTime = 0L
    private var angle = 0.0

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) return

        val now = System.currentTimeMillis()
        val moveDelta = now - lastMoveTime
        val attackDelta = now - lastAttackTime

        val player = session.localPlayer
        val target = findTarget() ?: return

        val attackInterval = 1000L / cps
        if (attackDelta >= attackInterval) {
            repeat(packetsPerAttack) {
                player.attack(target)
            }
            lastAttackTime = now
        }

        if (moveDelta < 20) return
        lastMoveTime = now

        val playerPos = player.vec3Position
        val targetPos = target.vec3Position

        val verticalOscillation = if (enableVerticalMotion) getVerticalOffset() else 0f
        val dy = (targetPos.y + yOffset + verticalOscillation) - playerPos.y
        val dx = targetPos.x - playerPos.x
        val dz = targetPos.z - playerPos.z
        val distance = sqrt(dx * dx + dy * dy + dz * dz)

        val speedScale = baseSpeed + (distance / 3.5)
        val moveVec = if (distance > 4) {

            val direction = Vector3f.from(dx, dy, dz).normalize()
            direction.mul(speedScale).add(jitterVec())
        } else {

            angle = (angle + speedScale * 40) % 360
            val rad = Math.toRadians(angle)
            val offsetX = cos(rad) * strafeRadius
            val offsetZ = sin(rad) * strafeRadius
            Vector3f.from(
                offsetX.toFloat() + jitter(),
                jitter(),
                offsetZ.toFloat() + jitter()
            )
        }

        val motion = if (player.vec3Position.y < 0.5f) {
            Vector3f.from(0f, 1.2f, 0f)
        } else moveVec

        val newPosition = player.vec3Position.add(motion)

        if (!noClip && isPathBlocked(player.vec3Position, newPosition)) return

        session.clientBound(MovePlayerPacket().apply {
            runtimeEntityId = player.runtimeEntityId
            position = newPosition
            rotation = player.vec3Rotation
            mode = MovePlayerPacket.Mode.NORMAL
            onGround = false
            ridingRuntimeEntityId = 0
            tick = player.tickExists
        })

    }

    private fun getVerticalOffset(): Float {
        val time = System.currentTimeMillis() / 1000.0
        return (sin(time * verticalSpeed) * verticalRange).toFloat()
    }

    private fun findTarget(): Entity? {
        return session.level.entityMap.values
            .filter { it != session.localPlayer && it is Player && !isBot(it) }
            .filter { it.vec3Position.distance(session.localPlayer.vec3Position) <= maxRange }
            .minByOrNull { it.vec3Position.distance(session.localPlayer.vec3Position) }
    }

    private fun isBot(entity: Entity): Boolean {
        if (entity !is Player || entity is LocalPlayer) return false
        val data = session.level.playerMap[entity.uuid]
        return data?.name.isNullOrEmpty()
    }

    private fun jitter(): Float =
        ((Math.random() - 0.5) * 2 * jitterPower).toFloat()

    private fun jitterVec(): Vector3f =
        Vector3f.from(jitter().toDouble(), jitter().toDouble(), jitter().toDouble())

    private fun isPathBlocked(start: Vector3f, end: Vector3f): Boolean {
        // TODO: Implement raycast or collision if needed
        return false
    }
}