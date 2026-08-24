package com.example.androidfarmerfriend.ui.screens.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.data.model.Alert
import com.example.androidfarmerfriend.data.model.AlertType
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.ui.components.AlertChip
import com.example.androidfarmerfriend.ui.components.EmptyState
import com.example.androidfarmerfriend.ui.components.ErrorState
import com.example.androidfarmerfriend.ui.components.HeroTitle
import com.example.androidfarmerfriend.ui.components.LocPill
import com.example.androidfarmerfriend.ui.components.PillChipGroup
import com.example.androidfarmerfriend.ui.components.ShimmerList
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme
import com.example.androidfarmerfriend.ui.theme.FarmerSpacing
import com.example.androidfarmerfriend.ui.theme.FarmerTheme
import com.example.androidfarmerfriend.util.relativeTimeLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel = viewModel(),
    onNavigateToAlert: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val strings = LocalAppStrings.current
    val colors = FarmerTheme.colors
    var isRefreshing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = FarmerSpacing.lg)
    ) {
        Spacer(Modifier.height(FarmerSpacing.lg))

        Row(verticalAlignment = Alignment.CenterVertically) {
            HeroTitle(text = strings.alertsTitle, modifier = Modifier.weight(1f, fill = false))
            if (state.unreadCount > 0) {
                Spacer(Modifier.width(10.dp))
                UnreadBadge(count = state.unreadCount)
            }
        }

        Spacer(Modifier.height(FarmerSpacing.s))

        LocPill(text = state.locationName)

        PillChipGroup(
            filters = AlertFilterType.entries.map { "${it.emoji()} ${it.displayKey(strings)}" },
            selectedFilter = "${state.selectedFilter.emoji()} ${state.selectedFilter.displayKey(strings)}",
            onFilterSelected = { display ->
                AlertFilterType.entries.find { "${it.emoji()} ${it.displayKey(strings)}" == display }?.let {
                    viewModel.onEvent(AlertEvent.SelectFilter(it))
                }
            }
        )

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.onEvent(AlertEvent.Refresh)
                isRefreshing = false
            }
        ) {
            when (val alertState = state.alertsState) {
                is UiState.Loading -> ShimmerList(rowCount = 4, rowHeight = 88.dp)
                is UiState.Error -> ErrorState(
                    message = alertState.message.ifBlank { strings.alertsLoadError },
                    onRetry = { viewModel.onEvent(AlertEvent.Refresh) },
                    modifier = Modifier.padding(top = 32.dp)
                )
                is UiState.Success -> {
                    if (state.filteredAlerts.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.Notifications,
                            title = strings.noAlerts
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(state.filteredAlerts) { alert ->
                                AlertItem(
                                    alert = alert,
                                    strings = strings,
                                    onClick = {
                                        viewModel.onEvent(AlertEvent.MarkRead(alert.id))
                                        if (alert.actionRoute.isNotEmpty()) {
                                            onNavigateToAlert(alert.actionRoute)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UnreadBadge(count: Int) {
    Text(
        text = "$count new",
        color = Color.White,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(FarmerTheme.colors.alertRed)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

/** Emoji per filter chip. */
private fun AlertFilterType.emoji(): String = when (this) {
    AlertFilterType.ALL -> "🔔"
    AlertFilterType.PRICE -> "💰"
    AlertFilterType.WEATHER -> "🌦️"
    AlertFilterType.CROP -> "🌾"
}

private data class AlertVisuals(
    val icon: ImageVector,
    val tint: Color,
    val container: Color,
    val label: String
)

@Composable
fun AlertItem(alert: Alert, strings: AppStrings, onClick: () -> Unit = {}) {
    val colors = FarmerTheme.colors
    val (iconVec, iconTint, iconContainer, label) = when (alert.type) {
        AlertType.PRICE -> AlertVisuals(Icons.Default.TrendingUp, colors.alertGreen, colors.softGreen, strings.priceAlertLabel)
        AlertType.WEATHER -> AlertVisuals(Icons.Default.WbCloudy, colors.alertBlue, colors.softBlue, strings.weatherAlertLabel)
        AlertType.CROP -> AlertVisuals(Icons.Default.Notifications, colors.alertPurple, colors.softPurple, strings.cropAlertLabel)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(if (alert.isRead) colors.outline else iconTint)
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            TintedIconTile(iconVec, iconTint, iconContainer)
            Spacer(Modifier.width(13.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = alert.title.ifBlank { label },
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (!alert.isRead) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(colors.alertRed)
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = alert.message,
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    lineHeight = 17.sp,
                    maxLines = 3
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AlertChip(text = label, color = iconTint, container = iconContainer)
                    Text(
                        text = relativeTimeLabel(alert.timestamp, strings),
                        color = colors.textTertiary,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun TintedIconTile(icon: ImageVector, tint: Color, container: Color) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun AlertsScreenPreview() {
    AndroidFarmerFriendTheme {
        AlertsScreen()
    }
}
