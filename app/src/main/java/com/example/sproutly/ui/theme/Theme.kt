package com.example.sproutly.ui.theme

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

private val ForestScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    onPrimary = WhitePure,
    primaryContainer = ForestGreenDark,
    onPrimaryContainer = WhitePure,
    background = ForestBackground,
    surface = WhitePure,
    onBackground = TextDark,
    onSurface = TextDark,
    secondary = EarthBrown
)

@Composable
fun SproutlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Desligado para manter o estilo Forest
    content: @Composable () -> Unit
) {
    val colorScheme = ForestScheme // Forçar sempre o tema Forest claro

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = ForestGreenDark.toArgb() // Barra de status verde escuro
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false // Ícones brancos na barra
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SproutlyTypography,
        content = content
    )
}