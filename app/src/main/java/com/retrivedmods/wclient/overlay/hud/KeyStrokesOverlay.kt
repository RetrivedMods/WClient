package com.retrivedmods.wclient.overlay.hud

import android.content.res.Configuration
import com.retrivedmods.wclient.overlay.OverlayWindow
import com.retrivedmods.wclient.overlay.OverlayManager
import android.view.WindowManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrivedmods.wclient.ui.theme.WColors
import kotlin.math.min

class KeyStrokesOverlay : OverlayWindow() {

    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = flags or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            x = 100
            y = 100
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    private var keyStates by mutableStateOf(
        mapOf(
            "W" to false,
            "A" to false,
            "S" to false,
            "D" to false,
            "Space" to false,
            "Shift" to false
        )
    )

    private var keySize by mutableIntStateOf(40)
    private var keySpacing by mutableIntStateOf(4)
    private var showSneak by mutableStateOf(true)
    private var spacebarWidth by mutableIntStateOf(130)
    private var backgroundOpacity by mutableFloatStateOf(0.85f)
    private var animationSpeed by mutableIntStateOf(80)
    private var showBackground by mutableStateOf(true)
    private var roundedCorners by mutableStateOf(true)

    companion object {
        val overlayInstance by lazy { KeyStrokesOverlay() }
        private var shouldShowOverlay = false

        fun showOverlay() {
            if (shouldShowOverlay) {
                try {
                    OverlayManager.showOverlayWindow(overlayInstance)
                } catch (e: Exception) {
                    println("Error showing KeyStrokesOverlay: ${e.message}")
                }
            }
        }

        fun dismissOverlay() {
            try {
                OverlayManager.dismissOverlayWindow(overlayInstance)
            } catch (e: Exception) {
                println("Error dismissing KeyStrokesOverlay: ${e.message}")
            }
        }

        fun setOverlayEnabled(enabled: Boolean) {
            shouldShowOverlay = enabled
            if (enabled) showOverlay() else dismissOverlay()
        }

        fun isOverlayEnabled(): Boolean = shouldShowOverlay

        fun setKeyState(key: String, isPressed: Boolean) {
            overlayInstance.keyStates = overlayInstance.keyStates.toMutableMap().apply {
                if (containsKey(key)) {
                    this[key] = isPressed
                }
            }
        }

        fun setPosition(x: Int, y: Int) {
            overlayInstance._layoutParams.x = x
            overlayInstance._layoutParams.y = y
        }

        fun setKeySize(size: Int) {
            overlayInstance.keySize = size
        }

        fun setKeySpacing(spacing: Int) {
            overlayInstance.keySpacing = spacing
        }

        fun setShowSneak(show: Boolean) {
            overlayInstance.showSneak = show
        }

        fun setSpacebarWidth(width: Int) {
            overlayInstance.spacebarWidth = width
        }

        fun setBackgroundOpacity(opacity: Float) {
            overlayInstance.backgroundOpacity = opacity
        }

        fun setAnimationSpeed(speed: Int) {
            overlayInstance.animationSpeed = speed
        }

        fun setShowBackground(show: Boolean) {
            overlayInstance.showBackground = show
        }

        fun setRoundedCorners(rounded: Boolean) {
            overlayInstance.roundedCorners = rounded
        }
    }

    @Composable
    override fun Content() {
        if (!isOverlayEnabled()) return

        val context = LocalContext.current
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        LaunchedEffect(isLandscape) {
            val width = context.resources.displayMetrics.widthPixels
            val height = context.resources.displayMetrics.heightPixels
            _layoutParams.x = min(width - 130, _layoutParams.x)
            _layoutParams.y = min(height - 130, _layoutParams.y)
            windowManager.updateViewLayout(composeView, _layoutParams)
        }

        KeyStrokesContent(keyStates = keyStates) { dx, dy ->
            _layoutParams.x += dx.toInt()
            _layoutParams.y += dy.toInt()
            windowManager.updateViewLayout(composeView, _layoutParams)
        }
    }

    @Composable
    private fun KeyStrokesContent(
        keyStates: Map<String, Boolean>,
        onDrag: (Float, Float) -> Unit
    ) {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .pointerInput(Unit) {
                    detectDragGestures { _, drag ->
                        onDrag(drag.x, drag.y)
                    }
                }
        ) {
            Column(
                modifier = Modifier.wrapContentSize(),
                verticalArrangement = Arrangement.spacedBy(keySpacing.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                KeyButton(
                    label = "W",
                    isPressed = keyStates["W"] ?: false,
                    modifier = Modifier.size(keySize.dp)
                )

                Row(
                    modifier = Modifier.wrapContentSize(),
                    horizontalArrangement = Arrangement.spacedBy(keySpacing.dp, Alignment.CenterHorizontally)
                ) {
                    KeyButton(
                        label = "A",
                        isPressed = keyStates["A"] ?: false,
                        modifier = Modifier.size(keySize.dp)
                    )
                    KeyButton(
                        label = "S",
                        isPressed = keyStates["S"] ?: false,
                        modifier = Modifier.size(keySize.dp)
                    )
                    KeyButton(
                        label = "D",
                        isPressed = keyStates["D"] ?: false,
                        modifier = Modifier.size(keySize.dp)
                    )
                }

                KeyButton(
                    label = " ",
                    isPressed = keyStates["Space"] ?: false,
                    modifier = Modifier.size(spacebarWidth.dp, keySize.dp)
                )

                if (showSneak) {
                    KeyButton(
                        label = "⇧",
                        isPressed = keyStates["Shift"] ?: false,
                        modifier = Modifier.size(spacebarWidth.dp, keySize.dp)
                    )
                }
            }
        }
    }

    @Composable
    private fun KeyButton(
        label: String,
        isPressed: Boolean,
        modifier: Modifier = Modifier
    ) {
        val animValue by animateFloatAsState(
            targetValue = if (isPressed) 1f else 0f,
            animationSpec = tween(durationMillis = animationSpeed),
            label = "KeyAnimation_$label"
        )
        val scale by animateFloatAsState(
            targetValue = lerp(0.96f, 0.91f, animValue),
            animationSpec = tween(durationMillis = animationSpeed),
            label = "ScaleAnimation_$label"
        )

        val baseColor = if (showBackground) {
            WColors.Surface.copy(alpha = backgroundOpacity)
        } else {
            Color.Transparent
        }
        val pressedColor = WColors.Accent.copy(alpha = 0.8f)
        val borderColor = WColors.OnSurface.copy(alpha = 0.6f)
        val textColor = WColors.OnSurface

        val cornerRadius = if (roundedCorners) 12.dp else 4.dp

        Box(
            modifier = modifier
                .scale(scale)
                .clip(RoundedCornerShape(cornerRadius))
                .background(
                    if (isPressed) pressedColor else baseColor
                )
                .border(
                    width = 1.dp,
                    color = if (isPressed) WColors.Accent else borderColor,
                    shape = RoundedCornerShape(cornerRadius)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = if (isPressed) Color.White else textColor,
                fontSize = (keySize * 0.4f).sp,
                fontWeight = if (isPressed) FontWeight.Bold else FontWeight.Medium,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

    private fun lerp(start: Float, stop: Float, fraction: Float): Float =
        start + fraction * (stop - start)
}
