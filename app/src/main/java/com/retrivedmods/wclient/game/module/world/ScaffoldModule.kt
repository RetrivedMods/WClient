package com.retrivedmods.wclient.game.module.world

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class ScaffoldModule : Module("scaffold", ModuleCategory.World) {
    private val mode by enumValue("Mode", Mode.NORMAL, Mode::class.java)
    private val blocksAhead by intValue("Blocks Ahead", 1, 0..5)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[Scaffold] enabled") }

    enum class Mode { NORMAL, EXPAND, TOWER }
}
