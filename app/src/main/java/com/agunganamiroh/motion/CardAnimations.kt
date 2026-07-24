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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay

fun Modifier.animateEntrance(
    delay: Int = 0,
    slidePx: Float = MotionTheme.Slide.cardOffsetPx,
    scaleEnter: Float = MotionTheme.Scale.cardEnter
): Modifier = composed {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delay.toLong())
        visible = true
    }
    val alphaProp by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = MotionTheme.Spec.spring,
        label = "cardAlpha"
    )
    val translateY by animateFloatAsState(
        targetValue = if (visible) 0f else slidePx,
        animationSpec = MotionTheme.Spec.spring,
        label = "cardTranslate"
    )
    val scaleProp by animateFloatAsState(
        targetValue = if (visible) 1f else scaleEnter,
        animationSpec = MotionTheme.Spec.spring,
        label = "cardScale"
    )
    Modifier
        .alpha(alphaProp)
        .graphicsLayer { translationY = translateY }
        .scale(scaleProp)
}
