package com.karigar.app.ui.theme

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
    primary = BluePrimary,
    onPrimary = White,
    primaryContainer = SkyBlue,
    onPrimaryContainer = BluePrimaryDark,
    secondary = BlueAccent,
    onSecondary = White,
    secondaryContainer = SkyBlue,
    onSecondaryContainer = BluePrimaryDark,
    tertiary = LightBlue,
    onTertiary = White,
    background = OffWhite,
    onBackground = SlateText,
    surface = White,
    onSurface = SlateText,
    surfaceVariant = SkyBlue,
    onSurfaceVariant = MutedText,
    outline = BorderGrey
)

private val DarkColors = darkColorScheme(
    primary = DarkBlueAccent,
    onPrimary = White,
    primaryContainer = DarkBlueContainer,
    onPrimaryContainer = DarkBlueLight,
    secondary = DarkBlueLight,
    onSecondary = DarkBackground,
    secondaryContainer = DarkBlueContainer,
    onSecondaryContainer = DarkBlueLight,
    tertiary = LightBlue,
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
        Brush.verticalGradient(listOf(BluePrimary, BlueAccent))
    }
}

@Composable
fun onHeaderColor(): Color = White
