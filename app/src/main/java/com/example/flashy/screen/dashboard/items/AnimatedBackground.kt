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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun AnimatedBackground() {
    val infinite = rememberInfiniteTransition(label = "bg")

    // Single slow nebula pulse — the only animation, kept subtle at 10s per cycle.
    // The two orbital ring animations (28s + 18s) were removed: they forced 60fps Canvas
    // redraws during scroll, causing jank. Static orb positions look identical at a glance.
    val nebulaPulse by infinite.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(10000, easing = LinearEasing), RepeatMode.Reverse),
        label = "neb"
    )

    // Pre-compute static orb angles once — avoids repeated constant-folding in draw
    val outerAngles = remember { List(6) { i -> (i * 60f) * (PI.toFloat() / 180f) } }
    val innerAngles  = remember { List(4) { i -> (i * 90f + 45f) * (PI.toFloat() / 180f) } }

    val stars = remember {
        List(70) {
            Triple(Random.nextFloat(), Random.nextFloat(), 0.15f + Random.nextFloat() * 0.55f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Warm amber core — breathes with nebulaPulse
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFB300).copy(alpha = 0.055f * nebulaPulse),
                    Color(0xFFFF8F00).copy(alpha = 0.025f * nebulaPulse),
                    Color.Transparent
                ),
                center = Offset(cx, cy * 0.5f),
                radius = size.width * 0.75f
            ),
            radius = size.width * 0.75f,
            center = Offset(cx, cy * 0.5f)
        )

        // Deep orange ember — bottom-right accent (static)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFF6B35).copy(alpha = 0.04f), Color.Transparent),
                center = Offset(cx * 1.7f, cy * 1.6f),
                radius = size.width * 0.6f
            ),
            radius = size.width * 0.6f,
            center = Offset(cx * 1.7f, cy * 1.6f)
        )

        // Outer static orbs — 6 evenly-spaced positions
        val r1 = minOf(size.width, size.height) * 0.40f
        outerAngles.forEach { theta ->
            drawCircle(
                color = Color(0xFFFFB300).copy(alpha = 0.055f),
                radius = 48f,
                center = Offset(cx + cos(theta) * r1, cy + sin(theta) * r1)
            )
        }

        // Inner static orbs — 4 offset positions
        val r2 = minOf(size.width, size.height) * 0.22f
        innerAngles.forEach { theta ->
            drawCircle(
                color = Color(0xFFFF8F00).copy(alpha = 0.07f),
                radius = 30f,
                center = Offset(cx + cos(theta) * r2, cy + sin(theta) * r2)
            )
        }

        // Warm star field
        stars.forEach { (fx, fy, alpha) ->
            drawCircle(
                color = Color(0xFFFFF8E7).copy(alpha = alpha),
                radius = 1.2f,
                center = Offset(fx * size.width, fy * size.height)
            )
        }
    }
}
