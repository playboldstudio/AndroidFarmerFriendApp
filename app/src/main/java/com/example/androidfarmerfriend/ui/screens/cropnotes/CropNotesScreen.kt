package com.example.androidfarmerfriend.ui.screens.cropnotes

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.data.model.CropNote
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.ui.components.ChipOption
import com.example.androidfarmerfriend.ui.components.EmptyState
import com.example.androidfarmerfriend.ui.components.ErrorState
import com.example.androidfarmerfriend.ui.components.LocPill
import com.example.androidfarmerfriend.ui.components.PillChip
import com.example.androidfarmerfriend.ui.components.SearchField
import com.example.androidfarmerfriend.ui.components.ShimmerList
import com.example.androidfarmerfriend.ui.components.SubScreenHeader
import com.example.androidfarmerfriend.ui.components.urlHostLabel
import com.example.androidfarmerfriend.ui.components.TintIconCircle
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme
import com.example.androidfarmerfriend.ui.theme.FarmerSpacing
import com.example.androidfarmerfriend.ui.theme.FarmerTheme
import com.example.androidfarmerfriend.util.WebSearchUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropNotesScreen(
    onBack: () -> Unit = {},
    viewModel: CropNotesViewModel = viewModel()
) {
    val strings = LocalAppStrings.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var isRefreshing by remember { mutableStateOf(false) }
    val colors = FarmerTheme.colors

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    // Keep the spinner visible briefly after refresh so the gesture reads honestly.
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            kotlinx.coroutines.delay(600)
            isRefreshing = false
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.loadData()
        }
    ) {
        com.example.androidfarmerfriend.ui.components.CenteredMaxWidth {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .background(colors.background)
                .padding(horizontal = FarmerSpacing.lg)
        ) {
            Spacer(Modifier.height(FarmerSpacing.lg))

            SubScreenHeader(title = strings.cropNotesTitle, onBack = onBack)

            Spacer(Modifier.height(FarmerSpacing.s))

            LocPill(text = strings.cropNotesSubtitle)

            Spacer(Modifier.height(FarmerSpacing.s))

            SearchField(
                value = state.searchQuery,
                onValueChange = { viewModel.onEvent(CropNoteEvent.Search(it)) },
                placeholder = strings.searchNotes
            )

            Spacer(Modifier.height(FarmerSpacing.xs))

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
                        option = ChipOption(displayName),
                        selected = crop == state.selectedCrop,
                        onClick = { viewModel.onEvent(CropNoteEvent.SelectCrop(crop)) }
                    )
                }
            }

            when (val notesState = state.notesState) {
                is UiState.Loading -> ShimmerList(rowCount = 4, rowHeight = 120.dp)
                is UiState.Error -> ErrorState(
                    message = notesState.message.ifBlank { strings.notesLoadError },
                    onRetry = { viewModel.onEvent(CropNoteEvent.Retry) },
                    modifier = Modifier.padding(top = 32.dp)
                )
                is UiState.Success -> {
                    if (state.filteredNotes.isEmpty()) {
                        EmptyState(icon = Icons.Default.SearchOff, title = strings.noNotes)
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(state.filteredNotes, key = { it.id }) { note ->
                                CropNoteItem(note = note, strings = strings)
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
fun CropNoteItem(note: CropNote, strings: AppStrings) {
    val context = LocalContext.current
    val colors = FarmerTheme.colors
    // Tapping toggles the in-place expand/collapse; the source link is opened
    // via the separate "Read full article" action below.
    var expanded by rememberSaveable(note.id) { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(
            modifier = Modifier
                .animateContentSize()
                .clickable { expanded = !expanded }
                .padding(16.dp)
        ) {
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
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary
                    )
                    Text(
                        text = note.cropName,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }
            }

            if (note.content.isNotBlank()) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 13.dp),
                    thickness = 1.dp,
                    color = colors.outline
                )
                Text(
                    text = note.content,
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = if (expanded) Int.MAX_VALUE else 3,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (expanded) strings.collapseLabel else strings.readMoreLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = colors.primary,
                        modifier = Modifier.weight(1f)
                    )
                    if (note.sourceUrl.isNotBlank() && expanded) {
                        Text(
                            text = urlHostLabel(note.sourceUrl),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textTertiary,
                            modifier = Modifier.clickable {
                                WebSearchUtil.openUrl(context, note.sourceUrl)
                            }
                        )
                    }
                }
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
