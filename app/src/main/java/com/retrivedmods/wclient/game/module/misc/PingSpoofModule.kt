package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory

class PingSpoofModule : Module("ping_spoof", ModuleCategory.Misc) {
    private val ping by intValue("Ping", 100, 0..1000)
    private val mode by enumValue("Mode", Mode.STATIC, Mode::class.java)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
    }

    override fun onEnabled() { session.displayClientMessage("§a[PingSpoof] $ping") }

    enum class Mode { STATIC, RANDOM, DYNAMIC }
}
