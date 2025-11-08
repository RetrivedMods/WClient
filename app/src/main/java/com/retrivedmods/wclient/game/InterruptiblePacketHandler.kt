package com.retrivedmods.wclient.game

import com.retrivedmods.wclient.game.InterceptablePacket
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket

interface InterruptiblePacketHandler {

    fun beforePacketBound(interceptablePacket: InterceptablePacket)

    fun afterPacketBound(packet: BedrockPacket) {}

    fun onDisconnect(reason: String) {}

}