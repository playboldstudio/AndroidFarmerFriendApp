package com.example.androidfarmerfriend.ui.screens.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.data.model.WeatherInfo
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.ui.components.DayPill
import com.example.androidfarmerfriend.ui.components.EmptyState
import com.example.androidfarmerfriend.ui.components.ErrorState
import com.example.androidfarmerfriend.ui.components.FarmTipCard
import com.example.androidfarmerfriend.ui.components.HeroTitle
import com.example.androidfarmerfriend.ui.components.LocationPickerSheet
import com.example.androidfarmerfriend.ui.components.LocPill
import com.example.androidfarmerfriend.ui.components.SectionTitle
import com.example.androidfarmerfriend.ui.components.ShimmerList
import com.example.androidfarmerfriend.ui.components.TintIconCircle
import com.example.androidfarmerfriend.ui.components.WeatherHeroCard
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme
import com.example.androidfarmerfriend.ui.theme.FarmerSpacing
import com.example.androidfarmerfriend.ui.theme.FarmerTheme

@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    val strings = LocalAppStrings.current
    var showLocationPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.setStrings(strings)
        viewModel.onEvent(WeatherEvent.LoadInitial)
    }

    LaunchedEffect(strings) {
        viewModel.setStrings(strings)
    }

    val currentLocation = state.selectedLocation
    if (showLocationPicker && currentLocation != null) {
        LocationPickerSheet(
            currentLocation = currentLocation,
            onLocationSelected = { loc ->
                showLocationPicker = false
                viewModel.onEvent(WeatherEvent.SelectLocation(loc))
            },
            onSearch = { query -> viewModel.searchLocations(query) },
            onDismiss = { showLocationPicker = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FarmerTheme.colors.background)
            .padding(horizontal = FarmerSpacing.lg)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(FarmerSpacing.lg))

        HeroTitle(text = strings.weatherTitle)

        Spacer(Modifier.height(FarmerSpacing.s))

        LocPill(
            text = state.selectedLocation?.name.orEmpty(),
            onClick = { showLocationPicker = true }
        )

        when (val weatherState = state.weatherState) {
            is UiState.Loading -> ShimmerList(rowCount = 2, rowHeight = 190.dp, modifier = Modifier.padding(top = 16.dp))
            is UiState.Error -> ErrorState(
                message = weatherState.message.ifBlank { strings.weatherLoadError },
                onRetry = { viewModel.onEvent(WeatherEvent.Retry) },
                modifier = Modifier.padding(top = 32.dp)
            )
            is UiState.Success -> WeatherDetailedView(weather = weatherState.data, strings = strings)
        }

        Spacer(Modifier.height(FarmerSpacing.xxl))
    }
}

@Composable
fun WeatherDetailedView(weather: WeatherInfo, strings: AppStrings = AppStrings.English) {
    val colors = FarmerTheme.colors

    WeatherHeroCard(
        weather = weather,
        strings = strings,
        centered = true,
        modifier = Modifier.padding(top = FarmerSpacing.lg)
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
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WeatherStatTile(
                    icon = Icons.Default.WaterDrop,
                    iconContainer = colors.softBlue,
                    iconTint = colors.weatherBlue,
                    label = strings.rainChance,
                    value = weather.rainChance,
                    modifier = Modifier.weight(1f)
                )
                WeatherStatTile(
                    icon = Icons.Default.Opacity,
                    iconContainer = colors.softMint,
                    iconTint = colors.primary,
                    label = strings.humidity,
                    value = weather.humidity,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WeatherStatTile(
                    icon = Icons.Default.Air,
                    iconContainer = colors.softLavender,
                    iconTint = colors.alertPurple,
                    label = strings.windSpeed,
                    value = weather.windSpeed,
                    modifier = Modifier.weight(1f)
                )
                WeatherStatTile(
                    icon = Icons.Default.Explore,
                    iconContainer = colors.softOrange,
                    iconTint = colors.diseaseOrange,
                    label = strings.windDirection,
                    value = weather.windDirection,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    Spacer(Modifier.height(14.dp))

    FarmTipCard(title = strings.farmTipTitle, body = strings.farmTipGeneric)
}

@Composable
private fun WeatherStatTile(
    icon: ImageVector,
    iconContainer: Color,
    iconTint: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val colors = FarmerTheme.colors
    Row(
        modifier = modifier
            .background(colors.surfaceMuted, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TintIconCircle(
            icon = icon,
            tint = iconTint,
            container = iconContainer,
            size = 36.dp,
            cornerRadius = 12.dp
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = colors.textSecondary,
                maxLines = 1
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    AndroidFarmerFriendTheme {
        WeatherScreen()
    }
}
