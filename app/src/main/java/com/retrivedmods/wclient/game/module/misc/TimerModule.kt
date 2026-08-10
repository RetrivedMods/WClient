package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class TimerModule : Module("timer", ModuleCategory.Misc) {
    private val speed by floatValue("Speed", 2.0f, 0.1f..10.0f)
    private val mode by enumValue("Mode", Mode.NORMAL, Mode::class.java)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[Timer] ${speed}x") }

    enum class Mode { NORMAL, PULSE, DELAY }
}
