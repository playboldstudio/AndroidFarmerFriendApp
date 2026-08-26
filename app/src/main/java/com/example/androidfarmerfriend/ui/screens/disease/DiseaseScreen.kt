package com.example.androidfarmerfriend.ui.screens.disease

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.data.model.Disease
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.ui.components.EmptyState
import com.example.androidfarmerfriend.ui.components.ErrorState
import com.example.androidfarmerfriend.ui.components.SubScreenHeader
import com.example.androidfarmerfriend.ui.components.RowCard
import com.example.androidfarmerfriend.ui.components.ShimmerList
import com.example.androidfarmerfriend.ui.components.SearchField
import com.example.androidfarmerfriend.ui.components.urlHostLabel
import com.example.androidfarmerfriend.ui.theme.FarmerTheme
import com.example.androidfarmerfriend.util.WebSearchUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseScreen(
    onBack: () -> Unit = {},
    viewModel: DiseaseViewModel = viewModel()
) {
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

    // Honest PTR: keep spinner visible briefly so the gesture reads honestly.
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            kotlinx.coroutines.delay(600)
            isRefreshing = false
        }
    }

    androidx.compose.material3.pulltorefresh.PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.loadData(languagePrefs.selectedLanguage)
        }
    ) {
        com.example.androidfarmerfriend.ui.components.CenteredMaxWidth(maxWidth = 640.dp) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            SubScreenHeader(title = strings.diseaseDetectionTitle, onBack = onBack)

            Spacer(modifier = Modifier.height(16.dp))

            SearchField(
                value = state.searchQuery,
                onValueChange = { viewModel.onEvent(DiseaseEvent.Search(it)) },
                placeholder = strings.searchDiseases
            )

            Spacer(modifier = Modifier.height(14.dp))

            when (val diseaseState = state.diseasesState) {
                is UiState.Loading -> ShimmerList(rowCount = 4, rowHeight = 72.dp, modifier = Modifier.padding(top = 16.dp))
                is UiState.Error -> ErrorState(
                    message = diseaseState.message.ifBlank { strings.diseaseLoadError },
                    onRetry = { viewModel.onEvent(DiseaseEvent.Retry) },
                    modifier = Modifier.padding(top = 32.dp)
                )
                is UiState.Success -> {
                    if (state.filteredDiseases.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.SearchOff,
                            title = strings.noDiseases
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(state.filteredDiseases) { disease ->
                                DiseaseItem(disease)
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
fun DiseaseItem(disease: Disease) {
    val context = LocalContext.current
    val colors = FarmerTheme.colors
    RowCard(
        title = disease.name,
        subtitle = disease.cropAffected.ifBlank { null },
        icon = Icons.Default.BugReport,
        iconTint = colors.diseaseOrange,
        iconContainer = colors.softOrange,
        end = {
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                if (disease.sourceUrl.isNotBlank()) {
                    SourceChip(url = disease.sourceUrl)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Icon(
                    @Suppress("DEPRECATION")
                    Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = colors.textTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        onClick = {
            if (disease.sourceUrl.isNotBlank()) {
                WebSearchUtil.openUrl(context, disease.sourceUrl)
            }
        }
    )
}

@Composable
private fun SourceChip(url: String) {
    Text(
        text = urlHostLabel(url),
        style = MaterialTheme.typography.labelSmall,
        color = FarmerTheme.colors.textTertiary,
        modifier = Modifier
            .background(FarmerTheme.colors.surfaceMuted, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun DiseaseScreenPreview() {
    com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme {
        DiseaseScreen()
    }
}
