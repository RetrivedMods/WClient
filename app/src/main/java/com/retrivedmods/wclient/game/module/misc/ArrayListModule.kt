package com.retrivedmods.wclient.game.module.misc

import com.retrivedmods.wclient.game.InterceptablePacket
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.ModuleManager
import com.retrivedmods.wclient.overlay.hud.ArrayListOverlay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ArrayListModule : Module("arraylist", ModuleCategory.Misc) {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val sortMode by enumValue("Sort", SortMode.LENGTH, SortMode::class.java)
    private val animationSpeed by intValue("Animation Speed", 300, 100..1000)
    private val showBackground by boolValue("Background", true)
    private val showBorder by boolValue("Border", true)
    private val borderStyle by enumValue("Border Style", BorderStyle.LEFT, BorderStyle::class.java)
    private val colorMode by enumValue("Color Mode", ColorMode.RAINBOW, ColorMode::class.java)
    private val rainbowSpeed by floatValue("Rainbow Speed", 1.0f, 0.1f..5.0f)
    private val fontSize by intValue("Font Size", 14, 8..24)
    private val spacing by intValue("Spacing", 2, 0..10)
    private val fadeAnimation by boolValue("Fade Animation", true)
    private val slideAnimation by boolValue("Slide Animation", true)

    private var lastUpdateTime = 0L
    private val updateInterval = 50L

    override fun onEnabled() {
        super.onEnabled()
        try {
            if (isSessionCreated) {
                ArrayListOverlay.setOverlayEnabled(true)
                updateSettings()
                startUpdateLoop()
            }
        } catch (e: Exception) {
            println("Error enabling ArrayList: ${e.message}")
        }
    }

    override fun onDisabled() {
        super.onDisabled()
        if (isSessionCreated) {
            ArrayListOverlay.setOverlayEnabled(false)
        }
    }

    override fun onDisconnect(reason: String) {
        if (isSessionCreated) {
            ArrayListOverlay.setOverlayEnabled(false)
        }
    }

    private fun updateSettings() {
        ArrayListOverlay.setSortMode(sortMode)
        ArrayListOverlay.setAnimationSpeed(animationSpeed)
        ArrayListOverlay.setShowBackground(showBackground)
        ArrayListOverlay.setShowBorder(showBorder)
        ArrayListOverlay.setBorderStyle(borderStyle)
        ArrayListOverlay.setColorMode(colorMode)
        ArrayListOverlay.setRainbowSpeed(rainbowSpeed)
        ArrayListOverlay.setFontSize(fontSize)
        ArrayListOverlay.setSpacing(spacing)
        ArrayListOverlay.setFadeAnimation(fadeAnimation)
        ArrayListOverlay.setSlideAnimation(slideAnimation)
    }

    private fun startUpdateLoop() {
        scope.launch {
            while (isEnabled && isSessionCreated) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastUpdateTime >= updateInterval) {
                    updateModuleList()
                    lastUpdateTime = currentTime
                }
                delay(updateInterval)
            }
        }
    }

    private fun updateModuleList() {
        val enabledModules = ModuleManager.modules
            .filter { module ->
                module.isEnabled &&
                module.name.isNotEmpty() &&
                module != this
            }
            .map { module ->
                ArrayListOverlay.ModuleInfo(
                    name = module.name,
                    category = module.category.name,
                    isEnabled = module.isEnabled,
                    priority = calculatePriority(module)
                )
            }

        ArrayListOverlay.setModules(enabledModules)
        updateSettings()
    }

    private fun calculatePriority(module: Module): Int {
        return when (sortMode) {
            SortMode.LENGTH -> module.name.length
            SortMode.ALPHABETICAL -> -module.name.first().code
            SortMode.CATEGORY -> module.category.ordinal
            SortMode.CUSTOM -> when (module.category) {
                ModuleCategory.Combat -> 1000
                ModuleCategory.Visual -> 800
                ModuleCategory.Misc -> 700
                else -> 500
            }
        }
    }

    override fun beforePacketBound(interceptablePacket: InterceptablePacket) {
        if (!isEnabled || !isSessionCreated) return
        
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime >= updateInterval) {
            updateModuleList()
            lastUpdateTime = currentTime
        }
    }

    enum class SortMode {
        LENGTH, ALPHABETICAL, CATEGORY, CUSTOM
    }

    enum class BorderStyle {
        LEFT, RIGHT, TOP, BOTTOM, FULL, NONE
    }

    enum class ColorMode {
        RAINBOW, GRADIENT, STATIC, CATEGORY_BASED, RANDOM
    }
}
