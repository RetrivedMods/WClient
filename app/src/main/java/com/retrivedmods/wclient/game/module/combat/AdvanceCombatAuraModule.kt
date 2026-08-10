package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class AdvanceCombatAuraModule : Module("advance_combat_aura", ModuleCategory.Combat) {
    private val range by floatValue("Range", 4.5f, 1.0f..8.0f)
    private val mode by enumValue("Mode", Mode.SINGLE, Mode::class.java)
    private val speed by intValue("Speed", 10, 1..20)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() {
        session.displayClientMessage("§a[AdvanceCombatAura] enabled")
    }

    enum class Mode { SINGLE, MULTI, SWITCH }
}
