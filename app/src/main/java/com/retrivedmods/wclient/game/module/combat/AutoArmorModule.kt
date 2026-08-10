package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class AutoArmorModule : Module("auto_armor", ModuleCategory.Combat) {
    private val delay by intValue("Delay", 2, 0..10)
    private val onlyBetter by boolValue("Only Better", true)
    private var tickCounter = 0

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) return
        tickCounter++
    }

    override fun onEnabled() {
        session.displayClientMessage("§a[AutoArmor] enabled")
    }

    override fun onDisabled() {
        session.displayClientMessage("§c[AutoArmor] disabled")
    }
}
