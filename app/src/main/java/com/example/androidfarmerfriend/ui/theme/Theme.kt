package com.example.androidfarmerfriend.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import com.example.androidfarmerfriend.data.util.UserPrefs

/** The user's theme preference: follow the system, or force light/dark. */
enum class ThemeMode(val key: String) {
    SYSTEM(UserPrefs.THEME_SYSTEM),
    LIGHT(UserPrefs.THEME_LIGHT),
    DARK(UserPrefs.THEME_DARK);

    companion object {
        fun fromKey(key: String?): ThemeMode =
            entries.firstOrNull { it.key == key } ?: SYSTEM
    }
}

// Design-system tokens made available via CompositionLocal.
val LocalFarmerColors = staticCompositionLocalOf { LightFarmerColors }

// Accessor: FarmerTheme.colors from anywhere under the theme.
object FarmerTheme {
    val colors: FarmerColors
        @Composable
        @ReadOnlyComposable
        get() = LocalFarmerColors.current
}

private val FarmerShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun AndroidFarmerFriendTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val farmerColors = if (darkTheme) DarkFarmerColors else LightFarmerColors

    val colorScheme = when {
        darkTheme -> darkColorScheme(
            primary = farmerColors.primary,
            onPrimary = farmerColors.onPrimary,
            primaryContainer = farmerColors.softGreen,
            onPrimaryContainer = farmerColors.primaryDeep,
            secondary = farmerColors.primaryBright,
            onSecondary = farmerColors.primaryDeep,
            secondaryContainer = farmerColors.softGreen,
            onSecondaryContainer = farmerColors.primaryDeep,
            tertiary = farmerColors.weatherBlue,
            onTertiary = OnAccentWhite,
            tertiaryContainer = farmerColors.softBlue,
            onTertiaryContainer = farmerColors.weatherBlue,
            background = farmerColors.background,
            onBackground = farmerColors.textPrimary,
            surface = farmerColors.surface,
            onSurface = farmerColors.textPrimary,
            surfaceVariant = farmerColors.surfaceMuted,
            onSurfaceVariant = farmerColors.textSecondary,
            outline = farmerColors.outline,
            outlineVariant = farmerColors.outline,
            error = farmerColors.alertRed,
            onError = OnAccentWhite
        )
        else -> lightColorScheme(
            primary = farmerColors.primary,
            onPrimary = farmerColors.onPrimary,
            primaryContainer = farmerColors.softGreen,
            onPrimaryContainer = farmerColors.primaryDeep,
            secondary = farmerColors.primaryBright,
            onSecondary = farmerColors.onPrimary,
            secondaryContainer = farmerColors.softGreen,
            onSecondaryContainer = farmerColors.primaryDeep,
            tertiary = farmerColors.weatherBlue,
            onTertiary = OnAccentWhite,
            tertiaryContainer = farmerColors.softBlue,
            onTertiaryContainer = farmerColors.weatherBlue,
            background = farmerColors.background,
            onBackground = farmerColors.textPrimary,
            surface = farmerColors.surface,
            onSurface = farmerColors.textPrimary,
            surfaceVariant = farmerColors.surfaceMuted,
            onSurfaceVariant = farmerColors.textSecondary,
            outline = farmerColors.outline,
            outlineVariant = farmerColors.outline,
            error = farmerColors.alertRed,
            onError = OnAccentWhite
        )
    }

    CompositionLocalProvider(
        LocalFarmerColors provides farmerColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = FarmerShapes,
            content = content
        )
    }
}

// onTertiary / onError are always near-white in both palettes.
private val OnAccentWhite = androidx.compose.ui.graphics.Color.White
