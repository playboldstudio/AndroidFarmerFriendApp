package com.example.androidfarmerfriend.ui.theme

import androidx.compose.ui.graphics.Color

/* ------------------------------------------------------------------
   Legacy top-level color vals (kept for backward compatibility —
   existing call sites reference these by name).
   ------------------------------------------------------------------ */
val FarmerGreenPrimary = Color(0xFF2E7D32)
val FarmerGreenSecondary = Color(0xFF4CAF50)
val FarmerGreenTertiary = Color(0xFF81C784)

val FarmerGreenDarkPrimary = Color(0xFF81C784)
val FarmerGreenDarkSecondary = Color(0xFF4CAF50)
val FarmerGreenDarkTertiary = Color(0xFF2E7D32)

val BackgroundLight = Color(0xFFF9F9F9)
val SurfaceLight = Color(0xFFFFFFFF)
val OnBackgroundLight = Color(0xFF000000)
val OnSurfaceLight = Color(0xFF000000)
val OutlineLight = Color(0xFFE0E0E0)
val SurfaceVariantLight = Color(0xFFF0F0F0)

val BackgroundDark = Color(0xFF000000)
val SurfaceDark = Color(0xFF121212)
val OnBackgroundDark = Color(0xFFFFFFFF)
val OnSurfaceDark = Color(0xFFFFFFFF)
val OutlineDark = Color(0xFF2C2C2C)
val SurfaceVariantDark = Color(0xFF1E1E1E)

val TrendGreen = Color(0xFF2E7D32)
val TrendRed = Color(0xFFE53935)
val WeatherYellow = Color(0xFFFBC02D)
val WeatherBlue = Color(0xFF2196F3)
val AlertBlue = Color(0xFF1E88E5)
val AlertGreen = Color(0xFF43A047)
val AlertRed = Color(0xFFE53935)
val GrayText = Color(0xFF757575)
val DiseaseOrange = Color(0xFFFF9800)
val SchemeLightGreen = Color(0xFF8BC34A)
val AlertPurple = Color(0xFF9C27B0)
val CropNotesBrown = Color(0xFF795548)

/* ------------------------------------------------------------------
   Farmer Friend design-system tokens.
   These mirror the palette in uiflow/styles.css (light) and add a
   hand-tuned warm dark palette (dark).
   ------------------------------------------------------------------ */
data class FarmerColors(
    // Neutrals
    val background: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val outline: Color,
    // Brand
    val primary: Color,
    val primaryDeep: Color,
    val primaryBright: Color,
    val onPrimary: Color,
    // Semantic accents
    val weatherBlue: Color,
    val weatherYellow: Color,
    val alertRed: Color,
    val alertBlue: Color,
    val alertGreen: Color,
    val alertPurple: Color,
    val diseaseOrange: Color,
    val cropBrown: Color,
    // Soft tint containers
    val softGreen: Color,
    val softBlue: Color,
    val softRed: Color,
    val softPurple: Color,
    val softOrange: Color,
    val softBrown: Color,
    val softMint: Color,
    val softLavender: Color,
    // Gold teaser (profile "what you unlock" card)
    val goldBorder: Color,
    val goldTitle: Color,
    val goldBody: Color,
    val unlockTop: Color,
    val unlockBottom: Color,
    val unlockTileBg: Color,
    val unlockTileIcon: Color
)

val LightFarmerColors = FarmerColors(
    background = Color(0xFFF7F6F3),
    surface = Color(0xFFFFFFFF),
    surfaceMuted = Color(0xFFF1F0EC),
    textPrimary = Color(0xFF1C1C1E),
    textSecondary = Color(0xFF5A5A5E),
    textTertiary = Color(0xFF76767B),
    outline = Color(0xFFEDECE7),
    primary = Color(0xFF2E7D32),
    primaryDeep = Color(0xFF1B5E20),
    primaryBright = Color(0xFF338038),
    onPrimary = Color(0xFFFFFFFF),
    weatherBlue = Color(0xFF2196F3),
    weatherYellow = Color(0xFFFBC02D),
    alertRed = Color(0xFFE53935),
    alertBlue = Color(0xFF1E88E5),
    alertGreen = Color(0xFF43A047),
    alertPurple = Color(0xFF9C27B0),
    diseaseOrange = Color(0xFFF57C00),
    cropBrown = Color(0xFF795548),
    softGreen = Color(0xFFE8F3E6),
    softBlue = Color(0xFFE7F0FD),
    softRed = Color(0xFFFDE8E8),
    softPurple = Color(0xFFF4E8FA),
    softOrange = Color(0xFFFDEEDD),
    softBrown = Color(0xFFF0EAE6),
    softMint = Color(0xFFE6F6E8),
    softLavender = Color(0xFFF0EDF9),
    goldBorder = Color(0xFFE8C96A),
    goldTitle = Color(0xFF8A6A12),
    goldBody = Color(0xFF6B5A20),
    unlockTop = Color(0xFFFFFDF5),
    unlockBottom = Color(0xFFFFF6E0),
    unlockTileBg = Color(0xFFFBEECB),
    unlockTileIcon = Color(0xFFC9A23B)
)

val DarkFarmerColors = FarmerColors(
    background = Color(0xFF121512),
    surface = Color(0xFF1B1F1B),
    surfaceMuted = Color(0xFF232823),
    textPrimary = Color(0xFFE6E6E1),
    textSecondary = Color(0xFFA8A8A0),
    textTertiary = Color(0xFF82827C),
    outline = Color(0xFF2A2F2A),
    primary = Color(0xFF81C784),
    primaryDeep = Color(0xFF4CAF50),
    primaryBright = Color(0xFF66BB6A),
    onPrimary = Color(0xFF0E2A12),
    weatherBlue = Color(0xFF64B5F6),
    weatherYellow = Color(0xFFFFD54F),
    alertRed = Color(0xFFEF5350),
    alertBlue = Color(0xFF42A5F5),
    alertGreen = Color(0xFF66BB6A),
    alertPurple = Color(0xFFBA68C8),
    diseaseOrange = Color(0xFFFFA726),
    cropBrown = Color(0xFFA1887F),
    softGreen = Color(0xFF1F3A24),
    softBlue = Color(0xFF16324A),
    softRed = Color(0xFF3B2226),
    softPurple = Color(0xFF33273F),
    softOrange = Color(0xFF3A2C1E),
    softBrown = Color(0xFF2E2722),
    softMint = Color(0xFF1F3527),
    softLavender = Color(0xFF2C2A38),
    goldBorder = Color(0xFF8A7433),
    goldTitle = Color(0xFFE4C36B),
    goldBody = Color(0xFFCBB57E),
    unlockTop = Color(0xFF2A2415),
    unlockBottom = Color(0xFF231D10),
    unlockTileBg = Color(0xFF3A3018),
    unlockTileIcon = Color(0xFFE4C36B)
)
