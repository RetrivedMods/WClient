package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.packet.SetEntityMotionPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class AntiKnockbackModule : Module("anti_knockback", ModuleCategory.Combat) {

    private val knockbackThreshold by floatValue("threshold", 0.1f, 0.1f..1.0f)
    private var lastValidMotion = Vector3f.ZERO

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is SetEntityMotionPacket && packet.runtimeEntityId == session.localPlayer.runtimeEntityId) {
            val motionDelta = packet.motion.length() - lastValidMotion.length()
            if (motionDelta > knockbackThreshold) {
                interceptablePacket.intercept()

                session.clientBound(SetEntityMotionPacket().apply {
                    runtimeEntityId = session.localPlayer.runtimeEntityId
                    motion = lastValidMotion
                })
            } else {
                lastValidMotion = packet.motion
            }
            return
        }

        if (packet is PlayerAuthInputPacket) {
            if (packet.motion.length() < knockbackThreshold) {
                lastValidMotion = Vector3f.from(
                    session.localPlayer.motionX,
                    session.localPlayer.motionY,
                    session.localPlayer.motionZ
                )
            }
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        lastValidMotion = Vector3f.ZERO
    }
}