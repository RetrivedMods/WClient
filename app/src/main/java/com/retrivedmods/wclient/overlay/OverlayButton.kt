package com.retrivedmods.wclient.overlay

import android.content.res.Configuration
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.retrivedmods.wclient.R
import kotlin.math.min

class OverlayButton : OverlayWindow() {

    private val _layoutParams by lazy {
        super.layoutParams.apply {
            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            windowAnimations = android.R.style.Animation_Toast
            x = 0
            y = 100
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    private val overlayClickGUI by lazy { OverlayClickGUI() }

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val width = context.resources.displayMetrics.widthPixels
        val height = context.resources.displayMetrics.heightPixels
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        // Reposition if screen orientation changes
        LaunchedEffect(isLandscape) {
            _layoutParams.x = min(width, _layoutParams.x)
            _layoutParams.y = min(height, _layoutParams.y)
            windowManager.updateViewLayout(composeView, _layoutParams)
        }

        ElevatedCard(
            onClick = {
                OverlayManager.showOverlayWindow(overlayClickGUI)
            },
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .padding(5.dp)
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        _layoutParams.x += dragAmount.x.toInt()
                        _layoutParams.y += dragAmount.y.toInt()
                        windowManager.updateViewLayout(composeView, _layoutParams)
                    }
                }
        ) {
            Image(
                painter = painterResource(R.drawable.my_icon),
                contentDescription = "Overlay Button Icon",
                modifier = Modifier
                    .padding(0.dp)
                    .size(50.dp)
            )
        }
    }
}
