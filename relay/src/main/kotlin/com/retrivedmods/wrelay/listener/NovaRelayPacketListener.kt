package com.retrivedmods.wrelay.listener

import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket

interface WRelayPacketListener {

    fun beforeClientBound(packet: BedrockPacket): Boolean {
        return false
    }

    fun beforeServerBound(packet: BedrockPacket): Boolean {
        return false
    }

    fun afterClientBound(packet: BedrockPacket) {}

    fun afterServerBound(packet: BedrockPacket) {}

    fun onDisconnect(reason: String) {}

}