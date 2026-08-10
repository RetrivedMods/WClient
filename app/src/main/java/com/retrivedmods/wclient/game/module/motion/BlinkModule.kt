package com.retrivedmods.wclient.game.module.motion

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class BlinkModule : Module("blink", ModuleCategory.Motion) {
    private val mode by enumValue("Mode", Mode.NORMAL, Mode::class.java)
    private val maxDelay by intValue("Max Delay", 1000, 100..5000)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[Blink] enabled") }
    override fun onDisabled() { session.displayClientMessage("§c[Blink] disabled") }

    enum class Mode { NORMAL, DELAY, PULSE }
}
