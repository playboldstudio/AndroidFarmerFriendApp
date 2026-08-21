package com.example.androidfarmerfriend.ui.screens.disease

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.data.model.Disease
import com.example.androidfarmerfriend.data.util.UiState
import com.example.androidfarmerfriend.ui.components.EmptyState
import com.example.androidfarmerfriend.ui.components.ErrorState
import com.example.androidfarmerfriend.ui.components.FullScreenLoading
import com.example.androidfarmerfriend.ui.components.HeroTitle
import com.example.androidfarmerfriend.ui.components.FarmerCard
import com.example.androidfarmerfriend.ui.components.TintIconCircle
import com.example.androidfarmerfriend.ui.components.SearchField
import com.example.androidfarmerfriend.ui.theme.FarmerTheme
import com.example.androidfarmerfriend.util.WebSearchUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiseaseScreen(viewModel: DiseaseViewModel = viewModel()) {
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

            HeroTitle(text = strings.diseaseDetectionTitle)

            Spacer(modifier = Modifier.height(16.dp))

            SearchField(
                value = state.searchQuery,
                onValueChange = { viewModel.onEvent(DiseaseEvent.Search(it)) },
                placeholder = strings.searchDiseases
            )

            Spacer(modifier = Modifier.height(14.dp))

            when (val diseaseState = state.diseasesState) {
                is UiState.Loading -> FullScreenLoading(modifier = Modifier.padding(top = 48.dp))
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

@Composable
fun DiseaseItem(disease: Disease) {
    val context = LocalContext.current
    val colors = FarmerTheme.colors
    FarmerCard(
        modifier = Modifier.clickable {
            if (disease.sourceUrl.isNotBlank()) {
                WebSearchUtil.openUrl(context, disease.sourceUrl)
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (disease.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = disease.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.softOrange),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                TintIconCircle(
                    icon = Icons.Default.BugReport,
                    tint = colors.diseaseOrange,
                    container = colors.softOrange,
                    size = 46.dp,
                    cornerRadius = 15.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = disease.name,
                    color = colors.textPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                if (disease.cropAffected.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = disease.cropAffected,
                        color = colors.textSecondary,
                        fontSize = 11.5.sp
                    )
                }
            }
            Icon(
                Icons.Default.OpenInNew,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiseaseScreenPreview() {
    com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme {
        DiseaseScreen()
    }
}
