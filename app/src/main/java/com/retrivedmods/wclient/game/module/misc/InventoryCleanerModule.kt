package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class InventoryCleanerModule : Module("inventory_cleaner", ModuleCategory.Misc) {
    private val mode by enumValue("Mode", Mode.BASIC, Mode::class.java)
    private val autoDrop by boolValue("Auto Drop", true)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[InventoryCleaner] enabled") }

    enum class Mode { BASIC, SMART, STRICT }
}
