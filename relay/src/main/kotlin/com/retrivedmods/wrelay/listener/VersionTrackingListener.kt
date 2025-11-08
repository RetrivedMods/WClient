package com.retrivedmods.wrelay.listener

import com.retrivedmods.wrelay.codec.VersionInfoProvider
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.RequestNetworkSettingsPacket

class VersionTrackingListener(
    private val onVersionDetected: (protocolVersion: Int, minecraftVersion: String) -> Unit = { _, _ -> }
) : WRelayPacketListener {

    private var versionDetected = false

    override fun beforeClientBound(packet: BedrockPacket): Boolean {
        if (!versionDetected && packet is RequestNetworkSettingsPacket) {
            val protocolVersion = packet.protocolVersion
            val versionInfo = VersionInfoProvider.getVersionInfo(protocolVersion)
            
            onVersionDetected(protocolVersion, versionInfo.minecraftVersion)
            versionDetected = true
        }
        return false
    }
}