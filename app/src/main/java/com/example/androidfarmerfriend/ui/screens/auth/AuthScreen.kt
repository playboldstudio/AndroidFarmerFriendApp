package com.example.androidfarmerfriend.ui.screens.auth

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    initialMode: AuthMode = AuthMode.SIGN_IN,
    onDismissed: (() -> Unit)? = null,
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val strings = LocalAppStrings.current
    val colors = FarmerTheme.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(initialMode) {
        viewModel.onEvent(AuthEvent.SetMode(initialMode))
    }
    // Keep the ViewModel's error copy in the active language.
    LaunchedEffect(strings) {
        viewModel.setStrings(strings)
    }
    val googleClientId = remember { serverClientId(context) }

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    fun submit() = viewModel.onEvent(AuthEvent.Submit)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(colors.softMint, colors.background)))
            .imePadding()
    ) {
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

            Spacer(Modifier.height(FarmerSpacing.lg))

            ModeSwitch(
                mode = state.mode,
                signInLabel = strings.signInAction,
                signUpLabel = strings.signUpAction,
                onModeSelected = { viewModel.onEvent(AuthEvent.SetMode(it)) }
            )

            Spacer(Modifier.height(FarmerSpacing.lg))

            if (state.mode == AuthMode.SIGN_UP) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = { viewModel.onEvent(AuthEvent.UpdateName(it)) },
                    label = { Text(strings.nameField) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(FarmerSpacing.s))
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
                Spacer(Modifier.height(FarmerSpacing.s))
            }

            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.onEvent(AuthEvent.UpdateEmail(it)) },
                label = { Text(strings.emailField) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(FarmerSpacing.s))

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
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    submit()
                }),
                modifier = Modifier.fillMaxWidth()
            )

            if (state.mode == AuthMode.SIGN_IN && !state.isLoading) {
                Text(
                    text = strings.forgotPassword,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.End)
                        .clickable { viewModel.onEvent(AuthEvent.ForgotPassword) }
                        .padding(vertical = 8.dp)
                )
            }

            if (state.info != null) {
                Spacer(Modifier.height(FarmerSpacing.s))
                Text(
                    text = state.info.orEmpty(),
                    color = colors.primary,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (state.error != null) {
                Spacer(Modifier.height(FarmerSpacing.s))
                Text(
                    text = state.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(FarmerSpacing.lg))

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

            Spacer(Modifier.height(FarmerSpacing.s))

            OutlinedButton(
                onClick = {
                    scope.launch {
                        val clientId = googleClientId
                        if (clientId == null) {
                            viewModel.onEvent(AuthEvent.SetError(strings.googleUnavailable))
                            return@launch
                        }
                        signInWithGoogle(
                            context = context,
                            clientId = clientId,
                            strings = strings,
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
                GoogleMark()
                Spacer(Modifier.width(FarmerSpacing.s))
                Text(strings.googleContinue, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
            }

            Spacer(Modifier.height(FarmerSpacing.xl))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (state.mode == AuthMode.SIGN_IN) strings.newHerePrompt else strings.alreadyRegisteredPrompt,
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary
                )
                Spacer(Modifier.width(FarmerSpacing.xs))
                Text(
                    text = if (state.mode == AuthMode.SIGN_IN) strings.signUpAction else strings.signInAction,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = colors.primary,
                    modifier = Modifier.clickable {
                        viewModel.onEvent(
                            AuthEvent.SetMode(if (state.mode == AuthMode.SIGN_IN) AuthMode.SIGN_UP else AuthMode.SIGN_IN)
                        )
                    }
                )
            }

            Spacer(Modifier.height(FarmerSpacing.xxl))
        }
    }
}

/** Fires the Credential Manager Google flow and forwards the id token. */
private suspend fun signInWithGoogle(
    context: Context,
    clientId: String,
    strings: com.example.androidfarmerfriend.data.localization.AppStrings,
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
            onError(strings.googleSignInFailed)
        }
    } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
        // User closed the account picker — nothing to do.
    } catch (e: androidx.credentials.exceptions.NoCredentialException) {
        // No Google accounts are available on the device (or none match the
        // registered OAuth clients). Surface a clear message instead of the raw
        // system error so the user can act on it (add a Google account in
        // Settings, or check the Firebase console's SHA-1 registration).
        onError(strings.noGoogleAccount)
    } catch (e: GetCredentialException) {
        onError(e.message ?: strings.noGoogleAccount)
    } catch (e: Exception) {
        onError(e.message ?: strings.googleSignInFailed)
    }
}

