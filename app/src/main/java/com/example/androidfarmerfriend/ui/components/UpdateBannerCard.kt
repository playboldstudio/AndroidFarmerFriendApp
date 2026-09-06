package com.example.androidfarmerfriend.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme
import com.example.androidfarmerfriend.ui.theme.FarmerSpacing
import com.example.androidfarmerfriend.ui.theme.FarmerTheme
import com.example.androidfarmerfriend.util.UpdateBanner

/**
 * Modern in-app update card. Surfaces the three flexible-update states
 * (available → downloading → ready-to-install) in a polished, gradient-led
 * dialog shell. Rendered centered by MainActivity.
 */
@Composable
fun UpdateBannerCard(
    banner: UpdateBanner,
    strings: AppStrings,
    onAction: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FarmerTheme.colors

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = colors.surface,
        shadowElevation = 12.dp
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            // Gradient hero header with the state icon.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(colors.primaryDeep, colors.primary)
                        )
                    )
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // White halo ring around the icon badge.
                    Box(
                        modifier = Modifier
                            .size(84.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center
                        ) {
                            val (headerIcon, headerTint) = when (banner) {
                                UpdateBanner.Available -> Icons.Default.SystemUpdate to Color.White
                                UpdateBanner.Downloading -> Icons.Default.Download to Color.White
                                UpdateBanner.ReadyToInstall -> Icons.Default.Check to Color.White
                            }
                            Icon(
                                headerIcon,
                                contentDescription = null,
                                tint = headerTint,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(FarmerSpacing.md))
                    Text(
                        text = when (banner) {
                            UpdateBanner.Available -> strings.updateAvailableTitle
                            UpdateBanner.Downloading -> strings.updateDownloadingTitle
                            UpdateBanner.ReadyToInstall -> strings.updateReadyTitle
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
                if (banner != UpdateBanner.ReadyToInstall) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .clickable(onClick = onDismiss)
                            .padding(8.dp)
                            .size(16.dp)
                    )
                }
            }

            // Body content area.
            Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp)) {
                when (banner) {
                    UpdateBanner.Available -> {
                        Text(
                            text = strings.updateAvailableBody,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(FarmerSpacing.xl))
                        UpdateDialogButtons(
                            primaryLabel = strings.actionAllow,
                            secondaryLabel = strings.cancelAction,
                            onPrimary = onAction,
                            onSecondary = onDismiss
                        )
                    }

                    UpdateBanner.Downloading -> {
                        DownloadingProgress()
                    }

                    UpdateBanner.ReadyToInstall -> {
                        Text(
                            text = strings.updateRestartBody,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(FarmerSpacing.xl))
                        UpdateDialogButtons(
                            primaryLabel = strings.updateRestartAction,
                            secondaryLabel = null,
                            onPrimary = onAction,
                            onSecondary = onDismiss
                        )
                    }
                }
            }
        }
    }
}

/** Animated indeterminate progress ring + helper caption. */
@Composable
private fun DownloadingProgress() {
    val colors = FarmerTheme.colors
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(56.dp),
                color = colors.primary,
                trackColor = colors.softGreen,
                strokeWidth = 5.dp
            )
            Spacer(Modifier.height(FarmerSpacing.lg))
            Text(
                text = "Please wait…",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary
            )
        }
    }
}

/** Primary + optional secondary CTA row, full-width primary button. */
@Composable
private fun UpdateDialogButtons(
    primaryLabel: String,
    secondaryLabel: String?,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit
) {
    val colors = FarmerTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(FarmerSpacing.s)) {
        // Primary
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(listOf(colors.primary, colors.primaryBright)))
                .clickable(onClick = onPrimary)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = primaryLabel,
                color = colors.onPrimary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
        if (secondaryLabel != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, colors.outline, RoundedCornerShape(16.dp))
                    .clickable(onClick = onSecondary)
                    .padding(vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = secondaryLabel,
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Previews for the three flexible-update states.
@Preview(showBackground = true, widthDp = 360)
@Composable
private fun UpdateBannerCardAvailablePreview() {
    AndroidFarmerFriendTheme {
        UpdateBannerCard(
            banner = UpdateBanner.Available,
            strings = AppStrings.English,
            onAction = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun UpdateBannerCardDownloadingPreview() {
    AndroidFarmerFriendTheme {
        UpdateBannerCard(
            banner = UpdateBanner.Downloading,
            strings = AppStrings.English,
            onAction = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun UpdateBannerCardReadyPreview() {
    AndroidFarmerFriendTheme {
        UpdateBannerCard(
            banner = UpdateBanner.ReadyToInstall,
            strings = AppStrings.English,
            onAction = {},
            onDismiss = {}
        )
    }
}
