package com.retrivedmods.wclient.overlay.hud

import android.annotation.SuppressLint
import com.retrivedmods.wclient.overlay.OverlayWindow
import com.retrivedmods.wclient.overlay.OverlayManager
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrivedmods.wclient.ui.theme.WColors
import kotlin.math.cos
import kotlin.math.sin

class PieChartOverlay : OverlayWindow() {

    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = flags or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.BOTTOM or Gravity.START
            x = 20
            y = 100
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    private var performanceData by mutableStateOf(mapOf<String, Long>())
    private var chartSize by mutableStateOf(140)
    private var showPercentages by mutableStateOf(true)
    private var showLabels by mutableStateOf(true)
    private var transparentBackground by mutableStateOf(true)
    private var animateTransitions by mutableStateOf(true)
    private var highlightLargest by mutableStateOf(true)
    private var chart3DDepth by mutableStateOf(15)
    private var chartTilt by mutableStateOf(0.6f)
    private var borderWidth by mutableStateOf(1.5f)
    private var legendSpacing by mutableStateOf(2)
    private var legendFontSize by mutableStateOf(11)

    private var colorIntensity by mutableStateOf(1.0f)

    private val categoryColors = mapOf(
        "Rendering" to Color(0xFF00FF00),      // Bright Green
        "Entities" to Color(0xFF0080FF),       // Bright Blue  
        "Movement" to Color(0xFFFF00FF),       // Magenta
        "Sound" to Color(0xFFFF8000),          // Orange
        "Block Updates" to Color(0xFFFF0000),  // Bright Red
        "Effects" to Color(0xFF00FFFF),        // Cyan
        "Network" to Color(0xFFFFFF00),        // Yellow
        "World Tick" to Color(0xFF8000FF),     // Purple
        "Unspecified" to Color(0xFFC0C0C0)     // Light Gray
    )

    companion object {
        val overlayInstance by lazy { PieChartOverlay() }
        private var shouldShowOverlay = false

        fun showOverlay() {
            if (shouldShowOverlay) {
                try {
                    OverlayManager.showOverlayWindow(overlayInstance)
                } catch (e: Exception) {
                    println("Error showing PieChartOverlay: ${e.message}")
                }
            }
        }

        fun dismissOverlay() {
            try {
                OverlayManager.dismissOverlayWindow(overlayInstance)
            } catch (e: Exception) {
                println("Error dismissing PieChartOverlay: ${e.message}")
            }
        }

        fun setOverlayEnabled(enabled: Boolean) {
            shouldShowOverlay = enabled
            if (enabled) showOverlay() else dismissOverlay()
        }

        fun isOverlayEnabled(): Boolean = shouldShowOverlay

        fun setPerformanceData(data: Map<String, Long>) {
            overlayInstance.performanceData = data
        }

        fun setChartSize(size: Int) {
            overlayInstance.chartSize = size
        }

        fun setShowPercentages(show: Boolean) {
            overlayInstance.showPercentages = show
        }

        fun setShowLabels(show: Boolean) {
            overlayInstance.showLabels = show
        }

        fun setTransparentBackground(transparent: Boolean) {
            overlayInstance.transparentBackground = transparent
        }

        fun setAnimateTransitions(animate: Boolean) {
            overlayInstance.animateTransitions = animate
        }

        fun setHighlightLargest(highlight: Boolean) {
            overlayInstance.highlightLargest = highlight
        }

        fun setChart3DDepth(depth: Int) {
            overlayInstance.chart3DDepth = depth
        }

        fun setChartTilt(tilt: Float) {
            overlayInstance.chartTilt = tilt
        }

        fun setBorderWidth(width: Float) {
            overlayInstance.borderWidth = width
        }

        fun setLegendSpacing(spacing: Int) {
            overlayInstance.legendSpacing = spacing
        }

        fun setLegendFontSize(size: Int) {
            overlayInstance.legendFontSize = size
        }



        fun setColorIntensity(intensity: Float) {
            overlayInstance.colorIntensity = intensity
        }

        fun setPosition(x: Int, y: Int) {
            overlayInstance._layoutParams.x = x
            overlayInstance._layoutParams.y = y
        }
    }

