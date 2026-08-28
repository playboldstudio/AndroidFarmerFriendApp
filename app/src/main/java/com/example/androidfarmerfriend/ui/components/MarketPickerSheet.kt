package com.example.androidfarmerfriend.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.data.model.MarketGroup
import com.example.androidfarmerfriend.data.model.MarketOption
import com.example.androidfarmerfriend.ui.screens.market.FilterType
import com.example.androidfarmerfriend.ui.theme.FarmerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketPickerSheet(
    markets: List<MarketOption>,
    selectedMarket: MarketOption,
    onMarketSelected: (MarketOption) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalAppStrings.current
    var searchQuery by remember { mutableStateOf("") }

    val filteredMarkets = remember(markets, searchQuery) {
        if (searchQuery.isBlank()) markets
        else markets.filter {
            it.displayName.contains(searchQuery, ignoreCase = true) ||
                    it.apiSlug.contains(searchQuery, ignoreCase = true)
        }
    }

    val majorMarkets = filteredMarkets.filter { it.group == MarketGroup.MAJOR_MARKETS }
    val states = filteredMarkets.filter { it.group == MarketGroup.STATES }
    val cities = filteredMarkets.filter { it.group == MarketGroup.CITIES }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.selectMarket,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(strings.searchMarkets, color = FarmerTheme.colors.textTertiary, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FarmerTheme.colors.textTertiary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = FarmerTheme.colors.textTertiary)
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = FarmerTheme.colors.outline,
                    focusedBorderColor = FarmerTheme.colors.primary,
                    unfocusedContainerColor = FarmerTheme.colors.surface,
                    focusedContainerColor = FarmerTheme.colors.surface,
                    unfocusedTextColor = FarmerTheme.colors.textPrimary,
                    focusedTextColor = FarmerTheme.colors.textPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Market list
            LazyColumn(
                modifier = Modifier.heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Major Markets section
                if (majorMarkets.isNotEmpty()) {
                    item {
                        SectionHeader(
                            icon = Icons.Default.Star,
                            title = strings.majorMarkets
                        )
                    }
                    items(majorMarkets, key = { it.apiSlug }) { market ->
                        MarketItem(
                            market = market,
                            isSelected = market.apiSlug == selectedMarket.apiSlug,
                            onClick = {
                                onMarketSelected(market)
                                onDismiss()
                            }
                        )
                    }
                }

                // States section
                if (states.isNotEmpty()) {
                    item {
                        SectionHeader(
                            icon = Icons.Default.LocationOn,
                            title = strings.allStates
                        )
                    }
                    items(states, key = { it.apiSlug }) { market ->
                        MarketItem(
                            market = market,
                            isSelected = market.apiSlug == selectedMarket.apiSlug,
                            onClick = {
                                onMarketSelected(market)
                                onDismiss()
                            }
                        )
                    }
                }

                // Cities section
                if (cities.isNotEmpty()) {
                    item {
                        SectionHeader(
                            icon = Icons.Default.LocationCity,
                            title = strings.allCities
                        )
                    }
                    items(cities, key = { it.apiSlug }) { market ->
                        MarketItem(
                            market = market,
                            isSelected = market.apiSlug == selectedMarket.apiSlug,
                            onClick = {
                                onMarketSelected(market)
                                onDismiss()
                            }
                        )
                    }
                }

                // Empty state
                if (filteredMarkets.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = strings.noResults,
                                color = FarmerTheme.colors.textSecondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = FarmerTheme.colors.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = FarmerTheme.colors.primary
        )
    }
}

@Composable
private fun MarketItem(
    market: MarketOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val strings = LocalAppStrings.current
    val colors = FarmerTheme.colors
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (isSelected) colors.softGreen else colors.surface,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = market.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) FarmerTheme.colors.primary
                    else FarmerTheme.colors.textPrimary
                )
                // Show supported categories as small text (built once per market/language).
                val categories = remember(market, strings) {
                    buildList {
                        if (market.supportsVegetables) add(strings.vegetables)
                        if (market.supportsFruits) add(strings.fruits)
                        if (market.supportsNonVeg) add(strings.nonVeg)
                        if (market.supportsCategory(FilterType.GOLD)) add(strings.gold)
                        if (market.supportsCategory(FilterType.EGG)) add(strings.egg)
                    }
                }
                if (categories.isNotEmpty()) {
                    Text(
                        text = categories.joinToString(" • "),
                        style = MaterialTheme.typography.labelSmall,
                        color = FarmerTheme.colors.textTertiary,
                        fontSize = 11.sp
                    )
                }
            }

            if (isSelected) {
                Surface(
                    modifier = Modifier.size(24.dp),
                    shape = CircleShape,
                    color = FarmerTheme.colors.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = FarmerTheme.colors.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
