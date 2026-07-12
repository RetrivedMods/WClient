package com.retrivedmods.inkloader

import com.retrivedmods.inkloader.listener.InkLoaderPacketListener
import io.netty.util.internal.PlatformDependent
import net.kyori.adventure.text.Component
import org.cloudburstmc.protocol.bedrock.BedrockClientSession
import org.cloudburstmc.protocol.bedrock.BedrockPeer
import org.cloudburstmc.protocol.bedrock.BedrockServerSession
import org.cloudburstmc.protocol.bedrock.netty.BedrockPacketWrapper
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacket
import org.cloudburstmc.protocol.bedrock.packet.BedrockPacketHandler
import org.cloudburstmc.protocol.bedrock.packet.UnknownPacket
import java.util.*


class InkLoaderSession internal constructor(
    peer: BedrockPeer,
    subClientId: Int,
    val inkLoader: InkLoader
) {

    val server = ServerSession(peer, subClientId)

    var client: ClientSession? = null
        internal set(value) {
            value?.let {
                try {
                    it.codec = server.codec
                    it.peer.codecHelper.blockDefinitions = server.peer.codecHelper.blockDefinitions
                    it.peer.codecHelper.itemDefinitions = server.peer.codecHelper.itemDefinitions
                    it.peer.codecHelper.cameraPresetDefinitions = server.peer.codecHelper.cameraPresetDefinitions
                    it.peer.codecHelper.encodingSettings = server.peer.codecHelper.encodingSettings

                    var pair: Pair<BedrockPacket, Boolean>
                    var processedCount = 0
                    while (packetQueue.poll().also { packetPair -> pair = packetPair } != null) {
                        try {
                            if (pair.second) {
                                it.sendPacketImmediately(pair.first)
                            } else {
                                it.sendPacket(pair.first)
                            }
                            processedCount++
                        } catch (e: Exception) {
                            println("Failed to send queued packet: ${e.message}")
                        }
                    }
                    if (processedCount > 0) {
                        println("Processed $processedCount queued packets")
                    }
                } catch (e: Exception) {
                    println("Failed to initialize client session: ${e.message}")
                    e.printStackTrace()
                }
            }
            field = value
        }

    val listeners: MutableList<InkLoaderPacketListener> = ArrayList()

    private val packetQueue: Queue<Pair<BedrockPacket, Boolean>> = PlatformDependent.newMpscQueue()
    private val maxQueueSize = 1000

    fun clientBound(packet: BedrockPacket) {
        try {
            server.sendPacket(packet)
        } catch (e: Exception) {
            println("Failed to send packet to client: ${e.message}")
        }
    }

    fun clientBoundImmediately(packet: BedrockPacket) {
        try {
            server.sendPacketImmediately(packet)
        } catch (e: Exception) {
            println("Failed to send packet immediately to client: ${e.message}")
        }
    }

    fun serverBound(packet: BedrockPacket) {
        if (client != null) {
            try {
                client!!.sendPacket(packet)
            } catch (e: Exception) {
                println("Failed to send packet to server: ${e.message}")
            }
        } else {
            if (packetQueue.size < maxQueueSize) {
                packetQueue.add(packet to false)
            } else {
                println("Packet queue full, dropping packet")
            }
        }
    }

    fun serverBoundImmediately(packet: BedrockPacket) {
        if (client != null) {
            try {
                client!!.sendPacketImmediately(packet)
            } catch (e: Exception) {
                println("Failed to send packet immediately to server: ${e.message}")
            }
        } else {
            if (packetQueue.size < maxQueueSize) {
                packetQueue.add(packet to true)
            } else {
                println("Packet queue full, dropping packet")
            }
        }
    }

}
