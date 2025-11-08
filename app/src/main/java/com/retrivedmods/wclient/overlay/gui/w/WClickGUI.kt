package com.retrivedmods.wclient.overlay.gui.w

import android.os.Build
import com.retrivedmods.wclient.overlay.OverlayWindow
import android.view.WindowManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.ModuleManager
import com.retrivedmods.wclient.ui.theme.WColors
import com.retrivedmods.wclient.util.translatedSelf

class WClickGUI : OverlayWindow() {

    override val layoutParams by lazy {
        WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            format = android.graphics.PixelFormat.TRANSLUCENT

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alpha = 1.0f
                blurBehindRadius = 20
                setFitInsetsTypes(0)
                setFitInsetsSides(0)
            }
        }
    }

    @Composable
    override fun Content() {
        var selectedCategory by remember { mutableStateOf(ModuleCategory.Combat) }
        var isVisible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            isVisible = true
        }

        // Completely transparent background - no shade or visual elements
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    WOverlayManager.dismissOverlayWindow(this@WClickGUI)
                }
        ) {
            // Main GUI container with W entrance animation
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeIn(
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ),
                exit = slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(300)
                ) + fadeOut(
                    animationSpec = tween(300)
                ),
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Category sidebar with W theme
                    WCategorySidebar(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it },
                        modifier = Modifier.zIndex(2f)
                    )

                    // Module panel with W theme
                    WModulePanel(
                        category = selectedCategory,
                        modifier = Modifier.zIndex(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WCategorySidebar(
    selectedCategory: ModuleCategory,
    onCategorySelected: (ModuleCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    // Animated glow effect
    val infiniteTransition = rememberInfiniteTransition(label = "sidebar_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = modifier
            .width(180.dp)
            .fillMaxHeight()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        WColors.Surface.copy(alpha = 0.95f),
                        WColors.SurfaceVariant.copy(alpha = 0.9f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        WColors.Primary.copy(alpha = glowAlpha),
                        WColors.Secondary.copy(alpha = glowAlpha * 0.7f),
                        WColors.Accent.copy(alpha = glowAlpha * 0.5f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // W header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "W",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Light,
                    color = WColors.Primary.copy(alpha = glowAlpha + 0.6f)
                )

                IconButton(
                    onClick = {
                        WOverlayManager.hide()
                    }
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = WColors.OnSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            HorizontalDivider(
                color = WColors.Primary.copy(alpha = 0.3f),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Category buttons
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ModuleCategory.entries) { category ->
                    WCategoryButton(
                        category = category,
                        isSelected = category == selectedCategory,
                        onClick = { onCategorySelected(category) }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // W footer
            Text(
                text = "Enhanced Gaming",
                style = MaterialTheme.typography.bodySmall,
                color = WColors.OnSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun WCategoryButton(
    category: ModuleCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (isSelected) {
            WColors.Primary.copy(alpha = 0.2f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(300),
        label = "category_bg"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) {
            WColors.Primary.copy(alpha = 0.6f)
        } else {
            Color.Transparent
        },
        animationSpec = tween(300),
        label = "category_border"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .background(
                color = animatedBackgroundColor,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = animatedBorderColor,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = if (isSelected) WColors.Primary else WColors.OnSurfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    )
            )

            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) WColors.Primary else WColors.OnSurface
            )
        }
    }
}

@Composable
private fun WModulePanel(
    category: ModuleCategory,
    modifier: Modifier = Modifier
) {
    val modules = remember(category) {
        ModuleManager.modules.filter { it.category == category }
    }

    Box(
        modifier = modifier
            .width(320.dp)
            .fillMaxHeight()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        WColors.Surface.copy(alpha = 0.95f),
                        WColors.SurfaceVariant.copy(alpha = 0.9f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        WColors.Secondary.copy(alpha = 0.4f),
                        WColors.Accent.copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = WColors.OnSurface
                )

                Badge(
                    containerColor = WColors.Primary.copy(alpha = 0.2f),
                    contentColor = WColors.Primary
                ) {
                    Text(
                        text = "${modules.size}",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            HorizontalDivider(color = WColors.OnSurface.copy(alpha = 0.1f))

            // Module list
            if (modules.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No modules available",
                            style = MaterialTheme.typography.bodyLarge,
                            color = WColors.OnSurfaceVariant
                        )
                        Text(
                            text = "This category is empty",
                            style = MaterialTheme.typography.bodySmall,
                            color = WColors.OnSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(modules) { module ->
                        WModuleItem(
                            module = module,
                            onToggle = {
                                module.isEnabled = !module.isEnabled
                            },
                            onSettings = {
                                WOverlayManager.showModuleSettings(module)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WModuleItem(
    module: com.retrivedmods.wclient.game.Module,
    onToggle: () -> Unit,
    onSettings: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "module_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onToggle()
            }
            .animateContentSize()
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .background(
                brush = if (module.isEnabled) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            WColors.Primary.copy(alpha = 0.1f),
                            WColors.Secondary.copy(alpha = 0.05f)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(
                            WColors.SurfaceVariant.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    )
                },
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = if (module.isEnabled) {
                    WColors.Primary.copy(alpha = 0.4f)
                } else {
                    WColors.OnSurface.copy(alpha = 0.1f)
                },
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = module.name.translatedSelf,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (module.isEnabled) WColors.Primary else WColors.OnSurface
                )
                Text(
                    text = "Module configuration available",
                    style = MaterialTheme.typography.bodySmall,
                    color = WColors.OnSurfaceVariant
                )
            }

            // Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Settings icon
                IconButton(
                    onClick = onSettings,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Module Settings",
                        tint = if (module.values.isNotEmpty())
                            WColors.Secondary.copy(alpha = 0.8f)
                        else
                            WColors.OnSurface.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Toggle switch with W glow
                Box {
                    Switch(
                        checked = module.isEnabled,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = WColors.Primary,
                            checkedTrackColor = WColors.Primary.copy(alpha = 0.5f),
                            uncheckedThumbColor = WColors.OnSurfaceVariant,
                            uncheckedTrackColor = WColors.OnSurfaceVariant.copy(alpha = 0.3f)
                        )
                    )

                    // W glow effect when enabled
                    if (module.isEnabled) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            WColors.Primary.copy(alpha = 0.3f),
                                            Color.Transparent
                                        ),
                                        radius = 40f
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                        )
                    }
                }
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
