package com.agunganamiroh.motion

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

@Composable
fun AnimatedCounter(
    target: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    fontWeight: FontWeight? = null,
    duration: Int = MotionTheme.Duration.numberCount,
    format: (Long) -> String = { it.toString() },
    color: Color? = null
) {
    val animatedValue = remember { Animatable(0f) }
    LaunchedEffect(target) {
        animatedValue.snapTo(0f)
        animatedValue.animateTo(
            targetValue = target.toFloat(),
            animationSpec = tween(
                durationMillis = duration,
                easing = MotionTheme.easingDecelerate
            )
        )
    }
    val displayValue = animatedValue.value.toLong()
    val formatted = format(displayValue)
    if (color != null) {
        Text(
            text = formatted,
            modifier = modifier,
            style = style,
            fontWeight = fontWeight,
            color = color
        )
    } else {
        Text(
            text = formatted,
            modifier = modifier,
            style = style,
            fontWeight = fontWeight
        )
    }
}
