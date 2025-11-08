package com.retrivedmods.wclient.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun LoadingScreen(onDone: () -> Unit) {
    var progress by remember { mutableStateOf(0f) }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 50, easing = FastOutSlowInEasing),
        label = "ProgressAnimation"
    )

    LaunchedEffect(Unit) {
        val loadingSteps = listOf(
            0f to 15f, 15f to 35f, 35f to 65f, 65f to 85f, 85f to 100f
        )
        for ((start, end) in loadingSteps) {
            val steps = (end - start).toInt()
            val delayTime = when {
                start < 15f -> 45L
                start < 65f -> 25L
                else -> 60L
            }
            repeat(steps) {
                delay(delayTime)
                progress = start + it + 1f
            }
        }
        delay(800)
        onDone()
    }

    val backgroundColor = Color(0xFF0A0A0A)
    val primaryColor = Color(0xFFD32F2F)
    val secondaryColor = Color(0xFFFF6B6B)

    val infiniteTransition = rememberInfiniteTransition(label = "InfiniteAnimations")

    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "GlowIntensity"
    )

    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreathingScale"
    )

    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -200f,
        targetValue = 200f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerOffset"
    )

    val particleOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ParticleOffset"
    )

    val textGlow by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "TextGlow"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(backgroundColor, Color(0xFF1A1A1A)),
                    radius = 800f
                )
            )
            .drawBehind {
                drawFloatingParticles(
                    color = primaryColor.copy(alpha = 0.1f),
                    offset = particleOffset,
                    size = size
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "WClient",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = primaryColor.copy(alpha = textGlow),
                modifier = Modifier
                    .padding(bottom = 50.dp)
                    .scale(breathingScale)
                    .drawBehind {
                        drawCircle(
                            color = primaryColor.copy(alpha = glowIntensity * 0.3f),
                            radius = 80f,
                            center = center
                        )
                        drawCircle(
                            color = primaryColor.copy(alpha = glowIntensity * 0.15f),
                            radius = 120f,
                            center = center
                        )
                    },
                textAlign = TextAlign.Center
            )

            Box(
                modifier = Modifier
                    .width(280.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.08f))
                    .drawBehind {
                        drawRoundRect(
                            color = primaryColor.copy(alpha = glowIntensity * 0.2f),
                            topLeft = Offset(-2f, -2f),
                            size = Size(size.width + 4f, size.height + 4f),
                            cornerRadius = CornerRadius(50f),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress / 100f)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.9f),
                                    secondaryColor.copy(alpha = glowIntensity),
                                    primaryColor.copy(alpha = 0.9f)
                                ),
                                startX = shimmerOffset - 100f,
                                endX = shimmerOffset + 100f
                            )
                        )
                        .drawBehind {
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.3f),
                                size = Size(width = size.width, height = size.height * 0.4f),
                                cornerRadius = CornerRadius(50f)
                            )
                        }
                )
            }

            val displayProgress by animateIntAsState(
                targetValue = animatedProgress.toInt(),
                animationSpec = tween(100, easing = FastOutSlowInEasing),
                label = "ProgressCounter"
            )

            Text(
                text = "$displayProgress%",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = primaryColor.copy(alpha = 0.8f),
                modifier = Modifier
                    .padding(top = 20.dp)
                    .alpha(0.9f),
                textAlign = TextAlign.Center
            )

            val loadingText = when {
                displayProgress < 15 -> "Initializing..."
                displayProgress < 35 -> "Loading assets..."
                displayProgress < 65 -> "Processing..."
                displayProgress < 85 -> "Finalizing..."
                displayProgress < 100 -> "Almost ready..."
                else -> "Complete!"
            }

            Text(
                text = loadingText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier
                    .padding(top = 8.dp)
                    .alpha(0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun DrawScope.drawFloatingParticles(
    color: Color,
    offset: Float,
    size: Size
) {
    val particleCount = 6
    val centerX = size.width / 2f
    val centerY = size.height / 2f

    repeat(particleCount) { i ->
        val angle = ((i * 60f) + (offset * 2f)) * (PI / 180f)
        val radius = 100f + sin(offset * 0.1f + i) * 20f
        val x = centerX + cos(angle) * radius
        val y = centerY + sin(angle) * radius

        drawCircle(
            color = color,
            radius = 3f + sin(offset * 0.15f + i) * 1f,
            center = Offset(x.toFloat(), y.toFloat())
        )
    }
}