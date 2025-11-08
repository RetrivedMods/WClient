package com.retrivedmods.wrelay.listener

import com.retrivedmods.wrelay.WRelaySession
import com.retrivedmods.wrelay.definition.Definitions
import org.cloudburstmc.protocol.bedrock.data.definitions.ItemDefinition
import org.cloudburstmc.protocol.bedrock.data.definitions.SimpleNamedDefinition
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.CameraPresetsPacket
import org.cloudburstmc.protocol.bedrock.packet.StartGamePacket
import org.cloudburstmc.protocol.common.NamedDefinition
import org.cloudburstmc.protocol.common.SimpleDefinitionRegistry

@Suppress("MemberVisibilityCanBePrivate")
class GamingPacketHandler(
    val wRelaySession: WRelaySession
) : WRelayPacketListener {

    override fun beforeClientBound(packet: BedrockPacket): Boolean {
        if (packet is org.cloudburstmc.protocol.bedrock.packet.DisconnectPacket) {
            println("Server sent disconnect: ${packet.kickMessage}")
        }
        return false
    }

    override fun beforeServerBound(packet: BedrockPacket): Boolean {
        if (packet is StartGamePacket) {
            try {
                println("Start game packet received, setting definitions")
                Definitions.itemDefinitions = SimpleDefinitionRegistry.builder<ItemDefinition>()
                    .addAll(packet.itemDefinitions)
                    .build()

                wRelaySession.client!!.peer.codecHelper.itemDefinitions = Definitions.itemDefinitions
                wRelaySession.server.peer.codecHelper.itemDefinitions = Definitions.itemDefinitions

                if (packet.isBlockNetworkIdsHashed) {
                    wRelaySession.client!!.peer.codecHelper.blockDefinitions = Definitions.blockDefinitionsHashed
                    wRelaySession.server.peer.codecHelper.blockDefinitions = Definitions.blockDefinitionsHashed
                } else {
                    wRelaySession.client!!.peer.codecHelper.blockDefinitions = Definitions.blockDefinitions
                    wRelaySession.server.peer.codecHelper.blockDefinitions = Definitions.blockDefinitions
                }
                println("Definitions set successfully")
            } catch (e: Exception) {
                println("Failed to set definitions: ${e.message}")
                e.printStackTrace()
            }
        }
        if (packet is CameraPresetsPacket) {
            try {
                println("Camera presets packet received")
                val cameraDefinitions =
                    SimpleDefinitionRegistry.builder<NamedDefinition>()
                        .addAll(List(packet.presets.size) {
                            SimpleNamedDefinition(packet.presets[it].identifier, it)
                        })
                        .build()

                wRelaySession.client!!.peer.codecHelper.cameraPresetDefinitions = cameraDefinitions
                wRelaySession.server.peer.codecHelper.cameraPresetDefinitions = cameraDefinitions
                println("Camera presets set successfully")
            } catch (e: Exception) {
                println("Failed to set camera presets: ${e.message}")
                e.printStackTrace()
            }
        }
        return false
    }

}