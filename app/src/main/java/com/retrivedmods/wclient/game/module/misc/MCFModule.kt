package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class MCFModule : Module("mcf", ModuleCategory.Misc) {
    private val mode by enumValue("Mode", Mode.FRIEND, Mode::class.java)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[MCF] enabled") }

    enum class Mode { FRIEND, ENEMY, ALL }
}
