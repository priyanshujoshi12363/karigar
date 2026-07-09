package com.karigar.worker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = White,
    primaryContainer = PaleGreen,
    onPrimaryContainer = GreenPrimaryDark,
    secondary = GreenAccent,
    onSecondary = White,
    secondaryContainer = PaleGreen,
    onSecondaryContainer = GreenPrimaryDark,
    tertiary = LightGreen,
    onTertiary = White,
    background = OffWhite,
    onBackground = SlateText,
    surface = White,
    onSurface = SlateText,
    surfaceVariant = PaleGreen,
    onSurfaceVariant = MutedText,
    outline = BorderGrey
)

private val DarkColors = darkColorScheme(
    primary = DarkGreenAccent,
    onPrimary = DarkBackground,
    primaryContainer = DarkGreenContainer,
    onPrimaryContainer = DarkGreenLight,
    secondary = DarkGreenLight,
    onSecondary = DarkBackground,
    secondaryContainer = DarkGreenContainer,
    onSecondaryContainer = DarkGreenLight,
    tertiary = LightGreen,
    onTertiary = DarkBackground,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline
)

@Composable
fun KarigarTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val colorScheme = if (dark) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun brandHeaderBrush(): Brush {
    return if (isSystemInDarkTheme()) {
        Brush.verticalGradient(listOf(DarkHeader, DarkSurface))
    } else {
        Brush.verticalGradient(listOf(GreenPrimary, GreenAccent))
    }
}
