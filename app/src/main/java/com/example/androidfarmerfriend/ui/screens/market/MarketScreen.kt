package com.example.androidfarmerfriend.ui.screens.market

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.data.model.Crop
import com.example.androidfarmerfriend.data.model.MarketData
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.ui.components.EmptyState
import com.example.androidfarmerfriend.ui.components.ErrorState
import com.example.androidfarmerfriend.ui.components.HeroTitle
import com.example.androidfarmerfriend.ui.components.LocPill
import com.example.androidfarmerfriend.ui.components.MarketPickerSheet
import com.example.androidfarmerfriend.ui.components.PillChipGroup
import com.example.androidfarmerfriend.ui.components.RowCard
import com.example.androidfarmerfriend.ui.components.SearchField
import com.example.androidfarmerfriend.ui.components.ShimmerList
import com.example.androidfarmerfriend.ui.components.TrendTag
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme
import com.example.androidfarmerfriend.ui.theme.FarmerSpacing
import com.example.androidfarmerfriend.ui.theme.FarmerTheme
import com.example.androidfarmerfriend.ui.theme.TrendRed

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
        MarketPickerSheet(
            markets = MarketData.marketsForCategory(state.selectedFilter),
            selectedMarket = state.selectedMarket,
            onMarketSelected = { market ->
                viewModel.onEvent(MarketEvent.ChangeMarket(market))
                showMarketPicker = false
            },
            onDismiss = { showMarketPicker = false }
        )
    }

    PullToRefreshBox(
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
                .padding(horizontal = FarmerSpacing.lg)
        ) {
            Spacer(Modifier.height(FarmerSpacing.lg))

            HeroTitle(text = strings.marketTitle, accent = strings.marketTitle.split(" ").getOrNull(1))

            Spacer(Modifier.height(FarmerSpacing.s))

            LocPill(
                text = state.selectedMarket.displayName,
                onClick = { showMarketPicker = true }
            )

            Spacer(Modifier.height(FarmerSpacing.lg))

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
                MarketInfoChip(text = "${strings.gold} · ${strings.chennai}")
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
                Spacer(Modifier.height(FarmerSpacing.s))
            }

            when (val cropsState = state.cropsState) {
                is UiState.Loading -> ShimmerList(modifier = Modifier.weight(1f))
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
                            subtitle = "${state.selectedFilter.displayKey(strings)} · ${state.selectedMarket.displayName}"
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(state.filteredCrops) { crop ->
                                MarketCropItem(crop, state.selectedMarket.displayName, strings)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MarketInfoChip(text: String) {
    Text(
        text = text,
        color = FarmerTheme.colors.primary,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .padding(vertical = 2.dp)
            .background(FarmerTheme.colors.softMint, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    )
}

/** Wholesale-to-retail spread summary; falls back to the day's price range. */
@Composable
fun MarketTrendCard(
    crops: List<Crop>,
    strings: AppStrings,
    modifier: Modifier = Modifier
) {
    val colors = FarmerTheme.colors
    val diffs = crops.mapNotNull { it.priceDiffPercent ?: it.trend.takeIf { d -> d != 0.0 } }
    val rising = diffs.any { it > 0 }
    val falling = diffs.any { it < 0 }
    val hasTrendData = diffs.isNotEmpty()

    val headline = when {
        rising && !falling -> "▲ ${strings.trendRising}"
        falling && !rising -> "▼ ${strings.trendFalling}"
        hasTrendData -> "▬ ${strings.trendStable}"
        else -> {
            val prices = crops.mapNotNull { it.priceValue }.filter { it > 0 }
            if (prices.isNotEmpty()) "₹${"%.0f".format(prices.min())} – ₹${"%.0f".format(prices.max())}" else "—"
        }
    }
    val headlineColor = when {
        rising && !falling -> colors.primary
        falling && !rising -> TrendRed
        else -> colors.textPrimary
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings.marketTrendToday.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = headline,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = headlineColor
                )
            }
            TrendDirectionIcon(rising = rising && !falling, falling = falling && !rising)
        }
    }
}

@Composable
private fun TrendDirectionIcon(rising: Boolean, falling: Boolean) {
    val colors = FarmerTheme.colors
    val tint = when {
        rising -> colors.primary
        falling -> TrendRed
        else -> colors.textTertiary
    }
    val icon = when {
        rising -> Icons.Default.TrendingUp
        falling -> Icons.Default.TrendingDown
        else -> Icons.Default.TrendingFlat
    }
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(colors.softGreen, RoundedCornerShape(15.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun MarketCropItem(crop: Crop, marketName: String, strings: AppStrings) {
    val colors = FarmerTheme.colors

    RowCard(
        title = crop.name,
        subtitle = crop.retailPrice.takeIf { it.isNotBlank() }
            ?.let { "${strings.retailPriceLabel}: ₹$it" }
            ?: marketName.takeIf { it.isNotEmpty() },
        icon = Icons.Default.ShoppingCart,
        iconTint = colors.primary,
        iconContainer = colors.softMint,
        end = {
            Column(horizontalAlignment = Alignment.End) {
                PriceText(price = crop.price)
                val trend = crop.priceDiffPercent ?: crop.trend.takeIf { it != 0.0 }
                if (trend != null && trend != 0.0) {
                    Spacer(Modifier.height(4.dp))
                    TrendTag(percent = trend)
                }
            }
        }
    )
}

/** Price with unit subscript. */
@Composable
private fun PriceText(price: String) {
    val colors = FarmerTheme.colors
    val unitMatch = Regex("^(.*?)\\s*/\\s*(.*)$").find(price)
    if (unitMatch != null) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = unitMatch.groupValues[1],
                color = colors.textPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.width(2.dp))
            Text(
                text = "/ ${unitMatch.groupValues[2]}",
                color = colors.textTertiary,
                style = MaterialTheme.typography.labelMedium
            )
        }
    } else {
        Text(
            text = price,
            color = colors.textPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MarketScreenPreview() {
    AndroidFarmerFriendTheme {
        MarketScreen()
    }
}
