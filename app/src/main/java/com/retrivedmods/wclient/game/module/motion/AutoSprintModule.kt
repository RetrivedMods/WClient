package com.retrivedmods.wclient.game.module.motion

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class AutoSprintModule : Module("auto_sprint", ModuleCategory.Motion) {
    private val mode by enumValue("Mode", Mode.LEGIT, Mode::class.java)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[AutoSprint] enabled") }

    enum class Mode { LEGIT, OMNI, BACKWARDS }
}
