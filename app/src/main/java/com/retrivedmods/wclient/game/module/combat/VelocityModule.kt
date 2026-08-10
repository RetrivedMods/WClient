package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class VelocityModule : Module("velocity", ModuleCategory.Combat) {
    private val horizontal by floatValue("Horizontal", 0.0f, 0.0f..100.0f)
    private val vertical by floatValue("Vertical", 0.0f, 0.0f..100.0f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() {
        session.displayClientMessage("§a[Velocity] enabled")
    }

    override fun onDisabled() {
        session.displayClientMessage("§c[Velocity] disabled")
    }
}
