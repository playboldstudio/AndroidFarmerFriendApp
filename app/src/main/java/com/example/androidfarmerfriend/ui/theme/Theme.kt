package com.example.androidfarmerfriend.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// Design-system tokens made available via CompositionLocal.
val LocalFarmerColors = staticCompositionLocalOf { LightFarmerColors }

/** True when the user has opted in to wallpaper-derived dynamic color. */
val LocalDynamicColorEnabled = staticCompositionLocalOf { false }

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
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val farmerColors = if (darkTheme) DarkFarmerColors else LightFarmerColors

    // Dynamic color: wallpaper-derived M3 palette on Android 12+ (API 31).
    // When enabled, the M3 colorScheme uses the device wallpaper palette while
    // FarmerTheme.colors (our custom token set) stays unchanged — the two systems
    // coexist: M3 components use the dynamic scheme, our custom composables use
    // FarmerTheme.colors.
    val useDynamic = dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val colorScheme = when {
        useDynamic && darkTheme -> dynamicDarkColorScheme(LocalContext.current)
        useDynamic -> dynamicLightColorScheme(LocalContext.current)
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
        LocalFarmerColors provides farmerColors,
        LocalDynamicColorEnabled provides dynamicColor
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
