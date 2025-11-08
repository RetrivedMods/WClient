package com.retrivedmods.wclient.overlay.hud

import android.view.Gravity
import com.retrivedmods.wclient.overlay.OverlayWindow
import com.retrivedmods.wclient.overlay.OverlayManager
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrivedmods.wclient.ui.theme.WColors
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class MinimapPosition(val x: Float, val y: Float)
data class MinimapEntity(val position: MinimapPosition, val type: EntityType, val name: String = "", val distance: Float = 0f)

enum class EntityType {
    PLAYER, MOB, ITEM
}

class MinimapOverlay : OverlayWindow() {
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
            x = 50
            y = 50
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    private var centerPosition by mutableStateOf(MinimapPosition(0f, 0f))
    private var playerRotation by mutableStateOf(0f)
    private var entities by mutableStateOf(listOf<MinimapEntity>())
    private var minimapSize by mutableStateOf(120f)
    private var targetRotation by mutableStateOf(0f)
    private var rotationSmoothStep = 0.15f
    private var minimapZoom by mutableStateOf(1.0f)
    private var minimapDotSize by mutableStateOf(3)
    private var showNames by mutableStateOf(true)
    private var showDistance by mutableStateOf(false)
    private var showCoordinates by mutableStateOf(false)
    private var entityCount by mutableStateOf(0)

    companion object {
        val overlayInstance by lazy { MinimapOverlay() }
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

        fun setCenter(x: Float, y: Float) {
            overlayInstance.centerPosition = MinimapPosition(x, y)
        }

        fun setPlayerRotation(rotation: Float) {
            overlayInstance.targetRotation = rotation
        }

        fun setEntities(entityList: List<MinimapEntity>) {
            overlayInstance.entities = entityList
            overlayInstance.entityCount = entityList.size
        }

        fun setMinimapSize(size: Float) {
            overlayInstance.minimapSize = size
        }

        fun setMinimapZoom(zoom: Float) {
            overlayInstance.minimapZoom = zoom
        }

        fun setDotSize(size: Int) {
            overlayInstance.minimapDotSize = size
        }

        fun setShowNames(show: Boolean) {
            overlayInstance.showNames = show
        }

        fun setShowDistance(show: Boolean) {
            overlayInstance.showDistance = show
        }

        fun setShowCoordinates(show: Boolean) {
            overlayInstance.showCoordinates = show
        }
    }

    @Composable
    override fun Content() {
        if (!isOverlayEnabled()) return

        LaunchedEffect(targetRotation) {
            while (kotlin.math.abs(playerRotation - targetRotation) > 0.001f) {
                var delta = (targetRotation - playerRotation) % (2 * Math.PI).toFloat()
                if (delta > Math.PI) delta -= (2 * Math.PI).toFloat()
                if (delta < -Math.PI) delta += (2 * Math.PI).toFloat()

                playerRotation += delta * rotationSmoothStep
                kotlinx.coroutines.delay(16L)
            }
        }

        Minimap(centerPosition, playerRotation, entities, minimapSize)
    }

    @Composable
    private fun Minimap(center: MinimapPosition, rotation: Float, entities: List<MinimapEntity>, size: Float) {
        val dpSize = size.dp
        val rawRadius = size / 2
        val radius = rawRadius * minimapZoom
        val scale = 2f * minimapZoom

        Column {
            Box(
                modifier = Modifier
                    .size(dpSize)
                    .background(WColors.MinimapBackground, shape = RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
            Canvas(modifier = Modifier.size(dpSize)) {
                val centerX = this.size.width / 2
                val centerY = this.size.height / 2

                val gridColor = WColors.MinimapGrid
                val gridSpacing = this.size.width / 10
                for (i in 1 until 10) {
                    val x = i * gridSpacing
                    drawLine(gridColor, Offset(x, 0f), Offset(x, this.size.height), strokeWidth = 1f)
                    drawLine(gridColor, Offset(0f, x), Offset(this.size.width, x), strokeWidth = 1f)
                }

                drawLine(WColors.MinimapCrosshair, Offset(centerX, 0f), Offset(centerX, this.size.height), strokeWidth = 1.5f)
                drawLine(WColors.MinimapCrosshair, Offset(0f, centerY), Offset(this.size.width, centerY), strokeWidth = 1.5f)

                val playerDotRadius = minimapDotSize * minimapZoom
                drawCircle(WColors.MinimapPlayerMarker, radius = playerDotRadius, center = Offset(centerX, centerY))

                val northAngle = -rotation
                val northDistance = rawRadius * 0.95f
                val northX = centerX + northDistance * sin(northAngle)
                val northY = centerY - northDistance * cos(northAngle)

                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLUE
                    textSize = size * 0.14f
                    textAlign = android.graphics.Paint.Align.CENTER
                    isFakeBoldText = true
                    isAntiAlias = true
                }

                drawContext.canvas.nativeCanvas.drawText("^", northX, northY - paint.textSize * 0.6f, paint)
                drawContext.canvas.nativeCanvas.drawText("N", northX, northY + paint.textSize * 0.4f, paint)

                entities.forEach { entity ->
                    val relX = entity.position.x - center.x
                    val relY = entity.position.y - center.y
                    val distance = sqrt(relX * relX + relY * relY) * scale

                    val dotRadius = minimapDotSize * minimapZoom

                    val angle = atan2(relY, relX) - rotation
                    val clampedDistance = if (distance < radius * 0.9f) distance else radius * 0.85f
                    val entityX = centerX + clampedDistance * sin(angle)
                    val entityY = centerY - clampedDistance * cos(angle)

                    val entityColor = when (entity.type) {
                        EntityType.PLAYER -> if (distance < radius * 0.9f) WColors.MinimapEntityClose else WColors.MinimapEntityFar
                        EntityType.MOB -> if (distance < radius * 0.9f) WColors.Secondary else WColors.SecondaryLight
                        EntityType.ITEM -> if (distance < radius * 0.9f) WColors.Accent else WColors.AccentLight
                    }

                    drawCircle(
                        color = entityColor,
                        radius = dotRadius,
                        center = Offset(entityX, entityY)
                    )

                    if (showNames && entity.name.isNotEmpty()) {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.WHITE
                            textSize = size * 0.08f
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }

                        val textY = entityY - dotRadius - 5f
                        val displayText = if (showDistance) {
                            "${entity.name} (${entity.distance.toInt()}m)"
                        } else {
                            entity.name
                        }

                        drawContext.canvas.nativeCanvas.drawText(displayText, entityX, textY, paint)
                    }
                }
            }
            }

            if (showCoordinates) {
                Text(
                    text = "X: ${center.x.toInt()} Z: ${center.y.toInt()} | Entities: $entityCount",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
