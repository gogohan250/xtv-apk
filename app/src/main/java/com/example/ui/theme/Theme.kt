package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val XtvColorScheme = darkColorScheme(
    primary = XtvAccent,
    onPrimary = XtvBackground,
    primaryContainer = XtvAccentDark,
    onPrimaryContainer = XtvTextPrimary,
    secondary = XtvAccentLight,
    onSecondary = XtvBackground,
    background = XtvBackground,
    onBackground = XtvTextPrimary,
    surface = XtvSurface,
    onSurface = XtvTextPrimary,
    surfaceVariant = XtvSurfaceVariant,
    onSurfaceVariant = XtvTextSecondary,
    outline = XtvBorder,
    error = XtvError,
    onError = XtvTextPrimary
)

@Composable
fun XtvTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = XtvColorScheme,
            typography = XtvTypography,
            content = content
        )
    }
}
