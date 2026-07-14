package com.example.androidfarmerfriend.ui.screens.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.data.model.Alert
import com.example.androidfarmerfriend.data.model.AlertType
import com.example.androidfarmerfriend.ui.components.FarmerCard
import com.example.androidfarmerfriend.ui.components.FilterChipGroup
import com.example.androidfarmerfriend.ui.components.ScreenHeader
import com.example.androidfarmerfriend.ui.theme.*

@Composable
fun AlertsScreen(viewModel: AlertsViewModel = viewModel()) {
    val alerts by viewModel.alertsState.collectAsState()
    var selectedFilter by remember { mutableStateOf("அனைத்து") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        ScreenHeader(title = "அலர்ட்கள்")
        
        FilterChipGroup(
            filters = listOf("அனைத்து", "விலை அலர்ட்", "வானிலை", "பயிர்"),
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(alerts) { alert ->
                AlertItem(alert)
            }
            
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "பழைய அலர்ட்களைப் பார்க்கவும்", 
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun AlertItem(alert: Alert) {
    val (icon, color, label) = when (alert.type) {
        AlertType.PRICE -> Triple(Icons.Default.TrendingUp, AlertRed, "விலை அலர்ட்")
        AlertType.WEATHER -> Triple(Icons.Default.WbCloudy, AlertBlue, "வானிலை அலர்ட்")
        AlertType.CROP -> Triple(Icons.Default.Notifications, AlertGreen, "பயிர் அலர்ட்")
    }

    FarmerCard {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                color = color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Surface(
                    color = color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = label, 
                        style = MaterialTheme.typography.labelSmall, 
                        color = color, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alert.title, 
                    style = MaterialTheme.typography.bodyLarge, 
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = alert.message, 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
                Text(
                    text = alert.time, 
                    style = MaterialTheme.typography.labelSmall, 
                    color = GrayText,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlertsScreenPreview() {
    AndroidFarmerFriendTheme {
        AlertsScreen()
    }
}
