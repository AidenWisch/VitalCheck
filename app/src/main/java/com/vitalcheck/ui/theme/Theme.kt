package com.vitalcheck.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = VitalRed,
    secondary = VitalBlue,
    tertiary = VitalGreen,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = VitalRed,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary
)

@Composable
fun VitalCheckTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = VitalCheckTypography,
        content = content
    )
}
