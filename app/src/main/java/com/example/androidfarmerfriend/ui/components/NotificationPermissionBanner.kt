package com.example.androidfarmerfriend.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.example.androidfarmerfriend.ui.theme.FarmerTheme

private const val PREFS_NAME = "perm_prefs"
private const val KEY_NOTIF_ASKED = "notif_asked"

/**
 * Asks for POST_NOTIFICATIONS on Android 13+ from the Home screen.
 *
 * - Shows a friendly banner when the permission is not granted.
 * - "Allow" records the ask and fires the system permission dialog.
 * - The ✕ / Not now dismisses and records the ask (never nags again).
 * - If the user already denied once ("don't ask again" state), the banner
 *   instead offers to open the app's notification settings.
 */
@Composable
fun NotificationPermissionBanner(
    modifier: Modifier = Modifier,
    titleText: String = "Stay updated",
    bodyText: String = "Allow notifications to get price alerts, weather warnings and scheme updates.",
    allowText: String = "Allow",
    settingsText: String = "Open settings"
) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val colors = FarmerTheme.colors

    val prefs = context.getSharedPreferences(PREFS_NAME, 0)
    val askedBefore = prefs.getBoolean(KEY_NOTIF_ASKED, false)

    var dismissed by remember { mutableStateOf(askedBefore) }

    val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
        PackageManager.PERMISSION_GRANTED

    if (granted || dismissed) return

    // If we already asked once and got denied, the system dialog won't reappear
    // (Android marks it "don't ask again") — point the user at settings instead.
    val permanentlyDenied = askedBefore &&
        !ActivityCompat.shouldShowRequestPermissionRationale(
            context as androidx.activity.ComponentActivity,
            Manifest.permission.POST_NOTIFICATIONS
        )

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* dismissal already recorded before launching */ }

    val markAsked: () -> Unit = {
        prefs.edit { putBoolean(KEY_NOTIF_ASKED, true) }
        dismissed = true
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colors.softGreen,
        border = BorderStroke(1.dp, colors.softMint)
    ) {
        Row(
            modifier = Modifier.padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titleText,
                    color = colors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = bodyText,
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (permanentlyDenied) {
                Text(
                    text = settingsText,
                    color = colors.onPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.primary)
                        .clickable {
                            val intent = Intent(
                                Settings.ACTION_APP_NOTIFICATION_SETTINGS,
                                Uri.parse("package:${context.packageName}")
                            ).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            context.startActivity(intent)
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            } else {
                Text(
                    text = allowText,
                    color = colors.onPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.primary)
                        .clickable {
                            markAsked()
                            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
            Icon(
                Icons.Default.Close,
                contentDescription = "Not now",
                tint = colors.textTertiary,
                modifier = Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { markAsked() }
                    .padding(3.dp)
            )
        }
    }
}
