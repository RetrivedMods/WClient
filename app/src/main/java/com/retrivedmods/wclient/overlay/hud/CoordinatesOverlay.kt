package com.retrivedmods.wclient.overlay.hud

import android.annotation.SuppressLint
import com.retrivedmods.wclient.overlay.OverlayWindow
import com.retrivedmods.wclient.overlay.OverlayManager
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrivedmods.wclient.game.module.visual.CoordinatesModule
import com.retrivedmods.wclient.ui.theme.WColors
import kotlinx.coroutines.delay
import kotlin.math.abs

class CoordinatesOverlay : OverlayWindow() {

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
            y = 120
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    private var showCoordinates by mutableStateOf(true)
    private var showDirection by mutableStateOf(true)
    private var showDimension by mutableStateOf(true)
    private var showSpeed by mutableStateOf(false)
    private var showNetherCoords by mutableStateOf(true)
    private var position by mutableStateOf(CoordinatesModule.Position.TOP_LEFT)
    private var fontSize by mutableIntStateOf(14)
    private var colorMode by mutableStateOf(CoordinatesModule.ColorMode.STATIC)
    private var showBackground by mutableStateOf(true)
    private var backgroundOpacity by mutableFloatStateOf(0.7f)
    private var showBorder by mutableStateOf(false)
    private var compactMode by mutableStateOf(false)
    private var precision by mutableIntStateOf(1)

    private var coordinates by mutableStateOf(Triple(0.0, 0.0, 0.0))
    private var direction by mutableStateOf("North")
    private var dimension by mutableStateOf("Overworld")
    private var speed by mutableDoubleStateOf(0.0)
    private var netherCoordinates by mutableStateOf(Triple(0.0, 0.0, 0.0))

    companion object {
        val overlayInstance by lazy { CoordinatesOverlay() }
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

        fun setShowCoordinates(show: Boolean) {
            overlayInstance.showCoordinates = show
        }

        fun setShowDirection(show: Boolean) {
            overlayInstance.showDirection = show
        }

        fun setShowDimension(show: Boolean) {
            overlayInstance.showDimension = show
        }

        fun setShowSpeed(show: Boolean) {
            overlayInstance.showSpeed = show
        }

        fun setShowNetherCoords(show: Boolean) {
            overlayInstance.showNetherCoords = show
        }

        fun setPosition(pos: CoordinatesModule.Position) {
            overlayInstance.position = pos
            overlayInstance.updateLayoutParams()
        }

        fun setFontSize(size: Int) {
            overlayInstance.fontSize = size
        }

        fun setColorMode(mode: CoordinatesModule.ColorMode) {
            overlayInstance.colorMode = mode
        }

        fun setShowBackground(show: Boolean) {
            overlayInstance.showBackground = show
        }

        fun setBackgroundOpacity(opacity: Float) {
            overlayInstance.backgroundOpacity = opacity
        }

        fun setShowBorder(show: Boolean) {
            overlayInstance.showBorder = show
        }

        fun setCompactMode(compact: Boolean) {
            overlayInstance.compactMode = compact
        }

        fun setPrecision(p: Int) {
            overlayInstance.precision = p
        }

        fun setCoordinates(coords: Triple<Double, Double, Double>) {
            overlayInstance.coordinates = coords
        }

        fun setDirection(dir: String) {
            overlayInstance.direction = dir
        }

        fun setDimension(dim: String) {
            overlayInstance.dimension = dim
        }

        fun setSpeed(s: Double) {
            overlayInstance.speed = s
        }

        fun setNetherCoordinates(coords: Triple<Double, Double, Double>) {
            overlayInstance.netherCoordinates = coords
        }
    }

    private fun updateLayoutParams() {
        val gravity = when (position) {
            CoordinatesModule.Position.TOP_LEFT -> Gravity.TOP or Gravity.START
            CoordinatesModule.Position.TOP_RIGHT -> Gravity.TOP or Gravity.END
            CoordinatesModule.Position.BOTTOM_LEFT -> Gravity.BOTTOM or Gravity.START
            CoordinatesModule.Position.BOTTOM_RIGHT -> Gravity.BOTTOM or Gravity.END
            CoordinatesModule.Position.CENTER_LEFT -> Gravity.CENTER_VERTICAL or Gravity.START
            CoordinatesModule.Position.CENTER_RIGHT -> Gravity.CENTER_VERTICAL or Gravity.END
        }
        
        _layoutParams.gravity = gravity
        
        try {
            windowManager.updateViewLayout(composeView, _layoutParams)
        } catch (e: Exception) {}
    }

