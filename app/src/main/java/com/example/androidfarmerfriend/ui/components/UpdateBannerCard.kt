package com.example.androidfarmerfriend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.ui.theme.FarmerSpacing
import com.example.androidfarmerfriend.ui.theme.FarmerTheme
import com.example.androidfarmerfriend.util.UpdateBanner

/**
 * In-app update banner: available → "Update now", downloading → progress,
 * downloaded → "Restart". Rendered above tab content by MainScreen.
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
        shape = RoundedCornerShape(16.dp),
        color = colors.softGreen,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.softMint)
    ) {
        Column(modifier = Modifier.padding(start = 14.dp, top = 10.dp, bottom = 10.dp, end = 6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.SystemUpdate,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(FarmerSpacing.s))
                Text(
                    text = when (banner) {
                        UpdateBanner.Available -> strings.updateAvailableTitle
                        UpdateBanner.Downloading -> strings.updateDownloadingTitle
                        UpdateBanner.ReadyToInstall -> strings.updateReadyTitle
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.textPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (banner != UpdateBanner.ReadyToInstall) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        tint = colors.textTertiary,
                        modifier = Modifier
                            .size(18.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(onClick = onDismiss)
                            .padding(3.dp)
                    )
                }
            }
            if (banner == UpdateBanner.Downloading) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.9f),
                    color = colors.primary,
                    trackColor = colors.surface
                )
            } else {
                Text(
                    text = when (banner) {
                        UpdateBanner.Available -> strings.updateAvailableBody
                        UpdateBanner.Downloading -> ""
                        UpdateBanner.ReadyToInstall -> strings.updateRestartBody
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
                if (banner == UpdateBanner.Available || banner == UpdateBanner.ReadyToInstall) {
                    Box(
                        modifier = Modifier
                            .padding(top = FarmerSpacing.s)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.primary)
                            .clickable(onClick = onAction)
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                        , contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (banner == UpdateBanner.Available) strings.actionAllow else strings.updateRestartAction,
                            color = colors.onPrimary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}
