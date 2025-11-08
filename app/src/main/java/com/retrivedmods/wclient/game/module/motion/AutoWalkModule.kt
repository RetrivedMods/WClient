package com.retrivedmods.wclient.game.module.motion

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket

class AutoWalkModule : Module("auto_walk", ModuleCategory.Motion) {

    private val glideSpeed by floatValue("walk_speed", 0.7f, 0.1f..2.0f)
    private val fallSpeed by floatValue("fall_speed", -0.125f, -0.5f..-0.05f)
    private var lastGlideTime = 0L
    private val glideInterval = 50L // 50ms between motion updates

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastGlideTime >= glideInterval) {
                lastGlideTime = currentTime

                val yaw = Math.toRadians(packet.rotation.y.toDouble())
                val motionX = -Math.sin(yaw) * glideSpeed
                val motionZ = Math.cos(yaw) * glideSpeed

                val motionPacket = SetEntityMotionPacket().apply {
                    runtimeEntityId = session.localPlayer.runtimeEntityId
                    motion = Vector3f.from(
                        motionX.toFloat(),
                        fallSpeed,
                        motionZ.toFloat()
                    )
                }
                session.clientBound(motionPacket)
            }
        }
    }
}