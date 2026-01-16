package com.ieum.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 이음 앱 컬러 팔레트
object IeumColors {
    // Primary - 따뜻한 코랄/살몬 핑크
    val Primary = Color(0xFFFF8A80)
    val PrimaryLight = Color(0xFFFFBCAF)
    val PrimaryDark = Color(0xFFC85A54)
    
    // Secondary - 소프트 퍼플
    val Secondary = Color(0xFFB39DDB)
    val SecondaryLight = Color(0xFFE6CEFF)
    val SecondaryDark = Color(0xFF836FA9)
    
    // Accent - 민트 그린
    val Accent = Color(0xFF80CBC4)
    val AccentLight = Color(0xFFB2FEF7)
    val AccentDark = Color(0xFF4F9A94)
    
    // Background
    val Background = Color(0xFFFFFBFA)
    val BackgroundDark = Color(0xFF1C1B1F)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceDark = Color(0xFF2D2D30)
    
    // Text
    val TextPrimary = Color(0xFF1C1B1F)
    val TextSecondary = Color(0xFF6B6B6B)
    val TextLight = Color(0xFFFFFFFF)
    
    // Status
    val Success = Color(0xFF81C784)
    val Warning = Color(0xFFFFD54F)
    val Error = Color(0xFFE57373)
    
    // Category Colors
    val CategoryFood = Color(0xFFFFAB91)
    val CategoryCafe = Color(0xFFBCAAA4)
    val CategoryDrink = Color(0xFFCE93D8)
    val CategoryCulture = Color(0xFF90CAF9)
    val CategoryTravel = Color(0xFFA5D6A7)
    val CategoryGame = Color(0xFFFFF176)
    
    // Gradient
    val GradientStart = Color(0xFFFF8A80)
    val GradientEnd = Color(0xFFB39DDB)
}

private val LightColorScheme = lightColorScheme(
    primary = IeumColors.Primary,
    onPrimary = IeumColors.TextLight,
    primaryContainer = IeumColors.PrimaryLight,
    onPrimaryContainer = IeumColors.PrimaryDark,
    secondary = IeumColors.Secondary,
    onSecondary = IeumColors.TextLight,
    secondaryContainer = IeumColors.SecondaryLight,
    onSecondaryContainer = IeumColors.SecondaryDark,
    tertiary = IeumColors.Accent,
    onTertiary = IeumColors.TextLight,
    background = IeumColors.Background,
    onBackground = IeumColors.TextPrimary,
    surface = IeumColors.Surface,
    onSurface = IeumColors.TextPrimary,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = IeumColors.TextSecondary,
    error = IeumColors.Error,
    onError = IeumColors.TextLight
)

private val DarkColorScheme = darkColorScheme(
    primary = IeumColors.PrimaryLight,
    onPrimary = IeumColors.PrimaryDark,
    primaryContainer = IeumColors.PrimaryDark,
    onPrimaryContainer = IeumColors.PrimaryLight,
    secondary = IeumColors.SecondaryLight,
    onSecondary = IeumColors.SecondaryDark,
    secondaryContainer = IeumColors.SecondaryDark,
    onSecondaryContainer = IeumColors.SecondaryLight,
    tertiary = IeumColors.AccentLight,
    onTertiary = IeumColors.AccentDark,
    background = IeumColors.BackgroundDark,
    onBackground = IeumColors.TextLight,
    surface = IeumColors.SurfaceDark,
    onSurface = IeumColors.TextLight,
    surfaceVariant = Color(0xFF3D3D40),
    onSurfaceVariant = Color(0xFFB0B0B0),
    error = IeumColors.Error,
    onError = IeumColors.TextLight
)

@Composable
fun IeumTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = IeumTypography,
        shapes = IeumShapes,
        content = content
    )
}
