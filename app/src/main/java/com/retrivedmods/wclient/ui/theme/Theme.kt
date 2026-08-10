package com.retrivedmods.wclient.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


object WColors {
    // Primary - vibrant neon red
    val Primary = Color(0xFFFF1744)
    val PrimaryLight = Color(0xFFFF616F)
    val PrimaryDark = Color(0xFFC4001D)
    val PrimaryGlow = Color(0xFFFF1744).copy(alpha = 0.15f)
    val OnPrimary = Color(0xFFFFFFFF)

    // Secondary - cyberpunk purple
    val Secondary = Color(0xFF7C4DFF)
    val SecondaryVariant = Color(0xFF651FFF)
    val SecondaryLight = Color(0xFFB388FF)
    val SecondaryGlow = Color(0xFF7C4DFF).copy(alpha = 0.12f)
    val OnSecondary = Color(0xFFFFFFFF)

    // Accent - hot pink / neon
    val Accent = Color(0xFFFF4081)
    val AccentLight = Color(0xFFFF80AB)
    val AccentDark = Color(0xFFC51162)
    val AccentGlow = Color(0xFFFF4081).copy(alpha = 0.12f)

    // Background - deeper dark with subtle blue tint
    val Background = Color(0xFF08080D)
    val Surface = Color(0xFF0E0D14)
    val SurfaceVariant = Color(0xFF1A1822)
    val SurfaceContainer = Color(0xFF131119)

    // Text
    val OnBackground = Color(0xFFF8FAFC)
    val OnSurface = Color(0xFFE8EAED)
    val OnSurfaceVariant = Color(0xFF9CA3B0)

    // Semantic
    val Error = Color(0xFFFF1744)
    val ErrorLight = Color(0xFFFF616F)
    val Success = Color(0xFF00E676)
    val SuccessLight = Color(0xFF69F0AE)
    val Warning = Color(0xFFFFAB00)
    val WarningLight = Color(0xFFFFD740)
    val Info = Color(0xFF40C4FF)
    val InfoLight = Color(0xFF80D8FF)

    // UI
    val Border = Color(0xFF2B2635)
    val BorderLight = Color(0xFF3D3648)

    val Overlay = Color(0x80000000)

    // Minimap - enhanced for visibility
    val MinimapBackground = Color(0xCC050508)
    val MinimapGrid = Color(0x55FFFFFF)
    val MinimapCrosshair = Color(0x80FFFFFF)
    val MinimapPlayerMarker = Color(0xFFFFFFFF)
    val MinimapNorth = Color(0xFFFF1744)
    val MinimapEntityClose = Color(0xFFFF1744)
    val MinimapEntityFar = Color(0xFFFFAB00)
    val MinimapZoom = 1.2f
    val MinimapDotSize = 6
}

object ClickGUIColors {
    val PrimaryBackground = Color(0xFF08080D)
    val SecondaryBackground = Color(0xFF110F18)

    val AccentColor = WColors.Primary
    val AccentColorVariant = WColors.AccentLight

    val PrimaryText = Color(0xFFFFFFFF)
    val SecondaryText = Color(0xFF9CA3B0)

    val PanelBackground = Color(0xF0100E16)
    val PanelBorder = Color(0x60FF1744)

    val ModuleEnabled = AccentColor
    val ModuleDisabled = Color(0xFF1F1D28)
    val ModuleHover = Color(0xFF2A2535)

    val SliderTrack = Color(0xFF282530)
    val SliderThumb = AccentColor
    val SliderFill = AccentColor

    val CheckboxBorder = AccentColor
    val CheckboxFill = AccentColor
    
    val CategoryActive = AccentColor
    val CategoryInactive = Color(0xFF5A5468)
    
    val SearchBackground = Color(0xFF14121C)
    val SearchText = Color(0xFFE8EAED)
    val SearchHint = Color(0xFF6B6478)
    
    val ScrollbarTrack = Color(0xFF1A1822)
    val ScrollbarThumb = Color(0xFF3D3648)
}

private val WDarkColorScheme = darkColorScheme(
    primary = WColors.Primary,
    onPrimary = WColors.OnPrimary,
    primaryContainer = WColors.PrimaryDark,
    onPrimaryContainer = WColors.PrimaryLight,

    secondary = WColors.Secondary,
    onSecondary = WColors.OnSecondary,
    secondaryContainer = WColors.SecondaryVariant,
    onSecondaryContainer = WColors.SecondaryLight,

    tertiary = WColors.Accent,
    onTertiary = Color.White,
    tertiaryContainer = WColors.AccentDark.copy(alpha = 0.25f),
    onTertiaryContainer = WColors.AccentLight,

    background = WColors.Background,
    onBackground = WColors.OnBackground,
    surface = WColors.Surface,
    onSurface = WColors.OnSurface,
    surfaceVariant = WColors.SurfaceVariant,
    onSurfaceVariant = WColors.OnSurfaceVariant,
    surfaceContainer = WColors.SurfaceContainer,

    error = WColors.Error,
    onError = Color.White,
    errorContainer = WColors.Error.copy(alpha = 0.22f),
    onErrorContainer = WColors.ErrorLight,

    outline = WColors.Border,
    outlineVariant = WColors.BorderLight.copy(alpha = 0.55f),

    scrim = WColors.Overlay,
    inverseSurface = WColors.OnSurface,
    inverseOnSurface = WColors.Surface,
    inversePrimary = WColors.PrimaryDark
)

private val WLightColorScheme = lightColorScheme(
    primary = WColors.Primary,
    onPrimary = WColors.OnPrimary,
    primaryContainer = WColors.Primary.copy(alpha = 0.12f),
    onPrimaryContainer = WColors.Primary,

    secondary = WColors.Secondary,
    onSecondary = WColors.OnSecondary,
    secondaryContainer = WColors.Secondary.copy(alpha = 0.12f),
    onSecondaryContainer = WColors.Secondary,

    tertiary = WColors.Accent,
    onTertiary = WColors.OnPrimary,
    tertiaryContainer = WColors.Accent.copy(alpha = 0.12f),
    onTertiaryContainer = WColors.Accent,

    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF111111),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF121212),
    surfaceVariant = Color(0xFFF3F3F4),
    onSurfaceVariant = Color(0xFF545B66),
    surfaceContainer = Color(0xFFE9E9EC),

    error = WColors.Error,
    onError = WColors.OnPrimary,
    outline = Color(0xFFE5E7EB),
    outlineVariant = Color(0xFFF1F5F9)
)


val WTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.sp
    )
)

@Composable
fun WClientTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> WDarkColorScheme
        else -> WLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = WTypography,
        content = content
    )
}