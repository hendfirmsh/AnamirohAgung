package com.agunganamiroh.motion

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

fun Modifier.staggerItem(
    index: Int,
    offsetMs: Int = MotionTheme.Duration.staggerOffset,
    slidePx: Float = MotionTheme.Slide.staggerOffsetPx
): Modifier = composed {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay((index * offsetMs).toLong())
        visible = true
    }
    val alphaProp by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = MotionTheme.Spec.spring,
        label = "staggerAlpha"
    )
    val translateY by animateFloatAsState(
        targetValue = if (visible) 0f else slidePx,
        animationSpec = MotionTheme.Spec.spring,
        label = "staggerTranslate"
    )
    Modifier
        .alpha(alphaProp)
        .graphicsLayer { translationY = translateY }
}
