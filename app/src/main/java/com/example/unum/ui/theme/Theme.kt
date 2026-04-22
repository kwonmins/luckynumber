package com.example.unum.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val Scheme = lightColorScheme(
    primary = Accent,
    secondary = Mint,
    tertiary = Gold,
    background = Background,
    surface = Surface,
    surfaceVariant = Surface2,
    onPrimary = Background,
    onSecondary = Background,
    onTertiary = Background,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    error = Rose
)

@Composable
fun UnumTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = Scheme,
        typography = UnumTypography,
        content = content
    )
}
