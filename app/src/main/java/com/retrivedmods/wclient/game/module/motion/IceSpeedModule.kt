package com.retrivedmods.wclient.game.module.motion

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class IceSpeedModule : Module("ice_speed", ModuleCategory.Motion) {
    private val multiplier by floatValue("Multiplier", 1.5f, 1.0f..5.0f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[IceSpeed] enabled") }
}
