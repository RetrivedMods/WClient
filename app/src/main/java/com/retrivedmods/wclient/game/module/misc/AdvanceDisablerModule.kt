package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.MovePlayerPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.random.Random

class AdvanceDisablerModule : Module("Advance Disabler", ModuleCategory.Misc) {

    private val lifeboatBypass by boolValue("Lifeboat Bypass", true)
    private val hiveBypass by boolValue("Hive Bypass", true)
    private val cubecraftBypass by boolValue("Cubecraft Bypass", true)

    private val spoofTeleportLag by boolValue("TP Lag Spoof", true)
    private val packetFlood by boolValue("Packet Flood", false)
    private val floodRate by intValue("Flood Rate", 5, 1..20)

    private val antiKick by boolValue("Anti Kick", true)
    private val blinkPackets by boolValue("Blink Mode", false)
    private var packetCounter = 0
    private val blinkBuffer = mutableListOf<PlayerAuthInputPacket>()

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        val packet = interceptablePacket.packet

        if (packet is MovePlayerPacket) {
            if (lifeboatBypass || hiveBypass || cubecraftBypass) {
                if (Random.nextInt(4) == 0) {
                    packet.onGround = true
                    packet.position = packet.position.add(0f, 0.015f + Random.nextFloat() * 0.01f, 0f)
                }
            }
            if (spoofTeleportLag && Random.nextInt(3) == 0) {

                packet.teleportationCause = MovePlayerPacket.TeleportationCause.UNKNOWN
            }
            if (antiKick && packet.position.y <= 0.5f) {
                packet.position = packet.position.add(0f, 1.5f + Random.nextFloat(), 0f)
                packet.onGround = true
            }
        }

        if (packet is PlayerAuthInputPacket) {
            if (blinkPackets) {
                blinkBuffer.add(packet)
                if (blinkBuffer.size >= 10 + Random.nextInt(5)) {
                    sendPacketsToServer(blinkBuffer)
                    blinkBuffer.clear()
                }
                return
            }
            if (packetFlood) {
                packetCounter++
                if (packetCounter >= floodRate) {
                    packetCounter = 0
                    repeat(3) {
                        val spoof = PlayerAuthInputPacket().apply {
                            position = packet.position
                            delta = packet.delta
                            inputMode = packet.inputMode
                            tick = packet.tick + Random.nextInt(1, 6)
                        }
                        sendPacketToServer(spoof)
                    }
                }
            }
            if (Random.nextBoolean()) {
                packet.delta = packet.delta.add(
                    0.001f * (Random.nextFloat() - 0.5f),
                    0f,
                    0.001f * (Random.nextFloat() - 0.5f)
                )
            }
        }
    }


    @Suppress("unused")
    fun onDisable() {
        if (blinkBuffer.isNotEmpty()) {
            sendPacketsToServer(blinkBuffer)
            blinkBuffer.clear()
        }
    }


    private fun sendPacketToServer(packet: PlayerAuthInputPacket) {
        // TODO: Implement actual packet sending logic using your client's networking/session API.

    }

    private fun sendPacketsToServer(packets: List<PlayerAuthInputPacket>) {
        for (pkt in packets) sendPacketToServer(pkt)
    }
}