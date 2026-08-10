package com.retrivedmods.wclient.game.module.visual

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class TracersModule : Module("tracers", ModuleCategory.Visual) {
    private val mode by enumValue("Mode", Mode.DIRECTION, Mode::class.java)
    private val color by enumValue("Color", ColorMode.PLAYER, ColorMode::class.java)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[Tracers] enabled") }

    enum class Mode { DIRECTION, PLAYER, BOTH }
    enum class ColorMode { PLAYER, TEAM, STATIC }
}
