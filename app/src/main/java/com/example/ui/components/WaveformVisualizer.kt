package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple

@Composable
fun WaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 5,
    barWidth: Dp = 3.dp,
    barHeight: Dp = 18.dp,
    activeColor: Color = AccentCyan
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")

    val h1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h1"
    )
    val h2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h2"
    )
    val h3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h3"
    )
    val h4 by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(410, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h4"
    )
    val h5 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(470, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "h5"
    )

    val heights = listOf(h1, h2, h3, h4, h5)

    Row(
        modifier = modifier.height(barHeight),
        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(barCount) { index ->
            val scale = if (isPlaying) heights[index % heights.size] else 0.25f
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight(scale)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(AccentPurple, activeColor)
                        )
                    )
            )
        }
    }
}
