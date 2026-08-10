package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.PlayerHotbarPacket

class HotbarSwitcherModule : Module("switcher", ModuleCategory.Combat) {


    private var startSlot by intValue("start_slot", 0, 0..8)
    private var endSlot by intValue("end_slot", 8, 0..8)
    private var switchDelay by intValue("switch_delay", 100, 10..1000)
    private var loopMode by boolValue("loop", true)
    private var reverseMode by boolValue("reverse", false)


    private var currentSlot = 0
    private var lastSwitchTime = 0L
    private var direction = 1

    override fun onEnabled() {
        currentSlot = startSlot
        lastSwitchTime = System.currentTimeMillis()
        direction = if (reverseMode) -1 else 1


        switchToSlot(currentSlot)
    }

    override fun onDisabled() {

    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val now = System.currentTimeMillis()
        if (now - lastSwitchTime < switchDelay) return


        currentSlot += direction


        if (currentSlot > endSlot) {
            if (loopMode) {
                currentSlot = startSlot
            } else {
                currentSlot = endSlot
                direction = -1
            }
        } else if (currentSlot < startSlot) {
            if (loopMode) {
                currentSlot = endSlot
            } else {
                currentSlot = startSlot
                direction = 1
            }
        }


        switchToSlot(currentSlot)
        lastSwitchTime = now
    }

    private fun switchToSlot(slot: Int) {
        val packet = PlayerHotbarPacket()
        packet.selectedHotbarSlot = slot
        packet.containerId = 0
        packet.setSelectHotbarSlot(true)

        session.clientBound(packet)
    }
}
