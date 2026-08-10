package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class AutoSoupModule : Module("auto_soup", ModuleCategory.Combat) {
    private val healthThreshold by floatValue("Health", 10.0f, 1.0f..20.0f)
    private val delay by intValue("Delay", 100, 50..500)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() {
        session.displayClientMessage("§a[AutoSoup] enabled")
    }
}
