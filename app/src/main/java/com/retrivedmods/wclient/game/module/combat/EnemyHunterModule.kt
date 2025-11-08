package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.*
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.PlayerAuthInputData
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.math.*

class EnemyHunterModule : Module("EnemyHunter", ModuleCategory.Combat) {

    private var maxRange by floatValue("Range", 500f, 50f..500f)
    private var baseSpeed by floatValue("Speed", 1.5f, 0.5f..20.0f)
    private var jitterPower by floatValue("Jitter", 0.1f, 0f..0.5f)
    private var strafeRadius by floatValue("Radius", 2f, 0f..10f)
    private var cps by intValue("CPS", 20, 1..50)
    private var packetsPerAttack by intValue("Packets", 3, 1..5)
    private var yOffset by floatValue("YOffset", 0.0f, -10.0f..10.0f)
    private var noClip by boolValue("NoClip", false)

    private var lastMoveTime = 0L
    private var lastAttackTime = 0L
    private var angle = 0.0
    private var isPathBlockedFlag = false

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) return

        // Mirror LockHeed-style collision sensing
        isPathBlockedFlag = packet.inputData.contains(PlayerAuthInputData.HORIZONTAL_COLLISION)

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

        val dx = targetPos.x - playerPos.x
        val dy = (targetPos.y + yOffset) - playerPos.y
        val dz = targetPos.z - playerPos.z
        val distance = sqrt(dx * dx + dy * dy + dz * dz)

        val speedScale = baseSpeed + (distance / 3.5)
        val moveVec = if (distance > 4) {
            val direction = Vector3f.from(dx.toFloat(), dy.toFloat(), dz.toFloat()).normalize()
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
            Vector3f.from(0f, 1.2f, 0f) // Anti-void jump
        } else moveVec

        val newPosition = player.vec3Position.add(motion)

        // Respect NoClip toggle with real-time collision like LockHeed
        if (!noClip && isPathBlockedFlag) return

        // --- Smooth Rotation ---
        val lookDX = targetPos.x - playerPos.x
        val lookDY = (targetPos.y + yOffset) - playerPos.y
        val lookDZ = targetPos.z - playerPos.z

        val horizontalDist = sqrt(lookDX * lookDX + lookDZ * lookDZ)
        val targetYaw = Math.toDegrees(atan2(-lookDX, lookDZ).toDouble()).toFloat()
        val targetPitch = Math.toDegrees((-atan2(lookDY, horizontalDist)).toDouble()).toFloat()

        val oldYaw = player.rotationYaw
        val oldPitch = player.rotationPitch
        val smoothFactor = 0.35f

        val newYaw = interpolateAngle(oldYaw, targetYaw, smoothFactor)
        val newPitch = interpolateAngle(oldPitch, targetPitch, smoothFactor)

        player.rotationYaw = newYaw
        player.rotationYawHead = newYaw
        player.rotationPitch = newPitch

        val rotationVec = Vector3f.from(newYaw, newPitch, 0f)

        session.clientBound(MovePlayerPacket().apply {
            runtimeEntityId = player.runtimeEntityId
            position = newPosition
            rotation = rotationVec
            mode = MovePlayerPacket.Mode.NORMAL
            onGround = false
            ridingRuntimeEntityId = 0
            tick = player.tickExists
        })

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
        // Legacy stub kept to preserve format; live collision uses PlayerAuthInputData flag.
        return isPathBlockedFlag
    }

    private fun interpolateAngle(old: Float, target: Float, factor: Float): Float {
        var delta = (target - old) % 360.0f
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        return (old + delta * factor) % 360.0f
    }
}
