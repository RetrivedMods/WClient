package com.retrivedmods.wclient.game.module.motion

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class DashModule : Module("dash", ModuleCategory.Motion) {
    private val distance by floatValue("Distance", 5.0f, 1.0f..20.0f)
    private val cooldown by intValue("Cooldown", 1000, 100..5000)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[Dash] enabled") }
}
