package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Frosted Glass Aesthetic Palette
val DarkBackground = Color(0xFF0D0D12)
val DarkSurface = Color(0xFF1C1B1F)
val DarkSurfaceVariant = Color(0xFF25242C)
val DarkCard = Color(0xFF18171F)
val DarkElevated = Color(0xFF26242E)

// Glass Surface Colors & Translucencies
val GlassCardBackground = Color(0x0DFFFFFF) // rgba(255, 255, 255, 0.05)
val GlassCardBackgroundHover = Color(0x14FFFFFF) // rgba(255, 255, 255, 0.08)
val GlassCardBackgroundElevated = Color(0x1FFFFFFF) // rgba(255, 255, 255, 0.12)
val GlassCardBorder = Color(0x1AFFFFFF) // rgba(255, 255, 255, 0.10)
val GlassCardBorderSubtle = Color(0x0DFFFFFF) // rgba(255, 255, 255, 0.05)
val GlassCardBorderActive = Color(0x40D0BCFF) // rgba(208, 188, 255, 0.25)
val GlassBadgeBg = Color(0x33FFFFFF) // rgba(255, 255, 255, 0.20)
val GlassActiveNav = Color(0x33D0BCFF) // rgba(208, 188, 255, 0.20)
val GlassBottomNavBg = Color(0xCC13121A) // Translucent glass bottom bar background

// Frosted Glass Accents & M3 Light Lavender Tones
val AccentPurple = Color(0xFFD0BCFF) // Main Frosted Glass Lavender Accent
val AccentPurpleDeep = Color(0xFF4F378B) // Deep Purple for Glass Gradients
val AccentPurpleDarkText = Color(0xFF381E72) // Text on Lavender Pill
val AccentCyan = Color(0xFF8CE8FF)
val AccentPink = Color(0xFFFFB2B6)
val AccentEmerald = Color(0xFF85F4AB)
val AccentAmber = Color(0xFFFFD978)

// Text & Slate Content Colors
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFF94A3B8) // Slate 400
val TextTertiary = Color(0xFF64748B) // Slate 500
val DividerColor = Color(0x0DFFFFFF)

// Gradients & Ambient Tones
val GradientFrostedGlass = listOf(AccentPurpleDeep.copy(alpha = 0.35f), Color.Transparent)
val GradientPurpleCyan = listOf(AccentPurple, AccentCyan)
val GradientDarkCard = listOf(DarkSurface, DarkSurfaceVariant)

