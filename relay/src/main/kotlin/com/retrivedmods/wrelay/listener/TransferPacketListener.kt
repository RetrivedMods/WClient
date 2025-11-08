package com.retrivedmods.wrelay.listener

import com.retrivedmods.wrelay.WRelaySession
import com.retrivedmods.wrelay.address.WAddress
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.TransferPacket

@Suppress("MemberVisibilityCanBePrivate")
class TransferPacketListener(
    val novaRelaySession: WRelaySession
) : WRelayPacketListener {

    override fun beforeServerBound(packet: BedrockPacket): Boolean {
        if (packet is TransferPacket) {
            val remoteAddress = WAddress(packet.address, packet.port)
            val localAddress = novaRelaySession.wRelay.localAddress
            novaRelaySession.wRelay.remoteAddress = remoteAddress
            novaRelaySession.clientBoundImmediately(TransferPacket().apply {
                address = localAddress.hostName
                port = localAddress.port
            })

            novaRelaySession.wRelay.wRelaySession = null
            return true
        }
        return false
    }

}