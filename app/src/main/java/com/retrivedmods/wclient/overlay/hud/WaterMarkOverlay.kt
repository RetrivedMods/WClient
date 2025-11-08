package com.retrivedmods.wclient.overlay.hud

import android.view.Gravity
import com.retrivedmods.wclient.overlay.OverlayWindow
import com.retrivedmods.wclient.overlay.OverlayManager
import android.view.WindowManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrivedmods.wclient.game.module.misc.WaterMarkModule
import com.retrivedmods.wclient.ui.theme.WColors
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.sin

class WaterMarkOverlay : OverlayWindow() {

    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = flags or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = 20
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    private var customText by mutableStateOf("W Client")
    private var showVersion by mutableStateOf(true)
    private var showTime by mutableStateOf(false)
    private var position by mutableStateOf(WaterMarkModule.Position.TOP_LEFT)
    private var fontSize by mutableStateOf(18)
    private var colorMode by mutableStateOf(WaterMarkModule.ColorMode.RAINBOW)
    private var rainbowSpeed by mutableStateOf(1.0f)
    private var showBackground by mutableStateOf(true)
    private var backgroundOpacity by mutableStateOf(0.7f)
    private var showShadow by mutableStateOf(true)
    private var shadowOffset by mutableStateOf(2)
    private var animateText by mutableStateOf(false)
    private var glowEffect by mutableStateOf(false)
    private var borderStyle by mutableStateOf(WaterMarkModule.BorderStyle.NONE)

    companion object {
        val overlayInstance by lazy { WaterMarkOverlay() }
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

        fun setCustomText(text: String) {
            overlayInstance.customText = text
        }

        fun setShowVersion(show: Boolean) {
            overlayInstance.showVersion = show
        }

        fun setShowTime(show: Boolean) {
            overlayInstance.showTime = show
        }

        fun setPosition(pos: WaterMarkModule.Position) {
            overlayInstance.position = pos
            overlayInstance.updateLayoutParams()
        }

        fun setFontSize(size: Int) {
            overlayInstance.fontSize = size
        }

        fun setColorMode(mode: WaterMarkModule.ColorMode) {
            overlayInstance.colorMode = mode
        }

        fun setRainbowSpeed(speed: Float) {
            overlayInstance.rainbowSpeed = speed
        }

        fun setShowBackground(show: Boolean) {
            overlayInstance.showBackground = show
        }

        fun setBackgroundOpacity(opacity: Float) {
            overlayInstance.backgroundOpacity = opacity
        }

        fun setShowShadow(show: Boolean) {
            overlayInstance.showShadow = show
        }

        fun setShadowOffset(offset: Int) {
            overlayInstance.shadowOffset = offset
        }

        fun setAnimateText(animate: Boolean) {
            overlayInstance.animateText = animate
        }

        fun setGlowEffect(glow: Boolean) {
            overlayInstance.glowEffect = glow
        }

        fun setBorderStyle(style: WaterMarkModule.BorderStyle) {
            overlayInstance.borderStyle = style
        }
    }

    private fun updateLayoutParams() {
        val gravity = when (position) {
            WaterMarkModule.Position.TOP_LEFT -> Gravity.TOP or Gravity.START
            WaterMarkModule.Position.TOP_CENTER -> Gravity.TOP or Gravity.CENTER_HORIZONTAL
            WaterMarkModule.Position.TOP_RIGHT -> Gravity.TOP or Gravity.END
            WaterMarkModule.Position.CENTER_LEFT -> Gravity.CENTER_VERTICAL or Gravity.START
            WaterMarkModule.Position.CENTER -> Gravity.CENTER
            WaterMarkModule.Position.CENTER_RIGHT -> Gravity.CENTER_VERTICAL or Gravity.END
            WaterMarkModule.Position.BOTTOM_LEFT -> Gravity.BOTTOM or Gravity.START
            WaterMarkModule.Position.BOTTOM_CENTER -> Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            WaterMarkModule.Position.BOTTOM_RIGHT -> Gravity.BOTTOM or Gravity.END
        }
        
        _layoutParams.gravity = gravity
        
        try {
            windowManager.updateViewLayout(composeView, _layoutParams)
        } catch (e: Exception) {}
    }

