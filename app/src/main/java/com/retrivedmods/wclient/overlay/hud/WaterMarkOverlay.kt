package com.retrivedmods.wclient.overlay.hud

import android.view.Gravity
import android.view.WindowManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrivedmods.wclient.BuildConfig
import com.retrivedmods.wclient.overlay.OverlayManager
import com.retrivedmods.wclient.overlay.OverlayWindow
import com.retrivedmods.wclient.game.module.misc.WaterMarkModule
import com.retrivedmods.wclient.ui.theme.WColors
import kotlinx.coroutines.delay
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


    private var customText by mutableStateOf("WClient")
    private var showVersion by mutableStateOf(true)
    private var position by mutableStateOf(WaterMarkModule.Position.TOP_LEFT)
    private var fontSize by mutableStateOf(18)
    private var rainbowSpeed by mutableStateOf(1.0f)
    private var showBackground by mutableStateOf(true)
    private var backgroundOpacity by mutableStateOf(0.7f)
    private var showShadow by mutableStateOf(true)
    private var shadowOffset by mutableStateOf(2)
    private var animateText by mutableStateOf(true)
    private var glowEffect by mutableStateOf(false)
    private var borderStyle by mutableStateOf(WaterMarkModule.BorderStyle.NONE)


    companion object {
        val overlayInstance by lazy { WaterMarkOverlay() }
        private var shouldShowOverlay = false

        fun setOverlayEnabled(enabled: Boolean) {
            shouldShowOverlay = enabled
            try {
                if (enabled) OverlayManager.showOverlayWindow(overlayInstance)
                else OverlayManager.dismissOverlayWindow(overlayInstance)
            } catch (_: Exception) {}
        }

        fun isOverlayEnabled(): Boolean = shouldShowOverlay

        fun setCustomText(text: String) {
            overlayInstance.customText = text
        }

        fun setShowVersion(show: Boolean) {
            overlayInstance.showVersion = show
        }

        fun setPosition(pos: WaterMarkModule.Position) {
            overlayInstance.position = pos
            overlayInstance.updateLayoutParams()
        }

        fun setFontSize(size: Int) {
            overlayInstance.fontSize = size
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
        _layoutParams.gravity = when (position) {
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

        try {
            windowManager.updateViewLayout(composeView, _layoutParams)
        } catch (_: Exception) {}
    }



    @Composable
    override fun Content() {
        if (!shouldShowOverlay) return

        var rainbowOffset by remember { mutableStateOf(0f) }
        var pulse by remember { mutableStateOf(0f) }

        LaunchedEffect(Unit) {
            while (true) {
                rainbowOffset = (rainbowOffset + rainbowSpeed * 0.01f) % 1f
                pulse = (pulse + 0.05f) % 1f
                delay(16)
            }
        }

        val scale by animateFloatAsState(
            targetValue = if (animateText) 1f + sin(pulse * 6.28f) * 0.05f else 1f,
            animationSpec = tween(120),
            label = "scale"
        )

        val rainbowColors = List(7) { i ->
            val hue = ((i * 360f / 7) + rainbowOffset * 360f) % 360f
            Color.hsv(hue, 1f, 1f)
        }

        val gradientBrush = Brush.horizontalGradient(rainbowColors)

        Box(
            modifier = Modifier
                .scale(scale)
                .pointerInput(Unit) {
                    detectDragGestures { _, drag ->
                        _layoutParams.x += drag.x.toInt()
                        _layoutParams.y += drag.y.toInt()
                        try {
                            windowManager.updateViewLayout(composeView, _layoutParams)
                        } catch (_: Exception) {}
                    }
                }
                .then(
                    if (showBackground)
                        Modifier
                            .background(
                                WColors.Surface.copy(alpha = backgroundOpacity),
                                RoundedCornerShape(10.dp)
                            )
                            .clip(RoundedCornerShape(10.dp))
                    else Modifier
                )
                .then(
                    if (borderStyle == WaterMarkModule.BorderStyle.SOLID)
                        Modifier.border(2.dp, gradientBrush, RoundedCornerShape(10.dp))
                    else Modifier
                )
                .then(
                    if (glowEffect)
                        Modifier.drawBehind {
                            drawIntoCanvas {
                                val paint = Paint().asFrameworkPaint().apply {
                                    color = Color.White.toArgb()
                                    setShadowLayer(20f, 0f, 0f, Color.White.toArgb())
                                }
                                it.nativeCanvas.drawRoundRect(
                                    0f, 0f, size.width, size.height,
                                    12f, 12f, paint
                                )
                            }
                        }
                    else Modifier
                )
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {

            if (showShadow) {
                Text(
                    text = buildAnnotatedWatermark(
                        Brush.horizontalGradient(listOf(Color.Black, Color.Black))
                    ),
                    modifier = Modifier.offset(shadowOffset.dp, shadowOffset.dp),
                    style = TextStyle(color = Color.Black.copy(alpha = 0.4f))
                )
            }

            Text(
                text = buildAnnotatedWatermark(gradientBrush),
                style = TextStyle(fontWeight = FontWeight.Medium)
            )
        }
    }

    @Composable
    private fun buildAnnotatedWatermark(brush: Brush): AnnotatedString {
        return buildAnnotatedString {
            withStyle(
                SpanStyle(
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.SemiBold,
                    brush = brush
                )
            ) {
                append(customText)
            }

            if (showVersion) {
                withStyle(
                    SpanStyle(
                        fontSize = (fontSize * 0.55f).sp,
                        fontWeight = FontWeight.Medium,
                        baselineShift = BaselineShift.Superscript,
                        brush = brush
                    )
                ) {
                    append(" v${BuildConfig.VERSION_NAME}")
                }
            }
        }
    }
}