    @Composable
    override fun Content() {
        if (!isOverlayEnabled()) return

        var animationTrigger by remember { mutableStateOf(0) }
        
        LaunchedEffect(performanceData) {
            if (animateTransitions) {
                animationTrigger++
            }
        }

        PieChartContent(
            performanceData = performanceData,
            chartSize = chartSize,
            showPercentages = showPercentages,
            showLabels = showLabels,
            transparentBackground = transparentBackground,
            highlightLargest = highlightLargest,
            chart3DDepth = chart3DDepth,
            chartTilt = chartTilt,
            borderWidth = borderWidth,
            legendSpacing = legendSpacing,
            legendFontSize = legendFontSize,

            colorIntensity = colorIntensity
        ) { dx, dy ->
            _layoutParams.x += dx.toInt()
            _layoutParams.y -= dy.toInt()
            windowManager.updateViewLayout(composeView, _layoutParams)
        }
    }

    @SuppressLint("DefaultLocale")
    @Composable
    private fun PieChartContent(
        performanceData: Map<String, Long>,
        chartSize: Int,
        showPercentages: Boolean,
        showLabels: Boolean,
        transparentBackground: Boolean,
        highlightLargest: Boolean,
        chart3DDepth: Int,
        chartTilt: Float,
        borderWidth: Float,
        legendSpacing: Int,
        legendFontSize: Int,

        colorIntensity: Float,
        onDrag: (Float, Float) -> Unit
    ) {
        val totalTime = performanceData.values.sum()
        if (totalTime == 0L) return

        val sortedData = performanceData.toList().sortedByDescending { it.second }
        val largestCategory = if (highlightLargest) sortedData.firstOrNull()?.first else null

        Box(
            modifier = Modifier
                .wrapContentSize()
                .pointerInput(Unit) {
                    detectDragGestures { _, drag ->
                        onDrag(drag.x, drag.y)
                    }
                }
        ) {
            Row(
                modifier = Modifier.wrapContentSize(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Box(
                    modifier = Modifier.size((chartSize * 1.2f).dp)
                ) {
                    Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        draw3DPieChart(
                            data = sortedData,
                            totalTime = totalTime,
                            largestCategory = largestCategory,
                            chart3DDepth = chart3DDepth,
                            chartTilt = chartTilt,
                            borderWidth = borderWidth,
                            colorIntensity = colorIntensity
                        )
                    }
                }

                if (showLabels) {
                    Column(
                        modifier = Modifier
                            .padding(bottom = 20.dp)
                            .let { modifier ->
                                if (!transparentBackground) {
                                    modifier.background(
                                        WColors.Surface.copy(alpha = 0.8f),
                                        RoundedCornerShape(6.dp)
                                    ).padding(8.dp)
                                } else modifier
                            },
                        verticalArrangement = Arrangement.spacedBy(legendSpacing.dp)
                    ) {

                        sortedData.forEach { (category, time) ->
                            val percentage = (time.toFloat() / totalTime * 100)
                            val baseColor = categoryColors[category] ?: Color.White
                            val color = Color(
                                red = (baseColor.red * colorIntensity).coerceAtMost(1f),
                                green = (baseColor.green * colorIntensity).coerceAtMost(1f),
                                blue = (baseColor.blue * colorIntensity).coerceAtMost(1f),
                                alpha = baseColor.alpha
                            )
                            val isLargest = category == largestCategory
                            
                            Text(
                                text = if (showPercentages) {
                                    "$category ${String.format("%.2f", percentage)}%"
                                } else {
                                    category
                                },
                                color = if (isLargest) color.copy(alpha = 1f) else color.copy(alpha = 0.9f),
                                fontSize = legendFontSize.sp,
                                fontWeight = if (isLargest) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }

    private fun DrawScope.draw3DPieChart(
        data: List<Pair<String, Long>>,
        totalTime: Long,
        largestCategory: String?,
        chart3DDepth: Int,
        chartTilt: Float,
        borderWidth: Float,
        colorIntensity: Float
    ) {
        val centerX = size.width * 0.5f
        val centerY = size.height * chartTilt
        val radiusX = size.width * 0.35f
        val radiusY = size.height * (0.25f * chartTilt)
        val depth = chart3DDepth.toFloat()
        
        var currentAngle = -90f

        data.forEach { (category, time) ->
            val sweepAngle = (time.toFloat() / totalTime * 360f)
            val baseColor = categoryColors[category] ?: Color.White
            val color = Color(
                red = (baseColor.red * colorIntensity).coerceAtMost(1f),
                green = (baseColor.green * colorIntensity).coerceAtMost(1f),
                blue = (baseColor.blue * colorIntensity).coerceAtMost(1f),
                alpha = baseColor.alpha
            )
            val darkerColor = Color(
                red = (color.red * 0.6f).coerceAtMost(1f),
                green = (color.green * 0.6f).coerceAtMost(1f),
                blue = (color.blue * 0.6f).coerceAtMost(1f),
                alpha = color.alpha
            )
            category == largestCategory

            if (sweepAngle > 2f) {
                val startAngleRad = Math.toRadians(currentAngle.toDouble())
                val endAngleRad = Math.toRadians((currentAngle + sweepAngle).toDouble())

                val startX1 = centerX + radiusX * cos(startAngleRad).toFloat()
                val startY1 = centerY + radiusY * sin(startAngleRad).toFloat()
                startX1
                startY1 + depth

                val endX1 = centerX + radiusX * cos(endAngleRad).toFloat()
                val endY1 = centerY + radiusY * sin(endAngleRad).toFloat()
                endX1
                endY1 + depth

                if (sin(startAngleRad) > 0 || sin(endAngleRad) > 0) {
                    val steps = maxOf(3, (sweepAngle / 10).toInt())
                    for (i in 0 until steps) {
                        val angle1 = startAngleRad + (endAngleRad - startAngleRad) * i / steps
                        val angle2 = startAngleRad + (endAngleRad - startAngleRad) * (i + 1) / steps
                        
                        if (sin(angle1) > 0 && sin(angle2) > 0) {
                            val x1 = centerX + radiusX * cos(angle1).toFloat()
                            val y1 = centerY + radiusY * sin(angle1).toFloat()
                            val x2 = centerX + radiusX * cos(angle2).toFloat()
                            val y2 = centerY + radiusY * sin(angle2).toFloat()

                            drawLine(
                                color = darkerColor,
                                start = Offset(x1, y1),
                                end = Offset(x1, y1 + depth),
                                strokeWidth = 2f
                            )
                            drawLine(
                                color = darkerColor,
                                start = Offset(x1, y1 + depth),
                                end = Offset(x2, y2 + depth),
                                strokeWidth = 2f
                            )
                        }
                    }
                }
            }
            
            currentAngle += sweepAngle
        }

        currentAngle = -90f
        data.forEach { (category, time) ->
            val sweepAngle = (time.toFloat() / totalTime * 360f)
            val baseColor = categoryColors[category] ?: Color.White
            val color = Color(
                red = (baseColor.red * colorIntensity).coerceAtMost(1f),
                green = (baseColor.green * colorIntensity).coerceAtMost(1f),
                blue = (baseColor.blue * colorIntensity).coerceAtMost(1f),
                alpha = baseColor.alpha
            )
            val isLargest = category == largestCategory

            val drawRadiusX = if (isLargest) radiusX * 1.05f else radiusX
            val drawRadiusY = if (isLargest) radiusY * 1.05f else radiusY

            drawArc(
                color = if (isLargest) color else color.copy(alpha = 0.9f),
                startAngle = currentAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(centerX - drawRadiusX, centerY - drawRadiusY),
                size = Size(drawRadiusX * 2, drawRadiusY * 2)
            )

            drawArc(
                color = Color.Black,
                startAngle = currentAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(centerX - drawRadiusX, centerY - drawRadiusY),
                size = Size(drawRadiusX * 2, drawRadiusY * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = borderWidth)
            )
            
            currentAngle += sweepAngle
        }

        currentAngle = -90f
        data.forEach { (category, time) ->
            val sweepAngle = (time.toFloat() / totalTime * 360f)
            val baseColor = categoryColors[category] ?: Color.White
            val color = Color(
                red = (baseColor.red * colorIntensity).coerceAtMost(1f),
                green = (baseColor.green * colorIntensity).coerceAtMost(1f),
                blue = (baseColor.blue * colorIntensity).coerceAtMost(1f),
                alpha = baseColor.alpha
            )
            val darkerColor = Color(
                red = (color.red * 0.7f).coerceAtMost(1f),
                green = (color.green * 0.7f).coerceAtMost(1f),
                blue = (color.blue * 0.7f).coerceAtMost(1f),
                alpha = color.alpha
            )
            val isLargest = category == largestCategory

            val drawRadiusX = if (isLargest) radiusX * 1.05f else radiusX
            val drawRadiusY = if (isLargest) radiusY * 1.05f else radiusY

            val midAngle = currentAngle + sweepAngle / 2
            if (sin(Math.toRadians(midAngle.toDouble())) > 0) {
                drawArc(
                    color = darkerColor,
                    startAngle = currentAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(centerX - drawRadiusX, centerY - drawRadiusY + depth),
                    size = Size(drawRadiusX * 2, drawRadiusY * 2)
                )

                drawArc(
                    color = Color.Black,
                    startAngle = currentAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(centerX - drawRadiusX, centerY - drawRadiusY + depth),
                    size = Size(drawRadiusX * 2, drawRadiusY * 2),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = borderWidth * 0.7f)
                )
            }
            
            currentAngle += sweepAngle
        }
    }
}