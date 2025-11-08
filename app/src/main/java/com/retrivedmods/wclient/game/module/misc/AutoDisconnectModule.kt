package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.entity.LocalPlayer
import com.retrivedmods.wclient.game.utils.constants.Attribute
import org.cloudburstmc.protocol.bedrock.packet.DisconnectPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class AutoDisconnectModule : Module("auto_disconnect", ModuleCategory.Misc) {

    private var healthThreshold by intValue("Health Threshold", 4, 1..20)
    private var checkDelay by intValue("Delay", 100, 0..1000)
    private var hasDisconnected = false
    private var lastCheckTime = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || hasDisconnected) return

        val packet = interceptablePacket.packet
        if (packet !is PlayerAuthInputPacket) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastCheckTime < checkDelay) return
        lastCheckTime = currentTime

        val player: LocalPlayer = session.localPlayer
        val health = player.attributes[Attribute.HEALTH]?.value ?: 20f

        if (health <= healthThreshold) {
            disconnectPlayer(health.toInt())
        }
    }

    private fun disconnectPlayer(currentHealth: Int) {
        val message = "§cAutoDisconnected at $currentHealth HP"
        val disconnectPacket = DisconnectPacket().apply {
            kickMessage = message
        }

        session.clientBound(disconnectPacket)
        session.serverBound(disconnectPacket)

        hasDisconnected = true
        isEnabled = false
        println("AutoDisconnect: Disconnected at $currentHealth HP.")
    }

    override fun onDisabled() {
        hasDisconnected = false
        lastCheckTime = 0L
    }
}
