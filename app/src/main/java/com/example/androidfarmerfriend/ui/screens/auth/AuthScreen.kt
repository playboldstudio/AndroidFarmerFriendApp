package com.example.androidfarmerfriend.ui.screens.auth

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.credentials.CustomCredential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.androidfarmerfriend.R
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.ui.theme.AndroidFarmerFriendTheme
import com.example.androidfarmerfriend.ui.theme.FarmerSpacing
import com.example.androidfarmerfriend.ui.theme.FarmerTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onDismissed: (() -> Unit)? = null,
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val strings = LocalAppStrings.current
    val colors = FarmerTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var passwordVisible by remember { mutableStateOf(false) }
    val googleClientId = remember { serverClientId(context) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colors.softMint, colors.background)))
            .imePadding()
    ) {
        if (onDismissed != null) {
            IconButton(
                onClick = onDismissed,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(FarmerSpacing.lg)
            ) {
                Icon(Icons.Default.Close, contentDescription = null, tint = colors.textSecondary)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = FarmerSpacing.xl)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(64.dp))

            Image(
                painter = painterResource(R.mipmap.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(88.dp)
            )
            Spacer(Modifier.height(FarmerSpacing.s))
            Text(
                text = strings.appName,
                style = MaterialTheme.typography.displaySmall,
                color = colors.primaryDeep
            )

            Spacer(Modifier.height(FarmerSpacing.xxl))

            ModeSwitch(
                mode = state.mode,
                signInLabel = strings.signInAction,
                signUpLabel = strings.signUpAction,
                onModeSelected = { viewModel.onEvent(AuthEvent.SetMode(it)) }
            )

            if (state.mode == AuthMode.SIGN_UP) {
                Spacer(Modifier.height(FarmerSpacing.md))
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { viewModel.onEvent(AuthEvent.UpdateName(it)) },
                    label = { Text(strings.nameField) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(FarmerSpacing.md))
                OutlinedTextField(
                    value = state.phone,
                    onValueChange = { viewModel.onEvent(AuthEvent.UpdatePhone(it)) },
                    label = { Text(strings.phoneField) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    prefix = { Text("+91 ", color = colors.textSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(FarmerSpacing.md))

            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.onEvent(AuthEvent.UpdateEmail(it)) },
                label = { Text(strings.emailField) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(FarmerSpacing.md))

            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.onEvent(AuthEvent.UpdatePassword(it)) },
                label = { Text(strings.passwordField) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (state.error != null) {
                Spacer(Modifier.height(FarmerSpacing.s))
                Text(
                    text = state.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(FarmerSpacing.xl))

            Button(
                onClick = { viewModel.onEvent(AuthEvent.Submit) },
                enabled = !state.isLoading,
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = colors.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = if (state.mode == AuthMode.SIGN_UP) strings.signUpAction else strings.signInAction,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (googleClientId != null) {
                Spacer(Modifier.height(FarmerSpacing.md))
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            signInWithGoogle(
                                context = context,
                                clientId = googleClientId,
                                onSuccess = { idToken -> viewModel.onEvent(AuthEvent.GoogleSignedIn(idToken)) },
                                onError = { message -> viewModel.onEvent(AuthEvent.SetError(message)) }
                            )
                        }
                    },
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("G", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFDB4437))
                    Spacer(Modifier.width(FarmerSpacing.s))
                    Text("Continue with Google", fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                }
            }

            Spacer(Modifier.height(FarmerSpacing.xxl))
        }
    }
}

/** Fires the Credential Manager Google flow and forwards the id token. */
private suspend fun signInWithGoogle(
    context: Context,
    clientId: String,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val manager = CredentialManager.create(context)
        val option = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        val response = manager.getCredential(context, request)
        val credential = response.credential as? CustomCredential
        if (credential?.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val token = GoogleIdTokenCredential.createFrom(credential.data).idToken
            onSuccess(token)
        } else {
            onError("Google sign-in failed")
        }
    } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
        // User closed the account picker — nothing to do.
    } catch (e: GetCredentialException) {
        onError(e.message ?: "No Google account available")
    } catch (e: Exception) {
        onError(e.message ?: "Google sign-in failed")
    }
}

@SuppressLint("DiscouragedApi")
private fun serverClientId(context: Context): String? {
    val id = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
    return if (id != 0) context.getString(id) else null
}

@Composable
private fun ModeSwitch(
    mode: AuthMode,
    signInLabel: String,
    signUpLabel: String,
    onModeSelected: (AuthMode) -> Unit
) {
    val colors = FarmerTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceMuted)
            .padding(4.dp)
    ) {
        ModeOption(mode == AuthMode.SIGN_IN, signInLabel, Modifier.weight(1f)) { onModeSelected(AuthMode.SIGN_IN) }
        ModeOption(mode == AuthMode.SIGN_UP, signUpLabel, Modifier.weight(1f)) { onModeSelected(AuthMode.SIGN_UP) }
    }
}

@Composable
private fun ModeOption(selected: Boolean, label: String, modifier: Modifier, onClick: () -> Unit) {
    val colors = FarmerTheme.colors
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) colors.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (selected) colors.onPrimary else colors.textSecondary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    AndroidFarmerFriendTheme {
        AuthScreen()
    }
}
