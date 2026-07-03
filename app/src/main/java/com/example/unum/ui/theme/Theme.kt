package com.example.unum.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val Scheme = darkColorScheme(
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
    error = Rose,
    outline = Border,
    outlineVariant = BorderStrong,
    surfaceContainer = Surface,
    surfaceContainerLow = Background,
    surfaceContainerHighest = Surface2
)

@Composable
fun UnumTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = Scheme,
        typography = UnumTypography,
        content = content
    )
}
