package com.example.androidfarmerfriend.ui.screens.schemes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.data.model.Scheme
import com.example.androidfarmerfriend.ui.components.FarmerCard
import com.example.androidfarmerfriend.ui.components.FilterChipGroup
import com.example.androidfarmerfriend.ui.components.ScreenHeader
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme
import com.example.androidfarmerfriend.ui.theme.GrayText

@Composable
fun SchemesScreen(viewModel: SchemesViewModel = viewModel()) {
    val schemes by viewModel.schemesState.collectAsState()
    var selectedFilter by remember { mutableStateOf("அனைத்து") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        ScreenHeader(title = "திட்டங்கள்")
        
        FilterChipGroup(
            filters = listOf("அனைத்து", "மத்திய அரசு", "மாநில அரசு"),
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(schemes) { scheme ->
                SchemeItem(scheme)
            }
        }
    }
}

@Composable
fun SchemeItem(scheme: Scheme) {
    FarmerCard {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) { }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = scheme.title, 
                    style = MaterialTheme.typography.bodyLarge, 
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = scheme.description, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = GrayText,
                    maxLines = 2,
                    lineHeight = 16.sp
                )
                Text(
                    text = "விவரங்கள்", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SchemesScreenPreview() {
    AndroidFarmerFriendTheme {
        SchemesScreen()
    }
}
