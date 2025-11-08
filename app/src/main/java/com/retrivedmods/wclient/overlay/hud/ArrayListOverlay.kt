package com.retrivedmods.wclient.overlay.hud

import android.view.Gravity
import com.retrivedmods.wclient.overlay.OverlayWindow
import com.retrivedmods.wclient.overlay.OverlayManager
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrivedmods.wclient.game.module.misc.ArrayListModule
import com.retrivedmods.wclient.ui.theme.WColors
import com.retrivedmods.wclient.util.translatedSelf
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class ArrayListOverlay : OverlayWindow() {

    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = flags or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP or Gravity.END
            x = 20
            y = 20
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    private var modules by mutableStateOf(listOf<ModuleInfo>())
    private var sortMode by mutableStateOf(ArrayListModule.SortMode.LENGTH)
    private var animationSpeed by mutableStateOf(300)
    private var showBackground by mutableStateOf(true)
    private var showBorder by mutableStateOf(true)
    private var borderStyle by mutableStateOf(ArrayListModule.BorderStyle.LEFT)
    private var colorMode by mutableStateOf(ArrayListModule.ColorMode.RAINBOW)
    private var rainbowSpeed by mutableStateOf(1.0f)
    private var fontSize by mutableStateOf(14)
    private var spacing by mutableStateOf(2)
    private var fadeAnimation by mutableStateOf(true)
    private var slideAnimation by mutableStateOf(true)

    data class ModuleInfo(
        val name: String,
        val category: String,
        val isEnabled: Boolean,
        val priority: Int
    )

    companion object {
        val overlayInstance by lazy { ArrayListOverlay() }
        private var shouldShowOverlay = false

        fun showOverlay() {
            if (shouldShowOverlay) {
                try {
                    OverlayManager.showOverlayWindow(overlayInstance)
                } catch (e: Exception) {}
            }
        }

        fun dismissOverlay() {
            try {
                OverlayManager.dismissOverlayWindow(overlayInstance)
            } catch (e: Exception) {}
        }

        fun setOverlayEnabled(enabled: Boolean) {
            shouldShowOverlay = enabled
            if (enabled) showOverlay() else dismissOverlay()
        }

        fun isOverlayEnabled(): Boolean = shouldShowOverlay

        fun setModules(moduleList: List<ModuleInfo>) {
            overlayInstance.modules = moduleList
        }

        fun setSortMode(mode: ArrayListModule.SortMode) {
            overlayInstance.sortMode = mode
        }

        fun setAnimationSpeed(speed: Int) {
            overlayInstance.animationSpeed = speed
        }

        fun setShowBackground(show: Boolean) {
            overlayInstance.showBackground = show
        }

        fun setShowBorder(show: Boolean) {
            overlayInstance.showBorder = show
        }

        fun setBorderStyle(style: ArrayListModule.BorderStyle) {
            overlayInstance.borderStyle = style
        }

        fun setColorMode(mode: ArrayListModule.ColorMode) {
            overlayInstance.colorMode = mode
        }

        fun setRainbowSpeed(speed: Float) {
            overlayInstance.rainbowSpeed = speed
        }

        fun setFontSize(size: Int) {
            overlayInstance.fontSize = size
        }

        fun setSpacing(space: Int) {
            overlayInstance.spacing = space
        }

        fun setFadeAnimation(fade: Boolean) {
            overlayInstance.fadeAnimation = fade
        }

        fun setSlideAnimation(slide: Boolean) {
            overlayInstance.slideAnimation = slide
        }
    }

    @Composable
    override fun Content() {
        if (!isOverlayEnabled()) return

        var rainbowOffset by remember { mutableStateOf(0f) }

        LaunchedEffect(Unit) {
            while (true) {
                rainbowOffset += rainbowSpeed * 0.01f
                if (rainbowOffset > 1f) rainbowOffset = 0f
                delay(16L)
            }
        }

        val sortedModules = when (sortMode) {
            ArrayListModule.SortMode.LENGTH -> modules.sortedByDescending { it.name.length }
            ArrayListModule.SortMode.ALPHABETICAL -> modules.sortedBy { it.name }
            ArrayListModule.SortMode.CATEGORY -> modules.sortedBy { it.category }
            ArrayListModule.SortMode.CUSTOM -> modules.sortedByDescending { it.priority }
        }

        Column(
            horizontalAlignment = Alignment.End
        ) {
            sortedModules.forEachIndexed { index, module ->
                AnimatedVisibility(
                    visible = true,
                    enter = if (fadeAnimation) fadeIn(animationSpec = tween(animationSpeed)) else fadeIn(tween(0)) +
                            if (slideAnimation) slideInHorizontally(animationSpec = tween(animationSpeed)) { it } else slideInHorizontally(tween(0)) { 0 },
                    exit = if (fadeAnimation) fadeOut(animationSpec = tween(animationSpeed)) else fadeOut(tween(0)) +
                           if (slideAnimation) slideOutHorizontally(animationSpec = tween(animationSpeed)) { it } else slideOutHorizontally(tween(0)) { 0 }
                ) {
                    ModuleItem(
                        module = module,
                        index = index,
                        rainbowOffset = rainbowOffset,
                        isLast = index == sortedModules.size - 1
                    )
                }
            }
        }
    }



    @Composable
    private fun ModuleItem(
        module: ModuleInfo,
        index: Int,
        rainbowOffset: Float,
        isLast: Boolean
    ) {
        val moduleColor = getModuleColor(index, rainbowOffset)
        val borderWidth = if (showBorder) 2.dp else 0.dp

        Box(
            modifier = Modifier
                .padding(bottom = if (!isLast) spacing.dp else 0.dp)
                .let { modifier ->
                    if (showBackground) {
                        modifier
                            .background(
                                WColors.Surface.copy(alpha = 0.7f),
                                RoundedCornerShape(4.dp)
                            )
                            .clip(RoundedCornerShape(4.dp))
                    } else modifier
                }
                .let { modifier ->
                    when (borderStyle) {
                        ArrayListModule.BorderStyle.LEFT -> modifier.border(
                            width = borderWidth,
                            color = moduleColor,
                            shape = RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                        )
                        ArrayListModule.BorderStyle.RIGHT -> modifier.border(
                            width = borderWidth,
                            color = moduleColor,
                            shape = RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                        )
                        ArrayListModule.BorderStyle.FULL -> modifier.border(
                            width = borderWidth,
                            color = moduleColor,
                            shape = RoundedCornerShape(4.dp)
                        )
                        else -> modifier
                    }
                }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = module.name.translatedSelf,
                color = moduleColor,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    private fun getModuleColor(index: Int, rainbowOffset: Float): Color {
        return when (colorMode) {
            ArrayListModule.ColorMode.RAINBOW -> {
                val hue = (rainbowOffset + index * 0.1f) % 1f
                hsvToRgb(hue, 0.8f, 1f)
            }
            ArrayListModule.ColorMode.GRADIENT -> {
                val progress = index.toFloat() / maxOf(modules.size - 1, 1)
                lerpColor(WColors.Accent, WColors.Secondary, progress)
            }
            ArrayListModule.ColorMode.STATIC -> WColors.Accent
            ArrayListModule.ColorMode.CATEGORY_BASED -> when (modules.getOrNull(index)?.category) {
                "Combat" -> Color.Red
                "Movement" -> Color.Blue
                "Visual" -> Color.Green
                "Misc" -> Color.Yellow
                "World" -> Color.Cyan
                else -> WColors.Accent
            }
            ArrayListModule.ColorMode.RANDOM -> {
                val colors = listOf(Color.Red, Color.Blue, Color.Green, Color.Yellow, Color.Magenta, Color.Cyan)
                colors[index % colors.size]
            }
        }
    }

    private fun hsvToRgb(h: Float, s: Float, v: Float): Color {
        val hDegrees = h * 360f
        val c = v * s
        val x = c * (1 - abs((hDegrees / 60f) % 2 - 1))
        val m = v - c

        val (r, g, b) = when {
            hDegrees < 60 -> Triple(c, x, 0f)
            hDegrees < 120 -> Triple(x, c, 0f)
            hDegrees < 180 -> Triple(0f, c, x)
            hDegrees < 240 -> Triple(0f, x, c)
            hDegrees < 300 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

        return Color(
            red = (r + m).coerceIn(0f, 1f),
            green = (g + m).coerceIn(0f, 1f),
            blue = (b + m).coerceIn(0f, 1f)
        )
    }

    private fun lerpColor(start: Color, stop: Color, fraction: Float): Color {
        return Color(
            red = start.red + fraction * (stop.red - start.red),
            green = start.green + fraction * (stop.green - start.green),
            blue = start.blue + fraction * (stop.blue - start.blue),
            alpha = start.alpha + fraction * (stop.alpha - start.alpha)
        )
    }
}
