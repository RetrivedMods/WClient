package com.mucheng.mucute.client.game.module.visual

import com.mucheng.mucute.client.game.InterceptablePacket
import com.mucheng.mucute.client.game.Module
import com.mucheng.mucute.client.game.ModuleCategory
import org.cloudburstmc.protocol.bedrock.packet.EntityEventPacket
import org.cloudburstmc.protocol.bedrock.data.entity.EntityEventType
import com.mucheng.mucute.client.game.entity.Player

class DamageTextModule : Module("DamageText", ModuleCategory.Visual) {

    // Intercepting the packet to track damage events
    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet

        if (packet is EntityEventPacket && packet.type == EntityEventType.HURT) {
            val entityId = packet.runtimeEntityId

            // Ignore self-damage (don't show message for local player)
            if (entityId == session.localPlayer.runtimeEntityId) return

            // Retrieve the player entity by runtimeEntityId from the entity map
            val entity = session.level.entityMap[entityId]

            // If the entity exists and is a player, get their username
            if (entity is Player) {
                val playerName = entity.username  // Assuming `username` exists

                val stateText = "$playerName was damaged!"
                val status = "§a$stateText"  // You can use colors like §a for green
                val message = "§l§c[WClient] §8» $status"


                        // Send the formatted damage message to the chat
                session.displayClientMessage(message)
            }
        }
    }
}
