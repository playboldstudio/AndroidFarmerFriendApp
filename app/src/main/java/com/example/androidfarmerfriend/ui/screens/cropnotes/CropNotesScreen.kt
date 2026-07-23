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
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.data.model.CropNote
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.ui.components.FarmerCard
import com.example.androidfarmerfriend.ui.components.FullScreenLoading
import com.example.androidfarmerfriend.ui.components.ScreenHeader
import com.example.androidfarmerfriend.ui.theme.*
import com.example.androidfarmerfriend.util.WebSearchUtil

private const val ALL_CROPS = "__all__"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropNotesScreen(viewModel: CropNotesViewModel = viewModel()) {
    val strings = LocalAppStrings.current
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var showSearchBar by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val languagePrefs = remember {
        com.example.androidfarmerfriend.data.localization.LanguagePrefs(context)
    }

    LaunchedEffect(Unit) {
        viewModel.loadData(languagePrefs.selectedLanguage)
    }

    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.loadData()
            isRefreshing = false
        }
    ) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        ScreenHeader(
            title = strings.cropNotesTitle,
            subtitle = strings.cropNotesSubtitle,
            isSearchActive = showSearchBar,
            onSearchClick = {
                showSearchBar = !showSearchBar
                if (!showSearchBar) viewModel.onEvent(CropNoteEvent.Search(""))
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (showSearchBar) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onEvent(CropNoteEvent.Search(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(strings.searchNotes, color = GrayText, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GrayText) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onEvent(CropNoteEvent.Search("")) }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = GrayText)
                        }
                    }
                },
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
            state.crops.forEach { crop ->
                val displayName = if (crop == ALL_CROPS) strings.filterAll else crop
                val isSelected = crop == state.selectedCrop
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.onEvent(CropNoteEvent.SelectCrop(crop)) },
                    label = {
                        Text(displayName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
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

        when (val notesState = state.notesState) {
            is UiState.Loading -> FullScreenLoading()
            is UiState.Error -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = TrendRed, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(strings.notesLoadError, color = GrayText, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(notesState.message, color = GrayText, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.onEvent(CropNoteEvent.Retry) }, colors = ButtonDefaults.buttonColors(containerColor = FarmerGreenPrimary)) {
                        Text(strings.retry)
                    }
                }
            }
            is UiState.Success -> {
                if (state.filteredNotes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, tint = GrayText, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(strings.noNotes, color = GrayText)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(state.filteredNotes) { note ->
                            CropNoteItem(note = note)
                        }
                    }
                }
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
                    contentDescription = null,
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
