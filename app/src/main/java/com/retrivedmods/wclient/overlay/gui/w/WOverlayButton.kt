package com.retrivedmods.wclient.overlay.gui.w

import android.content.Context
import com.retrivedmods.wclient.overlay.OverlayWindow
import android.graphics.BitmapFactory
import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.retrivedmods.wclient.R
import com.retrivedmods.wclient.ui.theme.WColors
import java.io.File

class WOverlayButton : OverlayWindow() {

    override val layoutParams by lazy {
        WindowManager.LayoutParams().apply {
            width = 120
            height = 120
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            format = android.graphics.PixelFormat.TRANSLUCENT

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alpha = 1.0f
                blurBehindRadius = 10
            }
        }
    }

    @Composable
    override fun Content() {
        var isPressed by remember { mutableStateOf(false) }
        val context = LocalContext.current

        // Simple press animation only
        val pressScale by animateFloatAsState(
            targetValue = if (isPressed) 0.95f else 1f,
            animationSpec = spring(stiffness = Spring.StiffnessHigh),
            label = "press_scale"
        )

        // Load custom icon if available
        val customIconPath = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getString("overlay_icon_path", null)

        val customIcon = if (customIconPath != null) {
            try {
                val file = File(customIconPath)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    bitmap?.asImageBitmap()
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                        layoutParams.x += dragAmount.x.toInt()
                        layoutParams.y += dragAmount.y.toInt()
                        try {
                            windowManager.updateViewLayout(composeView, layoutParams)
                        } catch (_: Exception) {
                            // Handle exception silently
                        }
                    }
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    isPressed = true
                    WOverlayManager.showClickGUI()
                },
            contentAlignment = Alignment.Center
        ) {
            // Simple main button - no glow ring
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .scale(pressScale)
                    .background(
                        color = Color.Black,
                        shape = CircleShape
                    )
                    .border(
                        width = 2.dp,
                        color = Color.Cyan,
                        shape = CircleShape
                    )
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (customIcon != null) {
                    // Display custom icon
                    Image(
                        bitmap = customIcon,
                        contentDescription = "Custom Overlay Icon",
                        modifier = Modifier.size(48.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    // Display default W_overlay_icon.png
                    Image(
                        painter = painterResource(id = R.drawable.w_overlay_icon),
                        contentDescription = "W Overlay Icon",
                        modifier = Modifier.size(48.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
        
        // Reset pressed state
        LaunchedEffect(isPressed) {
            if (isPressed) {
                kotlinx.coroutines.delay(100)
                isPressed = false
            }
        }
    }
}
