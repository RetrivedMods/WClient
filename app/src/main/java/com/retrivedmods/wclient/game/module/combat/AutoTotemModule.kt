package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.LocalPlayer
import com.retrivedmods.wclient.game.utils.constants.Attribute
import org.cloudburstmc.protocol.bedrock.data.inventory.ItemData
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class AutoTotemModule : Module("Auto Totem", ModuleCategory.Combat) {

    private var delay by intValue("Delay", 100, 0..1000)
    private var onlyWhenLowHealth by boolValue("Only When Low Health", false)
    private var healthThreshold by intValue("Health Threshold", 10, 1..20)
    private var replaceOffhand by boolValue("Replace Offhand", true)

    private var lastTotemTime = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) return

        val now = System.currentTimeMillis()
        if (now - lastTotemTime < delay) return

        val player: LocalPlayer = session.localPlayer

        if (onlyWhenLowHealth) {
            val health = player.attributes[Attribute.HEALTH]?.value ?: 20f
            if (health > healthThreshold) return
        }

        val offhandItem = player.inventory.offhand
        if (isTotem(offhandItem)) return

        val totemSlot = findTotemInInventory(player) ?: return

        if (!replaceOffhand && offhandItem != ItemData.AIR) return

        moveTotemToOffhand(player, totemSlot)
        lastTotemTime = now
    }

    private fun isTotem(item: ItemData): Boolean {
        return item != ItemData.AIR && item.definition?.identifier == "minecraft:totem_of_undying"
    }

    private fun findTotemInInventory(player: LocalPlayer): Int? {
        val inv = player.inventory
        for (i in 0 until 36) {
            val item = inv.content[i]
            if (isTotem(item)) return i
        }
        return null
    }

    private fun moveTotemToOffhand(player: LocalPlayer, sourceSlot: Int) {
        try {
            val inv = player.inventory
            val sourceItem = inv.content[sourceSlot]
            if (!isTotem(sourceItem)) return

            inv.moveItem(sourceSlot, 40, inv, session)
        } catch (_: Exception) {

        }
    }
}