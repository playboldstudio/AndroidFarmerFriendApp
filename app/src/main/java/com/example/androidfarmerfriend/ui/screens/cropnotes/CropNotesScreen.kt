package com.example.androidfarmerfriend.ui.screens.cropnotes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.data.model.CropNote
import com.example.androidfarmerfriend.ui.components.FarmerCard
import com.example.androidfarmerfriend.ui.components.ScreenHeader
import com.example.androidfarmerfriend.ui.theme.*
import com.example.androidfarmerfriend.util.WebSearchUtil

@Composable
fun CropNotesScreen(viewModel: CropNotesViewModel = viewModel()) {
    val notes by viewModel.cropNotesState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    var selectedCrop by remember { mutableStateOf("அனைத்து") }
    var showSearchBar by remember { mutableStateOf(false) }
    val crops = listOf("அனைத்து") + notes.map { it.cropName }.distinct()

    val filteredNotes = when {
        selectedCrop != "அனைத்து" -> notes.filter { it.cropName == selectedCrop }
        searchQuery.isNotBlank() -> notes.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.cropName.contains(searchQuery, ignoreCase = true) ||
            it.content.contains(searchQuery, ignoreCase = true)
        }
        else -> notes
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        ScreenHeader(
            title = "பயிர் குறிப்புகள்",
            subtitle = "விவசாய குறிப்புகள் மற்றும் தகவல்கள்",
            isSearchActive = showSearchBar,
            onSearchClick = {
                showSearchBar = !showSearchBar
                if (!showSearchBar) viewModel.onSearchQueryChanged("")
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (showSearchBar) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("குறிப்புகளைத் தேடவும்", color = GrayText, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GrayText) },
                trailingIcon = {},
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

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            crops.forEach { crop ->
                val isSelected = crop == selectedCrop
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCrop = crop },
                    label = {
                        Text(
                            crop,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        selectedContainerColor = FarmerGreenPrimary,
                        labelColor = MaterialTheme.colorScheme.onSurface,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        if (error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = TrendRed, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("குறிப்புகளை ஏற்ற முடியவில்லை", color = GrayText, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(error ?: "", color = GrayText, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.retry() }, colors = ButtonDefaults.buttonColors(containerColor = FarmerGreenPrimary)) {
                        Text("மீண்டும் முயற்சிக்க")
                    }
                }
            }
        } else if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = FarmerGreenPrimary)
            }
        } else if (filteredNotes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.SearchOff, contentDescription = null, tint = GrayText, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("குறிப்புகள் எதுவும் இல்லை", color = GrayText)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredNotes) { note ->
                    CropNoteItem(note = note)
                }
            }
        }
    }
}

@Composable
fun CropNoteItem(note: CropNote) {
    val context = LocalContext.current
    FarmerCard(
        modifier = Modifier.clickable {
            if (note.sourceUrl.isNotBlank()) {
                WebSearchUtil.openUrl(context, note.sourceUrl)
            }
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = FarmerGreenPrimary.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = FarmerGreenPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp
                    )
                    Text(
                        text = note.cropName,
                        style = MaterialTheme.typography.labelSmall,
                        color = FarmerGreenPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Icon(
                    Icons.Default.OpenInNew,
                    contentDescription = "திறக்க",
                    tint = FarmerGreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CropNotesScreenPreview() {
    AndroidFarmerFriendTheme {
        CropNotesScreen()
    }
}
