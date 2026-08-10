package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.*
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class InfiniteAuraModule : Module("InfiniteAura", ModuleCategory.Combat) {

    private var cpsValue by intValue("CPS", 12, 1..20)
    private var behindOffset by floatValue("Behind Offset", 2.0f, 0.5f..5.0f)
    private var playersOnly by boolValue("Players Only", true)
    private var mobsOnly by boolValue("Mobs Only", false)
    private var silentLagbacks by boolValue("Silent Lagbacks", true)
    private var rangeValue by floatValue("Range", 20f, 5f..50f)
    private var alwaysSwing by boolValue("Always Swing", true)
    private var multiTarget by boolValue("Multi Target", true)
    private var maxTargets by intValue("Max Targets", 3, 1..10)
    private var autoDisableOnLagback by boolValue("Auto Disable Lagback", true)

    private var lastAttackTime = 0L
    private var lastLagbackTime = 0L
    private var serverSidePos: Vector3f? = null

    override fun onEnabled() {
        serverSidePos = session.localPlayer.vec3Position
    }

    override fun onDisabled() {
        serverSidePos = session.localPlayer.vec3Position
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        val packet = interceptablePacket.packet as? PlayerAuthInputPacket ?: return

        val now = System.currentTimeMillis()
        val player = session.localPlayer
        val currentPos = player.vec3Position


        if (serverSidePos != null && serverSidePos!!.distance(currentPos) > 50f) {
            if (silentLagbacks) {
                serverSidePos = currentPos
                lastLagbackTime = now
            } else {
                isEnabled = false
                return
            }
        }

        if (lastLagbackTime > 0 && now - lastLagbackTime < 100) return

        val target = findTarget() ?: run {
            serverSidePos = currentPos
            packet.position = currentPos
            return
        }


        val targetYaw = ((target.vec3Rotation.y + 90f + 360f) % 360f).let { if (it > 180f) it - 360f else it }
        val yawRad = Math.toRadians(targetYaw.toDouble()).toFloat()

        val targetPos = Vector3f.from(target.vec3Position.x, target.vec3Position.y - 1.62f, target.vec3Position.z)
        val behindPos = Vector3f.from(
            targetPos.x - cos(yawRad) * behindOffset,
            targetPos.y,
            targetPos.z - sin(yawRad) * behindOffset
        )


        val delayMs = 1000L / cpsValue
        if (now - lastAttackTime >= delayMs) {
            player.swing()
            player.attack(target)
            lastAttackTime = now
        }


        serverSidePos = behindPos
        packet.position = behindPos
        packet.rotation = calculateRotationToTarget(behindPos, targetPos)
    }

    private fun calculateRotationToTarget(from: Vector3f, to: Vector3f): Vector3f {
        val deltaX = to.x - from.x
        val deltaZ = to.z - from.z
        val yaw = ((Math.toDegrees(atan2(deltaZ, deltaX).toDouble()) - 90.0 + 360.0) % 360.0).toFloat()
        return Vector3f.from(session.localPlayer.vec3Rotation.x, yaw, session.localPlayer.vec3Rotation.z)
    }

    private fun findTarget(): Entity? {
        return session.level.entityMap.values
            .filter { it.isTarget() }.minByOrNull { it.distance(session.localPlayer) }
    }

    private fun Entity.isTarget(): Boolean {
        return when (this) {
            is LocalPlayer -> false
            is Player -> {
                if (playersOnly && isBot(this)) false else true
            }
            is EntityUnknown -> !playersOnly
            else -> false
        }
    }

    private fun isBot(player: Player): Boolean {
        return session.level.playerMap[player.uuid]?.name.isNullOrBlank()
    }
}