/**
 * The official four-color "G" drawn with Canvas paths — no asset needed.
 * Geometry follows the standard 18×18 grid scaled to [size].
 */
@Composable
private fun GoogleMark(size: androidx.compose.ui.unit.Dp = 20.dp) {
    val blue = Color(0xFF4285F4)
    val green = Color(0xFF34A853)
    val yellow = Color(0xFFFBBC05)
    val red = Color(0xFFEA4335)
    Canvas(modifier = Modifier.size(size)) {
        val s = this.size.width / 18f
        fun path(block: (androidx.compose.ui.graphics.Path.() -> Unit)): androidx.compose.ui.graphics.Path =
            androidx.compose.ui.graphics.Path().apply(block)

        drawPath(path({
            moveTo(17.64f * s, 9.2045f * s)
            cubicTo(17.64f * s, 8.56636f * s, 17.5827f * s, 7.95273f * s, 17.4764f * s, 7.36364f * s)
            lineTo(9f * s, 7.36364f * s)
            lineTo(9f * s, 10.845f * s)
            lineTo(13.8436f * s, 10.845f * s)
            cubicTo(13.635f * s, 11.97f * s, 13.0009f * s, 12.9232f * s, 12.0477f * s, 13.5614f * s)
            lineTo(12.0477f * s, 15.8195f * s)
            lineTo(14.9564f * s, 15.8195f * s)
            cubicTo(16.6582f * s, 14.2527f * s, 17.64f * s, 11.9455f * s, 17.64f * s, 9.2045f * s)
            close()
        }), color = blue)

        drawPath(path({
            moveTo(9f * s, 18f * s)
            cubicTo(11.43f * s, 18f * s, 13.4673f * s, 17.1941f * s, 14.9564f * s, 15.8195f * s)
            lineTo(12.0477f * s, 13.5614f * s)
            cubicTo(11.2459f * s, 14.1014f * s, 10.2109f * s, 14.4205f * s, 9f * s, 14.4205f * s)
            cubicTo(6.69818f * s, 14.4205f * s, 4.75159f * s, 12.8373f * s, 4.06364f * s, 10.71f * s)
            lineTo(1.03841f * s, 10.71f * s)
            lineTo(1.03841f * s, 13.0418f * s)
            cubicTo(2.51955f * s, 15.9832f * s, 5.53159f * s, 18f * s, 9f * s, 18f * s)
            close()
        }), color = green)

        drawPath(path({
            moveTo(4.06364f * s, 10.71f * s)
            cubicTo(3.88432f * s, 10.17f * s, 3.78409f * s, 9.59318f * s, 3.78409f * s, 9f * s)
            cubicTo(3.78409f * s, 8.40682f * s, 3.88432f * s, 7.83f * s, 4.06364f * s, 7.29f * s)
            lineTo(4.06364f * s, 4.95818f * s)
            lineTo(1.03841f * s, 4.95818f * s)
            cubicTo(0.430909f * s, 6.17318f * s, 0.0818182f * s, 7.54772f * s, 0.0818182f * s, 9f * s)
            cubicTo(0.0818182f * s, 10.4523f * s, 0.430909f * s, 11.8268f * s, 1.03841f * s, 13.0418f * s)
            lineTo(4.06364f * s, 10.71f * s)
            close()
        }), color = yellow)

        drawPath(path({
            moveTo(9f * s, 3.57955f * s)
            cubicTo(10.3214f * s, 3.57955f * s, 11.5077f * s, 4.03364f * s, 12.4405f * s, 4.92545f * s)
            lineTo(15.0218f * s, 2.34409f * s)
            cubicTo(13.4632f * s, 0.891818f * s, 11.4259f * s, 0f * s, 9f * s, 0f * s)
            cubicTo(5.53159f * s, 0f * s, 2.51955f * s, 2.01682f * s, 1.03841f * s, 4.95818f * s)
            lineTo(4.06364f * s, 7.29f * s)
            cubicTo(4.75159f * s, 5.16273f * s, 6.69818f * s, 3.57955f * s, 9f * s, 3.57955f * s)
            close()
        }), color = red)
    }
}

private fun serverClientId(context: Context): String? {
    return try {
        context.getString(R.string.default_web_client_id)
    } catch (_: Exception) {
        null
    }
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
