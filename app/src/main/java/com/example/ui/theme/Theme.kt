package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CineVaultColorScheme = darkColorScheme(
    primary = CineGold,
    onPrimary = CineDarkBg,
    primaryContainer = CineGoldDark,
    onPrimaryContainer = CineTextPrimary,
    secondary = CineCyan,
    onSecondary = CineDarkBg,
    secondaryContainer = CineCyanMuted,
    onSecondaryContainer = CineTextPrimary,
    tertiary = CineRed,
    background = CineDarkBg,
    onBackground = CineTextPrimary,
    surface = CineSurface,
    onSurface = CineTextPrimary,
    surfaceVariant = CineSurfaceVariant,
    onSurfaceVariant = CineTextSecondary,
    outline = CineCardBorder
)

@Composable
fun CineVaultTheme(
    darkTheme: Boolean = true, // Default to premium dark cinematic mode
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = CineVaultColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
