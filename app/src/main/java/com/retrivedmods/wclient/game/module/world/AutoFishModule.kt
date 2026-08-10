package com.retrivedmods.wclient.game.module.world

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class AutoFishModule : Module("auto_fish", ModuleCategory.World) {
    private val fast by boolValue("Fast", true)
    private val autoCast by boolValue("Auto Cast", true)
    private var tickCounter = 0

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return
        if (interceptablePacket.packet is PlayerAuthInputPacket) tickCounter++
    }

    override fun onEnabled() { session.displayClientMessage("§a[AutoFish] enabled") }
}
