package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.entity.EntityEventType
import org.cloudburstmc.protocol.bedrock.packet.EntityEventPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.RespawnPacket

class FakeDeathModule : Module("fake_death", ModuleCategory.Misc) {

    private var lastDeathEffect = 0L
    private val deathEffectInterval = 2000L
    private var isDead = false

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastDeathEffect >= deathEffectInterval && !isDead) {
                lastDeathEffect = currentTime
                isDead = true

                // Send death animation packet
                session.clientBound(EntityEventPacket().apply {
                    runtimeEntityId = session.localPlayer.runtimeEntityId
                    type = EntityEventType.DEATH
                    data = 0
                })

                // Send respawn packet sequence
                session.clientBound(RespawnPacket().apply {
                    position = Vector3f.from(
                        session.localPlayer.posX,
                        session.localPlayer.posY,
                        session.localPlayer.posZ
                    )
                    state = RespawnPacket.State.SERVER_SEARCHING
                    runtimeEntityId = session.localPlayer.runtimeEntityId
                })
            }
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        if (isDead && isSessionCreated) {
            // Complete respawn sequence
            session.clientBound(RespawnPacket().apply {
                position = Vector3f.from(
                    session.localPlayer.posX,
                    session.localPlayer.posY,
                    session.localPlayer.posZ
                )
                state = RespawnPacket.State.SERVER_READY
                runtimeEntityId = session.localPlayer.runtimeEntityId
            })

            session.clientBound(RespawnPacket().apply {
                position = Vector3f.from(
                    session.localPlayer.posX,
                    session.localPlayer.posY,
                    session.localPlayer.posZ
                )
                state = RespawnPacket.State.CLIENT_READY
                runtimeEntityId = session.localPlayer.runtimeEntityId
            })
            isDead = false
        }
    }
}