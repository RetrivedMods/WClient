package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class AutoWeaponModule : Module("auto_weapon", ModuleCategory.Combat) {
    private val mode by enumValue("Mode", Mode.SWORD, Mode::class.java)
    private val autoSwitchBack by boolValue("Auto Switch Back", true)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() {
        session.displayClientMessage("§a[AutoWeapon] enabled")
    }

    enum class Mode { SWORD, AXE, BEST }
}
