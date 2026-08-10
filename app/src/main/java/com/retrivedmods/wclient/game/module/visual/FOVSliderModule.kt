package com.retrivedmods.wclient.game.module.visual

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class FOVSliderModule : Module("fov_slider", ModuleCategory.Visual) {
    private val fov by intValue("FOV", 100, 30..160)
    private val dynamic by boolValue("Dynamic", true)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[FOVSlider] $fov") }
}
