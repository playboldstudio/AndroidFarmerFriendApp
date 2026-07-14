package com.example.androidfarmerfriend.ui.screens.disease

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.data.model.Disease
import com.example.androidfarmerfriend.ui.components.FarmerCard
import com.example.androidfarmerfriend.ui.components.FilterChipGroup
import com.example.androidfarmerfriend.ui.components.ScreenHeader
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme
import com.example.androidfarmerfriend.ui.theme.GrayText

@Composable
fun DiseaseScreen(viewModel: DiseaseViewModel = viewModel()) {
    val diseases by viewModel.diseasesState.collectAsState()
    var selectedFilter by remember { mutableStateOf("அனைத்து") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        ScreenHeader(title = "நோய் கண்டறிதல்")
        
        OutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            placeholder = { Text("நோய் பெயரைத் தேடவும்", color = GrayText, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GrayText) },
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
        
        FilterChipGroup(
            filters = listOf("அனைத்து", "இலை நோய்கள்", "பழ நோய்கள்"),
            selectedFilter = selectedFilter,
            onFilterSelected = { selectedFilter = it }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(diseases) { disease ->
                DiseaseItem(disease)
            }
        }
    }
}

@Composable
fun DiseaseItem(disease: Disease) {
    FarmerCard {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) { }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = disease.name, 
                    style = MaterialTheme.typography.bodyLarge, 
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = disease.cropAffected, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = GrayText
                )
                Text(
                    text = "விவரங்கள்", 
                    style = MaterialTheme.typography.labelSmall, 
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiseaseScreenPreview() {
    AndroidFarmerFriendTheme {
        DiseaseScreen()
    }
}
