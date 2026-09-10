package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BrickPrimary,
    onPrimary = BrickOnPrimary,
    primaryContainer = BrickPrimaryContainer,
    onPrimaryContainer = BrickOnPrimaryContainer,
    secondary = BrickSecondary,
    onSecondary = BrickOnSecondary,
    secondaryContainer = BrickSecondaryContainer,
    onSecondaryContainer = BrickOnSecondaryContainer,
    tertiary = BrickTertiary,
    onTertiary = BrickOnTertiary,
    tertiaryContainer = BrickTertiaryContainer,
    onTertiaryContainer = BrickOnTertiaryContainer,
    background = BrickBackground,
    onBackground = BrickTextPrimary,
    surface = BrickSurface,
    onSurface = BrickTextPrimary,
    surfaceVariant = BrickSurfaceVariant,
    onSurfaceVariant = BrickTextSecondary,
    outline = BrickOutline,
    outlineVariant = BrickOutlineVariant
)

private val LightColorScheme = DarkColorScheme // Ensure Sophisticated Dark aesthetic remains standard

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
