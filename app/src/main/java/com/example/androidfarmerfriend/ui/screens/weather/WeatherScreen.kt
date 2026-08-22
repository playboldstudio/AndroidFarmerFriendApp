package com.example.androidfarmerfriend.ui.screens.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.data.location.LocationPrefs
import com.example.androidfarmerfriend.data.model.ForecastDay
import com.example.androidfarmerfriend.data.model.WeatherInfo
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.ui.components.DayPill
import com.example.androidfarmerfriend.ui.components.EmptyState
import com.example.androidfarmerfriend.ui.components.ErrorState
import com.example.androidfarmerfriend.ui.components.FarmTipCard
import com.example.androidfarmerfriend.ui.components.FullScreenLoading
import com.example.androidfarmerfriend.ui.components.HeroTitle
import com.example.androidfarmerfriend.ui.components.LocationPickerSheet
import com.example.androidfarmerfriend.ui.components.LocPill
import com.example.androidfarmerfriend.ui.components.MenuRow
import com.example.androidfarmerfriend.ui.components.SectionTitle
import com.example.androidfarmerfriend.ui.components.WeatherHeroCard
import com.example.androidfarmerfriend.ui.theme.FarmerTheme

@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val locationPrefs = remember { LocationPrefs(context) }
    var selectedLocation by remember { mutableStateOf(locationPrefs.selectedLocation) }
    var showLocationPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.setStrings(strings)
        viewModel.onEvent(WeatherEvent.LoadLocation(selectedLocation))
    }

    LaunchedEffect(strings) {
        viewModel.setStrings(strings)
    }

    val repository = remember { FarmerRepository() }

    if (showLocationPicker) {
        LocationPickerSheet(
            currentLocation = selectedLocation,
            onLocationSelected = { loc ->
                selectedLocation = loc
                locationPrefs.selectedLocation = loc
                showLocationPicker = false
                viewModel.onEvent(WeatherEvent.LoadLocation(loc))
            },
            onSearch = { query -> repository.searchLocations(query) },
            onDismiss = { showLocationPicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FarmerTheme.colors.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        HeroTitle(text = strings.weatherTitle)

        Spacer(modifier = Modifier.height(8.dp))

        LocPill(
            text = selectedLocation.name,
            onClick = { showLocationPicker = true }
        )

        when (val weatherState = state.weatherState) {
            is UiState.Loading -> FullScreenLoading(modifier = Modifier.padding(top = 48.dp))
            is UiState.Error -> ErrorState(
                message = weatherState.message.ifBlank { strings.weatherLoadError },
                onRetry = { viewModel.onEvent(WeatherEvent.Retry) },
                modifier = Modifier.padding(top = 32.dp)
            )
            is UiState.Success -> WeatherDetailedView(weather = weatherState.data, strings = strings)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun WeatherDetailedView(weather: WeatherInfo, strings: AppStrings = AppStrings.English) {
    val colors = FarmerTheme.colors

    WeatherHeroCard(
        weather = weather,
        strings = strings,
        centered = true,
        modifier = Modifier.padding(top = 16.dp)
    )

    if (weather.forecast.isNotEmpty()) {
        SectionTitle(title = strings.today)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            weather.forecast.forEachIndexed { index, day ->
                DayPill(
                    day = day.day,
                    weatherCode = day.weatherCode,
                    temp = day.maxTemp,
                    selected = index == 0
                )
            }
        }
    }

    SectionTitle(title = "Details")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp)) {
            WeatherDetailRow(
                icon = Icons.Default.WaterDrop,
                iconContainer = colors.softBlue,
                iconTint = colors.weatherBlue,
                label = strings.rainChance,
                value = weather.rainChance
            )
            WeatherDetailRow(
                icon = Icons.Default.Opacity,
                iconContainer = colors.softMint,
                iconTint = colors.primary,
                label = strings.humidity,
                value = weather.humidity
            )
            WeatherDetailRow(
                icon = Icons.Default.Air,
                iconContainer = colors.softLavender,
                iconTint = colors.alertPurple,
                label = strings.windSpeed,
                value = weather.windSpeed
            )
            WeatherDetailRow(
                icon = Icons.Default.Explore,
                iconContainer = colors.softOrange,
                iconTint = colors.diseaseOrange,
                label = strings.windDirection,
                value = weather.windDirection
            )
        }
    }

    Spacer(modifier = Modifier.height(14.dp))

    FarmTipCard(title = strings.farmTipTitle, body = strings.farmTipGeneric)
}

@Composable
fun WeatherDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconContainer: androidx.compose.ui.graphics.Color,
    iconTint: androidx.compose.ui.graphics.Color,
    label: String,
    value: String
) {
    val colors = FarmerTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Surface(
            modifier = Modifier.size(36.dp),
            shape = RoundedCornerShape(12.dp),
            color = iconContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            color = colors.textSecondary,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = colors.textPrimary,
            fontSize = 14.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme {
        WeatherScreen()
    }
}
