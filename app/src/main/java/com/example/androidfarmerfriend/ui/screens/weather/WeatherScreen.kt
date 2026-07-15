package com.example.androidfarmerfriend.ui.screens.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.data.location.LocationPrefs
import com.example.androidfarmerfriend.data.model.ForecastDay
import com.example.androidfarmerfriend.data.model.WeatherInfo
import com.example.androidfarmerfriend.data.repository.FarmerRepository
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.ui.components.LocationPickerSheet
import com.example.androidfarmerfriend.ui.components.ScreenHeader
import com.example.androidfarmerfriend.ui.components.weatherIconFor
import com.example.androidfarmerfriend.ui.theme.*

@Composable
fun WeatherScreen(viewModel: WeatherViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    val context = LocalContext.current
    val locationPrefs = remember { LocationPrefs(context) }
    var selectedLocation by remember { mutableStateOf(locationPrefs.selectedLocation) }
    var showLocationPicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.onEvent(WeatherEvent.LoadLocation(selectedLocation))
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
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        ScreenHeader(
            title = "வானிலை",
            subtitle = selectedLocation.name,
            showSearch = false,
            onLocationClick = { showLocationPicker = true }
        )

        Spacer(modifier = Modifier.height(8.dp))

        when (val weatherState = state.weatherState) {
            is UiState.Loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = FarmerGreenPrimary)
            }
            is UiState.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = GrayText, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("வானிலை தரவுகளை ஏற்ற முடியவில்லை", color = GrayText, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(weatherState.message, color = GrayText, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.onEvent(WeatherEvent.Retry) }, colors = ButtonDefaults.buttonColors(containerColor = FarmerGreenPrimary)) {
                        Text("மீண்டும் முயற்சிக்க")
                    }
                }
            }
            is UiState.Success -> WeatherDetailedView(weatherState.data)
        }
    }
}

@Composable
fun WeatherDetailedView(weather: WeatherInfo) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Icon(
            weatherIconFor(weather.weatherCode),
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = WeatherYellow
        )
        Text(
            text = weather.temperature,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 48.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = weather.condition,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (weather.todayLow.isNotEmpty() && weather.todayHigh.isNotEmpty()) {
            Text(
                text = "குறைந்தபட்சம் ${weather.todayLow} | அதிகபட்சம் ${weather.todayHigh}",
                style = MaterialTheme.typography.bodyMedium,
                color = GrayText
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (weather.forecast.isNotEmpty()) {
            WeatherForecastRow(weather.forecast)
            Spacer(modifier = Modifier.height(32.dp))
        }

        WeatherDetailsList(weather)
    }
}

@Composable
fun WeatherForecastRow(forecast: List<ForecastDay>) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        forecast.forEach { item ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = item.day, style = MaterialTheme.typography.labelMedium, color = GrayText)
                Spacer(modifier = Modifier.height(8.dp))
                Icon(
                    weatherIconFor(item.weatherCode),
                    contentDescription = null,
                    tint = WeatherBlue,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${item.minTemp}/${item.maxTemp}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun WeatherDetailsList(weather: WeatherInfo) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        WeatherDetailRow(icon = Icons.Default.WaterDrop, label = "மழை வாய்ப்பு", value = weather.rainChance)
        WeatherDetailRow(icon = Icons.Default.Opacity, label = "ஈரப்பதம்", value = weather.humidity)
        WeatherDetailRow(icon = Icons.Default.Air, label = "காற்றின் வேகம்", value = weather.windSpeed)
        WeatherDetailRow(icon = Icons.Default.Explore, label = "காற்றின் திசை", value = weather.windDirection)
    }
}

@Composable
fun WeatherDetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = WeatherBlue.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherScreenPreview() {
    AndroidFarmerFriendTheme {
        WeatherScreen()
    }
}
