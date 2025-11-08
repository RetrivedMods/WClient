package com.retrivedmods.wclient.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.retrivedmods.wclient.ui.theme.WColors

@Composable
fun WSidebar(
    selectedPage: Any,
    pages: List<WNavItem>,
    onPageSelected: (Any) -> Unit,
    modifier: Modifier = Modifier
) {
    WGlassCard(
        modifier = modifier
            .width(200.dp)
            .fillMaxHeight(0.85f)
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        glowColor = WColors.Primary,
        glowIntensity = 0.2f
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            // Compact Header
            CompactWHeader()

            // Navigation items - compact layout (no scrolling)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                pages.forEach { page ->
                    CompactWNavItemComponent(
                        item = page,
                        isSelected = selectedPage == page.page,
                        onClick = { onPageSelected(page.page) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactWHeader() {
    val infiniteTransition = rememberInfiniteTransition(label = "header_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "W",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Light,
            color = WColors.Primary.copy(alpha = glowAlpha)
        )

        Box(
            modifier = Modifier
                .height(1.dp)
                .width(25.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            WColors.Primary.copy(alpha = glowAlpha),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(1.dp)
                )
        )
    }
}

@Composable
private fun CompactWNavItemComponent(
    item: WNavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.7f,
        animationSpec = tween(300),
        label = "nav_alpha"
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 0.5.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                WColors.Primary.copy(alpha = 0.15f)
            } else {
                Color.Transparent
            }
        ),
        shape = RoundedCornerShape(6.dp),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(
                    colors = listOf(
                        WColors.Primary.copy(alpha = 0.5f),
                        WColors.Secondary.copy(alpha = 0.3f)
                    )
                )
            )
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (isSelected) WColors.Primary else WColors.OnSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )

            Text(
                text = item.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = if (isSelected) WColors.Primary else WColors.OnSurface.copy(alpha = animatedAlpha),
                maxLines = 1
            )
        }
    }
}


data class WNavItem(
    val page: Any,
    val label: String,
    val icon: ImageVector
)
