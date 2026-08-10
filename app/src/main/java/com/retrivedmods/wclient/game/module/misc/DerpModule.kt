package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class DerpModule : Module("derp", ModuleCategory.Misc) {
    private val mode by enumValue("Mode", Mode.HEADLESS, Mode::class.java)
    private val speed by intValue("Speed", 10, 1..20)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[Derp] enabled") }

    enum class Mode { HEADLESS, SPIN, JITTER }
}
