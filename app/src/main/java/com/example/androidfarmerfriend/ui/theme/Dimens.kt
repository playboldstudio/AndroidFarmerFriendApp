package com.example.androidfarmerfriend.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** App-wide spacing scale. */
object FarmerSpacing {
    val xs: Dp = 4.dp
    val s: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 20.dp
    val xxl: Dp = 24.dp
    val xxxl: Dp = 32.dp
    val xxxxl: Dp = 40.dp
    val section: Dp = 48.dp
    val screen: Dp = 56.dp
}

/** Consistent animation durations, curves, and springs. */
object FarmerMotion {
    // Durations (ms)
    const val durationFast = 100
    const val durationNormal = 200
    const val durationSlow = 300
    const val durationModal = 250

    // M3 easing curves
    val standardDecelerate = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f)
    val standardAccelerate = CubicBezierEasing(0.3f, 0.0f, 1.0f, 1.0f)
    val emphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val emphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

    // Springs
    val springBouncy = spring<Float>(dampingRatio = 0.5f, stiffness = 1500f)
    val springSnappy = spring<Float>(dampingRatio = 0.75f, stiffness = 2000f)
    val springGentle = spring<Float>(dampingRatio = 0.85f, stiffness = 800f)
}

/** Consistent icon sizing scale. */
object FarmerIcons {
    val sizeXs: Dp = 14.dp   // Inline badges
    val sizeSm: Dp = 18.dp   // Chip end-icons
    val sizeMd: Dp = 22.dp   // List item icons
    val sizeLg: Dp = 28.dp   // Section headers
    val sizeXl: Dp = 36.dp   // Empty state / hero icons
    val touchTarget: Dp = 48.dp  // Minimum touch target
}
