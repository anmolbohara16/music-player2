package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LavenderLight = lightColorScheme(
    primary = Color(0xFF7050A4), onPrimary = Color.White,
    primaryContainer = Color(0xFFE9DDF8), onPrimaryContainer = Color(0xFF34204F),
    secondary = Color(0xFF586A65), onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3ECE7), onSecondaryContainer = Color(0xFF293C35),
    tertiary = Color(0xFF956078), onTertiary = Color.White,
    background = Color(0xFFFAF8FC), onBackground = Color(0xFF272130),
    surface = Color.White, onSurface = Color(0xFF272130),
    surfaceVariant = Color(0xFFF0EAF6), onSurfaceVariant = Color(0xFF645B6E),
    surfaceContainer = Color(0xFFF2EDF7), surfaceContainerHigh = Color(0xFFEAE2F1),
    outline = Color(0xFF83758D), outlineVariant = Color(0xFFDED5E6)
)
private val PlumDark = darkColorScheme(
    primary = Color(0xFFD0B8F0), onPrimary = Color(0xFF38204F),
    primaryContainer = Color(0xFF493259), onPrimaryContainer = Color(0xFFEEDCFF),
    secondary = Color(0xFFB7CEC4), onSecondary = Color(0xFF243C32),
    secondaryContainer = Color(0xFF33483E), onSecondaryContainer = Color(0xFFD7EBDF),
    tertiary = Color(0xFFE4B5CA), onTertiary = Color(0xFF50293B),
    background = Color(0xFF18131E), onBackground = Color(0xFFF0E8F5),
    surface = Color(0xFF211A29), onSurface = Color(0xFFF0E8F5),
    surfaceVariant = Color(0xFF362B40), onSurfaceVariant = Color(0xFFC6BACF),
    surfaceContainer = Color(0xFF261F2E), surfaceContainerHigh = Color(0xFF31263B),
    outline = Color(0xFF9A89A6), outlineVariant = Color(0xFF493C53)
)

@Composable
fun PersonalMusicPlayerTheme(darkTheme: Boolean = isSystemInDarkTheme(), dynamicColor: Boolean = false, content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) SideEffect {
        (view.context as? Activity)?.window?.let { window ->
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }
    MaterialTheme(colorScheme = if (darkTheme) PlumDark else LavenderLight, typography = Typography,
        shapes = Shapes(small = RoundedCornerShape(8.dp), medium = RoundedCornerShape(14.dp), large = RoundedCornerShape(20.dp)), content = content)
}

@Composable
fun MyApplicationTheme(darkTheme: Boolean = isSystemInDarkTheme(), dynamicColor: Boolean = false, content: @Composable () -> Unit) =
    PersonalMusicPlayerTheme(darkTheme, dynamicColor, content)
