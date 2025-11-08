package com.retrivedmods.wclient.router.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.retrivedmods.wclient.ui.theme.WColors
import com.retrivedmods.wclient.viewmodel.MainScreenViewModel

@Immutable
enum class MainScreenPages(
    val icon: ImageVector,
    val content: @Composable () -> Unit
) {
    HomePage(Icons.Rounded.Home, { HomePageContent() }),
    AccountPage(Icons.Rounded.AccountCircle, { AccountPageContent() }),
    ServerPage(Icons.Rounded.Storage, { ServerPageContent() }),
    RealmsPage(Icons.Rounded.Cloud, { RealmsPageContent() }),
    SettingsPage(Icons.Rounded.Settings, { SettingsPageContent() }),
    AboutPage(Icons.Rounded.Info, { AboutPageContent() })
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen() {
    val vm: MainScreenViewModel = viewModel()
    val selectedPage by vm.selectedPage.collectAsStateWithLifecycle()
    val pages = remember { MainScreenPages.entries.toList() }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(WColors.Background)
    ) {
        IconSidebar(
            pages = pages,
            selected = selectedPage,
            onSelect = { if (selectedPage != it) vm.selectPage(it) },
            containerColor = WColors.Surface,
            activeColor = WColors.OnSurface,
            indicatorColor = MaterialTheme.colorScheme.primary
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            AnimatedContent(
                targetState = selectedPage,
                transitionSpec = {
                    slideInHorizontally(
                        initialOffsetX = { it / 3 },
                        animationSpec = tween(320, easing = FastOutSlowInEasing)
                    ) + fadeIn(tween(240)) togetherWith
                            slideOutHorizontally(
                                targetOffsetX = { -it / 4 },
                                animationSpec = tween(280, easing = FastOutSlowInEasing)
                            ) + fadeOut(tween(200))
                },
                label = "page_transition"
            ) { page ->
                page.content()
            }
        }
    }
}

@Composable
private fun IconSidebar(
    pages: List<MainScreenPages>,
    selected: MainScreenPages,
    onSelect: (MainScreenPages) -> Unit,
    containerColor: Color,
    activeColor: Color,
    indicatorColor: Color
) {
    Surface(
        color = containerColor,
        contentColor = activeColor,
        modifier = Modifier
            .fillMaxHeight()
            .widthIn(min = 72.dp)
    ) {
        Column(
            Modifier
                .fillMaxHeight()
                .padding(vertical = 10.dp)
                .selectableGroup()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
        ) {
            pages.forEach { page ->
                IconTab(
                    icon = page.icon,
                    selected = page == selected,
                    onClick = { onSelect(page) },
                    activeColor = activeColor,
                    indicatorColor = indicatorColor
                )
            }
        }
    }
}

@Composable
private fun IconTab(
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    activeColor: Color,
    indicatorColor: Color
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = 0.85f),
        label = ""
    )
    val bgAlpha by animateFloatAsState(
        targetValue = if (selected) 0.18f else 0f,
        animationSpec = tween(220),
        label = ""
    )
    val tintAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0.7f,
        animationSpec = tween(160),
        label = ""
    )

    Box(
        modifier = Modifier
            .size(60.dp)
            .background(indicatorColor.copy(alpha = bgAlpha), CircleShape)
            .scale(scale)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = activeColor.copy(alpha = tintAlpha),
            modifier = Modifier.size(24.dp)
        )
    }
}
