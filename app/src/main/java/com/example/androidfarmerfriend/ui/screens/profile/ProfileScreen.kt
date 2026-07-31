package com.example.androidfarmerfriend.ui.screens.profile

import com.example.androidfarmerfriend.BuildConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.data.localization.LanguagePrefs
import com.example.androidfarmerfriend.data.location.LocationPrefs
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.ui.components.HeroTitle
import com.example.androidfarmerfriend.ui.components.MenuRow
import com.example.androidfarmerfriend.ui.components.SectionTitle
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme
import com.example.androidfarmerfriend.ui.theme.FarmerTheme

@Composable
fun ProfileScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val strings = LocalAppStrings.current
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val locationPrefs = remember { LocationPrefs(context) }
    val location = remember { locationPrefs.selectedLocation }
    val languagePrefs = remember { LanguagePrefs(context) }
    var currentLang by remember { mutableStateOf(languagePrefs.selectedLanguage) }
    val colors = FarmerTheme.colors

    LaunchedEffect(Unit) {
        currentLang = languagePrefs.selectedLanguage
    }

    val navEvent by viewModel.navigation.collectAsState()
    LaunchedEffect(navEvent) {
        navEvent?.let { route ->
            onNavigate(route)
            viewModel.onNavigated()
        }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onEvent(ProfileEvent.DismissMessage)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            HeroTitle(text = strings.profileTitle)

            Spacer(modifier = Modifier.height(8.dp))

            // ── Profile Card ──
            ProfileCard(
                state = state,
                locationName = location.name,
                strings = strings,
                onStartEditing = { viewModel.onEvent(ProfileEvent.StartEditing) },
                onSaveProfile = { viewModel.onEvent(ProfileEvent.SaveProfile) },
                onCancelEditing = { viewModel.onEvent(ProfileEvent.CancelEditing) },
                onNameChange = { viewModel.onEvent(ProfileEvent.UpdateTempName(it)) },
                onPhoneChange = { viewModel.onEvent(ProfileEvent.UpdateTempPhone(it)) }
            )

            SectionTitle(title = strings.settings)

            // ── Settings menu card (rows separated by hairline dividers, like .menu-row) ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface)
            ) {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)) {
                    SettingsRow(
                        icon = Icons.Default.Person,
                        iconTint = colors.primary,
                        iconContainer = colors.softMint,
                        title = strings.myDetails,
                        onClick = { viewModel.onEvent(ProfileEvent.NavigateToDetails) },
                        showDivider = true
                    )
                    SettingsRow(
                        icon = Icons.Default.Landscape,
                        iconTint = colors.alertGreen,
                        iconContainer = colors.softGreen,
                        title = strings.myLands,
                        onClick = { viewModel.onEvent(ProfileEvent.NavigateToLands) },
                        showDivider = true
                    )
                    SettingsRow(
                        icon = Icons.Default.Language,
                        iconTint = colors.weatherBlue,
                        iconContainer = colors.softBlue,
                        title = strings.language,
                        trailing = currentLang.displayEnglish,
                        onClick = { viewModel.onEvent(ProfileEvent.NavigateToLanguage) },
                        showDivider = true
                    )
                    SettingsRow(
                        icon = Icons.Default.Notifications,
                        iconTint = colors.alertPurple,
                        iconContainer = colors.softPurple,
                        title = strings.notifications,
                        onClick = { viewModel.onEvent(ProfileEvent.NavigateToNotifications) },
                        showDivider = true
                    )
                    SettingsRow(
                        icon = Icons.Default.PrivacyTip,
                        iconTint = colors.cropBrown,
                        iconContainer = colors.softBrown,
                        title = strings.privacyPolicy,
                        onClick = { viewModel.onEvent(ProfileEvent.NavigateToPrivacy) },
                        showDivider = true
                    )
                    SettingsRow(
                        icon = Icons.Default.Settings,
                        iconTint = colors.textSecondary,
                        iconContainer = colors.surfaceMuted,
                        title = strings.settings,
                        onClick = { viewModel.onEvent(ProfileEvent.NavigateToSettings) },
                        showDivider = false
                    )
                }
            }

            // ── Version Info ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${strings.appName} v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
                Text(
                    text = strings.appTagline,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary.copy(alpha = 0.7f),
                    fontSize = 8.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

// ── Redesigned Profile Card ──
@Composable
private fun ProfileCard(
    state: ProfileState,
    locationName: String,
    strings: com.example.androidfarmerfriend.data.localization.AppStrings,
    onStartEditing: () -> Unit,
    onSaveProfile: () -> Unit,
    onCancelEditing: () -> Unit,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit
) {
    val colors = FarmerTheme.colors
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            if (state.isEditing) {
                // ── Edit mode: full-width stacked form ──
                OutlinedTextField(
                    value = state.tempName,
                    onValueChange = onNameChange,
                    label = { Text(strings.nameField, fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primary,
                        unfocusedBorderColor = colors.outline,
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.tempPhone,
                    onValueChange = onPhoneChange,
                    label = { Text(strings.phoneField, fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    prefix = {
                        Text(
                            text = "+91 ",
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.textSecondary
                        )
                    },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    isError = state.phoneError != null,
                    supportingText = state.phoneError?.let { err ->
                        { Text(err, color = MaterialTheme.colorScheme.error, fontSize = 11.sp) }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (state.phoneError != null) MaterialTheme.colorScheme.error else colors.primary,
                        unfocusedBorderColor = if (state.phoneError != null) MaterialTheme.colorScheme.error else colors.outline,
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = onSaveProfile,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
                        ),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                    TextButton(
                        onClick = onCancelEditing,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Text("Cancel", fontSize = 14.sp, color = colors.textSecondary)
                    }
                }
            } else {
                // ── Display mode: Avatar + Info + Edit ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar (soft green circle with a subtle surface ring — matches .avatar-lg)
                    Box(
                        modifier = Modifier
                            .size(74.dp)
                            .clip(CircleShape)
                            .background(colors.softGreen)
                            .border(3.dp, colors.surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = colors.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(15.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (state.userPhone.startsWith("+91")) state.userPhone else "+91 ${state.userPhone}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = colors.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = locationName,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    FilledTonalButton(
                        onClick = onStartEditing,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = colors.softGreen,
                            contentColor = colors.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/** Settings row with a hairline divider below (matches `.menu-row` border-bottom). */
@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    iconContainer: androidx.compose.ui.graphics.Color,
    title: String,
    trailing: String? = null,
    onClick: () -> Unit,
    showDivider: Boolean
) {
    Column {
        MenuRow(
            icon = icon,
            iconTint = iconTint,
            iconContainer = iconContainer,
            title = title,
            trailing = trailing,
            onClick = onClick
        )
        if (showDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 48.dp)
                    .height(1.dp)
                    .background(FarmerTheme.colors.outline)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    AndroidFarmerFriendTheme {
        ProfileScreen()
    }
}
