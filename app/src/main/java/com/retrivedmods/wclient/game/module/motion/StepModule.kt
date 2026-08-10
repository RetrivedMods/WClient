package com.retrivedmods.wclient.game.module.motion

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class StepModule : Module("step", ModuleCategory.Motion) {
    private val height by floatValue("Height", 2.0f, 1.0f..10.0f)
    private val mode by enumValue("Mode", Mode.VANILLA, Mode::class.java)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[Step] enabled") }

    enum class Mode { VANILLA, PACKET, SPOOF }
}
