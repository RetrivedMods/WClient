package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class SuperKnockbackModule : Module("super_knockback", ModuleCategory.Combat) {
    private val multiplier by floatValue("Multiplier", 2.0f, 1.0f..5.0f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() {
        session.displayClientMessage("§a[SuperKnockback] enabled")
    }

    override fun onDisabled() {
        session.displayClientMessage("§c[SuperKnockback] disabled")
    }
}
