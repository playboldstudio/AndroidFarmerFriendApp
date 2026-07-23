package com.example.androidfarmerfriend.ui.screens.profile

import com.example.androidfarmerfriend.BuildConfig
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.data.localization.LanguagePrefs
import com.example.androidfarmerfriend.data.location.LocationPrefs
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.ui.components.ScreenHeader
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme
import com.example.androidfarmerfriend.ui.theme.FarmerGreenPrimary
import com.example.androidfarmerfriend.ui.theme.GrayText

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
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            ScreenHeader(title = strings.profileTitle, showSearch = false)

            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(24.dp))

            // ── Menu Items ──
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileMenuItem(title = strings.myDetails, icon = Icons.Default.Person, onClick = { viewModel.onEvent(ProfileEvent.NavigateToDetails) })
                ProfileMenuItem(title = strings.myLands, icon = Icons.Default.Landscape, onClick = { viewModel.onEvent(ProfileEvent.NavigateToLands) })
                ProfileMenuItem(
                    title = strings.language,
                    icon = Icons.Default.Language,
                    trailingText = currentLang.displayEnglish,
                    onClick = { viewModel.onEvent(ProfileEvent.NavigateToLanguage) }
                )
                ProfileMenuItem(title = strings.notifications, icon = Icons.Default.Notifications, onClick = { viewModel.onEvent(ProfileEvent.NavigateToNotifications) })
                ProfileMenuItem(title = strings.privacyPolicy, icon = Icons.Default.PrivacyTip, onClick = { viewModel.onEvent(ProfileEvent.NavigateToPrivacy) })
                ProfileMenuItem(title = strings.settings, icon = Icons.Default.Settings, onClick = { viewModel.onEvent(ProfileEvent.NavigateToSettings) })
            }

            // ── Version Info ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${strings.appName} v${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelSmall,
                    color = GrayText.copy(alpha = 0.5f)
                )
                Text(
                    text = strings.appTagline,
                    style = MaterialTheme.typography.labelSmall,
                    color = GrayText.copy(alpha = 0.3f),
                    fontSize = 8.sp
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // ── Top row: Avatar + Info/Edit ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(FarmerGreenPrimary.copy(alpha = 0.12f))
                        .border(2.dp, FarmerGreenPrimary.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = FarmerGreenPrimary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Name / Phone / Location (display mode) or TextFields (edit mode)
                Column(modifier = Modifier.weight(1f)) {
                    if (state.isEditing) {
                        OutlinedTextField(
                            value = state.tempName,
                            onValueChange = onNameChange,
                            label = { Text(strings.nameField, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FarmerGreenPrimary,
                                unfocusedBorderColor = GrayText.copy(alpha = 0.3f)
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.tempPhone,
                            onValueChange = onPhoneChange,
                            label = { Text(strings.phoneField, fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FarmerGreenPrimary,
                                unfocusedBorderColor = GrayText.copy(alpha = 0.3f)
                            ),
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                    } else {
                        Text(
                            text = state.userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = state.userPhone,
                            style = MaterialTheme.typography.bodyMedium,
                            color = GrayText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = FarmerGreenPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = locationName,
                                style = MaterialTheme.typography.bodySmall,
                                color = GrayText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Edit / Save / Cancel buttons
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    FilledTonalButton(
                        onClick = {
                            if (state.isEditing) onSaveProfile() else onStartEditing()
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = FarmerGreenPrimary.copy(alpha = 0.12f),
                            contentColor = FarmerGreenPrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            if (state.isEditing) Icons.Default.Check else Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (state.isEditing) "Save" else "Edit",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    AnimatedVisibility(
                        visible = state.isEditing,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        TextButton(
                            onClick = onCancelEditing,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = 12.sp,
                                color = GrayText
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Menu Item Row ──
@Composable
fun ProfileMenuItem(title: String, icon: ImageVector, trailingText: String? = null, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = CircleShape,
                color = FarmerGreenPrimary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = FarmerGreenPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            if (trailingText != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = FarmerGreenPrimary.copy(alpha = 0.08f)
                ) {
                    Text(
                        text = trailingText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = FarmerGreenPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = GrayText.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
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
