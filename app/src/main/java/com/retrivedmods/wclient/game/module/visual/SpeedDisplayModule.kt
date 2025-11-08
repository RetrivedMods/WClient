package com.retrivedmods.wclient.game.module.visual

import android.annotation.SuppressLint
import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.ActionBarManager
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.math.sqrt

class SpeedDisplayModule : Module("speed_display", ModuleCategory.Visual) {

    private var lastDisplayTime = 0L
    private val displayInterval = 500L
    private val colorStyle by boolValue("colored_text", true)
    private val smoothingEnabled by boolValue("speed_smoothing", true)
    private val speedHistory = ArrayDeque<Double>(5)

    @SuppressLint("DefaultLocale")
    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled) return

        val packet = interceptablePacket.packet
        if (packet is PlayerAuthInputPacket) {
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastDisplayTime >= displayInterval) {
                lastDisplayTime = currentTime

                val xDist = session.localPlayer.motionX
                val zDist = session.localPlayer.motionZ
                val currentSpeed = sqrt(xDist * xDist + zDist * zDist) * 20

                val smoothedSpeed = if (smoothingEnabled) {
                    speedHistory.addLast(currentSpeed.toDouble())
                    if (speedHistory.size > 5) {
                        speedHistory.removeFirst()
                    }

                    val sortedSpeeds = speedHistory.sorted()
                    if (sortedSpeeds.size >= 3) {
                        sortedSpeeds.subList(1, sortedSpeeds.size - 1).average()
                    } else {
                        sortedSpeeds.average()
                    }
                } else {
                    currentSpeed
                }

                val speedText = if (colorStyle) {
                    "§l§c[Speed] §r§f${String.format("%.2f", smoothedSpeed)} §7bps"
                } else {
                    "Speed: ${String.format("%.2f", smoothedSpeed)} bps"
                }

                ActionBarManager.updateModule("speed", speedText)
                ActionBarManager.display(session)
            }
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated) {
            ActionBarManager.removeModule("speed")
            ActionBarManager.display(session)
            speedHistory.clear()
        }
    }
}