package com.retrivedmods.wclient.game.module.visual

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class AnimationsModule : Module("animations", ModuleCategory.Visual) {
    private val mode by enumValue("Mode", Mode.SMOOTH, Mode::class.java)
    private val speed by floatValue("Speed", 1.0f, 0.1f..5.0f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[Animations] enabled") }

    enum class Mode { SMOOTH, NONE, VANILLA }
}
