package com.retrivedmods.wclient.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.retrivedmods.wclient.ui.theme.WColors

/**
 * Enhanced UI components with W aesthetics and liquid glass effects
 */

@Composable
fun WGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    glowColor: Color = WColors.Primary,
    glowIntensity: Float = 0.3f,
    elevation: Dp = 8.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow_animation")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = glowIntensity * 0.5f,
        targetValue = glowIntensity,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    val cardModifier = modifier
        .shadow(
            elevation = elevation,
            shape = shape,
            ambientColor = glowColor.copy(alpha = glowAlpha),
            spotColor = glowColor.copy(alpha = glowAlpha)
        )
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    WColors.Surface.copy(alpha = 0.9f),
                    WColors.SurfaceVariant.copy(alpha = 0.7f)
                )
            ),
            shape = shape
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    glowColor.copy(alpha = glowAlpha),
                    Color.Transparent,
                    glowColor.copy(alpha = glowAlpha * 0.5f)
                )
            ),
            shape = shape
        )
        .clip(shape)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = cardModifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            content = content
        )
    } else {
        Card(
            modifier = cardModifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            content = content
        )
    }
}

@Composable
fun WButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = WColors.Primary,
        contentColor = WColors.OnPrimary,
        disabledContainerColor = WColors.SurfaceVariant,
        disabledContentColor = WColors.OnSurfaceVariant
    ),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(
        defaultElevation = 6.dp,
        pressedElevation = 2.dp,
        disabledElevation = 0.dp
    ),
    shape: Shape = RoundedCornerShape(12.dp),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "button_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Button(
        onClick = onClick,
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = shape,
                ambientColor = WColors.Primary.copy(alpha = if (enabled) glowAlpha else 0f),
                spotColor = WColors.Primary.copy(alpha = if (enabled) glowAlpha else 0f)
            ),
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        contentPadding = contentPadding,
        content = content
    )
}

@Composable
fun WFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    containerColor: Color = WColors.Primary,
    contentColor: Color = WColors.OnPrimary,
    elevation: FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(
        defaultElevation = 8.dp,
        pressedElevation = 4.dp
    ),
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fab_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .shadow(
                elevation = 12.dp,
                shape = shape,
                ambientColor = containerColor.copy(alpha = glowAlpha),
                spotColor = containerColor.copy(alpha = glowAlpha)
            ),
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = elevation,
        content = content
    )
}

/**
 * WSearchBar - A dark-themed search field with glow effect
 */
@Composable
fun WSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search...",
    onSearch: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp),
        placeholder = {
            Text(
                placeholder,
                color = WColors.OnSurfaceVariant.copy(alpha = 0.4f),
                fontSize = 13.sp
            )
        },
        leadingIcon = {
            Icon(
                Icons.Rounded.Search,
                contentDescription = "Search",
                tint = WColors.OnSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Rounded.Close,
                        contentDescription = "Clear",
                        tint = WColors.OnSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        },
        singleLine = true,
        enabled = enabled,
        shape = RoundedCornerShape(10.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = WColors.SurfaceVariant,
            unfocusedContainerColor = WColors.SurfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = WColors.Primary,
            focusedTextColor = WColors.OnSurface,
            unfocusedTextColor = WColors.OnSurface,
            focusedPlaceholderColor = WColors.OnSurfaceVariant.copy(alpha = 0.4f),
            unfocusedPlaceholderColor = WColors.OnSurfaceVariant.copy(alpha = 0.4f),
            disabledTextColor = WColors.OnSurfaceVariant.copy(alpha = 0.5f),
            disabledContainerColor = WColors.SurfaceVariant.copy(alpha = 0.5f)
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch?.invoke() }
        ),
        textStyle = TextStyle(
            fontSize = 13.sp,
            color = WColors.OnSurface
        )
    )
}

/**
 * WBadge - A small badge/chip for displaying counts, status, or labels
 */
@Composable
fun WBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = WColors.Primary,
    textColor: Color = WColors.OnPrimary,
    size: Dp = 18.dp,
    fontSize: Int = 10
) {
    Box(
        modifier = modifier
            .sizeIn(minWidth = size, minHeight = size)
            .background(color.copy(alpha = 0.9f), CircleShape)
            .padding(horizontal = 5.dp, vertical = 1.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * WSliderSetting - A labeled slider with current value display
 */
@Composable
fun WSliderSetting(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..10f,
    steps: Int = 0,
    suffix: String = "",
    description: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WColors.OnSurface,
                    fontWeight = FontWeight.Medium
                )
                if (description != null) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = WColors.OnSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }
            Text(
                text = "${value.toInt()}$suffix",
                style = MaterialTheme.typography.bodySmall,
                color = WColors.Primary,
                fontWeight = FontWeight.SemiBold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = WColors.Primary,
                activeTrackColor = WColors.Primary,
                inactiveTrackColor = WColors.SurfaceVariant,
                activeTickColor = WColors.Primary.copy(alpha = 0.5f),
                inactiveTickColor = WColors.SurfaceVariant.copy(alpha = 0.5f)
            )
        )
    }
}

/**
 * WToggleSetting - A labeled toggle switch with description
 */
@Composable
fun WToggleSetting(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    enabled: Boolean = true
) {
    val thumbColor by animateColorAsState(
        targetValue = if (checked) WColors.Primary else WColors.OnSurfaceVariant.copy(alpha = 0.5f),
        animationSpec = tween(200),
        label = "toggle_thumb"
    )
    val trackColor by animateColorAsState(
        targetValue = if (checked) WColors.Primary.copy(alpha = 0.3f) else WColors.SurfaceVariant,
        animationSpec = tween(200),
        label = "toggle_track"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled) { onCheckedChange(!checked) }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) WColors.OnSurface else WColors.OnSurfaceVariant.copy(alpha = 0.5f),
                fontWeight = FontWeight.Medium
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = WColors.OnSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = thumbColor,
                checkedTrackColor = trackColor,
                uncheckedThumbColor = thumbColor,
                uncheckedTrackColor = trackColor,
                checkedBorderColor = Color.Transparent,
                uncheckedBorderColor = WColors.Border.copy(alpha = 0.5f)
            )
        )
    }
}

/**
 * WDivider - A styled horizontal divider with gradient
 */
@Composable
fun WDivider(
    modifier: Modifier = Modifier,
    color: Color = WColors.Primary,
    thickness: Dp = 1.dp,
    alpha: Float = 0.2f
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        color.copy(alpha = alpha),
                        Color.Transparent
                    )
                )
            )
    )
}
