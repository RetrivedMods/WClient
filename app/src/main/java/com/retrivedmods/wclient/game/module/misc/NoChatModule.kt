package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.TextPacket

class NoChatModule : Module("no_chat", ModuleCategory.Misc) {

    private val blockAllChat by boolValue("block_all", false)
    private val blockPlayerChat by boolValue("block_player_chat", true)
    private val blockSystemChat by boolValue("block_system_chat", false)
    private val blockWhispers by boolValue("block_whispers", false)
    private val blockAnnouncements by boolValue("block_announcements", false)
    private val blockJoinLeaveMessages by boolValue("block_join_leave", false)

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is TextPacket) {
            if (blockAllChat) {
                interceptablePacket.intercept()
                return
            }

            when (packet.type) {
                TextPacket.Type.CHAT -> {
                    if (blockPlayerChat) {
                        interceptablePacket.intercept()
                        return
                    }
                }
                TextPacket.Type.SYSTEM -> {
                    if (blockSystemChat) {
                        interceptablePacket.intercept()
                        return
                    }
                }
                TextPacket.Type.WHISPER -> {
                    if (blockWhispers) {
                        interceptablePacket.intercept()
                        return
                    }
                }
                TextPacket.Type.ANNOUNCEMENT -> {
                    if (blockAnnouncements) {
                        interceptablePacket.intercept()
                        return
                    }
                }
                else -> {}
            }

            session.level.playerMap.values.forEach { player ->
                if (blockPlayerChat && packet.message.contains(player.name)) {
                    interceptablePacket.intercept()
                    return
                }
            }

            if (blockJoinLeaveMessages && (packet.message.contains("joined") || packet.message.contains("left"))) {
                interceptablePacket.intercept()
                return
            }
        }
    }
}