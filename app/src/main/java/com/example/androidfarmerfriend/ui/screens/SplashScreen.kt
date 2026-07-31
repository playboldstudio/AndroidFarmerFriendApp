package com.example.androidfarmerfriend.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidfarmerfriend.R
import com.example.androidfarmerfriend.data.localization.LocalAppStrings
import com.example.androidfarmerfriend.ui.theme.FarmerTheme
import kotlinx.coroutines.delay

/**
 * Modern iPhone-style splash: warm background, app badge (rounded green
 * gradient + wheat mark), app name and tagline. The badge scales in and the
 * text fades in, then hands off to the main screen.
 *
 * @param onReady called once the first frame is drawn so the platform splash
 *   can be dismissed and this animation becomes visible.
 * @param onFinished called after the animation completes to show the app.
 */
@Composable
fun SplashScreen(onReady: () -> Unit = {}, onFinished: () -> Unit) {
    val strings = LocalAppStrings.current
    val colors = FarmerTheme.colors

    val logoScale = remember { Animatable(0.7f) }
    val textAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        onReady() // first frame drawn → drop the platform splash
        logoScale.animateTo(1f, animationSpec = tween(450, easing = FastOutSlowInEasing))
        textAlpha.animateTo(1f, animationSpec = tween(400))
        delay(900)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(colors.background, colors.surfaceMuted)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.splash_logo),
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale.value)
            )

            Spacer(modifier = Modifier.height(22.dp))

            val alpha = textAlpha.value
            Text(
                text = strings.appName,
                color = colors.textPrimary.copy(alpha = alpha),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = strings.appTagline,
                color = colors.textSecondary.copy(alpha = alpha),
                fontSize = 13.sp
            )
        }
    }
}
