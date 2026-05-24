package com.qalab.launcher.presentation.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// QA Lab Color Palette
val Purple80 = Color(0xFFBB86FC)
val Purple40 = Color(0xFF6200EE)
val Teal80 = Color(0xFF80CBC4)
val Teal40 = Color(0xFF009688)
val Green80 = Color(0xFFA5D6A7)
val Green40 = Color(0xFF4CAF50)
val Red80 = Color(0xFFEF9A9A)
val Red40 = Color(0xFFE53935)
val Orange80 = Color(0xFFFFCC80)
val Orange40 = Color(0xFFFF9800)
val Blue80 = Color(0xFF90CAF9)
val Blue40 = Color(0xFF1976D2)

val DarkBackground = Color(0xFF0D1117)
val DarkSurface = Color(0xFF161B22)
val DarkSurfaceVariant = Color(0xFF21262D)
val DarkOnBackground = Color(0xFFF0F6FC)
val DarkOnSurface = Color(0xFFC9D1D9)

private val DarkColorScheme = darkColorScheme(
    primary = Teal80,
    secondary = Purple80,
    tertiary = Green80,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    error = Red80,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = Teal40,
    secondary = Purple40,
    tertiary = Green40,
    background = Color(0xFFF6F8FA),
    surface = Color.White,
    surfaceVariant = Color(0xFFE8EAED),
    error = Red40
)

@Composable
fun QALabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
