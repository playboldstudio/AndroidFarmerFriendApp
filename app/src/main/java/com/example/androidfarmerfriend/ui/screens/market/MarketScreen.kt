package com.example.androidfarmerfriend.ui.screens.market

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingFlat
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.data.model.Crop
import com.example.androidfarmerfriend.data.model.MarketData
import coil.compose.SubcomposeAsyncImage
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.ui.auth.AuthBridge
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
import com.example.androidfarmerfriend.util.RateCardRenderer
import com.example.androidfarmerfriend.util.RateCardSharer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(viewModel: MarketViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    var showMarketPicker by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val strings = LocalAppStrings.current
    val colors = FarmerTheme.colors
    val context = androidx.compose.ui.platform.LocalContext.current
    val requestSignIn = AuthBridge.LocalRequestSignIn.current

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

            Row(verticalAlignment = Alignment.CenterVertically) {
                HeroTitle(
                    text = strings.marketTitle,
                    accent = strings.marketTitle.split(" ").getOrNull(1),
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (state.fetchDate.isNotEmpty()) {
                    Spacer(Modifier.width(FarmerSpacing.s))
                    FreshnessChip(text = state.fetchDate.uppercase())
                }
            }

            Spacer(Modifier.height(FarmerSpacing.s))

            Row(verticalAlignment = Alignment.CenterVertically) {
                LocPill(
                    text = state.selectedMarket.displayName,
                    onClick = { showMarketPicker = true }
                )
                Spacer(Modifier.weight(1f))
                ShareRateCardButton(
                    onClick = {
                        requestSignIn {
                            scope.launch {
                                val bitmap = withContext(Dispatchers.Default) {
                                    RateCardRenderer.render(
                                        context = context,
                                        title = strings.marketTitle,
                                        dateLabel = state.fetchDate,
                                        marketName = state.selectedMarket.displayName,
                                        filterLabel = state.selectedFilter.displayKey(strings),
                                        crops = state.filteredCrops
                                    )
                                }
                                RateCardSharer.share(context, bitmap)
                            }
                        }
                    },
                    contentDescription = strings.shareAction
                )
            }

            Spacer(Modifier.height(FarmerSpacing.lg))

            SearchField(
                value = state.searchQuery,
                onValueChange = { viewModel.onEvent(MarketEvent.Search(it)) },
                placeholder = strings.searchHint
            )

            PillChipGroup(
                filters = FilterType.entries.map { "${it.emoji()} ${it.displayKey(strings)}" },
                selectedFilter = state.selectedFilter.displayKey(strings).let { "${state.selectedFilter.emoji()} $it" },
                onFilterSelected = { display ->
                    FilterType.entries.find { "${it.emoji()} ${it.displayKey(strings)}" == display }?.let {
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
                    filter = state.selectedFilter,
                    strings = strings,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
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
private fun ShareRateCardButton(onClick: () -> Unit, contentDescription: String) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(FarmerTheme.colors.softGreen, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Default.Share,
            contentDescription = contentDescription,
            tint = FarmerTheme.colors.primary,
            modifier = Modifier.size(22.dp)
        )
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

/** Compact single-line summary: today's price range (or NECC rate) for the filter. */
@Composable
fun MarketTrendCard(
    crops: List<Crop>,
    filter: FilterType,
    strings: AppStrings,
    modifier: Modifier = Modifier
) {
    val colors = FarmerTheme.colors
    val prices = crops.mapNotNull { it.priceValue }.filter { it > 0 }
    val unit = crops.firstOrNull()?.units ?: "kg"
    val value = if (prices.isNotEmpty()) {
        "₹${"%.0f".format(prices.min())} – ₹${"%.0f".format(prices.max())} / $unit"
    } else {
        "—"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(colors.softGreen, RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = filter.emoji(), fontSize = 15.sp)
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = strings.marketTrendToday.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary
                )
            }
            Spacer(Modifier.weight(1f))
            Text(text = filter.emoji(), fontSize = 20.sp)
        }
    }
}

private fun FilterType.emoji(): String = when (this) {
    FilterType.VEGETABLES -> "🥬"
    FilterType.FRUITS -> "🍎"
    FilterType.NONVEG -> "🍗"
    FilterType.GOLD -> "🥇"
    FilterType.EGG -> "🥚"
}

@Composable
private fun FreshnessChip(text: String) {
    Text(
        text = "📅 $text",
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = FarmerTheme.colors.primary,
        modifier = Modifier
            .background(FarmerTheme.colors.softMint, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
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
        leading = { CropThumbnail(crop) },
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

/** Product photo when available; falls back to the category icon tile. */
@Composable
private fun CropThumbnail(crop: Crop) {
    val colors = FarmerTheme.colors
    if (crop.imageUrl.isBlank()) {
        Icon(
            Icons.Default.ShoppingCart,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier
                .size(46.dp)
                .background(colors.softMint, RoundedCornerShape(15.dp))
                .padding(12.dp)
        )
        return
    }
    SubcomposeAsyncImage(
        model = crop.imageUrl,
        contentDescription = crop.nameEng.ifBlank { crop.name },
        modifier = Modifier
            .size(46.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(colors.surfaceMuted),
        loading = {
            Box(Modifier.matchParentSize().background(colors.softMint))
        },
        error = {
            Icon(
                Icons.Default.ShoppingCart,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.matchParentSize().padding(12.dp)
            )
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
