package com.retrivedmods.wclient.game.module.world

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class NukerModule : Module("nuker", ModuleCategory.World) {
    private val radius by intValue("Radius", 5, 1..15)
    private val mode by enumValue("Mode", Mode.ALL, Mode::class.java)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[Nuker] enabled") }

    enum class Mode { ALL, SELECTED, FLAT }
}
