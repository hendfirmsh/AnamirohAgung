package com.agunganamiroh.motion

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Easing as ComposeEasing

object MotionTheme {

    object Duration {
        val instant = 150
        val short = 200
        val normal = 300
        val medium = 400
        val slow = 600
        val shimmer = 1200
        val numberCount = 800
        val progress = 1000
        val snackbar = 250
        val staggerOffset = 60
    }

    val easingStandard: ComposeEasing = FastOutSlowInEasing
    val easingDecelerate: ComposeEasing = FastOutSlowInEasing
    val easingAccelerate: ComposeEasing = FastOutSlowInEasing

    object Spec {
        fun default() = tween<Float>(durationMillis = Duration.normal, easing = easingStandard)
        fun slowTween() = tween<Float>(durationMillis = Duration.slow, easing = easingStandard)
        val spring = spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
        val springLight = spring<Float>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        )
        val springStiff = spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        )
        fun press() = tween<Float>(durationMillis = Duration.instant, easing = easingStandard)

        fun navSlide() = tween<Int>(
            durationMillis = Duration.normal,
            easing = easingStandard
        )
    }

    object Scale {
        val press = 0.96f
        val pressLight = 0.98f
        val cardEnter = 0.95f
        val dialog = 0.85f
    }

    object Slide {
        val navOffsetFraction = 0.125f
        val cardOffsetPx = 40f
        val staggerOffsetPx = 30f
        val snackbarOffsetPx = 80f
    }
}
