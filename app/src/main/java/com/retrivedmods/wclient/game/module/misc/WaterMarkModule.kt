package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.overlay.hud.WaterMarkOverlay
import kotlinx.coroutines.*

class WaterMarkModule : Module("watermark", ModuleCategory.Misc) {

    private var scope: CoroutineScope? = null

    private val customText by stringValue("Text", "WClient", listOf())
    private val showVersion by boolValue("Show Version", true)
    private val position by enumValue("Position", Position.TOP_LEFT, Position::class.java)
    private val fontSize by intValue("Font Size", 18, 10..32)
    private val rainbowSpeed by floatValue("Rainbow Speed", 1.0f, 0.1f..5.0f)
    private val showBackground by boolValue("Background", false)
    private val backgroundOpacity by floatValue("BG Opacity", 0.7f, 0.0f..1.0f)
    private val showShadow by boolValue("Shadow", false)
    private val shadowOffset by intValue("Shadow Offset", 2, 0..10)
    private val animateText by boolValue("Animate Text", false)
    private val glowEffect by boolValue("Glow Effect", false)
    private val borderStyle by enumValue("Border", BorderStyle.NONE, BorderStyle::class.java)

    override fun onEnabled() {
        super.onEnabled()
        if (!isSessionCreated) return

        WaterMarkOverlay.setOverlayEnabled(true)
        applySettings()

        scope = CoroutineScope(Dispatchers.Main + SupervisorJob()).apply {
            launch {
                while (isActive && isEnabled && isSessionCreated) {
                    applySettings()
                    delay(500L)
                }
            }
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        scope?.cancel()
        scope = null

        if (isSessionCreated) {
            WaterMarkOverlay.setOverlayEnabled(false)
        }
    }

    override fun onDisconnect(reason: String) {
        scope?.cancel()
        scope = null
        WaterMarkOverlay.setOverlayEnabled(false)
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        // No packet logic needed anymore (overlay driven)
    }

    private fun applySettings() {
        WaterMarkOverlay.setCustomText(customText)
        WaterMarkOverlay.setShowVersion(showVersion)
        WaterMarkOverlay.setPosition(position)
        WaterMarkOverlay.setFontSize(fontSize)
        WaterMarkOverlay.setRainbowSpeed(rainbowSpeed)
        WaterMarkOverlay.setShowBackground(showBackground)
        WaterMarkOverlay.setBackgroundOpacity(backgroundOpacity)
        WaterMarkOverlay.setShowShadow(showShadow)
        WaterMarkOverlay.setShadowOffset(shadowOffset)
        WaterMarkOverlay.setAnimateText(animateText)
        WaterMarkOverlay.setGlowEffect(glowEffect)
        WaterMarkOverlay.setBorderStyle(borderStyle)
    }

    enum class Position {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        CENTER_LEFT, CENTER, CENTER_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }

    enum class BorderStyle {
        NONE, SOLID, DASHED, DOTTED, GLOW
    }
}
