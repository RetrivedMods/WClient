package com.retrivedmods.wclient.game.module.world

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import org.cloudburstmc.math.vector.Vector3f
import org.cloudburstmc.protocol.bedrock.data.LevelEvent
import org.cloudburstmc.protocol.bedrock.packet.LevelEventPacket
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket

class WeatherControllerModule : Module("weather_controller", ModuleCategory.World) {

    private var clear by boolValue("clear", true)
    private var rain by boolValue("rain", false)
    private var thunderstorm by boolValue("thunderstorm", false)

    private var lastUpdate = 0L
    private var lastPosition = Vector3f.ZERO

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) {
            return
        }

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            lastPosition = Vector3f.from(packet.position.x, packet.position.y, packet.position.z)
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastUpdate >= 100) {
                lastUpdate = currentTime

                session.clientBound(LevelEventPacket().apply {
                    type = LevelEvent.STOP_RAINING
                    position = lastPosition
                    data = 0
                })
                session.clientBound(LevelEventPacket().apply {
                    type = LevelEvent.STOP_THUNDERSTORM
                    position = lastPosition
                    data = 0
                })

                when {
                    clear -> {

                    }

                    rain -> {
                        session.clientBound(LevelEventPacket().apply {
                            type = LevelEvent.START_RAINING
                            position = lastPosition
                            data = 10000
                        })
                    }

                    thunderstorm -> {
                        session.clientBound(LevelEventPacket().apply {
                            type = LevelEvent.START_RAINING
                            position = lastPosition
                            data = 10000
                        })
                        session.clientBound(LevelEventPacket().apply {
                            type = LevelEvent.START_THUNDERSTORM
                            position = lastPosition
                            data = 10000
                        })
                    }
                }
            }
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated) {
            session.clientBound(LevelEventPacket().apply {
                type = LevelEvent.STOP_RAINING
                position = lastPosition
                data = 0
            })
            session.clientBound(LevelEventPacket().apply {
                type = LevelEvent.STOP_THUNDERSTORM
                position = lastPosition
                data = 0
            })
        }
    }
}
