package com.retrivedmods.wclient.game.module.visual

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.data.Effect
import org.cloudburstmc.protocol.bedrock.packet.MobEffectPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class FullbrightModule : Module("fullbright", ModuleCategory.Visual) {

    private val amplifierValue by intValue("amplifier", 1, 1..5)
    private val mode by enumValue("mode", FullbrightMode.NIGHT_VISION, FullbrightMode::class.java)
    private val fadeTime by intValue("fadeTime", 0, 0..5000)
    private val antiFlash by boolValue("antiFlash", true)
    private val renewInterval by intValue("renewInterval", 20, 5..100)
    
    enum class FullbrightMode { NIGHT_VISION, GAMMA, BOTH }

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated) {
            session.clientBound(MobEffectPacket().apply {
                runtimeEntityId = session.localPlayer.runtimeEntityId
                event = MobEffectPacket.Event.REMOVE
                effectId = Effect.NIGHT_VISION
            })
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            if (session.localPlayer.tickExists % 20 == 0L) {
                session.clientBound(MobEffectPacket().apply {
                    runtimeEntityId = session.localPlayer.runtimeEntityId
                    event = MobEffectPacket.Event.ADD
                    effectId = Effect.NIGHT_VISION
                    amplifier = amplifierValue
                    isParticles = false
                    duration = 360000
                })
            }
        }
    }
}
