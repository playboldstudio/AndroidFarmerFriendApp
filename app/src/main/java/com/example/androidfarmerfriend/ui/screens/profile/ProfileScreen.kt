package com.example.androidfarmerfriend.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.BuildConfig
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.ui.auth.AuthBridge
import com.example.androidfarmerfriend.ui.components.HeroTitle
import com.example.androidfarmerfriend.ui.screens.auth.AuthMode
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme
import com.example.androidfarmerfriend.ui.theme.FarmerSpacing
import com.example.androidfarmerfriend.ui.theme.FarmerTheme

// Google brand red is fixed across themes (official mark color).


@Composable
fun ProfileScreen(
    onNavigate: (String) -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val strings = LocalAppStrings.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val isSignedIn = AuthBridge.LocalIsSignedIn.current
    val requestSignIn = AuthBridge.LocalRequestSignIn.current

    val navEvent by viewModel.navigation.collectAsStateWithLifecycle()
    LaunchedEffect(navEvent) {
        navEvent?.let { route ->
            onNavigate(route)
            viewModel.onNavigated()
        }
    }

    // Re-read prefs + Firestore every time this screen appears so signup changes show up.
    LaunchedEffect(Unit) {
        viewModel.refresh()
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
        com.example.androidfarmerfriend.ui.components.CenteredMaxWidth(
            maxWidth = 640.dp,
            modifier = Modifier.fillMaxSize()
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

                if (isSignedIn) {
                    IdentityCard(
                        state = state,
                        strings = strings,
                        onStartEditing = { viewModel.onEvent(ProfileEvent.StartEditing) }
                    )

                    MenuSection(title = stringAccount(strings)) {
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
                            trailing = strings.soonLabel,
                            trailMuted = true,
                            showDivider = true,
                            onClick = { viewModel.onEvent(ProfileEvent.NavigateToLands) }
                        )
                        SettingsRow(
                            icon = Icons.Default.Logout,
                            iconTint = FarmerTheme.colors.alertRed,
                            iconContainer = FarmerTheme.colors.softRed,
                            title = strings.logout,
                            danger = true,
                            showDivider = false,
                            onClick = { viewModel.onEvent(ProfileEvent.ConfirmLogout) }
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
                            trailing = strings.onLabel,
                            trailAccent = FarmerTheme.colors.primary,
                            showDivider = false,
                            onClick = { viewModel.onEvent(ProfileEvent.NavigateToNotifications) }
                        )
                    }
                } else {
                    GuestHero(strings = strings)

                    Spacer(Modifier.height(FarmerSpacing.md))

                    Button(
                        onClick = { requestSignIn(AuthMode.SIGN_IN) {} },
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(vertical = 13.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(strings.signInAction, fontWeight = FontWeight.Bold)
                    }

                    UnlockTeaserCard(strings = strings)

                    MenuSection(title = strings.preferencesSection) {
                        SettingsRow(
                            icon = Icons.Default.Language,
                            iconTint = FarmerTheme.colors.weatherBlue,
                            iconContainer = FarmerTheme.colors.softBlue,
                            title = strings.language,
                            trailing = state.languageLabel,
                            showDivider = false,
                            onClick = { viewModel.onEvent(ProfileEvent.NavigateToLanguage) }
                        )
                    }
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
                        showDivider = false,
                        onClick = { viewModel.onEvent(ProfileEvent.NavigateToTerms) }
                    )
                }

                VersionFooter(
                    label = "${strings.appName} v${BuildConfig.VERSION_NAME}",
                    tagline = strings.appTagline
                )
            }
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

    if (state.isEditing) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(ProfileEvent.CancelEditing) },
            title = {
                Text(
                    text = strings.editAction,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = FarmerTheme.colors.primary
                )
            },
            text = {
                ProfileEditForm(
                    state = state,
                    strings = strings,
                    onNameChange = { viewModel.onEvent(ProfileEvent.UpdateTempName(it)) },
                    onPhoneChange = { viewModel.onEvent(ProfileEvent.UpdateTempPhone(it)) }
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onEvent(ProfileEvent.SaveProfile) },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(strings.saveAction)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.onEvent(ProfileEvent.CancelEditing) },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(strings.cancelAction, color = FarmerTheme.colors.textSecondary)
                }
            }
        )
    }
}

// English-only section label until a dedicated key exists in all languages.
private fun stringAccount(strings: AppStrings): String =
    strings.accountSection

/* ----------------------------- Guest ----------------------------- */

