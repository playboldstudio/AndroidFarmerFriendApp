package com.example.androidfarmerfriend.ui.screens.market

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlinx.coroutines.withContext
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.data.model.Crop
import com.example.androidfarmerfriend.data.model.MarketData
import coil.compose.SubcomposeAsyncImage
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.ui.components.ChipOption
import com.example.androidfarmerfriend.ui.components.EmptyState
import com.example.androidfarmerfriend.ui.components.ErrorState
import com.example.androidfarmerfriend.ui.components.HeroTitle
import com.example.androidfarmerfriend.ui.components.LocPill
import com.example.androidfarmerfriend.ui.components.MarketPickerSheet
import com.example.androidfarmerfriend.ui.components.OfflineState
import com.example.androidfarmerfriend.ui.components.PillChipGroup
import com.example.androidfarmerfriend.ui.components.RowCard
import com.example.androidfarmerfriend.ui.components.SearchField
import com.example.androidfarmerfriend.ui.components.ShimmerList
import com.example.androidfarmerfriend.ui.components.SlowNetworkState
import com.example.androidfarmerfriend.ui.components.TrendTag
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme
import com.example.androidfarmerfriend.ui.theme.FarmerSpacing
import com.example.androidfarmerfriend.ui.theme.FarmerTheme
import com.example.androidfarmerfriend.util.NetworkUtil
import com.example.androidfarmerfriend.util.RateCardRenderer
import com.example.androidfarmerfriend.util.RateCardSharer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(viewModel: MarketViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showMarketPicker by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }
    // Honest pull-to-refresh: spinner tracks the real UiState, not a
    // synchronously-set-and-cleared flag.
    val isRefreshing = state.cropsState is UiState.Loading
    val scope = rememberCoroutineScope()

    // Gmail-style FAB: it slides down out of the way while the list scrolls
    // down and resurfaces when the user scrolls back up. Tracked from the
    // LazyColumn's own scroll state so it reacts to real scrolling.
    val listState = rememberLazyListState()
    var fabHidden by remember { mutableStateOf(false) }
    var prevScroll by remember { mutableStateOf(0) }
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                val now = index * 10000 + offset
                val delta = now - prevScroll
                if (delta > 0 && index > 0) fabHidden = true
                else if (delta < 0) fabHidden = false
                prevScroll = now
            }
    }

    val strings = LocalAppStrings.current
    val colors = FarmerTheme.colors
    val context = androidx.compose.ui.platform.LocalContext.current

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

    val shareRateCard: () -> Unit = {
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
            RateCardSharer.share(
                context, bitmap,
                marketName = state.selectedMarket.displayName,
                dateLabel = state.fetchDate
            )
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.onEvent(MarketEvent.Retry) }
    ) {
        Box(modifier = Modifier.fillMaxSize().imePadding()) {
            com.example.androidfarmerfriend.ui.components.CenteredMaxWidth(
                modifier = Modifier.background(colors.background)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = FarmerSpacing.lg)
                ) {
                    Spacer(Modifier.height(FarmerSpacing.lg))

                    val titleAccent = remember(strings.marketTitle) {
                        strings.marketTitle.split(" ").getOrNull(1)
                    }
                    HeroTitle(
                        text = strings.marketTitle,
                        accent = titleAccent
                    )

                    Spacer(Modifier.height(FarmerSpacing.s))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LocPill(
                            text = state.selectedMarket.displayName,
                            onClick = { showMarketPicker = true },
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (state.fetchDate.isNotEmpty()) {
                            Spacer(Modifier.width(FarmerSpacing.s))
                            FreshnessChip(text = state.fetchDate.uppercase())
                        }
                    }

                    Spacer(Modifier.height(FarmerSpacing.lg))

                    SearchField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.onEvent(MarketEvent.Search(it)) },
                        placeholder = strings.searchHint
                    )

                    // Sort control
                    var sortMenuExpanded by remember { mutableStateOf(false) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            IconButton(onClick = { sortMenuExpanded = true }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = "Sort",
                                    tint = FarmerTheme.colors.textSecondary
                                )
                            }
                            DropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false }
                            ) {
                                SortOrder.entries.forEach { order ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = order.label(strings),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = if (order == state.sortBy) FontWeight.Bold else FontWeight.Normal,
                                                color = if (order == state.sortBy) FarmerTheme.colors.primary else FarmerTheme.colors.textPrimary
                                            )
                                        },
                                        onClick = {
                                            viewModel.onEvent(MarketEvent.Sort(order))
                                            sortMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        Text(
                            text = state.sortBy.label(strings),
                            style = MaterialTheme.typography.labelMedium,
                            color = FarmerTheme.colors.textSecondary
                        )
                    }

                    PillChipGroup(
                        filters = FilterType.entries.map { ChipOption(it.displayKey(strings), it.icon()) },
                        selectedFilter = state.selectedFilter.displayKey(strings),
                        onFilterSelected = { option ->
                            FilterType.entries.find { it.displayKey(strings) == option.label }?.let {
                                viewModel.onEvent(MarketEvent.SelectFilter(it))
                            }
                        }
                    )

                    if (state.selectedFilter == FilterType.GOLD) {
                        MarketInfoChip(text = "${strings.gold} · ${strings.chennai}")
                    }

                    when (val cropsState = state.cropsState) {
                        is UiState.Loading -> SlowNetworkState(
                            title = strings.slowNetworkTitle,
                            body = strings.slowNetworkBody,
                            modifier = Modifier.weight(1f)
                        )
                        // When there's no connection the generic error is misleading —
                        // say explicitly that we're offline and let retry re-check.
                        is UiState.Error -> if (!NetworkUtil.isOnline(context)) {
                            OfflineState(
                                title = strings.offlineTitle,
                                body = strings.offlineBody,
                                onRetry = { viewModel.onEvent(MarketEvent.Retry) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            ErrorState(
                                message = cropsState.message.ifBlank { strings.loadError },
                                onRetry = { viewModel.onEvent(MarketEvent.Retry) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        is UiState.Success -> {
                            if (state.filteredCrops.isEmpty()) {
                                EmptyState(
                                    icon = Icons.Default.SearchOff,
                                    title = strings.noData,
                                    subtitle = "${state.selectedFilter.displayKey(strings)} · ${state.selectedMarket.displayName}"
                                )
                            } else {
                                LazyColumn(
                                    state = listState,
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(bottom = 16.dp)
                                ) {
                                    items(state.filteredCrops, key = { it.id }) { crop ->
                                        MarketCropItem(crop, state.selectedMarket.displayName, strings)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            val fabOffsetY by animateFloatAsState(
                targetValue = if (fabHidden) 240f else 0f,
                label = "fabOffset",
                animationSpec = tween(durationMillis = 220)
            )
            FloatingActionButton(
                onClick = shareRateCard,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(FarmerSpacing.lg)
                    .offset { IntOffset(0, fabOffsetY.roundToInt()) }
                    .alpha(if (fabHidden) 0f else 1f),
                containerColor = colors.primary,
                contentColor = colors.onPrimary
            ) {
                Icon(Icons.Default.Share, contentDescription = strings.shareAction)
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
        maxLines = 1,
        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
        modifier = Modifier
            .padding(vertical = 2.dp)
            .background(FarmerTheme.colors.softMint, RoundedCornerShape(999.dp))
            .padding(horizontal = 12.dp, vertical = 5.dp)
    )
}

private fun FilterType.icon(): ImageVector = when (this) {
    FilterType.VEGETABLES -> Icons.Default.Eco
    FilterType.FRUITS -> Icons.Default.ShoppingBasket
    FilterType.NONVEG -> Icons.Default.Restaurant
    FilterType.GOLD -> Icons.Default.Diamond
    FilterType.EGG -> Icons.Default.Egg
}

@Composable
private fun FreshnessChip(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(FarmerTheme.colors.softMint, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Icon(
            Icons.Default.Event,
            contentDescription = null,
            tint = FarmerTheme.colors.primary,
            modifier = Modifier.size(13.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = FarmerTheme.colors.primary,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MarketCropItem(crop: Crop, marketName: String, strings: AppStrings) {
    val colors = FarmerTheme.colors
    // Category-aware icon + tint + container so the RowCard chip
    // visually matches the CropThumbnail in the leading slot.
    val (cardIcon, cardTint, cardContainer) = when (crop.category) {
        "vegetable" -> Triple(Icons.Default.Eco, colors.categoryVegetable, colors.softGreen)
        "fruit"     -> Triple(Icons.Default.ShoppingBasket, colors.categoryFruit, colors.softOrange)
        "nonveg"    -> Triple(Icons.Default.Restaurant, colors.categoryNonVeg, colors.softRed)
        "gold"      -> Triple(Icons.Default.Diamond, colors.categoryGold, colors.softLavender)
        "egg"       -> Triple(Icons.Default.Egg, colors.categoryEgg, colors.softBrown)
        else        -> Triple(Icons.Default.ShoppingCart, colors.textSecondary, colors.surfaceMuted)
    }

    RowCard(
        title = crop.name,
        subtitle = crop.retailPrice.takeIf { it.isNotBlank() }
            ?.let { "${strings.retailPriceLabel}: ₹$it" }
            ?: marketName.takeIf { it.isNotEmpty() },
        icon = cardIcon,
        iconTint = cardTint,
        iconContainer = cardContainer,
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

/** Product photo when available; falls back to a category-aware icon tile. */
@Composable
private fun CropThumbnail(crop: Crop) {
    val colors = FarmerTheme.colors
    if (crop.imageUrl.isBlank()) {
        // Category-aware fallback: distinct icon + tint + soft background
        // per category so Fruits/NonVeg/Gold/Egg don't all look like a
        // placeholder (only vegetable items carry API images).
        val (icon, tint, container) = when (crop.category) {
            "vegetable" -> Triple(Icons.Default.Eco, colors.categoryVegetable, colors.softGreen)
            "fruit"     -> Triple(Icons.Default.ShoppingBasket, colors.categoryFruit, colors.softOrange)
            "nonveg"    -> Triple(Icons.Default.Restaurant, colors.categoryNonVeg, colors.softRed)
            "gold"      -> Triple(Icons.Default.Diamond, colors.categoryGold, colors.softLavender)
            "egg"       -> Triple(Icons.Default.Egg, colors.categoryEgg, colors.softBrown)
            else        -> Triple(Icons.Default.ShoppingCart, colors.textSecondary, colors.surfaceMuted)
        }
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(container),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
        }
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
            val (icon, tint, container) = when (crop.category) {
                "vegetable" -> Triple(Icons.Default.Eco, colors.categoryVegetable, colors.softGreen)
                "fruit"     -> Triple(Icons.Default.ShoppingBasket, colors.categoryFruit, colors.softOrange)
                "nonveg"    -> Triple(Icons.Default.Restaurant, colors.categoryNonVeg, colors.softRed)
                "gold"      -> Triple(Icons.Default.Diamond, colors.categoryGold, colors.softLavender)
                "egg"       -> Triple(Icons.Default.Egg, colors.categoryEgg, colors.softBrown)
                else        -> Triple(Icons.Default.ShoppingCart, colors.textSecondary, colors.surfaceMuted)
            }
            Box(Modifier.matchParentSize().background(container), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            }
        }
    )
}

/** Price with unit subscript. */
/** Compiled once — avoids per-item per-recomposition Regex allocation in the market list. */
private val PRICE_UNIT_REGEX = Regex("^(.*?)\\s*/\\s*(.*)$")

@Composable
private fun PriceText(price: String) {
    val colors = FarmerTheme.colors
    val unitMatch = PRICE_UNIT_REGEX.find(price)
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
