package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CyberColorScheme = darkColorScheme(
    primary = CyberGoldPrimary,
    onPrimary = CyberDarkBackground,
    secondary = CyberNeonGreen,
    onSecondary = CyberDarkBackground,
    tertiary = CyberGoldVariant,
    background = CyberDarkBackground,
    onBackground = CyberTextPrimary,
    surface = CyberCardSurface,
    onSurface = CyberTextPrimary,
    surfaceVariant = CyberCardSurfaceLighter,
    onSurfaceVariant = CyberTextSecondary,
    outline = CyberGoldOutline
)

@Composable
fun A23ProTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    A23ProTheme(content = content)
}
