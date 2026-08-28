package com.example.androidfarmerfriend.ui.screens.language

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.data.localization.Language
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.ui.components.SubScreenHeader
import com.example.androidfarmerfriend.ui.theme.FarmerSpacing
import com.example.androidfarmerfriend.ui.theme.FarmerTheme

@Composable
fun LanguageScreen(
    onLanguageChanged: (Language) -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: LanguageViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selectedLanguage = state.selectedLanguage
    val strings = LocalAppStrings.current
    val colors = FarmerTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = FarmerSpacing.lg)
    ) {
        Spacer(Modifier.height(FarmerSpacing.lg))

        SubScreenHeader(title = strings.selectLanguage, onBack = onBack)

        Spacer(Modifier.height(FarmerSpacing.s))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(FarmerSpacing.s),
            contentPadding = PaddingValues(vertical = FarmerSpacing.s)
        ) {
            items(Language.entries, key = { it.name }) { language ->
                LanguageCard(
                    language = language,
                    displayName = if (selectedLanguage == Language.TAMIL) {
                        language.displayTamil
                    } else {
                        language.displayEnglish
                    },
                    isSelected = language == selectedLanguage,
                    onClick = {
                        viewModel.onEvent(LanguageEvent.SelectLanguage(language))
                        onLanguageChanged(language)
                    }
                )
            }
        }

        Text(
            text = LocalAppStrings.current.languageAppliedHint,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            modifier = Modifier.padding(vertical = FarmerSpacing.lg)
        )
    }
}

@Composable
private fun LanguageCard(
    language: Language,
    displayName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = FarmerTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) colors.softGreen else colors.surface)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) colors.primary else colors.outline,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) colors.primary else colors.textPrimary
            )
            if (language.displayEnglish != displayName) {
                Text(
                    text = language.displayEnglish,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
            }
        }
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