    @SuppressLint("UnrememberedMutableState")
    @Composable
    override fun Content() {
        if (!isOverlayEnabled()) return

        var rainbowOffset by mutableFloatStateOf(0f)

        LaunchedEffect(Unit) {
            while (true) {
                rainbowOffset += 0.02f
                if (rainbowOffset > 1f) rainbowOffset = 0f
                delay(16L)
            }
        }

        val textColor = getTextColor(rainbowOffset)

        Box(
            modifier = Modifier
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
                    if (showBorder) {
                        modifier.border(1.dp, WColors.Border, RoundedCornerShape(8.dp))
                    } else modifier
                }
                .padding(8.dp)
        ) {
            if (compactMode) {
                CompactCoordinatesDisplay(textColor)
            } else {
                DetailedCoordinatesDisplay(textColor)
            }
        }
    }

    @Composable
    private fun CompactCoordinatesDisplay(textColor: Color) {
        val coordText = buildCompactCoordinatesText()
        Text(
            text = coordText,
            color = textColor,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Medium
        )
    }

    @Composable
    private fun DetailedCoordinatesDisplay(textColor: Color) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (showCoordinates) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "XYZ:",
                        color = textColor.copy(alpha = 0.8f),
                        fontSize = fontSize.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatCoordinates(coordinates),
                        color = textColor,
                        fontSize = fontSize.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (showDirection) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Facing:",
                        color = textColor.copy(alpha = 0.8f),
                        fontSize = fontSize.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = direction,
                        color = getDirectionColor(direction),
                        fontSize = fontSize.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (showDimension) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Dimension:",
                        color = textColor.copy(alpha = 0.8f),
                        fontSize = fontSize.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = dimension,
                        color = getDimensionColor(dimension),
                        fontSize = fontSize.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (showSpeed) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Speed:",
                        color = textColor.copy(alpha = 0.8f),
                        fontSize = fontSize.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${"%.${precision}f".format(speed)} m/s",
                        color = getSpeedColor(speed),
                        fontSize = fontSize.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (showNetherCoords && dimension != "Nether") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Nether:",
                        color = textColor.copy(alpha = 0.8f),
                        fontSize = fontSize.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatCoordinates(netherCoordinates),
                        color = Color(0xFFFF6B6B), // Nether red
                        fontSize = fontSize.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    private fun buildCompactCoordinatesText(): String {
        val parts = mutableListOf<String>()
        
        if (showCoordinates) {
            parts.add(formatCoordinates(coordinates))
        }
        
        if (showDirection) {
            parts.add(direction.take(2))
        }
        
        if (showDimension) {
            parts.add(dimension.take(1))
        }
        
        if (showSpeed) {
            parts.add("${"%.1f".format(speed)}m/s")
        }
        
        return parts.joinToString(" | ")
    }

    private fun formatCoordinates(coords: Triple<Double, Double, Double>): String {
        val format = "%.${precision}f"
        return "${format.format(coords.first)}, ${format.format(coords.second)}, ${format.format(coords.third)}"
    }

    private fun getTextColor(rainbowOffset: Float): Color {
        return when (colorMode) {
            CoordinatesModule.ColorMode.STATIC -> WColors.OnSurface
            CoordinatesModule.ColorMode.RAINBOW -> hsvToRgb(rainbowOffset, 0.8f, 1f)
            CoordinatesModule.ColorMode.DIMENSION_BASED -> getDimensionColor(dimension)
            CoordinatesModule.ColorMode.SPEED_BASED -> getSpeedColor(speed)
        }
    }

    private fun getDirectionColor(dir: String): Color {
        return when (dir) {
            "North" -> Color(0xFF4FC3F7)
            "South" -> Color(0xFFFFB74D)
            "East" -> Color(0xFF81C784)
            "West" -> Color(0xFFE57373)
            "Northeast" -> Color(0xFF64B5F6)
            "Northwest" -> Color(0xFFAED581)
            "Southeast" -> Color(0xFFFFD54F)
            "Southwest" -> Color(0xFFFF8A65)
            else -> WColors.OnSurface
        }
    }

    private fun getDimensionColor(dim: String): Color {
        return when (dim) {
            "Overworld" -> Color(0xFF4CAF50) // Green
            "Nether" -> Color(0xFFFF5722) // Deep Orange/Red
            "End" -> Color(0xFF9C27B0) // Purple
            else -> WColors.OnSurface
        }
    }



    private fun getSpeedColor(speed: Double): Color {
        return when {
            speed < 2.0 -> Color(0xFF4CAF50)
            speed < 6.0 -> Color(0xFFFF9800)
            speed < 15.0 -> Color(0xFFFF5722)
            else -> Color(0xFFE91E63)
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
}