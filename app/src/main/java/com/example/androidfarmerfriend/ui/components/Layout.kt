package com.example.androidfarmerfriend.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Content cap chosen from the current window width so layouts read well on
 * both phones and tablets instead of always centering a narrow 640dp column.
 */
@Composable
fun adaptiveContentMaxWidth(): Dp {
    val width = LocalConfiguration.current.screenWidthDp
    return when {
        width >= 840 -> 1080.dp // expanded: tablets / large landscape phone
        width >= 600 -> 720.dp  // medium: large phones & small tablets
        else -> 480.dp          // compact: phones
    }
}

/**
 * Centers screen content and caps its width so layouts stay readable in
 * landscape and on tablets instead of stretching edge to edge. On wide
 * (tablet) screens the cap widens adaptively so the content fills more of the
 * display rather than sitting in a narrow centered column.
 */
@Composable
fun CenteredMaxWidth(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val maxWidth = adaptiveContentMaxWidth()
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Box(modifier = Modifier.widthIn(max = maxWidth).fillMaxSize(), content = content)
    }
}
