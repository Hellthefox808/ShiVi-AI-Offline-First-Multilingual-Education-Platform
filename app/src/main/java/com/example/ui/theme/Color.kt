package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// BhashaSetu Collective Design System (Stitch)
// Brand Identity: Institutional Authority, Technological Precision, Educational Clarity
// ==========================================

// Primary - Deep Navy (Institutional Authority & Structure)
val BhashaNavyPrimary = Color(0xFF0F172A)
val BhashaOnPrimary = Color(0xFFFFFFFF)
val BhashaPrimaryContainer = Color(0xFF1E293B)
val BhashaOnPrimaryContainer = Color(0xFFE2E8F0)

// Secondary - Educational Teal (Action, FLN Progress & Interactivity)
val BhashaTealSecondary = Color(0xFF0D9488)
val BhashaOnSecondary = Color(0xFFFFFFFF)
val BhashaSecondaryContainer = Color(0xFF86F2E4)
val BhashaOnSecondaryContainer = Color(0xFF006F66)

// Tertiary - Slate Neutral (Data, Metrics & Precision)
val BhashaSlateTertiary = Color(0xFF334155)
val BhashaOnTertiary = Color(0xFFFFFFFF)
val BhashaTertiaryContainer = Color(0xFF0D1C2F)
val BhashaOnTertiaryContainer = Color(0xFF76859B)

// Surface & Background (Light Mode - Clean Academic Canvas)
val BhashaBackgroundLight = Color(0xFFF7F9FB)
val BhashaSurfaceLight = Color(0xFFFFFFFF)
val BhashaSurfaceContainerLowest = Color(0xFFFFFFFF)
val BhashaSurfaceContainerLow = Color(0xFFF2F4F6)
val BhashaSurfaceContainer = Color(0xFFECEEF0)
val BhashaSurfaceContainerHigh = Color(0xFFE6E8EA)
val BhashaSurfaceContainerHighest = Color(0xFFE0E3E5)
val BhashaSurfaceVariantLight = Color(0xFFE0E3E5)
val BhashaOutlineLight = Color(0xFFCBD5E1)
val BhashaOutlineVariantLight = Color(0xFFE2E8F0)
val BhashaTextPrimaryLight = Color(0xFF191C1E)
val BhashaTextSecondaryLight = Color(0xFF45464D)
val BhashaTextTertiaryLight = Color(0xFF76777D)

// Dark Mode Palette (Midnight Slate & Luminous Teal)
val BhashaBackgroundDark = Color(0xFF0B1120)
val BhashaSurfaceDark = Color(0xFF0F172A)
val BhashaSurfaceVariantDark = Color(0xFF1E293B)
val BhashaPrimaryDark = Color(0xFF38BDF8)
val BhashaOnPrimaryDark = Color(0xFF0B1120)
val BhashaPrimaryContainerDark = Color(0xFF1E293B)
val BhashaOnPrimaryContainerDark = Color(0xFFE2E8F0)

val BhashaSecondaryDark = Color(0xFF2DD4BF)
val BhashaOnSecondaryDark = Color(0xFF003833)
val BhashaSecondaryContainerDark = Color(0xFF134E4A)
val BhashaOnSecondaryContainerDark = Color(0xFF86F2E4)

val BhashaTertiaryDark = Color(0xFF94A3B8)
val BhashaOnTertiaryDark = Color(0xFF0F172A)
val BhashaTertiaryContainerDark = Color(0xFF0D1C2F)
val BhashaOnTertiaryContainerDark = Color(0xFF76859B)

val BhashaOutlineDark = Color(0xFF334155)
val BhashaOutlineVariantDark = Color(0xFF1E293B)
val BhashaTextPrimaryDark = Color(0xFFF8FAFC)
val BhashaTextSecondaryDark = Color(0xFF94A3B8)

// Atmospheric Canvas Tones (Subtle Educational Refraction Backdrop)
val BhashaAtmosphereTop = Color(0xFFF1F5F9)
val BhashaAtmosphereMid = Color(0xFFF8FAFC)
val BhashaAtmosphereBottom = Color(0xFFEDF2F7)
val BhashaAtmosphereTeal = Color(0x140D9488)
val BhashaAtmosphereNavy = Color(0x0F0F172A)

// Status & Tribal Language Accent Colors (Grounding & Cultural Precision)
val SanthaliAccent = Color(0xFF0D9488)   // Educational Teal (Ol Chiki)
val HoAccent = Color(0xFFC2410C)         // Warm Terracotta Ochre (Warang Chiti)
val MundariAccent = Color(0xFFB91C1C)    // Earthy Crimson (Devanagari / Nag Mundari)
val SuccessGreen = Color(0xFF16A34A)     // Verified Emerald Green
val WarningAmber = Color(0xFFD97706)     // High-contrast Warm Amber
val ErrorRed = Color(0xFFBA1A1A)         // Deep Crimson Alert

// ==========================================
// Theme-Aware Glass Colors for Glassmorphism
// ==========================================

// Standalone Glass Tokens (Stitch Crisp Academic Glass)
val GlassSurfaceLight = Color(0xE6FFFFFF)           // 90% Pure White Glass
val GlassSurfaceTinted = Color(0xF2F8FAFC)          // 95% Soft Neutral Glass
val GlassSurfaceUltraLight = Color(0xB3FFFFFF)      // 70% Translucent Glass
val GlassSurfaceFloating = Color(0xF8FFFFFF)        // 97% Floating Card Surface
val GlassBorderLight = Color(0x60CBD5E1)            // Soft 1dp hairline border
val GlassBorderHighlight = Color(0xCCFFFFFF)        // Crisp top edge highlight

val GlassSurfaceDark = Color(0xCC0F172A)            // 80% Midnight Navy Glass
val GlassBorderDark = Color(0x40475569)             // Muted Dark Slate Hairline

data class GlassColors(
    val surface: Color,
    val surfaceTinted: Color,
    val surfaceUltraLight: Color,
    val surfaceFloating: Color,
    val borderLight: Color,
    val borderHighlight: Color
)

val GlassColorsLight = GlassColors(
    surface = GlassSurfaceLight,
    surfaceTinted = GlassSurfaceTinted,
    surfaceUltraLight = GlassSurfaceUltraLight,
    surfaceFloating = GlassSurfaceFloating,
    borderLight = GlassBorderLight,
    borderHighlight = GlassBorderHighlight
)

val GlassColorsDark = GlassColors(
    surface = GlassSurfaceDark,
    surfaceTinted = Color(0xE61E293B),     // 90% Slate Container Glass
    surfaceUltraLight = Color(0x990F172A), // 60% Translucent Dark Glass
    surfaceFloating = Color(0xF20F172A),   // 95% Floating Midnight Surface
    borderLight = GlassBorderDark,
    borderHighlight = Color(0x3094A3B8)    // Soft Edge Glow
)

