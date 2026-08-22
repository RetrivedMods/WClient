package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.util.setPacketField
import org.cloudburstmc.protocol.bedrock.data.inventory.transaction.InventoryTransactionType
import org.cloudburstmc.protocol.bedrock.packet.InventoryTransactionPacket
import org.cloudburstmc.protocol.bedrock.packet.TextPacket

/**
 * Debug tool: prints the fields of every real ITEM_USE (block place) InventoryTransactionPacket
 * to chat as it passes through the relay - including ones the real Minecraft client sends when
 * you manually place a block by hand. Enable this, place one block yourself, and compare the
 * logged fields against what PistonCrystalModule/SurroundModule construct, field by field, to
 * find whatever's still wrong/missing.
 *
 * Doesn't see our own modules' auto-placed packets - those go out via session.serverBound(...)
 * directly, bypassing the intercept pipeline this module listens on, same as every other module
 * here (see e.g. PositionLoggerModule for the same TextPacket-based logging pattern).
 */
class PacketLoggerModule : Module("packet_logger", ModuleCategory.Misc) {

    private var logPlacements by boolValue("log_placements", true)

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
