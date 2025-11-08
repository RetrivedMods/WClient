package com.retrivedmods.wclient.overlay.hud

import android.graphics.Bitmap
import com.retrivedmods.wclient.overlay.OverlayWindow
import com.retrivedmods.wclient.overlay.OverlayManager
import android.graphics.BitmapFactory
import android.view.Gravity
import android.view.WindowManager
import org.cloudburstmc.protocol.bedrock.data.skin.SerializedSkin
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class TargetHudOverlay : OverlayWindow() {
    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = flags or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.CENTER
            x = 0
            y = -200
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    companion object {
        private val overlayInstance by lazy { TargetHudOverlay() }
        private var isVisible = false

        private var targetUsername by mutableStateOf("")
        private var targetImage by mutableStateOf<Bitmap?>(null)
        private var targetDistance by mutableStateOf(0f)
        private var targetMaxDistance by mutableStateOf(50f)
        private var targetHurtTime by mutableStateOf(0f)
        private var overlayScale by mutableStateOf(1.0f)
        private var displayDistance by mutableStateOf(true)
        private var displayStatus by mutableStateOf(true)
        private var bgOpacity by mutableStateOf(0.6f)

        fun showTargetHud(
            username: String,
            skin: SerializedSkin?,
            distance: Float,
            maxDistance: Float = 50f,
            hurtTime: Float = 0f
        ) {
            targetUsername = username
            targetImage = skin?.let { extractFaceFromSkin(it) }
            targetDistance = distance.coerceIn(0f, maxDistance)
            targetMaxDistance = maxDistance
            targetHurtTime = hurtTime

            if (!isVisible) {
                isVisible = true
                try {
                    OverlayManager.showOverlayWindow(overlayInstance)
                } catch (_: Exception) {
                }
            }
        }

        private fun extractFaceFromSkin(skin: SerializedSkin): Bitmap? {
            return try {
                val skinData = skin.skinData
                val width = skinData.width
                val height = skinData.height
                val imageBytes = skinData.image

                if (imageBytes.isEmpty()) {
                    return null
                }

                val decodedBitmap = try {
                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                } catch (e: Exception) {
                    null
                }

                val fullBitmap = if (decodedBitmap != null && decodedBitmap.width > 0 && decodedBitmap.height > 0) {
                    decodedBitmap
                } else if (width > 0 && height > 0) {
                    try {
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        val buffer = java.nio.ByteBuffer.wrap(imageBytes)
                        bitmap.copyPixelsFromBuffer(buffer)
                        bitmap
                    } catch (e: Exception) {
                        return null
                    }
                } else {
                    return null
                }

                val scale = fullBitmap.width / 64f
                val faceSize = (8 * scale).toInt().coerceAtLeast(8)
                val faceX = (8 * scale).toInt()
                val faceY = (8 * scale).toInt()

                if (faceX + faceSize > fullBitmap.width || faceY + faceSize > fullBitmap.height) {
                    val altSize = (fullBitmap.width / 8).coerceAtLeast(8).coerceAtMost(fullBitmap.width)
                    val altX = (fullBitmap.width / 8).coerceAtMost(fullBitmap.width - altSize)
                    val altY = (fullBitmap.height / 8).coerceAtMost(fullBitmap.height - altSize)

                    if (altX + altSize <= fullBitmap.width && altY + altSize <= fullBitmap.height && altSize > 0) {
                        val faceBitmap = Bitmap.createBitmap(fullBitmap, altX, altY, altSize, altSize)
                        val scaledFace = Bitmap.createScaledBitmap(faceBitmap, 48, 48, false)
                        fullBitmap.recycle()
                        faceBitmap.recycle()
                        return scaledFace
                    }

                    fullBitmap.recycle()
                    return null
                }

                val faceBitmap = Bitmap.createBitmap(
                    fullBitmap,
                    faceX,
                    faceY,
                    faceSize,
                    faceSize
                )

                val scaledFace = Bitmap.createScaledBitmap(
                    faceBitmap,
                    48,
                    48,
                    false
                )

                fullBitmap.recycle()
                faceBitmap.recycle()
                scaledFace
            } catch (e: Exception) {
                null
            }
        }

        fun dismissTargetHud() {
            isVisible = false
            try {
                OverlayManager.dismissOverlayWindow(overlayInstance)
            } catch (_: Exception) {
            }
        }

        fun isTargetHudVisible(): Boolean = isVisible

        fun setPosition(x: Int, y: Int) {
            overlayInstance._layoutParams.x = x
            overlayInstance._layoutParams.y = y
        }

        fun setScale(scale: Float) {
            overlayScale = scale
        }

        fun setShowDistance(show: Boolean) {
            displayDistance = show
        }

        fun setShowStatus(show: Boolean) {
            displayStatus = show
        }

        fun setBackgroundOpacity(opacity: Float) {
            bgOpacity = opacity
        }
    }

    @Composable
    override fun Content() {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(
                animationSpec = tween(durationMillis = 200)
            ),
            exit = fadeOut(
                animationSpec = tween(durationMillis = 200)
            )
        ) {
            TargetHudContent(
                username = targetUsername,
                image = targetImage,
                distance = targetDistance,
                maxDistance = targetMaxDistance,
                hurtTime = targetHurtTime,
                scale = overlayScale,
                showDistance = displayDistance,
                showStatus = displayStatus,
                backgroundOpacity = bgOpacity
            )
        }
    }

    @Composable
    private fun TargetHudContent(
        username: String,
        image: Bitmap?,
        distance: Float,
        maxDistance: Float,
        hurtTime: Float,
        scale: Float,
        showDistance: Boolean,
        showStatus: Boolean,
        backgroundOpacity: Float
    ) {
        val animatedDistance by animateFloatAsState(
            targetValue = distance,
            animationSpec = tween(durationMillis = 600, easing = EaseOutCubic),
            label = "distance_animation"
        )

        val hurtScale by animateFloatAsState(
            targetValue = if (hurtTime > 0) 0.85f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh
            ),
            label = "hurt_scale"
        )

        val hurtAlpha by animateFloatAsState(
            targetValue = if (hurtTime > 0) 0.7f else 1f,
            animationSpec = tween(durationMillis = 200),
            label = "hurt_alpha"
        )

        val baseHue = remember(username) {
            val charCount = username.length
            val charSum = username.sumOf { it.code }
            ((charCount * 137.5f + charSum * 31.7f) % 360f).coerceIn(0f, 360f)
        }
        val themeColors = remember(username) {
            Pair(
                Color.hsv(baseHue, 0.8f, 1.0f, 0.5f),
                Color.hsv((baseHue + 25f) % 360f, 0.7f, 1.0f, 0.4f)
            )
        }
        val statusColor = remember(username) {
            Color.hsv((baseHue + 50f) % 360f, 0.75f, 1.0f, 0.9f)
        }
        val backgroundColor = Color(0xFF151515).copy(backgroundOpacity)

        Box(
            modifier = Modifier
                .scale(scale)
                .width(240.dp)
                .height(90.dp)
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(15.dp)
                )
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .scale(hurtScale)
                        .alpha(hurtAlpha)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(
                            2.dp,
                            Color.White.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (image != null) {
                        Image(
                            bitmap = image.asImageBitmap(),
                            contentDescription = "Player Avatar",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = username.take(2).uppercase(),
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = username,
                        color = Color.White.copy(alpha = 1.0f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.4f))
                        ) {
                            val distancePercentage = (1f - (animatedDistance / maxDistance)).coerceIn(0f, 1f)

                            if (distancePercentage > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(distancePercentage)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    themeColors.first,
                                                    themeColors.second
                                                )
                                            )
                                        )
                                )
                            }
                        }

                        if (showDistance || showStatus) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (showDistance) {
                                    Text(
                                        text = "${String.format("%.1f", animatedDistance)}m",
                                        color = themeColors.first.copy(alpha = 1.0f),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (showStatus) {
                                    Text(
                                        text = when {
                                            animatedDistance <= 5f -> "DANGER"
                                            animatedDistance <= 15f -> "CLOSE"
                                            animatedDistance <= 30f -> "MEDIUM"
                                            else -> "FAR"
                                        },
                                        color = statusColor,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
