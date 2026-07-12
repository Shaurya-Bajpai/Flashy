package com.dsb.flashy.screen.dashboard.items

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

@Composable
fun AnimatedBackground() {
    val infinite = rememberInfiniteTransition(label = "bg")

    val pulse by infinite.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    val stars = remember {
        List(90) {
            Triple(Random.nextFloat(), Random.nextFloat(), 0.05f + Random.nextFloat() * 0.25f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Very subtle warm amber wash — thin band at the top, not a full-screen circle
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFF8F00).copy(alpha = 0.028f + 0.018f * pulse),
                    Color(0xFFFFB300).copy(alpha = 0.012f + 0.008f * pulse),
                    Color.Transparent
                ),
                startY = 0f,
                endY = size.height * 0.38f
            )
        )

        // Warm star field — tiny 1px dots scattered across the screen
        stars.forEach { (fx, fy, alpha) ->
            drawCircle(
                color = Color(0xFFFFF8E7).copy(alpha = alpha),
                radius = 1f,
                center = Offset(fx * size.width, fy * size.height)
            )
        }
    }
}
