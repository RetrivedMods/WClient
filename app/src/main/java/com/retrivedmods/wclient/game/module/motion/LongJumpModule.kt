package com.retrivedmods.wclient.game.module.motion

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class LongJumpModule : Module("long_jump", ModuleCategory.Motion) {
    private val height by floatValue("Height", 2.0f, 1.0f..10.0f)
    private val mode by enumValue("Mode", Mode.MOTION, Mode::class.java)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[LongJump] enabled") }

    enum class Mode { MOTION, PACKET, VANILLA }
}
