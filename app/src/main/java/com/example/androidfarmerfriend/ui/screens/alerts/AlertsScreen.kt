package com.example.androidfarmerfriend.ui.screens.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
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
import com.example.androidfarmerfriend.ui.components.ChipOption
import com.example.androidfarmerfriend.ui.components.CrossfadeUiState
import com.example.androidfarmerfriend.ui.components.EmptyState
import com.example.androidfarmerfriend.ui.components.ErrorState
import com.example.androidfarmerfriend.ui.components.HeroTitle
import com.example.androidfarmerfriend.ui.components.PillChipGroup
import com.example.androidfarmerfriend.ui.components.ShimmerList
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme
import com.example.androidfarmerfriend.ui.theme.FarmerSpacing
import com.example.androidfarmerfriend.ui.theme.FarmerTheme
import com.example.androidfarmerfriend.util.relativeTimeShort
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    viewModel: AlertsViewModel = viewModel(),
    onNavigateToAlert: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = LocalAppStrings.current
    val colors = FarmerTheme.colors

    // Honest pull-to-refresh: the spinner stays up until the realtime
    // listener actually delivers fresh data.
    var awaitingRefresh by remember { mutableStateOf(false) }
    LaunchedEffect(state.alertsState) {
        if (awaitingRefresh && state.alertsState is UiState.Success) {
            awaitingRefresh = false
        }
    }

    com.example.androidfarmerfriend.ui.components.CenteredMaxWidth(
        modifier = Modifier.background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = FarmerSpacing.lg)
        ) {
            Spacer(Modifier.height(FarmerSpacing.lg))

            // Title + unread badge + overflow menu on one row (no location row).
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HeroTitle(
                    text = strings.alertsTitle,
                    modifier = Modifier.weight(1f)
                )
                if (state.unreadCount > 0) {
                    UnreadBadge(count = state.unreadCount)
                    Spacer(Modifier.width(4.dp))
                }
                // Mark-all-read lives in an overflow dropdown, not inline.
                var menuExpanded by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = strings.markAllRead,
                            tint = colors.primary
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = strings.markAllRead,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.DoneAll, contentDescription = null)
                            },
                            onClick = {
                                viewModel.onEvent(AlertEvent.MarkAllRead)
                                menuExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(2.dp))

            PillChipGroup(
                filters = AlertFilterType.entries.map { ChipOption(it.displayKey(strings), it.icon()) },
                selectedFilter = state.selectedFilter.displayKey(strings),
                onFilterSelected = { option ->
                    AlertFilterType.entries.find { it.displayKey(strings) == option.label }?.let {
                        viewModel.onEvent(AlertEvent.SelectFilter(it))
                    }
                }
            )

            PullToRefreshBox(
                isRefreshing = awaitingRefresh,
                onRefresh = {
                    awaitingRefresh = true
                    viewModel.onEvent(AlertEvent.Refresh)
                }
            ) {
                CrossfadeUiState(state = state.alertsState) { alertState ->
                    when (alertState) {
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
                                    // Group alerts by recency: Today / Yesterday / Earlier.
                                    val grouped = groupAlertsByTime(state.filteredAlerts)
                                    grouped.forEach { (groupKey, alerts) ->
                                        item(key = "header_$groupKey") {
                                            SectionLabel(text = when (groupKey) {
                                                "today" -> strings.today
                                                "yesterday" -> strings.yesterday
                                                else -> strings.earlierLabel
                                            })
                                        }
                                        items(alerts, key = { it.id }) { alert ->
                                            SwipeableAlertItem(
                                                alert = alert,
                                                strings = strings,
                                                onMarkRead = { viewModel.onEvent(AlertEvent.MarkRead(alert.id)) },
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
        }
    }
}

@Composable
private fun UnreadBadge(count: Int) {
    val strings = LocalAppStrings.current
    Text(
        text = String.format(strings.unreadCountBadge, count),
        color = Color.White,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(FarmerTheme.colors.alertRed)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

private fun AlertFilterType.icon(): ImageVector = when (this) {
    AlertFilterType.ALL -> Icons.Default.Notifications
    AlertFilterType.PRICE -> Icons.Default.Paid
    AlertFilterType.WEATHER -> Icons.Default.WbCloudy
    AlertFilterType.CROP -> Icons.Default.Eco
}

private data class AlertVisuals(
    val icon: ImageVector,
    val tint: Color,
    val container: Color,
    val label: String
)

/** Rough severity derived from alert content, so users can scan for danger fast. */
internal enum class AlertPriority { CRITICAL, WARNING, INFO }

/**
 * Classifies an alert's severity by scanning its title/message for risk-laden
 * keywords. Content-driven because the backend sends no severity field. The
 * classifier stays deliberately conservative: only strong signals escalate, and
 * everything else reads as informational.
 */
internal fun classifyAlertPriority(alert: Alert): AlertPriority {
    val text = "${alert.title} ${alert.message}".lowercase()
    val critical = listOf(
        "severe", "extreme", "emergency", "danger", "warning", "flood",
        "cyclone", "storm", "heavy rain", "heatwave", "heat wave", "critical",
        "urgent"
    )
    val warning = listOf(
        "alert", "advisory", "high", "risk", "watch", "moderate",
        "price drop", "price rise", "increase", "decrease"
    )
    return when {
        critical.any { text.contains(it) } -> AlertPriority.CRITICAL
        warning.any { text.contains(it) } -> AlertPriority.WARNING
        else -> AlertPriority.INFO
    }
}

@Composable
fun AlertItem(alert: Alert, strings: AppStrings, onClick: () -> Unit = {}) {
    val colors = FarmerTheme.colors
    val (iconVec, iconTint, iconContainer, label) = when (alert.type) {
        AlertType.PRICE -> AlertVisuals(Icons.Default.Paid, colors.alertGreen, colors.softGreen, strings.priceAlertLabel)
        AlertType.WEATHER -> AlertVisuals(Icons.Default.WbCloudy, colors.alertBlue, colors.softBlue, strings.weatherAlertLabel)
        AlertType.CROP -> AlertVisuals(Icons.Default.Eco, colors.alertPurple, colors.softPurple, strings.cropAlertLabel)
    }

    // Severity drives the left bar + a labeled chip (color AND text/icon, so
    // severity is never signalled by color alone — a11y).
    val priority = classifyAlertPriority(alert)
    val (severityTint, severityContainer, severityLabel, severityIcon) = when (priority) {
        AlertPriority.CRITICAL -> Quad(colors.alertRed, colors.softRed, strings.severityCritical, Icons.Default.Warning)
        AlertPriority.WARNING -> Quad(colors.diseaseOrange, colors.softOrange, strings.severityWarning, Icons.Default.WarningAmber)
        AlertPriority.INFO -> Quad(colors.textSecondary, colors.surfaceMuted, strings.severityInfo, Icons.Default.Info)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surface)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(if (alert.isRead) colors.outline else severityTint)
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
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
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
                    maxLines = 3,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AlertChip(text = label, color = iconTint, container = iconContainer)
                        // Severity chip: icon + localized label so it reads by
                        // text, not just color (non-color-only signaling).
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(severityContainer)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                severityIcon,
                                contentDescription = null,
                                tint = severityTint,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = severityLabel,
                                color = severityTint,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                    Text(
                        text = relativeTimeShort(alert.timestamp),
                        color = colors.textTertiary,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/** Small local tuple wrapper to keep the severity `when` readable. */
private data class Quad(
    val tint: Color,
    val container: Color,
    val label: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableAlertItem(
    alert: Alert,
    strings: AppStrings,
    onMarkRead: () -> Unit,
    onClick: () -> Unit
) {
    // Only unread alerts are swipeable — swiping marks them read.
    if (!alert.isRead) {
        val dismissState = rememberSwipeToDismissBoxState()
        val haptics = LocalHapticFeedback.current
        // The state settles to EndToStart once the dismiss threshold is crossed.
        LaunchedEffect(dismissState.currentValue) {
            if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                onMarkRead()
            }
        }
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true,
            backgroundContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(FarmerTheme.colors.softGreen),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = FarmerTheme.colors.alertGreen,
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .size(24.dp)
                    )
                }
            }
        ) {
            AlertItem(alert = alert, strings = strings, onClick = onClick)
        }
    } else {
        AlertItem(alert = alert, strings = strings, onClick = onClick)
    }
}

/** Small section header used between time-grouped alert rows. */
@Composable
private fun SectionLabel(text: String) {
    val colors = FarmerTheme.colors
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = colors.textSecondary,
        maxLines = 1,
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
    )
}

/**
 * Buckets alerts into Today / Yesterday / Earlier groups based on their
 * timestamp, preserving newest-first order within each group.
 */
private fun groupAlertsByTime(alerts: List<Alert>): LinkedHashMap<String, List<Alert>> {
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val yesterdayStart = Calendar.getInstance().apply {
        add(Calendar.DATE, -1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val result = LinkedHashMap<String, List<Alert>>()
    alerts.sortedByDescending { it.timestamp }.forEach { alert ->
        val group = when {
            alert.timestamp >= todayStart.timeInMillis -> "today"
            alert.timestamp >= yesterdayStart.timeInMillis -> "yesterday"
            else -> "earlier"
        }
        result.merge(group, listOf(alert)) { a, b -> a + b }
    }
    return result
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
