package com.retrivedmods.wclient.game.module.motion

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class InvWalkModule : Module("inv_walk", ModuleCategory.Motion) {
    private val speed by floatValue("Speed", 0.5f, 0.1f..2.0f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[InvWalk] enabled") }
}
