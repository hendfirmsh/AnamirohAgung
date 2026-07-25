package com.agunganamiroh.motion

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
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

fun Modifier.heroEnter(
    delay: Int = 0,
    slidePx: Float = 20f
): Modifier = composed {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delay.toLong())
        visible = true
    }
    val alphaProp by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = MotionTheme.Spec.spring,
        label = "heroAlpha"
    )
    val translateY by animateFloatAsState(
        targetValue = if (visible) 0f else slidePx,
        animationSpec = MotionTheme.Spec.spring,
        label = "heroTranslate"
    )
    Modifier
        .alpha(alphaProp)
        .graphicsLayer { translationY = translateY }
}

@Composable
fun heroAlphaIn(
    visible: Boolean,
    duration: Int = MotionTheme.Duration.normal
): Float {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = MotionTheme.Spec.spring,
        label = "heroAlphaIn"
    )
    return alpha
}

@Composable
fun heroScaleIn(
    visible: Boolean,
    targetScale: Float = 1f,
    startScale: Float = MotionTheme.Scale.cardEnter
): Float {
    val scale by animateFloatAsState(
        targetValue = if (visible) targetScale else startScale,
        animationSpec = MotionTheme.Spec.spring,
        label = "heroScale"
    )
    return scale
}
