package com.agunganamiroh.motion

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset

object NavigationMotion {

    private val fadeSpec = tween<Float>(
        durationMillis = MotionTheme.Duration.normal,
        easing = MotionTheme.easingStandard
    )

    private val slideSpec = tween<IntOffset>(
        durationMillis = MotionTheme.Duration.normal,
        easing = MotionTheme.easingStandard
    )

    val enterTransition: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        fadeIn(animationSpec = fadeSpec) +
            slideInHorizontally(animationSpec = slideSpec) { w ->
                (w * MotionTheme.Slide.navOffsetFraction).toInt()
            }
    }

    val exitTransition: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        fadeOut(animationSpec = fadeSpec) +
            slideOutHorizontally(animationSpec = slideSpec) { w ->
                (w * MotionTheme.Slide.navOffsetFraction).toInt()
            }
    }

    val popEnterTransition: AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        fadeIn(animationSpec = fadeSpec) +
            slideInHorizontally(animationSpec = slideSpec) { w ->
                -(w * MotionTheme.Slide.navOffsetFraction).toInt()
            }
    }

    val popExitTransition: AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        fadeOut(animationSpec = fadeSpec) +
            slideOutHorizontally(animationSpec = slideSpec) { w ->
                -(w * MotionTheme.Slide.navOffsetFraction).toInt()
            }
    }
}
