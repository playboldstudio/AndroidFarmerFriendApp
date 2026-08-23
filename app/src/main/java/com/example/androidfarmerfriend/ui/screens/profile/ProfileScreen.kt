package com.example.androidfarmerfriend.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.BuildConfig
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.ui.components.HeroTitle
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme
import com.example.androidfarmerfriend.ui.theme.FarmerSpacing
import com.example.androidfarmerfriend.ui.theme.FarmerTheme

@Composable
fun ProfileScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val strings = LocalAppStrings.current
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FarmerTheme.colors.background)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = FarmerSpacing.lg)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(FarmerSpacing.lg))

            HeroTitle(text = strings.profileTitle)

            Spacer(Modifier.height(FarmerSpacing.md))

            IdentityCard(
                state = state,
                strings = strings,
                onStartEditing = { viewModel.onEvent(ProfileEvent.StartEditing) },
                onSaveProfile = { viewModel.onEvent(ProfileEvent.SaveProfile) },
                onCancelEditing = { viewModel.onEvent(ProfileEvent.CancelEditing) },
                onNameChange = { viewModel.onEvent(ProfileEvent.UpdateTempName(it)) },
                onPhoneChange = { viewModel.onEvent(ProfileEvent.UpdateTempPhone(it)) }
            )

            MenuSection(title = strings.accountSection) {
                SettingsRow(
                    icon = Icons.Default.Person,
                    iconTint = FarmerTheme.colors.primary,
                    iconContainer = FarmerTheme.colors.softMint,
                    title = strings.myDetails,
                    showDivider = true,
                    onClick = { viewModel.onEvent(ProfileEvent.NavigateToDetails) }
                )
                SettingsRow(
                    icon = Icons.Default.Landscape,
                    iconTint = FarmerTheme.colors.alertGreen,
                    iconContainer = FarmerTheme.colors.softGreen,
                    title = strings.myLands,
                    showDivider = false,
                    onClick = { viewModel.onEvent(ProfileEvent.NavigateToLands) }
                )
            }

            MenuSection(title = strings.preferencesSection) {
                SettingsRow(
                    icon = Icons.Default.Language,
                    iconTint = FarmerTheme.colors.weatherBlue,
                    iconContainer = FarmerTheme.colors.softBlue,
                    title = strings.language,
                    trailing = state.languageLabel,
                    showDivider = true,
                    onClick = { viewModel.onEvent(ProfileEvent.NavigateToLanguage) }
                )
                SettingsRow(
                    icon = Icons.Default.Notifications,
                    iconTint = FarmerTheme.colors.alertPurple,
                    iconContainer = FarmerTheme.colors.softPurple,
                    title = strings.notifications,
                    showDivider = false,
                    onClick = { viewModel.onEvent(ProfileEvent.NavigateToNotifications) }
                )
            }

            MenuSection(title = strings.legalSection) {
                SettingsRow(
                    icon = Icons.Default.PrivacyTip,
                    iconTint = FarmerTheme.colors.cropBrown,
                    iconContainer = FarmerTheme.colors.softBrown,
                    title = strings.privacyPolicy,
                    showDivider = true,
                    onClick = { viewModel.onEvent(ProfileEvent.NavigateToPrivacy) }
                )
                SettingsRow(
                    icon = Icons.Default.Description,
                    iconTint = FarmerTheme.colors.weatherBlue,
                    iconContainer = FarmerTheme.colors.softBlue,
                    title = strings.termsOfUse,
                    showDivider = true,
                    onClick = { viewModel.onEvent(ProfileEvent.NavigateToTerms) }
                )
                SettingsRow(
                    icon = Icons.Default.Settings,
                    iconTint = FarmerTheme.colors.textSecondary,
                    iconContainer = FarmerTheme.colors.surfaceMuted,
                    title = strings.settings,
                    showDivider = false,
                    onClick = { viewModel.onEvent(ProfileEvent.NavigateToSettings) }
                )
            }

            LogoutRow(label = strings.logout, onClick = { viewModel.onEvent(ProfileEvent.ConfirmLogout) })

            VersionFooter(
                label = "${strings.appName} v${BuildConfig.VERSION_NAME}",
                tagline = strings.appTagline
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (state.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(ProfileEvent.DismissLogout) },
            title = { Text(strings.logoutTitle) },
            text = { Text(strings.logoutMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.onEvent(ProfileEvent.SignOut) }) {
                    Text(strings.yes, color = FarmerTheme.colors.alertRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onEvent(ProfileEvent.DismissLogout) }) {
                    Text(strings.no)
                }
            }
        )
    }
}

@Composable
private fun LogoutRow(label: String, onClick: () -> Unit) {
    val colors = FarmerTheme.colors
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = FarmerSpacing.lg),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = colors.softRed)
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TintedIconTile(
                icon = Icons.Default.Logout,
                tint = colors.alertRed,
                container = colors.surface
            )
            Spacer(Modifier.width(FarmerSpacing.md))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = colors.alertRed
            )
        }
    }
}

