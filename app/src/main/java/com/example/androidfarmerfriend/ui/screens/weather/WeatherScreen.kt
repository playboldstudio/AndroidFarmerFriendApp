package com.example.androidfarmerfriend.ui.screens.weather

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.data.model.ForecastDay
import com.example.androidfarmerfriend.data.model.WeatherInfo
import com.example.androidfarmerfriend.ui.components.ScreenHeader
import com.example.androidfarmerfriend.ui.components.weatherIconFor
import com.example.androidfarmerfriend.ui.screens.home.HomeViewModel
import com.example.androidfarmerfriend.ui.theme.*

@Composable
fun WeatherScreen(viewModel: HomeViewModel = viewModel()) {
    val weather by viewModel.weatherState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        ScreenHeader(
            title = "வானிலை",
            subtitle = weather?.location ?: "Namakkal, Tamil Nadu",
            showSearch = false
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        weather?.let {
            WeatherDetailedView(it)
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
