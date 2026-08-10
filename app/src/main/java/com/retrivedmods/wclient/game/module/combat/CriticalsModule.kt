package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class CriticalsModule : Module("criticals", ModuleCategory.Combat) {
    private val mode by enumValue("Mode", Mode.PACKET, Mode::class.java)
    private val chance by floatValue("Chance", 100.0f, 1.0f..100.0f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() {
        session.displayClientMessage("§a[Criticals] enabled")
    }

    enum class Mode { PACKET, MINI_JUMP, POSITION }
}
