package com.example.androidfarmerfriend.ui.screens.cropnotes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.data.model.CropNote
import com.example.androidfarmerfriend.ui.components.FarmerCard
import com.example.androidfarmerfriend.ui.components.ScreenHeader
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme
import com.example.androidfarmerfriend.ui.theme.FarmerGreenPrimary
import com.example.androidfarmerfriend.ui.theme.GrayText
import com.example.androidfarmerfriend.ui.theme.TrendGreen

@Composable
fun CropNotesScreen(viewModel: CropNotesViewModel = viewModel()) {
    val notes by viewModel.cropNotesState.collectAsState()
    var expandedNoteId by remember { mutableStateOf<Int?>(null) }
    var selectedCrop by remember { mutableStateOf("அனைத்து") }
    val crops = listOf("அனைத்து") + notes.map { it.cropName }.distinct()

    val filteredNotes = if (selectedCrop == "அனைத்து") notes
    else notes.filter { it.cropName == selectedCrop }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        ScreenHeader(
            title = "பயிர் குறிப்புகள்",
            subtitle = "விவசாய குறிப்புகள் மற்றும் தகவல்கள்"
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 4.dp)
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

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredNotes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("இந்த பயிருக்கு குறிப்புகள் இல்லை", color = GrayText)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredNotes) { note ->
                    val isExpanded = expandedNoteId == note.id
                    CropNoteItem(
                        note = note,
                        isExpanded = isExpanded,
                        onToggle = { expandedNoteId = if (isExpanded) null else note.id }
                    )
                }
            }
        }
    }
}

@Composable
fun CropNoteItem(note: CropNote, isExpanded: Boolean, onToggle: () -> Unit) {
    FarmerCard(
        modifier = Modifier.clickable { onToggle() }
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
                    Row {
                        Text(
                            text = note.cropName,
                            style = MaterialTheme.typography.labelSmall,
                            color = FarmerGreenPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        if (note.season.isNotEmpty()) {
                            Text(
                                text = " · ${note.season}",
                                style = MaterialTheme.typography.labelSmall,
                                color = GrayText
                            )
                        }
                    }
                }

                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = GrayText,
                    modifier = Modifier.size(20.dp)
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    lineHeight = 22.sp
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
