package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.overlay.hud.WaterMarkOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class WaterMarkModule : Module("watermark", ModuleCategory.Misc) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val customText by stringValue("Text", "Nova Client", listOf())
    private val showVersion by boolValue("Show Version", true)
    private val showTime by boolValue("Show Time", false)
    private val position by enumValue("Position", Position.TOP_LEFT, Position::class.java)
    private val fontSize by intValue("Font Size", 18, 10..32)
    private val colorMode by enumValue("Color Mode", ColorMode.RAINBOW, ColorMode::class.java)
    private val rainbowSpeed by floatValue("Rainbow Speed", 1.0f, 0.1f..5.0f)
    private val showBackground by boolValue("Background", true)
    private val backgroundOpacity by floatValue("BG Opacity", 0.7f, 0.0f..1.0f)
    private val showShadow by boolValue("Shadow", true)
    private val shadowOffset by intValue("Shadow Offset", 2, 0..10)
    private val animateText by boolValue("Animate Text", false)
    private val glowEffect by boolValue("Glow Effect", false)
    private val borderStyle by enumValue("Border", BorderStyle.NONE, BorderStyle::class.java)

    private var lastUpdateTime = 0L
    private val updateInterval = 1000L

    override fun onEnabled() {
        super.onEnabled()
        try {
            if (isSessionCreated) {
                WaterMarkOverlay.setOverlayEnabled(true)
                updateSettings()
                startUpdateLoop()
            }
        } catch (e: Exception) {
            println("Error enabling WaterMark: ${e.message}")
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated) {
            WaterMarkOverlay.setOverlayEnabled(false)
        }
    }

    override fun onDisconnect(reason: String) {
        if (isSessionCreated) {
            WaterMarkOverlay.setOverlayEnabled(false)
        }
    }

    private fun updateSettings() {
        WaterMarkOverlay.setCustomText(customText)
        WaterMarkOverlay.setShowVersion(showVersion)
        WaterMarkOverlay.setShowTime(showTime)
        WaterMarkOverlay.setPosition(position)
        WaterMarkOverlay.setFontSize(fontSize)
        WaterMarkOverlay.setColorMode(colorMode)
        WaterMarkOverlay.setRainbowSpeed(rainbowSpeed)
        WaterMarkOverlay.setShowBackground(showBackground)
        WaterMarkOverlay.setBackgroundOpacity(backgroundOpacity)
        WaterMarkOverlay.setShowShadow(showShadow)
        WaterMarkOverlay.setShadowOffset(shadowOffset)
        WaterMarkOverlay.setAnimateText(animateText)
        WaterMarkOverlay.setGlowEffect(glowEffect)
        WaterMarkOverlay.setBorderStyle(borderStyle)
    }

    private fun startUpdateLoop() {
        scope.launch {
            while (isEnabled && isSessionCreated) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastUpdateTime >= updateInterval) {
                    updateSettings()
                    lastUpdateTime = currentTime
                }
                delay(100L)
            }
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || !isSessionCreated) return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime >= updateInterval) {
            updateSettings()
            lastUpdateTime = currentTime
        }
    }

    enum class Position {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        CENTER_LEFT, CENTER, CENTER_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }

    enum class ColorMode {
        RAINBOW, GRADIENT, STATIC, PULSING, WAVE
    }

    enum class BorderStyle {
        NONE, SOLID, DASHED, DOTTED, GLOW
    }
}
