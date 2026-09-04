package com.example.androidfarmerfriend.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.androidfarmerfriend.ui.theme.FarmerTheme

/**
 * Pulsing placeholder list shown while data loads.
 *
 * Default render mirrors a row-card list (leading tile + headline/body lines)
 * so the skeleton reads as "content about to appear" rather than an empty bar.
 * A plain-block variant ([ShimmerBlockList]) is available for hero/weather rows.
 */
@Composable
fun ShimmerList(
    rowCount: Int = 6,
    rowHeight: Dp = 72.dp,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(vertical = 8.dp),
    rowSpacing: Dp = 10.dp
) {
    val alpha = rememberShimmerAlpha()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(rowSpacing)
    ) {
        repeat(rowCount) {
            ShimmerRowCard(
                rowHeight = rowHeight,
                alpha = alpha
            )
        }
    }
}

/** Plain full-width pulsing bars for hero/weather blocks (one big rounded card). */
@Composable
fun ShimmerBlockList(
    rowCount: Int = 1,
    blockHeight: Dp = 140.dp,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(vertical = 8.dp),
    rowSpacing: Dp = 10.dp
) {
    val alpha = rememberShimmerAlpha()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(rowSpacing)
    ) {
        repeat(rowCount) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(blockHeight)
                    .alpha(alpha)
                    .clip(RoundedCornerShape(20.dp))
                    .background(FarmerTheme.colors.surfaceMuted)
            )
        }
    }
}

/** Shared infinite pulse, extracted so both variants animate in sync. */
@Composable
private fun rememberShimmerAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 650, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )
    return alpha
}

/** A row-shaped skeleton: leading rounded tile + two text bars, like RowCard. */
@Composable
private fun ShimmerRowCard(rowHeight: Dp, alpha: Float) {
    val colors = FarmerTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .alpha(alpha)
                .clip(RoundedCornerShape(15.dp))
                .background(colors.surfaceMuted)
        )
        Spacer(Modifier.width(13.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .alpha(alpha),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Headline bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(colors.surfaceMuted)
            )
            // Body bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(colors.surfaceMuted)
            )
        }
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(12.dp)
                .alpha(alpha)
                .clip(RoundedCornerShape(6.dp))
                .background(colors.surfaceMuted)
        )
    }
}
