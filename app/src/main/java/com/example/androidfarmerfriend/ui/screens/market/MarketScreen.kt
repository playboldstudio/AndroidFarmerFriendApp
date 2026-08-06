package com.example.androidfarmerfriend.ui.screens.market

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.data.model.Crop
import com.example.androidfarmerfriend.data.model.MarketData
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.ui.components.EmptyState
import com.example.androidfarmerfriend.ui.components.ErrorState
import com.example.androidfarmerfriend.ui.components.FullScreenLoading
import com.example.androidfarmerfriend.ui.components.HeroTitle
import com.example.androidfarmerfriend.ui.components.LocPill
import com.example.androidfarmerfriend.ui.components.MarketPickerSheet
import com.example.androidfarmerfriend.ui.components.PillChipGroup
import com.example.androidfarmerfriend.ui.components.RowCard
import com.example.androidfarmerfriend.ui.components.SearchField
import com.example.androidfarmerfriend.ui.components.TrendTag
import com.example.androidfarmerfriend.ui.theme.FarmerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(viewModel: MarketViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    var showMarketPicker by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    val strings = LocalAppStrings.current
    val colors = FarmerTheme.colors

    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
    }

    if (showMarketPicker) {
        val marketsForFilter = MarketData.marketsForCategory(state.selectedFilter)
        MarketPickerSheet(
            markets = marketsForFilter,
            selectedMarket = state.selectedMarket,
            onMarketSelected = { market ->
                viewModel.onEvent(MarketEvent.ChangeMarket(market))
                showMarketPicker = false
            },
            onDismiss = { showMarketPicker = false }
        )
    }

    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.onEvent(MarketEvent.Retry)
            isRefreshing = false
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            HeroTitle(text = strings.marketTitle, accent = strings.marketTitle.split(" ").getOrNull(1))

            Spacer(modifier = Modifier.height(8.dp))

            LocPill(
                text = state.selectedMarket.displayName,
                onClick = { showMarketPicker = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SearchField(
                value = state.searchQuery,
                onValueChange = { viewModel.onEvent(MarketEvent.Search(it)) },
                placeholder = strings.searchHint
            )

            PillChipGroup(
                filters = FilterType.entries.map { it.displayKey(strings) },
                selectedFilter = state.selectedFilter.displayKey(strings),
                onFilterSelected = { display ->
                    FilterType.entries.find { it.displayKey(strings) == display }?.let {
                        viewModel.onEvent(MarketEvent.SelectFilter(it))
                    }
                }
            )

            if (state.selectedFilter == FilterType.GOLD) {
                Text(
                    text = "${strings.gold} • Chennai only",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    textAlign = TextAlign.Center
                )
            }

            val crops = state.filteredCrops
            if (crops.isNotEmpty()) {
                MarketTrendCard(
                    crops = crops,
                    strings = strings,
                    modifier = Modifier.padding(bottom = 14.dp)
                )
            }

            if (state.fetchDate.isNotEmpty()) {
                Text(
                    text = "${strings.updatedAt}: ${state.fetchDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            when (val cropsState = state.cropsState) {
                is UiState.Loading -> FullScreenLoading(modifier = Modifier.weight(1f))
                is UiState.Error -> ErrorState(
                    message = cropsState.message.ifBlank { strings.loadError },
                    onRetry = { viewModel.onEvent(MarketEvent.Retry) },
                    modifier = Modifier.weight(1f)
                )
                is UiState.Success -> {
                    if (state.filteredCrops.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.SearchOff,
                            title = strings.noData,
                            subtitle = "${state.selectedFilter.displayKey(strings)} data not available for ${state.selectedMarket.displayName}.\nTry a different location."
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(state.filteredCrops) { crop ->
                                MarketCropItem(crop, state.selectedMarket.displayName)
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Green trend summary card — matches the "MARKET TREND TODAY" card in market.html. */
@Composable
fun MarketTrendCard(
    crops: List<Crop>,
    strings: com.example.androidfarmerfriend.data.localization.AppStrings,
    modifier: Modifier = Modifier
) {
    val colors = FarmerTheme.colors
    val diffs = crops.mapNotNull { it.priceDiffPercent ?: it.trend.takeIf { d -> d != 0.0 } }
    val rising = diffs.any { it > 0 }
    val falling = diffs.any { it < 0 }
    // No trend data available → show the price range instead.
    val hasTrendData = diffs.isNotEmpty()
    val (label, accent, icon) = when {
        rising && !falling -> Triple(strings.trendRising, colors.primary, "▲")
        falling && !rising -> Triple(strings.trendFalling, TrendDownColor, "▼")
        hasTrendData -> Triple(strings.trendStable, colors.textSecondary, "▬")
        else -> {
            val prices = crops.mapNotNull { it.priceValue }.filter { it > 0 }
            if (prices.isNotEmpty()) {
                Triple(
                    "₹${"%.0f".format(prices.min())} – ₹${"%.0f".format(prices.max())}",
                    colors.primary,
                    "💰"
                )
            } else {
                Triple("—", colors.textSecondary, "💰")
            }
        }
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = strings.marketTrendToday,
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (icon == "💰") label else "$icon $label",
                    color = accent,
                    fontSize = if (icon == "💰") 16.sp else 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                text = "🌾",
                fontSize = 30.sp
            )
        }
    }
}

private val TrendDownColor = com.example.androidfarmerfriend.ui.theme.TrendRed

@Composable
fun MarketCropItem(crop: Crop, marketName: String = "") {
    val colors = FarmerTheme.colors

    RowCard(
        title = crop.name,
        subtitle = if (marketName.isNotEmpty()) "📍 $marketName" else null,
        icon = Icons.Default.ShoppingCart,
        iconTint = colors.primary,
        iconContainer = colors.softMint,
        end = {
            Column(horizontalAlignment = Alignment.End) {
                PriceText(price = crop.price)
                val trend = crop.priceDiffPercent ?: crop.trend.takeIf { it != 0.0 }
                if (trend != null && trend != 0.0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    TrendTag(percent = trend)
                }
            }
        }
    )
}

/** Price with unit subscript — matches `.price` in the HTML. */
@Composable
private fun PriceText(price: String) {
    val colors = FarmerTheme.colors
    val unitMatch = Regex("^(.*?)\\s*/\\s*(.*)$").find(price)
    if (unitMatch != null) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = unitMatch.groupValues[1],
                color = colors.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.3).sp
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "/ ${unitMatch.groupValues[2]}",
                color = colors.textTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    } else {
        Text(
            text = price,
            color = colors.textPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MarketScreenPreview() {
    com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme {
        MarketScreen()
    }
}
