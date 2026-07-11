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

    val orb1Angle by infinite.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(28000, easing = LinearEasing)),
        label = "o1"
    )
    val orb2Angle by infinite.animateFloat(
        initialValue = 360f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(18000, easing = LinearEasing)),
        label = "o2"
    )
    val nebulaPulse by infinite.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Reverse),
        label = "neb"
    )

    // Deterministic star field — computed once and stable
    val stars = remember {
        List(70) {
            Triple(Random.nextFloat(), Random.nextFloat(), 0.15f + Random.nextFloat() * 0.55f)
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        // Warm amber core — the unseen light source above
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

        // Deep orange ember — bottom-right accent
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFF6B35).copy(alpha = 0.04f),
                    Color.Transparent
                ),
                center = Offset(cx * 1.7f, cy * 1.6f),
                radius = size.width * 0.6f
            ),
            radius = size.width * 0.6f,
            center = Offset(cx * 1.7f, cy * 1.6f)
        )

        // Outer orbital ring — 6 orbs clockwise
        val r1 = minOf(size.width, size.height) * 0.40f
        repeat(6) { i ->
            val theta = (orb1Angle + i * 60f) * (PI.toFloat() / 180f)
            val ox = cx + cos(theta) * r1
            val oy = cy + sin(theta) * r1
            drawCircle(color = Color(0xFFFFB300).copy(alpha = 0.055f), radius = 48f, center = Offset(ox, oy))
        }

        // Inner orbital ring — 4 orbs counter-clockwise, tighter
        val r2 = minOf(size.width, size.height) * 0.22f
        repeat(4) { i ->
            val theta = (orb2Angle + i * 90f) * (PI.toFloat() / 180f)
            val ox = cx + cos(theta) * r2
            val oy = cy + sin(theta) * r2
            drawCircle(color = Color(0xFFFF8F00).copy(alpha = 0.07f), radius = 30f, center = Offset(ox, oy))
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
