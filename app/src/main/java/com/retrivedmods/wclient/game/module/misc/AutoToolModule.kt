package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class AutoToolModule : Module("auto_tool", ModuleCategory.Misc) {
    private val mode by enumValue("Mode", Mode.SWORD, Mode::class.java)
    private val autoSwitchBack by boolValue("Auto Switch Back", true)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[AutoTool] enabled") }

    enum class Mode { SWORD, PICKAXE, AXE, SHOVEL, ALL }
}
