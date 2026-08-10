package com.retrivedmods.wclient.game.module.world

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class AutoMineModule : Module("auto_mine", ModuleCategory.World) {
    private val mode by enumValue("Mode", Mode.SEQUENTIAL, Mode::class.java)
    private val range by floatValue("Range", 6.0f, 1.0f..10.0f)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[AutoMine] enabled") }

    enum class Mode { SEQUENTIAL, ALL, SMART }
}
