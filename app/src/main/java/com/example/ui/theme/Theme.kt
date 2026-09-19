package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

private val DarkColorScheme = darkColorScheme(
    primary = BhashaPrimaryDark,
    onPrimary = BhashaOnPrimaryDark,
    primaryContainer = BhashaPrimaryContainerDark,
    onPrimaryContainer = BhashaOnPrimaryContainerDark,
    secondary = BhashaSecondaryDark,
    onSecondary = BhashaOnSecondaryDark,
    secondaryContainer = BhashaSecondaryContainerDark,
    onSecondaryContainer = BhashaOnSecondaryContainerDark,
    tertiary = BhashaTertiaryDark,
    onTertiary = BhashaOnTertiaryDark,
    tertiaryContainer = BhashaTertiaryContainerDark,
    onTertiaryContainer = BhashaOnTertiaryContainerDark,
    background = BhashaBackgroundDark,
    surface = BhashaSurfaceDark,
    surfaceVariant = BhashaSurfaceVariantDark,
    onBackground = BhashaTextPrimaryDark,
    onSurface = BhashaTextPrimaryDark,
    onSurfaceVariant = BhashaTextSecondaryDark,
    outline = BhashaOutlineDark,
    outlineVariant = BhashaOutlineVariantDark,
    error = ErrorRed,
    onError = BhashaOnPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = BhashaNavyPrimary,
    onPrimary = BhashaOnPrimary,
    primaryContainer = BhashaPrimaryContainer,
    onPrimaryContainer = BhashaOnPrimaryContainer,
    secondary = BhashaTealSecondary,
    onSecondary = BhashaOnSecondary,
    secondaryContainer = BhashaSecondaryContainer,
    onSecondaryContainer = BhashaOnSecondaryContainer,
    tertiary = BhashaSlateTertiary,
    onTertiary = BhashaOnTertiary,
    tertiaryContainer = BhashaTertiaryContainer,
    onTertiaryContainer = BhashaOnTertiaryContainer,
    background = BhashaBackgroundLight,
    surface = BhashaSurfaceLight,
    surfaceVariant = BhashaSurfaceVariantLight,
    onBackground = BhashaTextPrimaryLight,
    onSurface = BhashaTextPrimaryLight,
    onSurfaceVariant = BhashaTextSecondaryLight,
    outline = BhashaOutlineLight,
    outlineVariant = BhashaOutlineVariantLight,
    error = ErrorRed,
    onError = BhashaOnPrimary
)

/**
 * CompositionLocal for theme-aware glass colors.
 * Provides [GlassColors] that switch between light and dark mode variants.
 */
val LocalGlassColors = staticCompositionLocalOf { GlassColorsLight }

/**
 * Convenient accessor for current theme [GlassColors], matching MaterialTheme conventions.
 */
object GlassTheme {
    val colors: GlassColors
        @Composable
        @ReadOnlyComposable
        get() = LocalGlassColors.current
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val glassColors = if (darkTheme) GlassColorsDark else GlassColorsLight

    CompositionLocalProvider(LocalGlassColors provides glassColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
