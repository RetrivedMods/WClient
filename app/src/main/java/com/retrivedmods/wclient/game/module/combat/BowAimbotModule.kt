package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class BowAimbotModule : Module("bow_aimbot", ModuleCategory.Combat) {
    private val range by floatValue("Range", 50.0f, 10.0f..100.0f)
    private val silent by boolValue("Silent", true)
    private val predict by boolValue("Predict", true)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() {
        session.displayClientMessage("§a[BowAimbot] enabled")
    }

    override fun onDisabled() {
        session.displayClientMessage("§c[BowAimbot] disabled")
    }
}
