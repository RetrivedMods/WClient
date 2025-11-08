package com.retrivedmods.wclient.game.module.visual

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.overlay.hud.CoordinatesOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.cloudburstmc.protocol.bedrock.packet.PlayerAuthInputPacket
import kotlin.math.sqrt

class CoordinatesModule : Module("Coordinates", ModuleCategory.Visual) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val showCoordinates by boolValue("Show Coordinates", true)
    private val showDirection by boolValue("Show Direction", true)
    private val showDimension by boolValue("Show Dimension", true)
    private val showSpeed by boolValue("Show Speed", false)
    private val showNetherCoords by boolValue("Show Nether Coords", true)
    private val position by enumValue("Position", Position.TOP_LEFT, Position::class.java)
    private val fontSize by intValue("Font Size", 14, 10..24)
    private val colorMode by enumValue("Color Mode", ColorMode.STATIC, ColorMode::class.java)
    private val showBackground by boolValue("Background", true)
    private val backgroundOpacity by floatValue("BG Opacity", 0.7f, 0.0f..1.0f)
    private val showBorder by boolValue("Border", false)
    private val compactMode by boolValue("Compact Mode", false)
    private val precision by intValue("Decimal Places", 1, 0..3)
    private val updateRate by intValue("Update Rate (ms)", 100, 50..1000)

    private var lastUpdateTime = 0L
    private var lastPosition = Triple(0.0, 0.0, 0.0)
    private var lastTime = 0L
    private var currentSpeed = 0.0
    private var currentDimension = "Overworld"

    override fun onEnabled() {
        super.onEnabled()
        try {
            if (isSessionCreated) {
                CoordinatesOverlay.Companion.setOverlayEnabled(true)
                updateSettings()
                startUpdateLoop()
            }
        } catch (e: Exception) {
            println("Error enabling Coordinates: ${e.message}")
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated) {
            CoordinatesOverlay.Companion.setOverlayEnabled(false)
        }
    }

    override fun onDisconnect(reason: String) {
        if (isSessionCreated) {
            CoordinatesOverlay.Companion.setOverlayEnabled(false)
        }
    }

    private fun updateSettings() {
        CoordinatesOverlay.Companion.setShowCoordinates(showCoordinates)
        CoordinatesOverlay.Companion.setShowDirection(showDirection)
        CoordinatesOverlay.Companion.setShowDimension(showDimension)
        CoordinatesOverlay.Companion.setShowSpeed(showSpeed)
        CoordinatesOverlay.Companion.setShowNetherCoords(showNetherCoords)
        CoordinatesOverlay.Companion.setPosition(position)
        CoordinatesOverlay.Companion.setFontSize(fontSize)
        CoordinatesOverlay.Companion.setColorMode(colorMode)
        CoordinatesOverlay.Companion.setShowBackground(showBackground)
        CoordinatesOverlay.Companion.setBackgroundOpacity(backgroundOpacity)
        CoordinatesOverlay.Companion.setShowBorder(showBorder)
        CoordinatesOverlay.Companion.setCompactMode(compactMode)
        CoordinatesOverlay.Companion.setPrecision(precision)
    }

    private fun startUpdateLoop() {
        scope.launch {
            while (isEnabled && isSessionCreated) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastUpdateTime >= updateRate) {
                    updateCoordinatesDisplay()
                    updateSettings()
                    lastUpdateTime = currentTime
                }
                delay(updateRate.toLong())
            }
        }
    }

    private fun updateCoordinatesDisplay() {
        try {
            val player = session.localPlayer
            val currentPos = Triple(player.posX.toDouble(), player.posY.toDouble(), player.posZ.toDouble())
            val currentTime = System.currentTimeMillis()

            if (lastTime > 0) {
                val deltaTime = (currentTime - lastTime) / 1000.0
                val deltaX = currentPos.first - lastPosition.first
                val deltaZ = currentPos.third - lastPosition.third
                val distance = sqrt(deltaX * deltaX + deltaZ * deltaZ)
                currentSpeed = if (deltaTime > 0) distance / deltaTime else 0.0
            }

            currentDimension = getCurrentDimension()

            val yaw = player.rotationYaw
            val direction = getDirectionFromYaw(yaw)

            val netherCoords = if (currentDimension == "Overworld") {
                Triple(currentPos.first / 8, currentPos.second, currentPos.third / 8)
            } else if (currentDimension == "Nether") {
                Triple(currentPos.first * 8, currentPos.second, currentPos.third * 8)
            } else {
                currentPos
            }

            CoordinatesOverlay.Companion.setCoordinates(currentPos)
            CoordinatesOverlay.Companion.setDirection(direction)
            CoordinatesOverlay.Companion.setDimension(currentDimension)
            CoordinatesOverlay.Companion.setSpeed(currentSpeed)
            CoordinatesOverlay.Companion.setNetherCoordinates(netherCoords)

            lastPosition = currentPos
            lastTime = currentTime

        } catch (e: Exception) {
            println("Error updating coordinates display: ${e.message}")
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || !isSessionCreated) return

        when (interceptablePacket.packet) {
            is PlayerAuthInputPacket -> {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastUpdateTime >= updateRate) {
                    updateCoordinatesDisplay()
                    lastUpdateTime = currentTime
                }
            }
        }
    }

    private fun getDirectionFromYaw(yaw: Float): String {
        val normalizedYaw = ((yaw % 360 + 360) % 360)
        return when {
            normalizedYaw >= 337.5 || normalizedYaw < 22.5 -> "South"
            normalizedYaw >= 22.5 && normalizedYaw < 67.5 -> "Southwest"
            normalizedYaw >= 67.5 && normalizedYaw < 112.5 -> "West"
            normalizedYaw >= 112.5 && normalizedYaw < 157.5 -> "Northwest"
            normalizedYaw >= 157.5 && normalizedYaw < 202.5 -> "North"
            normalizedYaw >= 202.5 && normalizedYaw < 247.5 -> "Northeast"
            normalizedYaw >= 247.5 && normalizedYaw < 292.5 -> "East"
            normalizedYaw >= 292.5 && normalizedYaw < 337.5 -> "Southeast"
            else -> "Unknown"
        }
    }

    private fun getCurrentDimension(): String {
        return try {
            val player = session.localPlayer
            when {
                player.posY < 0 -> "Nether"
                player.posY > 256 -> "End"
                else -> "Overworld"
            }
        } catch (e: Exception) {
            "Overworld"
        }
    }

    enum class Position {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER_LEFT, CENTER_RIGHT
    }

    enum class ColorMode {
        STATIC, RAINBOW, DIMENSION_BASED, SPEED_BASED
    }
}