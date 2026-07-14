package com.example.androidfarmerfriend.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.androidfarmerfriend.R
import com.example.androidfarmerfriend.data.model.WeatherInfo
import com.example.androidfarmerfriend.ui.components.FarmerCard
import com.example.androidfarmerfriend.ui.components.ScreenHeader
import com.example.androidfarmerfriend.ui.navigation.Screen
import com.example.androidfarmerfriend.ui.theme.*

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val weather by viewModel.weatherState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        FarmerFriendLogo()

        Spacer(modifier = Modifier.height(12.dp))

        ScreenHeader(
            title = "வணக்கம், விவசாயி! 👋",
            subtitle = weather?.location ?: "Namakkal, Tamil Nadu",
            isHome = true,
            showSearch = false
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        when {
            isLoading -> Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = FarmerGreenPrimary, modifier = Modifier.size(32.dp))
            }
            error != null -> Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = GrayText, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("வானிலை தரவு இல்லை", color = GrayText, style = MaterialTheme.typography.bodySmall)
                }
            }
            weather != null -> WeatherSummaryCard(weather!!)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "விரைவு அணுகல்",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp),
            color = MaterialTheme.colorScheme.onBackground
        )
        
        QuickAccessGrid(onNavigate = onNavigate)
    }
}

@Composable
fun FarmerFriendLogo() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.mipmap.ic_launcher),
            contentDescription = "Farmer Friend Logo",
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = "Farmer Friend",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = FarmerGreenPrimary
            )
            Text(
                text = "விவசாயி நண்பன்",
                style = MaterialTheme.typography.bodySmall,
                color = GrayText
            )
        }
    }
}

@Composable
fun WeatherSummaryCard(weather: WeatherInfo) {
    FarmerCard {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.WbSunny, 
                    contentDescription = null, 
                    modifier = Modifier.size(44.dp),
                    tint = WeatherYellow
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = weather.temperature, 
                            style = MaterialTheme.typography.headlineMedium, 
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = weather.condition, 
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "நாளை 25° | அதிகபட்சம் 33°", 
                        style = MaterialTheme.typography.bodySmall, 
                        color = GrayText
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                WeatherStatItem(label = "மழை", value = weather.rainChance, icon = Icons.Default.WaterDrop)
                WeatherStatItem(label = "ஈரப்பதம்", value = weather.humidity, icon = Icons.Default.Opacity)
                WeatherStatItem(label = "காற்று", value = weather.windSpeed, icon = Icons.Default.Air)
            }
        }
    }
}

@Composable
fun WeatherStatItem(label: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = GrayText, fontSize = 10.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

data class QuickActionItem(val title: String, val icon: ImageVector, val color: Color, val route: String)

@Composable
fun QuickAccessGrid(onNavigate: (String) -> Unit = {}) {
    val items = listOf(
        QuickActionItem("மார்க்கெட்", Icons.Default.BarChart, FarmerGreenSecondary, Screen.Market.route),
        QuickActionItem("வானிலை", Icons.Default.WbCloudy, WeatherBlue, Screen.Weather.route),
        QuickActionItem("திட்டங்கள்", Icons.Default.LibraryBooks, SchemeLightGreen, Screen.Schemes.route),
        QuickActionItem("நோய்கள்", Icons.Default.BugReport, DiseaseOrange, Screen.Disease.route),
        QuickActionItem("அறிவிப்புகள்", Icons.Default.Notifications, AlertPurple, Screen.Alerts.route),
        QuickActionItem("பயிர் குறிப்புகள்", Icons.Default.MenuBook, CropNotesBrown, Screen.CropNotes.route)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(items) { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onNavigate(item.route) }
            ) {
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    shadowElevation = 1.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(item.icon, contentDescription = item.title, tint = item.color, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AndroidFarmerFriendTheme {
        HomeScreen()
    }
}
