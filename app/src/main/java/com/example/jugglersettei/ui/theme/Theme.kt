package com.example.jugglersettei.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary = JugglerGold,
    secondary = JugglerRed,
    background = JugglerNavy,
    surface = JugglerSurface,
)

private val LightColors = lightColorScheme(
    primary = JugglerRed,
    secondary = JugglerGold,
)

@Composable
fun JugglerSetteiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