@Composable
private fun GuestHero(strings: AppStrings) {
    val colors = FarmerTheme.colors
    IdentitySurface {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(colors.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.width(FarmerSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = strings.guestLabel,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = colors.primaryDeep
            )
            Text(
                text = strings.guestSubtitle,
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun UnlockTeaserCard(strings: AppStrings) {
    val colors = FarmerTheme.colors
    val rows = listOf(
        Icons.Default.Share to strings.unlockShare,
        Icons.Default.Sync to strings.unlockSync,
        Icons.Default.Notifications to strings.unlockAlerts
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = FarmerSpacing.md),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.goldBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(colors.unlockTop, colors.unlockBottom)))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Lock,
                    contentDescription = null,
                    tint = colors.goldTitle,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(FarmerSpacing.xs))
                Text(
                    text = strings.unlockTitle,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = colors.goldTitle
                )
            }
            Spacer(Modifier.height(FarmerSpacing.s))
            rows.forEach { (icon, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = FarmerSpacing.s)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.unlockTileBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = colors.unlockTileIcon,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Spacer(Modifier.width(FarmerSpacing.md))
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.goldBody
                    )
                }
            }
        }
    }
}

/* --------------------------- Signed in --------------------------- */

@Composable
private fun IdentityCard(
    state: ProfileState,
    strings: AppStrings,
    onStartEditing: () -> Unit
) {
    IdentitySurface {
        InitialsAvatar(name = state.displayName)

        Spacer(Modifier.width(FarmerSpacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = state.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = FarmerTheme.colors.primaryDeep,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Spacer(Modifier.width(FarmerSpacing.xs))
                Icon(
                    Icons.Default.Verified,
                    contentDescription = null,
                    tint = FarmerTheme.colors.primaryBright,
                    modifier = Modifier.size(16.dp)
                )
            }
            if (state.userPhone.isNotBlank() && state.userPhone != "9876543210") {
                Spacer(Modifier.height(FarmerSpacing.xs))
                Text(
                    text = phoneWithDialCode(state.userPhone),
                    style = MaterialTheme.typography.labelMedium,
                    color = FarmerTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            if (state.userEmail.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = state.userEmail,
                    style = MaterialTheme.typography.labelSmall,
                    color = FarmerTheme.colors.textTertiary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }

        EditPillButton(onClick = onStartEditing, label = strings.editAction)
    }
}

@Composable
private fun IdentitySurface(content: @Composable RowScope.() -> Unit) {
    val colors = FarmerTheme.colors
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(colors.softMint, colors.softGreen)))
                .padding(FarmerSpacing.xl),
            content = content
        )
    }
}

@Composable
internal fun ProfileEditForm(
    state: ProfileState,
    strings: AppStrings,
    onNameChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit
) {
    val colors = FarmerTheme.colors

    Column(verticalArrangement = Arrangement.spacedBy(FarmerSpacing.s)) {
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

        OutlinedTextField(
            value = state.tempPhone,
            onValueChange = onPhoneChange,
            label = { Text(strings.phoneField) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            prefix = {
                Text(
                    text = "+91 ",
                    color = colors.textSecondary,
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            isError = state.phoneError != null,
            supportingText = state.phoneError?.let { error ->
                { Text(error, color = MaterialTheme.colorScheme.error) }
            },
            colors = editFieldColors()
        )
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
            .size(72.dp)
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
        Icon(
            Icons.Default.Edit,
            contentDescription = null,
            tint = colors.onPrimary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(FarmerSpacing.xs))
        Text(text = label, color = colors.onPrimary, style = MaterialTheme.typography.labelMedium)
    }
}

/* ------------------------------ Menus ---------------------------- */

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
            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = FarmerSpacing.xs),
                content = content
            )
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
    trailing: String? = null,
    trailAccent: Color? = null,
    trailMuted: Boolean = false,
    danger: Boolean = false
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
            TintedIconTile(
                icon = icon,
                tint = iconTint,
                container = iconContainer
            )
            Spacer(Modifier.width(FarmerSpacing.md))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (danger) colors.alertRed else colors.textPrimary,
                modifier = Modifier.weight(1f)
            )
            if (trailing != null) {
                Text(
                    text = trailing,
                    style = MaterialTheme.typography.labelMedium,
                    color = when {
                        trailAccent != null -> trailAccent
                        trailMuted -> colors.textTertiary
                        else -> colors.textSecondary
                    }
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
