package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.util.PacketDebugLog
import com.retrivedmods.wclient.util.setPacketField
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket

/**
 * Debug tool: prints the fields of every ITEM_USE (block place) InventoryTransactionPacket to
 * chat, tagged by where it came from:
 *  - [PlaceLog] - real packets the actual Minecraft client sends, e.g. when you manually place a
 *    block by hand, seen here via the normal beforePacketBound intercept pipeline.
 *  - [AutoPlaceLog] - packets our own modules (PistonCrystalModule/SurroundModule, via
 *    LocalPlayer.placeBlock) send directly with session.serverBound(...), which bypass that
 *    pipeline - these come through PacketDebugLog instead, toggled on/off by this module.
 * Enable this, place one block yourself and let an auto-place module try one too, then compare
 * the two logs field by field.
 */
class PacketLoggerModule : Module("packet_logger", ModuleCategory.Misc) {

    private var logPlacements by boolValue("log_placements", true)

    override fun onEnabled() {
        super.onEnabled()
        PacketDebugLog.enabled = true
    }

    override fun onDisabled() {
        super.onDisabled()
        PacketDebugLog.enabled = false
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || !logPlacements) return

        val packet = interceptablePacket.packet
        if (packet is InventoryTransactionPacket &&
            packet.transactionType == InventoryTransactionType.ITEM_USE &&
            packet.actionType == 0
        ) {
            logPlacement(packet)
        }
    }

    private fun logPlacement(packet: InventoryTransactionPacket) {
        val msg = buildString {
            append("§l§b[PlaceLog]§r\n")
            append("§eblockPosition: §f${packet.blockPosition}\n")
            append("§eblockFace: §f${packet.blockFace}\n")
            append("§eblockDefinition: §f${packet.blockDefinition}\n")
            append("§eclickPosition: §f${packet.clickPosition}\n")
            append("§eplayerPosition: §f${packet.playerPosition}\n")
            append("§eheadPosition: §f${packet.headPosition}\n")
            append("§ehotbarSlot: §f${packet.hotbarSlot}\n")
            append("§eitemInHand: §f${packet.itemInHand}\n")
            append("§eactions: §f${packet.actions}")
        }
        sendMessage(msg)
    }

    private fun sendMessage(msg: String) {
        val textPacket = TextPacket().apply {
            type = TextPacket.Type.RAW
            setPacketField("needsTranslation", false)
            setPacketField("message", msg)
            xuid = ""
            sourceName = ""
        }
        session.clientBound(textPacket)
    }
}