@Composable
private fun IdentityCard(
    state: ProfileState,
    strings: AppStrings,
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
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(colors.softMint, colors.softGreen)))
                .padding(FarmerSpacing.xl)
        ) {
            if (state.isEditing) {
                ProfileEditForm(
                    state = state,
                    strings = strings,
                    onSave = onSaveProfile,
                    onCancel = onCancelEditing,
                    onNameChange = onNameChange,
                    onPhoneChange = onPhoneChange
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    InitialsAvatar(name = state.displayName)

                    Spacer(Modifier.width(FarmerSpacing.lg))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.primaryDeep
                        )
                        Spacer(Modifier.height(FarmerSpacing.xs))
                        Text(
                            text = phoneWithDialCode(state.userPhone),
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.textSecondary
                        )
                        Spacer(Modifier.height(FarmerSpacing.xs))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(FarmerSpacing.xs))
                            Text(
                                text = state.locationName,
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textSecondary
                            )
                        }
                    }

                    EditPillButton(onClick = onStartEditing, label = strings.editAction)
                }
            }
        }
    }
}

@Composable
private fun ProfileEditForm(
    state: ProfileState,
    strings: AppStrings,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit
) {
    val colors = FarmerTheme.colors

    OutlinedTextField(
        value = state.tempName,
        onValueChange = onNameChange,
        label = { Text(strings.nameField) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        colors = editFieldColors()
    )

    Spacer(Modifier.height(FarmerSpacing.md))

    OutlinedTextField(
        value = state.tempPhone,
        onValueChange = onPhoneChange,
        label = { Text(strings.phoneField) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        prefix = {
            Text(text = "+91 ", color = colors.textSecondary, style = MaterialTheme.typography.bodyLarge)
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
        isError = state.phoneError != null,
        supportingText = state.phoneError?.let { error ->
            { Text(error, color = MaterialTheme.colorScheme.error) }
        },
        colors = editFieldColors()
    )

    Spacer(Modifier.height(FarmerSpacing.lg))

    Row(horizontalArrangement = Arrangement.spacedBy(FarmerSpacing.s)) {
        Button(
            onClick = onSave,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(strings.saveAction)
        }
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(strings.cancelAction, color = colors.textSecondary)
        }
    }
}

@Composable
private fun editFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = FarmerTheme.colors.primary,
    unfocusedBorderColor = FarmerTheme.colors.outline,
    focusedContainerColor = FarmerTheme.colors.surface.copy(alpha = 0.7f),
    unfocusedContainerColor = FarmerTheme.colors.surface.copy(alpha = 0.7f)
)

@Composable
private fun InitialsAvatar(name: String) {
    val colors = FarmerTheme.colors
    val initials = name.trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifBlank { "?" }

    Box(
        modifier = Modifier
            .size(84.dp)
            .clip(CircleShape)
            .background(colors.surface)
            .border(3.dp, colors.primary.copy(alpha = 0.35f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = colors.primary
        )
    }
}

@Composable
private fun EditPillButton(onClick: () -> Unit, label: String) {
    val colors = FarmerTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.primary)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Icon(Icons.Default.Edit, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(FarmerSpacing.xs))
        Text(text = label, color = colors.onPrimary, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
private fun MenuSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = FarmerTheme.colors
    Column(modifier = Modifier.padding(top = FarmerSpacing.lg)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = colors.textTertiary,
            modifier = Modifier.padding(start = FarmerSpacing.xs, bottom = FarmerSpacing.s)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface)
        ) {
            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = FarmerSpacing.xs), content = content)
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconTint: Color,
    iconContainer: Color,
    title: String,
    onClick: () -> Unit,
    showDivider: Boolean,
    trailing: String? = null
) {
    val colors = FarmerTheme.colors
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TintedIconTile(icon = icon, tint = iconTint, container = iconContainer)
            Spacer(Modifier.width(FarmerSpacing.md))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            if (trailing != null) {
                Text(
                    text = trailing,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary
                )
                Spacer(Modifier.width(FarmerSpacing.xs))
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textTertiary,
                modifier = Modifier.size(16.dp)
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 48.dp),
                thickness = 1.dp,
                color = colors.outline
            )
        }
    }
}

@Composable
private fun TintedIconTile(icon: ImageVector, tint: Color, container: Color) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(container),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(19.dp))
    }
}

@Composable
private fun VersionFooter(label: String, tagline: String) {
    val colors = FarmerTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = FarmerSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary
        )
        Text(
            text = tagline,
            style = MaterialTheme.typography.labelSmall,
            color = colors.textTertiary.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

private fun phoneWithDialCode(phone: String): String =
    if (phone.startsWith("+91")) phone else "+91 $phone"

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    AndroidFarmerFriendTheme {
        ProfileScreen()
    }
}
