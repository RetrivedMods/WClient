package com.retrivedmods.wclient.overlay.gui.classic

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrivedmods.wclient.R
import com.retrivedmods.wclient.game.ModuleCategory
import com.retrivedmods.wclient.game.ModuleContent
import com.retrivedmods.wclient.game.ModuleManager
import com.retrivedmods.wclient.overlay.OverlayManager
import com.retrivedmods.wclient.overlay.OverlayWindow
import com.retrivedmods.wclient.ui.theme.WColors

private val DarkBackground = Color(0xFF08080D)
private val SidebarBackground = Color(0xFF0E0D14)
private val HeaderBackground = Color(0xFF0A0A10)
private val AccentPrimary = WColors.Primary
private val TextPrimary = Color(0xFFE8EAED)
private val TextSecondary = Color(0xFF9CA3B0)
private val ButtonBackground = Color(0xFF1A1822)
private val SearchBackground = Color(0xFF14121C)

class OverlayClickGUI : OverlayWindow() {

    private val _layoutParams by lazy {
        super.layoutParams.apply {
            flags = flags or WindowManager.LayoutParams.FLAG_DIM_BEHIND
            if (Build.VERSION.SDK_INT >= 31) blurBehindRadius = 20
            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            dimAmount = 0.75f
            windowAnimations = android.R.style.Animation_Dialog
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
        }
    }

    override val layoutParams: WindowManager.LayoutParams
        get() = _layoutParams

    private var selectedModuleCategory by mutableStateOf(ModuleCategory.Combat)
    private var searchQuery by mutableStateOf("")

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    override fun Content() {
        val context = LocalContext.current

        Box(
            Modifier
                .fillMaxSize()
                .background(Color(0xD0000000))
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) {
                    OverlayManager.dismissOverlayWindow(this)
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(640.dp)
                    .height(400.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF08080D),
                                Color(0xFF0E0D14)
                            )
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    ) {}
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    HeaderBar(
                        moduleCount = ModuleManager.modules.count { !it.private },
                        onDiscord = {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://discord.gg/N2Gejr8Fbp")
                                )
                            )
                        },
                        onWebsite = {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://wclient.neocities.org/")
                                )
                            )
                        },
                        onClose = { OverlayManager.dismissOverlayWindow(this@OverlayClickGUI) }
                    )
                    SearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onClear = { searchQuery = "" }
                    )
                    MainArea()
                }
            }
        }
    }

    @Composable
    private fun HeaderBar(
        moduleCount: Int,
        onDiscord: () -> Unit,
        onWebsite: () -> Unit,
        onClose: () -> Unit
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "header_glow")
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 0.8f,
            animationSpec = infiniteRepeatable<Float>(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "header_glow_alpha"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .background(HeaderBackground)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "WClient",
                    color = AccentPrimary.copy(alpha = glowAlpha),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
                Box(
                    modifier = Modifier
                        .background(AccentPrimary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        "v2.0",
                        color = AccentPrimary.copy(alpha = 0.7f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    "$moduleCount modules",
                    color = TextSecondary.copy(alpha = 0.5f),
                    fontSize = 10.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                IconButton(onClick = onDiscord) {
                    Icon(
                        painter = painterResource(R.drawable.ic_discord),
                        contentDescription = "Discord",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onWebsite) {
                    Icon(
                        painter = painterResource(R.drawable.ic_web),
                        contentDescription = "Website",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    @Composable
    private fun SearchBar(
        query: String,
        onQueryChange: (String) -> Unit,
        onClear: () -> Unit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                placeholder = {
                    Text(
                        "Search modules...",
                        color = TextSecondary.copy(alpha = 0.4f),
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = TextSecondary.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = onClear,
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Clear",
                                tint = TextSecondary.copy(alpha = 0.4f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = SearchBackground,
                    unfocusedContainerColor = SearchBackground,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = AccentPrimary,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedPlaceholderColor = TextSecondary.copy(alpha = 0.3f),
                    unfocusedPlaceholderColor = TextSecondary.copy(alpha = 0.3f)
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontSize = 13.sp,
                    color = TextPrimary
                )
            )
        }
    }

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    private fun MainArea() {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CategorySidebar()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground, RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                AnimatedContent(
                    targetState = selectedModuleCategory to searchQuery,
                    transitionSpec = {
                        fadeIn(tween(200)) + slideInHorizontally { it / 3 } togetherWith
                                fadeOut(tween(200)) + slideOutHorizontally { -it / 3 }
                    },
                    label = "CategoryContent"
                ) { (category, _) ->
                    if (category == ModuleCategory.Config) {
                        ConfigurationScreen(snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() })
                    } else {
                        if (searchQuery.isNotBlank()) {
                            FilteredModuleContent(category = category, query = searchQuery)
                        } else {
                            ModuleContent(category)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun FilteredModuleContent(
        category: ModuleCategory,
        query: String
    ) {
        val context = LocalContext.current
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Search results for \"$query\" in ${category.displayName}",
                color = TextSecondary.copy(alpha = 0.5f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }

    @Composable
    private fun CategorySidebar() {
        val categories = remember { ModuleCategory.entries }
        val infiniteTransition = rememberInfiniteTransition(label = "sidebar_glow")
        val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 0.7f,
            animationSpec = infiniteRepeatable<Float>(
                animation = tween(2500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "sidebar_glow_alpha"
        )

        LazyColumn(
            modifier = Modifier
                .width(72.dp)
                .fillMaxHeight()
                .background(SidebarBackground, RoundedCornerShape(12.dp))
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(categories.size) { index ->
                val category = categories[index]
                CategoryIcon(
                    category = category,
                    isSelected = selectedModuleCategory == category,
                    glowAlpha = if (selectedModuleCategory == category) glowAlpha else 0.3f,
                    onClick = {
                        selectedModuleCategory = category
                        searchQuery = ""
                    }
                )
            }
        }
    }

    @Composable
    private fun CategoryIcon(
        category: ModuleCategory,
        isSelected: Boolean,
        glowAlpha: Float,
        onClick: () -> Unit
    ) {
        val scale by animateFloatAsState(
            targetValue = if (isSelected) 1.08f else 1f,
            animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
            label = "catScale"
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.clickable { onClick() }
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .scale(scale)
                    .background(
                        if (isSelected) AccentPrimary.copy(alpha = 0.15f) else Color.Transparent,
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(category.iconResId),
                    contentDescription = category.displayName,
                    tint = if (isSelected) AccentPrimary.copy(alpha = glowAlpha) else TextSecondary.copy(alpha = 0.4f),
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = category.displayName,
                color = if (isSelected) AccentPrimary else TextSecondary.copy(alpha = 0.4f),
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
