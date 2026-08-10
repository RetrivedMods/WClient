package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class BlockClutchModule : Module("block_clutch", ModuleCategory.Combat) {
    private val mode by enumValue("Mode", Mode.BLOCK, Mode::class.java)
    private val healthThreshold by floatValue("Health", 8.0f, 1.0f..20.0f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() {
        session.displayClientMessage("§a[BlockClutch] enabled")
    }

    enum class Mode { BLOCK, SHIELD, BOTH }
}
