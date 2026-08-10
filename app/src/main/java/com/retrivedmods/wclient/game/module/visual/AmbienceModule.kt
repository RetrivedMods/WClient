package com.retrivedmods.wclient.game.module.visual

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class AmbienceModule : Module("ambience", ModuleCategory.Visual) {
    private val mode by enumValue("Mode", Mode.RED, Mode::class.java)
    private val intensity by floatValue("Intensity", 0.5f, 0.1f..1.0f)
    private val particles by boolValue("Particles", true)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[Ambience] enabled") }

    enum class Mode { RED, BLUE, GREEN, PURPLE, ORANGE, PINK }
}
