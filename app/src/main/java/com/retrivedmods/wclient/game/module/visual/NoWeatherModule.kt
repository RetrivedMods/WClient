package com.retrivedmods.wclient.game.module.visual

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class NoWeatherModule : Module("no_weather", ModuleCategory.Visual) {
    private val mode by enumValue("Mode", Mode.CLEAR, Mode::class.java)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[NoWeather] enabled") }

    enum class Mode { CLEAR, SUNNY, NIGHT }
}