    @Composable
    override fun Content() {
        if (!isOverlayEnabled()) return

        var rainbowOffset by remember { mutableStateOf(0f) }
        var pulseOffset by remember { mutableStateOf(0f) }
        var waveOffset by remember { mutableStateOf(0f) }

        LaunchedEffect(Unit) {
            while (true) {
                rainbowOffset += rainbowSpeed * 0.02f
                pulseOffset += 0.05f
                waveOffset += 0.1f
                if (rainbowOffset > 1f) rainbowOffset = 0f
                if (pulseOffset > 1f) pulseOffset = 0f
                if (waveOffset > 1f) waveOffset = 0f
                delay(16L)
            }
        }

        val scale by animateFloatAsState(
            targetValue = if (animateText) 1f + sin(pulseOffset * 6.28f) * 0.05f else 1f,
            animationSpec = tween(100),
            label = "TextScale"
        )

        val watermarkText = buildWatermarkText()
        val textColor = getTextColor(rainbowOffset, pulseOffset, waveOffset)

        Box(
            modifier = Modifier
                .scale(scale)
                .pointerInput(Unit) {
                    detectDragGestures { _, drag ->
                        _layoutParams.x = (_layoutParams.x + drag.x.toInt()).coerceAtLeast(0)
                        _layoutParams.y = (_layoutParams.y + drag.y.toInt()).coerceAtLeast(0)
                        try {
                            windowManager.updateViewLayout(composeView, _layoutParams)
                        } catch (e: Exception) {}
                    }
                }
                .let { modifier ->
                    if (showBackground) {
                        modifier
                            .background(
                                WColors.Surface.copy(alpha = backgroundOpacity),
                                RoundedCornerShape(8.dp)
                            )
                            .clip(RoundedCornerShape(8.dp))
                    } else modifier
                }
                .let { modifier ->
                    when (borderStyle) {
                        WaterMarkModule.BorderStyle.SOLID -> modifier.border(
                            2.dp, textColor, RoundedCornerShape(8.dp)
                        )
                        WaterMarkModule.BorderStyle.DASHED -> modifier.border(
                            2.dp, textColor, RoundedCornerShape(8.dp)
                        )
                        WaterMarkModule.BorderStyle.GLOW -> modifier.drawBehind {
                            if (glowEffect) {
                                drawIntoCanvas { canvas ->
                                    val paint = Paint().asFrameworkPaint().apply {
                                        color = textColor.toArgb()
                                        setShadowLayer(10f, 0f, 0f, textColor.toArgb())
                                    }
                                    canvas.nativeCanvas.drawRoundRect(
                                        0f, 0f, size.width, size.height, 8f, 8f, paint
                                    )
                                }
                            }
                        }
                        else -> modifier
                    }
                }
                .padding(12.dp)
        ) {
            if (showShadow && shadowOffset > 0) {
                Text(
                    text = watermarkText,
                    color = Color.Black.copy(alpha = 0.5f),
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.offset(shadowOffset.dp, shadowOffset.dp)
                )
            }

            Text(
                text = watermarkText,
                color = textColor,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    private fun buildWatermarkText(): String {
        val parts = mutableListOf<String>()

        parts.add(customText)

        if (showVersion) {
            parts.add("v1.9")
        }

        if (showTime) {
            val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            parts.add(timeFormat.format(Date()))
        }

        return parts.joinToString(" | ")
    }

    private fun getTextColor(rainbowOffset: Float, pulseOffset: Float, waveOffset: Float): Color {
        return when (colorMode) {
            WaterMarkModule.ColorMode.RAINBOW -> {
                hsvToRgb(rainbowOffset, 0.8f, 1f)
            }
            WaterMarkModule.ColorMode.GRADIENT -> {
                lerpColor(WColors.Accent, WColors.Secondary, rainbowOffset)
            }
            WaterMarkModule.ColorMode.STATIC -> WColors.Accent
            WaterMarkModule.ColorMode.PULSING -> {
                val alpha = 0.5f + sin(pulseOffset * 6.28f) * 0.5f
                WColors.Accent.copy(alpha = alpha)
            }
            WaterMarkModule.ColorMode.WAVE -> {
                val hue = (waveOffset + sin(waveOffset * 6.28f) * 0.2f) % 1f
                hsvToRgb(hue, 0.8f, 1f)
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
