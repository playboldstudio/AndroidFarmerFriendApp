package com.example.androidfarmerfriend.ui.screens.market

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.example.androidfarmerfriend.data.model.MarketData
import com.example.androidfarmerfriend.data.model.Crop
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.ui.components.FarmerCard
import com.example.androidfarmerfriend.ui.components.FilterChipGroup
import com.example.androidfarmerfriend.ui.components.FullScreenLoading
import com.example.androidfarmerfriend.ui.components.MarketPickerSheet
import com.example.androidfarmerfriend.ui.components.ScreenHeader
import com.example.androidfarmerfriend.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(viewModel: MarketViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    var showSearchBar by remember { mutableStateOf(false) }
    var showMarketPicker by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }

    val strings = LocalAppStrings.current

    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
    }

    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.onEvent(MarketEvent.Retry)
            isRefreshing = false
        }
    ) {

    // Market picker sheet
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        ScreenHeader(
            title = strings.marketTitle,
            subtitle = state.selectedMarket.displayName,
            isSearchActive = showSearchBar,
            onSearchClick = {
                showSearchBar = !showSearchBar
                if (!showSearchBar) viewModel.onEvent(MarketEvent.Search(""))
            },
            onLocationClick = { showMarketPicker = true }
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (showSearchBar) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onEvent(MarketEvent.Search(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(strings.searchHint, color = GrayText, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GrayText) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onEvent(MarketEvent.Search("")) }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = GrayText)
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }

        FilterChipGroup(
            filters = FilterType.entries.map { it.displayKey(strings) },
            selectedFilter = state.selectedFilter.displayKey(strings),
            onFilterSelected = { display ->
                FilterType.entries.find { it.displayKey(strings) == display }?.let {
                    viewModel.onEvent(MarketEvent.SelectFilter(it))
                }
            }
        )

        // Show Chennai-only hint for Gold filter
        if (state.selectedFilter == FilterType.GOLD) {
            Text(
                text = "${strings.gold} • Chennai only",
                style = MaterialTheme.typography.labelSmall,
                color = GrayText.copy(alpha = 0.7f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                textAlign = TextAlign.Center
            )
        }

        if (state.fetchDate.isNotEmpty()) {
            Text(
                text = "${strings.updatedAt}: ${state.fetchDate}",
                style = MaterialTheme.typography.labelSmall,
                color = GrayText,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (val cropsState = state.cropsState) {
            is UiState.Loading -> FullScreenLoading()
            is UiState.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = TrendRed, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(strings.loadError, color = GrayText, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(cropsState.message, color = GrayText, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.onEvent(MarketEvent.Retry) }, colors = ButtonDefaults.buttonColors(containerColor = FarmerGreenPrimary)) {
                        Text(strings.retry)
                    }
                }
            }
            is UiState.Success -> {
                if (state.filteredCrops.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, tint = GrayText, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            if (state.searchQuery.isNotEmpty()) {
                                Text(strings.noData, color = GrayText, style = MaterialTheme.typography.bodyLarge)
                            } else {
                                Text(
                                    text = strings.noData,
                                    color = GrayText,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${state.selectedFilter.displayKey(strings)} data not available for ${state.selectedMarket.displayName}.\nTry a different location.",
                                    color = GrayText.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 32.dp)
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
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

@Composable
fun MarketCropItem(crop: Crop, marketName: String = "") {
    val strings = LocalAppStrings.current

    FarmerCard {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = FarmerGreenPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = crop.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = crop.price,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GrayText
                    )
                    if (crop.retailPrice.isNotEmpty()) {
                        Text(
                            text = " • ${strings.retailPriceLabel}: ${crop.retailPrice}",
                            style = MaterialTheme.typography.bodySmall,
                            color = GrayText.copy(alpha = 0.7f)
                        )
                    }
                }
                if (marketName.isNotEmpty()) {
                    Text(
                        text = "📍 $marketName",
                        style = MaterialTheme.typography.labelSmall,
                        color = GrayText.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MarketScreenPreview() {
    AndroidFarmerFriendTheme {
        MarketScreen()
    }
}
