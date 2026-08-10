package com.retrivedmods.wclient.game.module.world

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class PortalClickerModule : Module("portal_clicker", ModuleCategory.World) {
    private val mode by enumValue("Mode", Mode.INSTANT, Mode::class.java)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[PortalClicker] enabled") }

    enum class Mode { INSTANT, FAST, NORMAL }
}
