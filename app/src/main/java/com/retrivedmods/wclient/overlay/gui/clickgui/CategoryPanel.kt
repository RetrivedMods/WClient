package com.retrivedmods.wclient.overlay.gui.clickgui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.retrivedmods.wclient.game.Module
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.ui.theme.ClickGUIColors
import kotlin.math.roundToInt
import android.content.Context

@Composable
fun CategoryPanel(
    category: ModuleCategory,
    modules: List<Module>,
    position: IntOffset,
    onPositionChange: (IntOffset) -> Unit,
    onModuleToggle: (Module) -> Unit,
    onModuleSettings: ((Module) -> Unit)? = null,
    isTopMost: Boolean = false,
    onBringToFront: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sharedPreferences = remember {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    val panelWidth = remember {
        sharedPreferences.getInt("clickgui_panel_width", 140)
    }.dp
    val panelHeight = remember {
        sharedPreferences.getInt("clickgui_panel_height", 240)
    }.dp

    LocalDensity.current
    var isDragging by remember(category) { mutableStateOf(false) }
    var dragOffset by remember(category) { mutableStateOf(IntOffset.Zero) }

    var expandedModules by remember(category) { mutableStateOf(setOf<String>()) }

    var currentPosition by remember(category) { mutableStateOf(position) }

    LaunchedEffect(position) {
        if (!isDragging) {
            currentPosition = position
        }
    }
    
    val elevation by animateFloatAsState(
        targetValue = if (isDragging || isTopMost) 12f else 6f,
        label = "panel_elevation"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.02f else 1f,
        label = "panel_scale"
    )

    Box(
        modifier = modifier
            .offset {
                IntOffset(
                    x = currentPosition.x + dragOffset.x,
                    y = currentPosition.y + dragOffset.y
                )
            }
            .zIndex(if (isTopMost) 1000f else 0f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .shadow(
                elevation = elevation.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false
            )
            .width(panelWidth)
            .clip(RoundedCornerShape(12.dp))
            .background(ClickGUIColors.PanelBackground)
            .border(
                width = 1.5.dp,
                color = ClickGUIColors.PanelBorder,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Column {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        ClickGUIColors.AccentColor.copy(alpha = 0.1f),
                        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    )
                    .pointerInput(category) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                isDragging = true
                                onBringToFront()
                                dragOffset = IntOffset.Zero
                            },
                            onDragEnd = {
                                isDragging = false
                                val finalPosition = IntOffset(
                                    x = (currentPosition.x + dragOffset.x).coerceAtLeast(0),
                                    y = (currentPosition.y + dragOffset.y).coerceAtLeast(0)
                                )
                                println("${category.name} drag ended: current=$currentPosition, offset=$dragOffset, final=$finalPosition")
                                currentPosition = finalPosition
                                onPositionChange(finalPosition)
                                dragOffset = IntOffset.Zero
                            }
                        ) { change, dragAmount ->
                            dragOffset = IntOffset(
                                x = dragOffset.x + dragAmount.x.roundToInt(),
                                y = dragOffset.y + dragAmount.y.roundToInt()
                            )
                        }
                    }
                    .padding(10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = category.iconResId),
                        contentDescription = null,
                        tint = ClickGUIColors.AccentColor,
                        modifier = Modifier.size(18.dp)
                    )

                    Text(
                        text = category.displayName,
                        color = ClickGUIColors.PrimaryText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = panelHeight)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                items(modules) { module ->
                    val isExpanded = expandedModules.contains(module.name)
                    ModuleToggle(
                        module = module,
                        onToggle = { onModuleToggle(module) },
                        onSettingsClick = if (module.values.isNotEmpty()) {
                            {
                                expandedModules = if (isExpanded) {
                                    expandedModules - module.name
                                } else {
                                    expandedModules + module.name
                                }
                            }
                        } else null,
                        isExpanded = isExpanded
                    )
                }
                
                if (modules.isEmpty()) {
                    item {
                        Text(
                            text = "No modules available",
                            color = ClickGUIColors.SecondaryText,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}