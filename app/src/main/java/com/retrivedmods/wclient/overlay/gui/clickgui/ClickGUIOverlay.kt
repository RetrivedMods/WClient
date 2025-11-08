package com.retrivedmods.wclient.overlay.gui.clickgui

import android.content.Context
import com.retrivedmods.wclient.overlay.OverlayWindow
import com.retrivedmods.wclient.overlay.OverlayManager
import android.os.Build
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.ModuleManager
import com.retrivedmods.wclient.overlay.gui.clickgui.CategoryPanel
import com.retrivedmods.wclient.ui.theme.ClickGUIColors
import com.retrivedmods.wclient.ui.theme.WClientTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ClickGUIOverlay : OverlayWindow() {

    override val layoutParams by lazy {
        WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
            format = android.graphics.PixelFormat.TRANSLUCENT

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                alpha = 1.0f
                blurBehindRadius = 20
                fitInsetsTypes = 0
                fitInsetsSides = 0
            }
        }
    }

    @Composable
    override fun Content() {
        WClientTheme {
            ClickGUIContent()
        }
    }
}

@Composable
private fun ClickGUIContent() {
    val context = LocalContext.current

    val sharedPreferences = remember {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    }

    val visibleCategories = remember {
        ModuleCategory.entries.filter { category ->
            sharedPreferences.getBoolean("clickgui_category_${category.name}", true)
        }
    }

    val modulesByCategory = remember(visibleCategories) {
        ModuleManager.modules.groupBy { it.category }.filterKeys { it in visibleCategories }
    }

    val panelPositions = remember(visibleCategories) {
        visibleCategories.mapIndexed { index, category ->
            val column = index % 2
            val row = index / 2
            category to mutableStateOf(
                IntOffset(
                    x = 80 + (column * 220),
                    y = 120 + (row * 280)
                )
            )
        }.toMap()
    }

    var topMostPanel by remember { mutableStateOf<ModuleCategory?>(null) }

    var positionsLoaded by remember { mutableStateOf(false) }

    var saveJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(Unit) {
        if (!positionsLoaded) {
            loadPanelPositions(context, panelPositions)
            positionsLoaded = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClickGUIColors.PrimaryBackground.copy(alpha = 0.8f))
    ) {
        IconButton(
            onClick = {
                OverlayManager.dismissClickGUI()
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = ClickGUIColors.PrimaryText,
                modifier = Modifier.size(24.dp)
            )
        }

        visibleCategories.forEach { category ->
            val modules = modulesByCategory[category] ?: emptyList()
            val positionState = panelPositions[category]!!
            val position by positionState



            CategoryPanel(
                category = category,
                modules = modules,
                position = position,
                onPositionChange = { newPosition ->
                    positionState.value = newPosition
                    saveJob?.cancel()
                    saveJob = CoroutineScope(Dispatchers.IO).launch {
                        delay(300)
                        savePanelPositions(context, panelPositions)
                    }
                },
                onModuleToggle = { module ->
                    module.isEnabled = !module.isEnabled
                },
                isTopMost = topMostPanel == category,
                onBringToFront = {
                    topMostPanel = category
                }
            )
        }
    }
}

private fun loadPanelPositions(
    context: Context,
    positionStates: Map<ModuleCategory, MutableState<IntOffset>>
) {
    try {
        val sharedPreferences = context.getSharedPreferences("clickgui_positions", Context.MODE_PRIVATE)

        positionStates.keys.forEach { category ->
            val positionState = positionStates[category]!!
            val defaultPosition = positionState.value

            if (sharedPreferences.contains("${category.name}_x") &&
                sharedPreferences.contains("${category.name}_y")) {
                val x = sharedPreferences.getInt("${category.name}_x", defaultPosition.x)
                val y = sharedPreferences.getInt("${category.name}_y", defaultPosition.y)
                positionState.value = IntOffset(x, y)
                println("Loaded position for ${category.name}: ($x, $y)")
            } else {
                println("No saved position for ${category.name}, using default: $defaultPosition")
            }
        }
    } catch (e: Exception) {
        println("Failed to load panel positions: ${e.message}")
    }
}

private fun savePanelPositions(
    context: Context,
    positionStates: Map<ModuleCategory, MutableState<IntOffset>>
) {
    try {
        val sharedPreferences = context.getSharedPreferences("clickgui_positions", Context.MODE_PRIVATE)
        sharedPreferences.edit {

            positionStates.forEach { (category, positionState) ->
                val position = positionState.value
                putInt("${category.name}_x", position.x)
                putInt("${category.name}_y", position.y)
            }

        }
        val positions = positionStates.mapValues { it.value.value }
        println("Saved panel positions: $positions")
    } catch (e: Exception) {
        println("Failed to save panel positions: ${e.message}")
    }
}