package com.example.androidfarmerfriend.ui.screens.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.data.location.LocationPrefs
import com.example.androidfarmerfriend.data.model.Alert
import com.example.androidfarmerfriend.data.model.AlertType
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.ui.components.AlertChip
import com.example.androidfarmerfriend.ui.components.EmptyState
import com.example.androidfarmerfriend.ui.components.ErrorState
import com.example.androidfarmerfriend.ui.components.FullScreenLoading
import com.example.androidfarmerfriend.ui.components.HeroTitle
import com.example.androidfarmerfriend.ui.components.LocPill
import com.example.androidfarmerfriend.ui.components.PillChipGroup
import com.example.androidfarmerfriend.ui.theme.FarmerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val context = LocalContext.current
    val locationPrefs = remember { LocationPrefs(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Title with unread count badge
        val unreadCount = (state.alertsState as? UiState.Success)
            ?.data?.count { !it.isRead } ?: 0
        Row(verticalAlignment = Alignment.CenterVertically) {
            HeroTitle(text = strings.alertsTitle, modifier = Modifier.weight(1f, fill = false))
            if (unreadCount > 0) {
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "$unreadCount new",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(colors.alertRed)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LocPill(text = locationPrefs.selectedLocation.name)

        PillChipGroup(
            filters = AlertFilterType.entries.map { it.displayKey(strings) },
            selectedFilter = state.selectedFilter.displayKey(strings),
            onFilterSelected = { display ->
                AlertFilterType.entries.find { it.displayKey(strings) == display }?.let {
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
                is UiState.Loading -> FullScreenLoading(modifier = Modifier.padding(top = 48.dp))
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
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .then(
                if (!alert.isRead) Modifier.border(1.5.dp, colors.alertRed, RoundedCornerShape(20.dp))
                else Modifier
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(iconContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconVec, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            if (!alert.isRead) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(colors.alertRed)
                )
            }
        }
        Spacer(modifier = Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = alert.title.ifBlank { label },
                color = colors.textPrimary,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = alert.message,
                color = colors.textSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AlertChip(text = label, color = iconTint, container = iconContainer)
                Text(
                    text = formatTimestamp(alert.timestamp, strings),
                    color = colors.textTertiary,
                    fontSize = 10.sp
                )
            }
        }
    }
}

/** Visual descriptors for an alert row. */
private data class AlertVisuals(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val tint: Color,
    val container: Color,
    val label: String
)

private fun formatTimestamp(timestamp: Long, strings: AppStrings): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = diff / 60000
    val hours = diff / 3600000
    val days = diff / 86400000

    return when {
        minutes < 1 -> strings.timeJustNow
        minutes < 60 -> strings.timeMinutesAgo.format(minutes)
        hours < 24 -> strings.timeHoursAgo.format(hours)
        days < 7 -> strings.timeDaysAgo.format(days)
        else -> {
            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlertsScreenPreview() {
    com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme {
        AlertsScreen()
    }
}
