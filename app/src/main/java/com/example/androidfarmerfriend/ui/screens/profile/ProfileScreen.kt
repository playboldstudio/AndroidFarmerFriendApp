package com.example.androidfarmerfriend.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.data.localization.AppStrings
import com.example.androidfarmerfriend.data.localization.Language
import com.example.androidfarmerfriend.data.localization.LanguagePrefs
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.data.location.LocationPrefs
import com.example.androidfarmerfriend.ui.components.ScreenHeader
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme
import com.example.androidfarmerfriend.ui.theme.FarmerGreenPrimary
import com.example.androidfarmerfriend.ui.theme.GrayText
import com.example.androidfarmerfriend.ui.theme.TrendRed

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

            Spacer(modifier = Modifier.height(8.dp))

            // Profile Info Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(FarmerGreenPrimary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = FarmerGreenPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        if (state.isEditing) {
                            OutlinedTextField(
                                value = state.tempName,
                                onValueChange = { viewModel.onEvent(ProfileEvent.UpdateTempName(it)) },
                                label = { Text(strings.nameField, fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = MaterialTheme.shapes.medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = state.tempPhone,
                                onValueChange = { viewModel.onEvent(ProfileEvent.UpdateTempPhone(it)) },
                                label = { Text(strings.phoneField, fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = MaterialTheme.shapes.medium
                            )
                        } else {
                            Text(
                                text = state.userName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = state.userPhone,
                                style = MaterialTheme.typography.bodyMedium,
                                color = GrayText
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = FarmerGreenPrimary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = location.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = GrayText
                                )
                            }
                        }
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(onClick = {
                            if (state.isEditing) {
                                viewModel.onEvent(ProfileEvent.SaveProfile)
                            } else {
                                viewModel.onEvent(ProfileEvent.StartEditing)
                            }
                        }) {
                            Icon(
                                if (state.isEditing) Icons.Default.Save else Icons.Default.Edit,
                                contentDescription = null,
                                tint = FarmerGreenPrimary
                            )
                        }
                        
                        if (state.isEditing) {
                            IconButton(onClick = { viewModel.onEvent(ProfileEvent.CancelEditing) }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = TrendRed)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Menu Items
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

            // Version Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${strings.appName} v1.0.0",
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

@Composable
fun ProfileMenuItem(title: String, icon: ImageVector, trailingText: String? = null, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp),
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
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            
            if (trailingText != null) {
                Text(
                    text = trailingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = FarmerGreenPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = GrayText.copy(alpha = 0.5f),
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
