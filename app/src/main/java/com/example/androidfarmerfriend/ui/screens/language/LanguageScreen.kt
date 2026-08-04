package com.example.androidfarmerfriend.ui.screens.language

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.data.localization.Language
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.ui.components.SubScreenHeader
import com.example.androidfarmerfriend.ui.theme.FarmerTheme

@Composable
fun LanguageScreen(
    onLanguageChanged: (Language) -> Unit = {},
    onBack: () -> Unit = {},
    viewModel: LanguageViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val selectedLanguage = state.selectedLanguage
    val strings = LocalAppStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FarmerTheme.colors.background)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        SubScreenHeader(title = strings.selectLanguage, onBack = onBack)

        Spacer(modifier = Modifier.height(8.dp))

        Language.entries.forEach { language ->
            val isSelected = language == selectedLanguage
            val displayName = if (selectedLanguage == Language.TAMIL) language.displayTamil else language.displayEnglish

            Surface(
                onClick = {
                    viewModel.onEvent(LanguageEvent.SelectLanguage(language))
                    onLanguageChanged(language)
                },
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = null, // Surface handles click — prevents double-dispatch
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontSize = 16.sp,
                        color = FarmerTheme.colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (selectedLanguage == Language.TAMIL) "மொழி உடனடியாக மாற்றப்படும்" else "Language will be applied immediately",
            style = MaterialTheme.typography.bodySmall,
            color = FarmerTheme.colors.textSecondary,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
