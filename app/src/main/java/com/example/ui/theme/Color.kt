package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Compatibility tokens keep every existing screen on the same adaptive design system.
val DarkBackground: Color @Composable get() = MaterialTheme.colorScheme.background
val DarkSurface: Color @Composable get() = MaterialTheme.colorScheme.surface
val DarkSurfaceVariant: Color @Composable get() = MaterialTheme.colorScheme.surfaceVariant
val DarkCard: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainer
val DarkElevated: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh
val GlassCardBackground: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainer
val GlassCardBackgroundHover: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh
val GlassCardBackgroundElevated: Color @Composable get() = MaterialTheme.colorScheme.surfaceVariant
val GlassCardBorder: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant
val GlassCardBorderSubtle: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
val GlassCardBorderActive: Color @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
val GlassBadgeBg: Color @Composable get() = MaterialTheme.colorScheme.secondaryContainer
val GlassActiveNav: Color @Composable get() = MaterialTheme.colorScheme.primaryContainer
val GlassBottomNavBg: Color @Composable get() = MaterialTheme.colorScheme.surface
val AccentPurple: Color @Composable get() = MaterialTheme.colorScheme.primary
val AccentPurpleDeep: Color @Composable get() = MaterialTheme.colorScheme.primaryContainer
val AccentPurpleDarkText: Color @Composable get() = MaterialTheme.colorScheme.onPrimary
val AccentCyan: Color @Composable get() = MaterialTheme.colorScheme.secondary
val AccentPink: Color @Composable get() = MaterialTheme.colorScheme.tertiary
val AccentEmerald: Color @Composable get() = MaterialTheme.colorScheme.secondary
val AccentAmber: Color @Composable get() = MaterialTheme.colorScheme.tertiary
val AccentGold: Color @Composable get() = MaterialTheme.colorScheme.tertiary
val TextPrimary: Color @Composable get() = MaterialTheme.colorScheme.onSurface
val TextSecondary: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
val TextTertiary: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
val DividerColor: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant
val AppBackground: Color @Composable get() = MaterialTheme.colorScheme.background
val AppSurface: Color @Composable get() = MaterialTheme.colorScheme.surface
val SupportingSurface: Color @Composable get() = MaterialTheme.colorScheme.surfaceContainer
val SupportingLavender: Color @Composable get() = MaterialTheme.colorScheme.primaryContainer
val SupportingPink: Color @Composable get() = MaterialTheme.colorScheme.tertiaryContainer
val SupportingMint: Color @Composable get() = MaterialTheme.colorScheme.secondaryContainer
val PrimaryAction: Color @Composable get() = MaterialTheme.colorScheme.primary
val OnPrimaryAction: Color @Composable get() = MaterialTheme.colorScheme.onPrimary
val TextMuted: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
val GradientFrostedGlass: List<Color> @Composable get() = listOf(AccentPurple.copy(alpha = 0.08f), Color.Transparent)
val GradientPurpleCyan: List<Color> @Composable get() = listOf(AccentPurple, AccentCyan)
val GradientDarkCard: List<Color> @Composable get() = listOf(DarkSurface, DarkSurfaceVariant)
