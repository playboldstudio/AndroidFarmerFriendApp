package com.example.androidfarmerfriend.ui.screens.cropnotes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.draw.clip
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
import com.example.androidfarmerfriend.ui.components.EmptyState
import com.example.androidfarmerfriend.ui.components.ErrorState
import com.example.androidfarmerfriend.ui.components.FullScreenLoading
import com.example.androidfarmerfriend.ui.components.HeroTitle
import com.example.androidfarmerfriend.ui.components.LocPill
import com.example.androidfarmerfriend.ui.components.PillChip
import com.example.androidfarmerfriend.ui.components.SearchField
import com.example.androidfarmerfriend.ui.components.TintIconCircle
import com.example.androidfarmerfriend.ui.theme.FarmerTheme
import com.example.androidfarmerfriend.util.WebSearchUtil

private const val ALL_CROPS = "__all__"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropNotesScreen(viewModel: CropNotesViewModel = viewModel()) {
    val strings = LocalAppStrings.current
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var isRefreshing by remember { mutableStateOf(false) }
    val colors = FarmerTheme.colors
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
                .background(colors.background)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            HeroTitle(text = strings.cropNotesTitle, accent = strings.cropNotesTitle.split(" ").getOrNull(1))

            Spacer(modifier = Modifier.height(8.dp))

            LocPill(text = strings.cropNotesSubtitle)

            Spacer(modifier = Modifier.height(8.dp))

            SearchField(
                value = state.searchQuery,
                onValueChange = { viewModel.onEvent(CropNoteEvent.Search(it)) },
                placeholder = strings.searchNotes
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 8.dp)
            ) {
                state.crops.forEach { crop ->
                    val displayName = if (crop == ALL_CROPS) strings.filterAll else crop
                    PillChip(
                        text = displayName,
                        selected = crop == state.selectedCrop,
                        onClick = { viewModel.onEvent(CropNoteEvent.SelectCrop(crop)) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            when (val notesState = state.notesState) {
                is UiState.Loading -> FullScreenLoading(modifier = Modifier.padding(top = 48.dp))
                is UiState.Error -> ErrorState(
                    message = notesState.message.ifBlank { strings.notesLoadError },
                    onRetry = { viewModel.onEvent(CropNoteEvent.Retry) },
                    modifier = Modifier.padding(top = 32.dp)
                )
                is UiState.Success -> {
                    if (state.filteredNotes.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.SearchOff,
                            title = strings.noNotes
                        )
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

/** HTML-style crop note card: avatar + title + status + divider + content + tags + time. */
@Composable
fun CropNoteItem(note: CropNote) {
    val context = LocalContext.current
    val colors = FarmerTheme.colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (note.sourceUrl.isNotBlank()) {
                    WebSearchUtil.openUrl(context, note.sourceUrl)
                }
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                TintIconCircle(
                    icon = Icons.Default.MenuBook,
                    tint = colors.cropBrown,
                    container = colors.softBrown,
                    size = 46.dp,
                    cornerRadius = 15.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.title,
                        color = colors.textPrimary,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = note.cropName,
                        color = colors.textSecondary,
                        fontSize = 11.5.sp
                    )
                }
                // Status chip
                Text(
                    text = "ACTIVE",
                    color = colors.primary,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.softGreen)
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 13.dp)
                    .height(1.dp)
                    .background(colors.outline)
            )

            Text(
                text = note.content,
                color = colors.textSecondary,
                fontSize = 12.5.sp,
                lineHeight = 19.sp
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                modifier = Modifier.padding(top = 12.dp)
            ) {
                NoteTag("💧 Watered")
                NoteTag("🧪 Fertilized")
            }

            Text(
                text = "Today",
                color = colors.textTertiary,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}

@Composable
private fun NoteTag(text: String) {
    val colors = FarmerTheme.colors
    Text(
        text = text,
        color = colors.textSecondary,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surfaceMuted)
            .padding(horizontal = 9.dp, vertical = 4.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun CropNotesScreenPreview() {
    com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme {
        CropNotesScreen()
    }
}
