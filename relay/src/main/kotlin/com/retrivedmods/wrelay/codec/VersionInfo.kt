package com.retrivedmods.wrelay.codec

import org.cloudburstmc.protocol.bedrock.codec.BedrockCodec

data class VersionInfo(
    val protocolVersion: Int,
    val minecraftVersion: String,
    val codec: BedrockCodec,
    val isExactMatch: Boolean = true
) {
    override fun toString(): String {
        return "Minecraft $minecraftVersion (Protocol $protocolVersion)${if (!isExactMatch) " [Closest Match]" else ""}"
    }
}

object VersionInfoProvider {

    fun getVersionInfo(protocolVersion: Int): VersionInfo {
        val exactCodec = CodecRegistry.getCodecByProtocol(protocolVersion)
        
        return if (exactCodec != null) {
            val minecraftVersion = CodecRegistry.getMinecraftVersion(protocolVersion) 
                ?: exactCodec.minecraftVersion
            VersionInfo(
                protocolVersion = protocolVersion,
                minecraftVersion = minecraftVersion,
                codec = exactCodec,
                isExactMatch = true
            )
        } else {
            val closestCodec = CodecRegistry.getClosestCodec(protocolVersion)
            val minecraftVersion = CodecRegistry.getMinecraftVersion(closestCodec.protocolVersion)
                ?: closestCodec.minecraftVersion
            VersionInfo(
                protocolVersion = protocolVersion,
                minecraftVersion = minecraftVersion,
                codec = closestCodec,
                isExactMatch = false
            )
        }
    }

}