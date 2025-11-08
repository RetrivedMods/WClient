package com.retrivedmods.wrelay

import com.retrivedmods.wrelay.listener.WRelayPacketListener
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


class WRelaySession internal constructor(
    peer: BedrockPeer,
    subClientId: Int,
    val wRelay: WRelay
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

    val listeners: MutableList<WRelayPacketListener> = ArrayList()

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

    inner class ServerSession(peer: BedrockPeer, subClientId: Int) :
        BedrockServerSession(peer, subClientId) {

        init {
            packetHandler = object : BedrockPacketHandler {
                override fun onDisconnect(reason: CharSequence) {
                    println("Client disconnect: $reason")
                    runCatching {
                        client?.disconnect()
                    }
                    listeners.forEach {
                        runCatching {
                            it.onDisconnect(reason.toString())
                        }
                    }
                    wRelay.connectionManager?.cleanup()
                }
            }
        }

        override fun onPacket(wrapper: BedrockPacketWrapper) {
            try {
                listeners.forEach { listener ->
                    try {
                        if (listener.beforeClientBound(wrapper.packet)) {
                            return
                        }
                    } catch (e: Throwable) {
                        println("Before client bound error: ${e.message}")
                        e.printStackTrace()
                    }
                }

                val buffer = wrapper.packetBuffer
                    .retainedSlice()
                    .skipBytes(wrapper.headerLength)

                val unknownPacket = UnknownPacket()
                unknownPacket.payload = buffer
                unknownPacket.packetId = wrapper.packetId
                serverBound(unknownPacket)

                listeners.forEach { listener ->
                    try {
                        listener.afterClientBound(wrapper.packet)
                    } catch (e: Throwable) {
                        println("After client bound error: ${e.message}")
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                println("Error processing client packet: ${e.message}")
                e.printStackTrace()
            }
        }

    }

    inner class ClientSession(peer: BedrockPeer, subClientId: Int) :
        BedrockClientSession(peer, subClientId) {

        init {
            packetHandler = object : BedrockPacketHandler {

                override fun onDisconnect(reason: CharSequence) {
                    println("Server disconnect: $reason")
                    runCatching {
                        server.disconnect(reason.toString())
                    }
                    listeners.forEach {
                        runCatching {
                            it.onDisconnect(reason.toString())
                        }
                    }
                    wRelay.connectionManager?.cleanup()
                }

            }
        }

        override fun onPacket(wrapper: BedrockPacketWrapper) {
            try {
                listeners.forEach { listener ->
                    try {
                        if (listener.beforeServerBound(wrapper.packet)) {
                            return
                        }
                    } catch (e: Throwable) {
                        println("Before server bound error: ${e.message}")
                        e.printStackTrace()
                    }
                }

                val buffer = wrapper.packetBuffer
                    .retainedSlice()
                    .skipBytes(wrapper.headerLength)

                val unknownPacket = UnknownPacket()
                unknownPacket.payload = buffer
                unknownPacket.packetId = wrapper.packetId
                clientBound(unknownPacket)

                listeners.forEach { listener ->
                    try {
                        listener.afterServerBound(wrapper.packet)
                    } catch (e: Throwable) {
                        println("After server bound error: ${e.message}")
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                println("Error processing server packet: ${e.message}")
                e.printStackTrace()
            }
        }

    }

}