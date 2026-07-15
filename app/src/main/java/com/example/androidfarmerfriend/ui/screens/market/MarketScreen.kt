package com.example.androidfarmerfriend.ui.screens.market

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.data.location.LocationPrefs
import com.example.androidfarmerfriend.data.model.Crop
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.ui.components.FarmerCard
import com.example.androidfarmerfriend.ui.components.FilterChipGroup
import com.example.androidfarmerfriend.ui.components.LocationPickerSheet
import com.example.androidfarmerfriend.ui.components.ScreenHeader
import com.example.androidfarmerfriend.ui.theme.*

@Composable
fun MarketScreen(viewModel: MarketViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    var showSearchBar by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val locationPrefs = remember { LocationPrefs(context) }
    var selectedLocation by remember { mutableStateOf(locationPrefs.selectedLocation) }
    var showLocationPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onEvent(MarketEvent.ChangeLocation(locationPrefs.selectedLocation.marketName))
    }

    val repository = remember { FarmerRepository() }

    if (showLocationPicker) {
        LocationPickerSheet(
            currentLocation = selectedLocation,
            onLocationSelected = { loc ->
                selectedLocation = loc
                locationPrefs.selectedLocation = loc
                showLocationPicker = false
                viewModel.onEvent(MarketEvent.ChangeLocation(loc.marketName))
            },
            onSearch = { query -> repository.searchLocations(query) },
            onDismiss = { showLocationPicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        ScreenHeader(
            title = "மார்க்கெட் விலை",
            subtitle = selectedLocation.name,
            isSearchActive = showSearchBar,
            onSearchClick = {
                showSearchBar = !showSearchBar
                if (!showSearchBar) viewModel.onEvent(MarketEvent.Search(""))
            },
            onLocationClick = { showLocationPicker = true }
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (showSearchBar) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onEvent(MarketEvent.Search(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("பொருளைத் தேடவும்", color = GrayText, fontSize = 14.sp) },
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
            filters = listOf("காய்கறிகள்", "பழங்கள்", "இறைச்சி", "தங்கம்", "முட்டை"),
            selectedFilter = state.selectedFilter,
            onFilterSelected = { viewModel.onEvent(MarketEvent.SelectFilter(it)) }
        )

        if (state.fetchDate.isNotEmpty()) {
            Text(
                text = "புதுப்பிக்கப்பட்டது: ${state.fetchDate}",
                style = MaterialTheme.typography.labelSmall,
                color = GrayText,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (val cropsState = state.cropsState) {
            is UiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = FarmerGreenPrimary)
            }
            is UiState.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = TrendRed, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("தரவுகளை ஏற்ற முடியவில்லை", color = GrayText, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(cropsState.message, color = GrayText, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.onEvent(MarketEvent.Retry) }, colors = ButtonDefaults.buttonColors(containerColor = FarmerGreenPrimary)) {
                        Text("மீண்டும் முயற்சிக்க")
                    }
                }
            }
            is UiState.Success -> {
                if (state.filteredCrops.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, tint = GrayText, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("தரவுகள் ஏதுமில்லை", color = GrayText, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(state.filteredCrops) { crop ->
                            MarketCropItem(crop)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarketCropItem(crop: Crop) {
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
                Text(
                    text = crop.price,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayText
                )
            }

            MarketTrendIndicator(crop.trend)
        }
    }
}

@Composable
fun MarketTrendIndicator(trend: Double) {
    if (trend == 0.0) {
        Text(
            text = "0%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = GrayText
        )
        return
    }
    val isPositive = trend > 0
    val color = if (isPositive) TrendGreen else TrendRed
    val icon = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
    val sign = if (isPositive) "+" else ""

    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = "$sign$trend%",
            color = color,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
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
