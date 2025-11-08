package com.retrivedmods.wclient.game.module.visual

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.overlay.hud.CrosshairOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CrosshairModule : Module("Crosshair", ModuleCategory.Visual) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val crosshairType by enumValue("Type", CrosshairType.CROSS, CrosshairType::class.java)
    private val size by intValue("Size", 10, 3..30)
    private val thickness by intValue("Thickness", 2, 1..8)
    private val gap by intValue("Gap", 3, 0..15)
    private val colorMode by enumValue("Color Mode", ColorMode.STATIC, ColorMode::class.java)
    private val staticColor by enumValue("Static Color", StaticColor.WHITE, StaticColor::class.java)
    private val rainbowSpeed by floatValue("Rainbow Speed", 1.0f, 0.1f..5.0f)
    private val outline by boolValue("Outline", true)
    private val outlineThickness by intValue("Outline Thickness", 1, 1..3)
    private val dynamicColor by boolValue("Dynamic Color", false)
    private val hitMarker by boolValue("Hit Marker", true)
    private val hitMarkerDuration by intValue("Hit Duration", 500, 100..2000)
    private val pulsing by boolValue("Pulsing", false)
    private val pulseSpeed by floatValue("Pulse Speed", 1.0f, 0.1f..3.0f)

    private var lastUpdateTime = 0L
    private val updateInterval = 16L // ~60 FPS
    private var hitMarkerTime = 0L
    private var showHitMarker = false

    override fun onEnabled() {
        super.onEnabled()
        try {
            if (isSessionCreated) {
                CrosshairOverlay.setOverlayEnabled(true)
                updateSettings()
                startUpdateLoop()
            }
        } catch (e: Exception) {
            println("Error enabling Crosshair: ${e.message}")
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated) {
            CrosshairOverlay.setOverlayEnabled(false)
        }
    }

    override fun onDisconnect(reason: String) {
        if (isSessionCreated) {
            CrosshairOverlay.setOverlayEnabled(false)
        }
    }

    private fun updateSettings() {
        CrosshairOverlay.setCrosshairType(crosshairType)
        CrosshairOverlay.setSize(size)
        CrosshairOverlay.setThickness(thickness)
        CrosshairOverlay.setGap(gap)
        CrosshairOverlay.setColorMode(colorMode)
        CrosshairOverlay.setStaticColor(staticColor)
        CrosshairOverlay.setRainbowSpeed(rainbowSpeed)
        CrosshairOverlay.setOutline(outline)
        CrosshairOverlay.setOutlineThickness(outlineThickness)
        CrosshairOverlay.setDynamicColor(dynamicColor)
        CrosshairOverlay.setHitMarker(hitMarker)
        CrosshairOverlay.setPulsing(pulsing)
        CrosshairOverlay.setPulseSpeed(pulseSpeed)
        CrosshairOverlay.setShowHitMarker(showHitMarker)
    }

    private fun startUpdateLoop() {
        scope.launch {
            while (isEnabled && isSessionCreated) {
                val currentTime = System.currentTimeMillis()
                
                if (currentTime - lastUpdateTime >= updateInterval) {
                    if (showHitMarker && currentTime - hitMarkerTime > hitMarkerDuration) {
                        showHitMarker = false
                    }
                    
                    updateSettings()
                    lastUpdateTime = currentTime
                }
                
                delay(updateInterval)
            }
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || !isSessionCreated) return

    }

    enum class CrosshairType {
        CROSS, DOT, CIRCLE, SQUARE, DIAMOND, T_SHAPE, PLUS
    }

    enum class ColorMode {
        STATIC, RAINBOW, HEALTH_BASED, TARGET_BASED, PULSING
    }

    enum class StaticColor {
        WHITE, RED, GREEN, BLUE, YELLOW, CYAN, MAGENTA, ORANGE
    }
}