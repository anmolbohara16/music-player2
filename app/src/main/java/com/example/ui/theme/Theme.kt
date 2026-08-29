package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MusicDarkColorScheme = darkColorScheme(
    primary = AccentPurple,
    onPrimary = TextPrimary,
    primaryContainer = DarkElevated,
    onPrimaryContainer = AccentCyan,
    secondary = AccentCyan,
    onSecondary = DarkBackground,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = AccentCyan,
    tertiary = AccentPink,
    onTertiary = TextPrimary,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = DarkCard,
    surfaceContainerHigh = DarkElevated,
    outline = DividerColor,
    outlineVariant = DarkElevated
)

@Composable
fun PersonalMusicPlayerTheme(
    darkTheme: Boolean = true, // Default to sleek immersive dark mode for music player
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = MusicDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkBackground.toArgb()
            window.navigationBarColor = DarkBackground.toArgb()
            val windowInsetsController = WindowCompat.getInsetsController(window, view)
            windowInsetsController.isAppearanceLightStatusBars = false
            windowInsetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    PersonalMusicPlayerTheme(darkTheme, dynamicColor, content)
}
