package com.example.androidfarmerfriend.ui.screens.market

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.data.model.Crop
import com.example.androidfarmerfriend.ui.components.FarmerCard
import com.example.androidfarmerfriend.ui.components.FilterChipGroup
import com.example.androidfarmerfriend.ui.components.ScreenHeader
import com.example.androidfarmerfriend.ui.theme.*

@Composable
fun MarketScreen(viewModel: MarketViewModel = viewModel()) {
    val crops by viewModel.cropsState.collectAsState()
    var selectedCategory by remember { mutableStateOf("காய்கறிகள்") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        ScreenHeader(
            title = "மார்க்கெட் விலை",
            showSearch = true
        )
        
        Text(
            text = "Koyambedu Market, Chennai",
            style = MaterialTheme.typography.bodyMedium,
            color = GrayText
        )
        Text(
            text = "24 மே, 2025",
            style = MaterialTheme.typography.bodySmall,
            color = GrayText,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        FilterChipGroup(
            filters = listOf("காய்கறிகள்", "பழங்கள்", "தானியங்கள்"),
            selectedFilter = selectedCategory,
            onFilterSelected = { selectedCategory = it }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(crops) { crop ->
                MarketCropItem(crop)
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
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) { }
            
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
    val isPositive = trend >= 0
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
