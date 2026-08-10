package com.retrivedmods.wclient.game.module.visual

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class ChamsModule : Module("chams", ModuleCategory.Visual) {
    private val color by enumValue("Color", Mode.RED, Mode::class.java)
    private val alpha by floatValue("Alpha", 0.5f, 0.1f..1.0f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[Chams] enabled") }

    enum class Mode { RED, BLUE, GREEN, YELLOW, PURPLE }
}
