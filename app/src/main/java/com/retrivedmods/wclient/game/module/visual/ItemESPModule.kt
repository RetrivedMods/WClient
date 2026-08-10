package com.retrivedmods.wclient.game.module.visual

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class ItemESPModule : Module("item_esp", ModuleCategory.Visual) {
    private val mode by enumValue("Mode", Mode.OUTLINE, Mode::class.java)
    private val distance by floatValue("Distance", 32.0f, 5.0f..64.0f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[ItemESP] enabled") }

    enum class Mode { OUTLINE, GLOW, BOX }
}
