package com.example.androidfarmerfriend.ui.screens.schemes

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.data.model.Scheme
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
fun SchemesScreen(
    onBack: () -> Unit = {},
    viewModel: SchemesViewModel = viewModel()
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

            SubScreenHeader(title = strings.schemesTitle, onBack = onBack)

            Spacer(modifier = Modifier.height(16.dp))

            SearchField(
                value = state.searchQuery,
                onValueChange = { viewModel.onEvent(SchemeEvent.Search(it)) },
                placeholder = strings.searchSchemes
            )

            Spacer(modifier = Modifier.height(14.dp))

            when (val schemeState = state.schemesState) {
                is UiState.Loading -> ShimmerList(rowCount = 4, rowHeight = 88.dp, modifier = Modifier.padding(top = 16.dp))
                is UiState.Error -> ErrorState(
                    message = schemeState.message.ifBlank { strings.schemesLoadError },
                    onRetry = { viewModel.onEvent(SchemeEvent.Retry) },
                    modifier = Modifier.padding(top = 32.dp)
                )
                is UiState.Success -> {
                    if (state.filteredSchemes.isEmpty()) {
                        EmptyState(
                            icon = Icons.Default.SearchOff,
                            title = strings.noSchemes
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(state.filteredSchemes) { scheme ->
                                SchemeItem(scheme)
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
fun SchemeItem(scheme: Scheme) {
    val context = LocalContext.current
    val colors = FarmerTheme.colors
    RowCard(
        title = scheme.title,
        subtitle = scheme.description,
        icon = Icons.Default.AccountBalance,
        iconTint = colors.alertGreen,
        iconContainer = colors.softLavender,
        end = {
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                if (scheme.sourceUrl.isNotBlank()) {
                    Text(
                        text = urlHostLabel(scheme.sourceUrl),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary,
                        modifier = Modifier
                            .background(colors.surfaceMuted, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    )
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
            if (scheme.sourceUrl.isNotBlank()) {
                WebSearchUtil.openUrl(context, scheme.sourceUrl)
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SchemesScreenPreview() {
    com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme {
        SchemesScreen()
    }
}
