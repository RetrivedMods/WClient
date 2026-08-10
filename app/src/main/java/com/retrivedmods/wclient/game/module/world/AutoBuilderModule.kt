package com.retrivedmods.wclient.game.module.world

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class AutoBuilderModule : Module("auto_builder", ModuleCategory.World) {
    private val mode by enumValue("Mode", Mode.PILLAR, Mode::class.java)
    private val speed by intValue("Speed", 10, 1..20)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[AutoBuilder] enabled") }

    enum class Mode { PILLAR, WALL, BRIDGE, SPHERE }
}
