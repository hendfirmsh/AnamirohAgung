package com.agunganamiroh.motion

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun shimmerBrush(targetValue: Float): Brush {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.1f),
        Color.LightGray.copy(alpha = 0.3f)
    )
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = targetValue, y = targetValue)
    )
}

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(12.dp)
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = MotionTheme.Duration.shimmer,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush = shimmerBrush(translate))
    )
}

@Composable
fun StatCardSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            repeat(4) {
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp)
                )
            }
        }
    }
}

@Composable
fun ListCardSkeleton(count: Int = 5) {
    Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
        repeat(count) {
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .padding(horizontal = 20.dp)
            )
        }
    }
}

@Composable
fun DetailSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            ShimmerBox(modifier = Modifier.size(56.dp), shape = CircleShape)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.6f).height(18.dp))
                Spacer(modifier = Modifier.height(6.dp))
                ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f).height(14.dp))
            }
        }
        repeat(4) {
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(60.dp))
        }
    }
}

@Composable
fun DashboardStatSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.4f).height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
        ) {
            repeat(4) {
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(90.dp)
                )
            }
        }
    }
}

@Composable
fun MenuGridSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
    ) {
        ShimmerBox(modifier = Modifier.fillMaxWidth(0.3f).height(16.dp))
        repeat(3) {
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(64.dp))
        }
    }
}
