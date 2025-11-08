package com.retrivedmods.wclient.game.module.combat

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.entity.Entity
import com.retrivedmods.wclient.game.entity.EntityUnknown
import com.retrivedmods.wclient.game.entity.LocalPlayer
import com.retrivedmods.wclient.game.entity.MobList
import com.retrivedmods.wclient.game.entity.Player
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataMap
import org.cloudburstmc.protocol.bedrock.data.entity.EntityDataTypes
import org.cloudburstmc.protocol.bedrock.data.entity.EntityEventType
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import org.cloudburstmc.protocol.bedrock.packet.SetEntityDataPacket
import org.cloudburstmc.protocol.bedrock.packet.EntityEventPacket

class HitboxModule : Module("hitbox", ModuleCategory.Combat) {

    private val hitboxWidth by floatValue("width", 1.5f, 0.5f..12f)
    private val hitboxHeight by floatValue("height", 1.5f, 0.5f..12f)
    private var playersOnly by boolValue("players_only", true)
    private var mobsOnly by boolValue("include_mobs", false)
    private val particleCount by intValue("particles", 8, 4..16)
    private val visualizeHitbox by boolValue("visualize", true)
    private var lastParticleTime = 0L
    private val particleInterval = 500L

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val currentTime = System.currentTimeMillis()

            if (session.localPlayer.tickExists % 40 == 0L) {
                session.level.entityMap.values.forEach { entity ->
                    if (entity.isTarget()) {
                        val metadata = EntityDataMap()
                        metadata.put(EntityDataTypes.WIDTH, hitboxWidth)
                        metadata.put(EntityDataTypes.HEIGHT, hitboxHeight)
                        metadata.put(EntityDataTypes.SCALE, 1.0f)

                        session.clientBound(SetEntityDataPacket().apply {
                            runtimeEntityId = entity.runtimeEntityId
                            this.metadata = metadata
                        })

                        if (visualizeHitbox && currentTime - lastParticleTime >= particleInterval) {
                            visualizeHitboxWithParticles(entity)
                            lastParticleTime = currentTime
                        }
                    }
                }
            }
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated) {
            session.level.entityMap.values.forEach { entity ->
                if (entity.isTarget()) {
                    val metadata = EntityDataMap()
                    metadata.put(EntityDataTypes.WIDTH, 0.6f)
                    metadata.put(EntityDataTypes.HEIGHT, 1.8f)
                    metadata.put(EntityDataTypes.SCALE, 1.0f)

                    session.clientBound(SetEntityDataPacket().apply {
                        runtimeEntityId = entity.runtimeEntityId
                        this.metadata = metadata
                    })
                }
            }
        }
    }

    private fun visualizeHitboxWithParticles(entity: Entity) {
        val pos = entity.vec3Position
        val radius = hitboxWidth / 2f

        repeat(particleCount) { i ->
            val angle = (i * (360.0 / particleCount)).toDouble()
            val x = pos.x + radius * Math.cos(Math.toRadians(angle))
            val z = pos.z + radius * Math.sin(Math.toRadians(angle))

            session.clientBound(EntityEventPacket().apply {
                runtimeEntityId = entity.runtimeEntityId
                type = EntityEventType.LOVE_PARTICLES
                data = 0
            })
        }
    }

    private fun EntityUnknown.isMob(): Boolean {
        return this.identifier in MobList.mobTypes
    }

    private fun Player.isBot(): Boolean {
        if (this is LocalPlayer) return false
        val playerList = session.level.playerMap[this.uuid] ?: return false // Changed: treat unknown players as real players
        return playerList.name.isBlank()
    }

    private fun Entity.isTarget(): Boolean {
        return when (this) {
            is LocalPlayer -> false
            is Player -> {
                if (mobsOnly) {
                    false
                } else if (playersOnly) {
                    !this.isBot()
                } else {
                    !this.isBot()
                }
            }
            is EntityUnknown -> {
                if (playersOnly) {
                    false
                } else if (mobsOnly) {
                    isMob()
                } else {
                    true
                }
            }
            else -> false
        }
    }
}