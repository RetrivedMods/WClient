package com.retrivedmods.wclient.overlay.gui.classic

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrivedmods.wclient.R
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.ModuleContent
import com.retrivedmods.wclient.overlay.OverlayManager
import com.retrivedmods.wclient.overlay.OverlayWindow

class OverlayClickGUI : OverlayWindow() {

    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            if (Build.VERSION.SDK_INT >= 31) blurBehindRadius = 30
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            dimAmount = 0.8f
            windowAnimations = android.R.style.Animation_Dialog
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    private var selectedModuleCategory by mutableStateOf(ModuleCategory.Combat)

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    override fun Content() {
        val context = LocalContext.current

        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0x95000000), Color(0xE0000000)),
                        radius = 1000f
                    )
                )
                .clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) {
                    OverlayManager.dismissOverlayWindow(this)
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(720.dp)
                    .height(480.dp)
                    .glowBorder()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0A0A0A), Color(0xFF151515), Color(0xFF0A0A0A))
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) {}
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HeaderBar(
                        onDiscord = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/N2Gejr8Fbp")))
                        },
                        onWebsite = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://wclient.neocities.org/")))
                        },
                        onClose = { OverlayManager.dismissOverlayWindow(this@OverlayClickGUI) }
                    )
                    MainArea()
                }
            }
        }
    }

    @Composable
    private fun HeaderBar(
        onDiscord: () -> Unit,
        onWebsite: () -> Unit,
        onClose: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0x35FF0080), Color(0x3500FF80), Color(0x358000FF), Color(0x35FF0080))
                    ),
                    RoundedCornerShape(15.dp)
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(15.dp))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            Brush.radialGradient(listOf(Color(0x40FFFFFF), Color(0x20FFFFFF))),
                            CircleShape
                        )
                        .border(1.dp, Color.White.copy(0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(painter = painterResource(R.drawable.ic_wclient), contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
                CyclingText("WClient", fontSize = 20f, fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                HeaderIconButton(R.drawable.ic_discord, onDiscord)
                HeaderIconButton(R.drawable.ic_web, onWebsite)
                HeaderIconButton(R.drawable.ic_close, onClose)
            }
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    private fun MainArea() {
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CategorySidebar()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(listOf(Color(0x12FFFFFF), Color(0x08FFFFFF), Color(0x12FFFFFF))),
                        RoundedCornerShape(15.dp)
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(15.dp))
                    .padding(16.dp)
            ) {
                AnimatedContent(
                    targetState = selectedModuleCategory,
                    transitionSpec = {
                        fadeIn(tween(300)) + slideInHorizontally { it / 4 } togetherWith
                                fadeOut(tween(300)) + slideOutHorizontally { -it / 4 }
                    },
                    label = "CategoryContent"
                ) { category ->
                    ModuleContent(category)
                }
            }
        }
    }

    @Composable
    private fun CategorySidebar() {
        val categories = remember { ModuleCategory.entries.filter { it.name != "Config" } }

        LazyColumn(
            modifier = Modifier
                .width(70.dp)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(listOf(Color(0x25FFFFFF), Color(0x15FFFFFF), Color(0x25FFFFFF))),
                    RoundedCornerShape(15.dp)
                )
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(15.dp))
                .padding(vertical = 12.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(categories.size) { index ->
                val category = categories[index]
                CategoryIcon(
                    category = category,
                    isSelected = selectedModuleCategory == category,
                    onClick = { selectedModuleCategory = category }
                )
            }
        }
    }

    @Composable
    private fun CategoryIcon(
        category: ModuleCategory,
        isSelected: Boolean,
        onClick: () -> Unit
    ) {
        val scale by animateFloatAsState(
            targetValue = if (isSelected) 1.1f else 1f,
            animationSpec = spring(dampingRatio = 0.6f),
            label = "catScale"
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.clickable { onClick() }
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .scale(scale)
                    .background(
                        if (isSelected) {
                            Brush.radialGradient(listOf(Color(0xFF00FF88), Color(0xFF0088FF), Color(0xFF8800FF)))
                        } else {
                            Brush.radialGradient(listOf(Color(0x35FFFFFF), Color(0x15FFFFFF)))
                        },
                        CircleShape
                    )
                    .border(if (isSelected) 2.dp else 1.dp, if (isSelected) Color.White.copy(0.4f) else Color.White.copy(0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(painter = painterResource(category.iconResId), contentDescription = category.name, tint = if (isSelected) Color.White else Color.White.copy(0.8f), modifier = Modifier.size(22.dp))
            }
            Text(
                text = category.name,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f),
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    @Composable
    private fun HeaderIconButton(iconRes: Int, onClick: () -> Unit) {
        val t = rememberInfiniteTransition(label = "btnShimmerTransition")
        val shimmer by t.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing)),
            label = "btnShimmer"
        )
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Brush.radialGradient(listOf(Color(0x30FFFFFF), Color(0x15FFFFFF))), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.2f + shimmer * 0.1f), CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = painterResource(iconRes), contentDescription = null, tint = Color.White.copy(alpha = 0.9f), modifier = Modifier.size(18.dp))
        }
    }

    @Composable
    private fun CyclingText(text: String, fontSize: Float, fontWeight: FontWeight = FontWeight.Normal) {
        val t = rememberInfiniteTransition(label = "rainbowTransition")
        val phase by t.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(animation = tween(3000, easing = LinearEasing)),
            label = "rainbowPhase"
        )
        val colors = List(10) { i -> Color.hsv((i * 36 + phase) % 360, 0.85f, 1f) }
        Text(text = text, style = TextStyle(fontSize = fontSize.sp, fontWeight = fontWeight, brush = Brush.linearGradient(colors)))
    }

    @Composable
    private fun Modifier.glowBorder(): Modifier {
        val t = rememberInfiniteTransition(label = "borderTransition")
        val phase by t.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(animation = tween(2500, easing = LinearEasing)),
            label = "borderPhase"
        )
        return this.drawBehind {
            val strokeWidth = 4.dp.toPx()
            val radius = 20.dp.toPx()
            val colors = listOf(
                Color.hsv((phase) % 360f, 0.9f, 1f),
                Color.hsv((phase + 45) % 360f, 0.85f, 1f),
                Color.hsv((phase + 90) % 360f, 0.9f, 1f),
                Color.hsv((phase + 135) % 360f, 0.85f, 1f),
                Color.hsv((phase + 180) % 360f, 0.9f, 1f),
                Color.hsv((phase + 225) % 360f, 0.85f, 1f),
                Color.hsv((phase + 270) % 360f, 0.9f, 1f),
                Color.hsv((phase + 315) % 360f, 0.85f, 1f),
                Color.hsv((phase) % 360f, 0.9f, 1f)
            )
            val brush = Brush.sweepGradient(colors)
            drawRoundRect(brush = brush, style = Stroke(width = strokeWidth), cornerRadius = CornerRadius(radius))
        }
    }
}
