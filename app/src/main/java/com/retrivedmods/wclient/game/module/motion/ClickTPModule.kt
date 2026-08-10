package com.retrivedmods.wclient.game.module.motion

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class ClickTPModule : Module("click_tp", ModuleCategory.Motion) {
    private val mode by enumValue("Mode", Mode.VANILLA, Mode::class.java)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[ClickTP] enabled") }

    enum class Mode { VANILLA, PACKET, BLINK }
}
