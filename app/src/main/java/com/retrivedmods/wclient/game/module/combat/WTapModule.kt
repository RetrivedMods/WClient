package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class WTapModule : Module("w_tap", ModuleCategory.Combat) {
    private val mode by enumValue("Mode", Mode.W_TAP, Mode::class.java)
    private val chance by floatValue("Chance", 100.0f, 1.0f..100.0f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() {
        session.displayClientMessage("§a[WTap] enabled")
    }

    enum class Mode { W_TAP, S_TAP, RESET }
}
