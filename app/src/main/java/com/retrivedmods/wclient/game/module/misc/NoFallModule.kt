package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class NoFallModule : Module("no_fall", ModuleCategory.Misc) {
    private val mode by enumValue("Mode", Mode.PACKET, Mode::class.java)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[NoFall] enabled") }

    enum class Mode { PACKET, GROUND, EDIT }
}
