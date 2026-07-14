package com.example.androidfarmerfriend.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FarmerGreenDarkPrimary,
    secondary = FarmerGreenDarkSecondary,
    tertiary = FarmerGreenDarkTertiary,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = OnBackgroundDark,
    onSurface = OnSurfaceDark,
    outlineVariant = OutlineDark,
    surfaceVariant = Color(0xFF1E1E1E)
)

private val LightColorScheme = lightColorScheme(
    primary = FarmerGreenPrimary,
    secondary = FarmerGreenSecondary,
    tertiary = FarmerGreenTertiary,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = OnBackgroundLight,
    onSurface = OnSurfaceLight,
    outlineVariant = OutlineLight,
    surfaceVariant = Color(0xFFF0F0F0)
)

@Composable
fun AndroidFarmerFriendTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
