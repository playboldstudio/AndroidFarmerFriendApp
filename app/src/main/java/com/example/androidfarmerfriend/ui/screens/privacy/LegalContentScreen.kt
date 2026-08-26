package com.example.androidfarmerfriend.ui.screens.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.androidfarmerfriend.ui.components.SubScreenHeader
import com.example.androidfarmerfriend.ui.theme.FarmerSpacing
import com.example.androidfarmerfriend.ui.theme.FarmerTheme
import com.example.androidfarmerfriend.util.WebSearchUtil

/** One titled block of a legal document. */
internal data class LegalSection(
    val heading: String,
    val body: String = "",
    val bullets: List<String> = emptyList()
)

/**
 * Native renderer for the legal pages (English-only by design).
 * Shows the summarized document in-app plus a link to the full hosted copy.
 */
@Composable
internal fun LegalContentScreen(
    title: String,
    lastUpdated: String,
    sections: List<LegalSection>,
    fullDocumentUrl: String,
    onBack: () -> Unit
) {
    val colors = FarmerTheme.colors
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = FarmerSpacing.lg)
    ) {
        Spacer(Modifier.height(FarmerSpacing.lg))

        SubScreenHeader(title = title, onBack = onBack)

        Spacer(Modifier.height(FarmerSpacing.xs))

        Text(
            text = "Last updated: $lastUpdated",
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary,
            modifier = Modifier.padding(start = FarmerSpacing.xs)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = FarmerSpacing.md),
            verticalArrangement = Arrangement.spacedBy(FarmerSpacing.md)
        ) {
            sections.forEach { section ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = section.heading,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.primaryDeep
                        )
                        if (section.body.isNotBlank()) {
                            Spacer(Modifier.height(FarmerSpacing.s))
                            Text(
                                text = section.body,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                        if (section.bullets.isNotEmpty()) {
                            Spacer(Modifier.height(FarmerSpacing.s))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                section.bullets.forEach { bullet ->
                                    Row {
                                        Text(
                                            text = "•  ",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.primary
                                        )
                                        Text(
                                            text = bullet,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = colors.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { WebSearchUtil.openUrl(context, fullDocumentUrl) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.softGreen)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "View full document",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.primaryDeep
                        )
                        Text(
                            text = "Opens the hosted copy in your browser",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                    Icon(
                        Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = colors.primaryDeep
                    )
                }
            }

            HorizontalDivider(thickness = 1.dp, color = colors.outline)

            Text(
                text = "For questions about this document, contact us through the Play Store listing.",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary,
                modifier = Modifier.padding(bottom = FarmerSpacing.xxl)
            )
        }
    }
}
