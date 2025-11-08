package com.retrivedmods.wclient.game.module.world

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.LevelEvent
import org.cloudburstmc.protocol.bedrock.data.entity.EntityEventType
import org.cloudburstmc.protocol.bedrock.packet.EntityEventPacket
import org.cloudburstmc.protocol.bedrock.packet.LevelEventPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class ParticlesModule : Module("particles", ModuleCategory.World) {

    private val breezeWindExplosion by boolValue("Breeze Wind Explosion", false)
    private val bubble by boolValue("Bubble", false)
    private val dust by boolValue("Dust", false)
    private val explosion by boolValue("Explosion", false)
    private val eyeOfEnderDeath by boolValue("Eye of Ender Death", false)
    private val fizz by boolValue("Fizz", false)
    private val heart by boolValue("Heart", false)

    private val interval by intValue("Interval", 500, 100..2000)
    private val offsetY by floatValue("Height Offset", 1.0f, -2.0f..5.0f)
    private val particleSize by floatValue("Size", 1.0f, 0.1f..5.0f)
    private val particleCount by intValue("Count", 1, 1..10)
    private val randomOffset by boolValue("Random Offset", false)
    private val offsetRadius by floatValue("Offset Radius", 1.0f, 0.1f..5.0f)

    private var lastParticleTime = 0L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastParticleTime >= interval) {
                lastParticleTime = currentTime

                repeat(particleCount) {
                    val offsetX = if (randomOffset) (Math.random() * 2 - 1) * offsetRadius else 0.0
                    val offsetZ = if (randomOffset) (Math.random() * 2 - 1) * offsetRadius else 0.0

                    val position = Vector3f.from(
                        packet.position.x + offsetX.toFloat(),
                        packet.position.y + offsetY,
                        packet.position.z + offsetZ.toFloat()
                    )

                    if (breezeWindExplosion) {
                        session.clientBound(LevelEventPacket().apply {
                            type = LevelEvent.PARTICLE_BREEZE_WIND_EXPLOSION
                            this.position = position
                            data = (particleSize * 1000).toInt()
                        })
                    }

                    if (bubble) {
                        session.clientBound(LevelEventPacket().apply {
                            type = LevelEvent.PARTICLE_BUBBLES
                            this.position = position
                            data = (particleSize * 1000).toInt()
                        })
                    }

                    if (dust) {
                        session.clientBound(EntityEventPacket().apply {
                            runtimeEntityId = session.localPlayer.runtimeEntityId
                            type = EntityEventType.DUST_PARTICLES
                            data = 0
                        })
                    }

                    if (explosion) {
                        session.clientBound(LevelEventPacket().apply {
                            type = LevelEvent.PARTICLE_EXPLOSION
                            this.position = position
                            data = (particleSize * 1000).toInt()
                        })
                    }

                    if (eyeOfEnderDeath) {
                        session.clientBound(LevelEventPacket().apply {
                            type = LevelEvent.PARTICLE_EYE_OF_ENDER_DEATH
                            this.position = position
                            data = (particleSize * 1000).toInt()
                        })
                    }

                    if (fizz) {
                        session.clientBound(LevelEventPacket().apply {
                            type = LevelEvent.PARTICLE_FIZZ_EFFECT
                            this.position = position
                            data = (particleSize * 1000).toInt()
                        })
                    }

                    if (heart) {
                        session.clientBound(EntityEventPacket().apply {
                            runtimeEntityId = session.localPlayer.runtimeEntityId
                            type = EntityEventType.LOVE_PARTICLES
                            data = 0
                        })
                    }
                }
            }
        }
    }
}
