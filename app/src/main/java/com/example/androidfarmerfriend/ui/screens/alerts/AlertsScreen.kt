package com.example.androidfarmerfriend.ui.screens.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.androidfarmerfriend.ui.components.FarmerCard
import com.example.androidfarmerfriend.ui.components.FilterChipGroup
import com.example.androidfarmerfriend.ui.components.ScreenHeader
import com.example.androidfarmerfriend.ui.theme.*
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
    var isRefreshing by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        ScreenHeader(title = strings.alertsTitle)

        FilterChipGroup(
            filters = AlertFilterType.entries.map { it.displayKey(strings) },
            selectedFilter = state.selectedFilter.displayKey(strings),
            onFilterSelected = { display ->
                AlertFilterType.entries.find { it.displayKey(strings) == display }?.let {
                    viewModel.onEvent(AlertEvent.SelectFilter(it))
                }
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.onEvent(AlertEvent.Refresh)
                isRefreshing = false
            }
        ) {
            when (val alertState = state.alertsState) {
                is UiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = FarmerGreenPrimary)
                }
                is UiState.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = TrendRed, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(strings.alertsLoadError, color = GrayText, style = MaterialTheme.typography.bodyLarge)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { viewModel.onEvent(AlertEvent.Refresh) }) {
                            Text(strings.retry)
                        }
                    }
                }
                is UiState.Success -> {
                    if (state.filteredAlerts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = GrayText, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(strings.noAlerts, color = GrayText, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
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
    val (icon, color, label) = when (alert.type) {
        AlertType.PRICE -> Triple(Icons.Default.TrendingUp, AlertRed, strings.priceAlertLabel)
        AlertType.WEATHER -> Triple(Icons.Default.WbCloudy, AlertBlue, strings.weatherAlertLabel)
        AlertType.CROP -> Triple(Icons.Default.Notifications, AlertGreen, strings.cropAlertLabel)
    }

    val bgColor = if (!alert.isRead) color.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface

    FarmerCard(
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box {
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    color = color.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                    }
                }
                if (!alert.isRead) {
                    Surface(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .align(Alignment.TopEnd)
                            .offset(x = 4.dp, y = (-4).dp),
                        color = AlertRed
                    ) {}
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = color.copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = label,
                            color = color,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    if (alert.title.isNotEmpty()) {
                        Text(
                            text = alert.title,
                            style = MaterialTheme.typography.labelSmall,
                            color = GrayText,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp,
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTimestamp(alert.timestamp, strings),
                        style = MaterialTheme.typography.labelSmall,
                        color = GrayText
                    )
                    if (alert.location.isNotEmpty()) {
                        Text(
                            text = "📍 ${alert.location}",
                            style = MaterialTheme.typography.labelSmall,
                            color = GrayText.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

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
    AndroidFarmerFriendTheme {
        AlertsScreen()
    }
}
