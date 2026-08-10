package com.retrivedmods.wclient.game.module.world

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class XRayModule : Module("xray", ModuleCategory.World) {
    private val mode by enumValue("Mode", Mode.ORES, Mode::class.java)
    private val distance by intValue("Distance", 32, 8..128)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[XRay] enabled") }

    enum class Mode { ORES, ALL, CUSTOM }
}
