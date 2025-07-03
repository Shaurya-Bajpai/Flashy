package com.example.flashy.screen

import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random
import com.example.flashy.R

@Composable
fun FlashyScreen(onComplete: () -> Unit) {
    var phase by remember { mutableIntStateOf(0) }
    var isExploding by remember { mutableStateOf(false) }
    var showShockwave by remember { mutableStateOf(false) }

    // Multiple infinite transitions for complex animations
    val infiniteTransition = rememberInfiniteTransition(label = "intro")
    val explosionTransition = rememberInfiniteTransition(label = "explosion")
    val waveTransition = rememberInfiniteTransition(label = "wave")
    val particleTransition = rememberInfiniteTransition(label = "particles")

    // Enhanced animations
    val hyperPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hyperpulse"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val flashIntensity by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flash"
    )

    val shockwaveRadius by waveTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shockwave"
    )

    val particleRotation by particleTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_rotation"
    )

    val explosionScale by explosionTransition.animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "explosion"
    )

    // Enhanced phase management
    LaunchedEffect(Unit) {
        delay(300)
        phase = 1 // Logo explosion entry
        delay(800)
        isExploding = true
        delay(200)
        showShockwave = true
        delay(600)
        phase = 2 // Title with particles
        delay(800)
        phase = 3 // Subtitle with lightning
        delay(1000)
        phase = 4 // Final effects
        delay(1500)
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF000000),
                        Color(0xFF0A0A0A),
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    ),
                    radius = 1800f
                )
            )
    ) {
        // Dynamic particle field
        MeteorShower(particleRotation, phase)

        // Enhanced lightning storm
        LightningStorm(flashIntensity, phase, rotationAngle)

        // Shockwave effect
        if (showShockwave) {
            ShockwaveEffect(shockwaveRadius, phase)
        }

        // Floating orbs
        FloatingOrbs(rotationAngle, phase)

        // Main content with enhanced animations
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ultra-enhanced logo
            AnimatedVisibility(
                visible = phase >= 1,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(animationSpec = tween(500))
            ) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .scale(hyperPulse)
                        .graphicsLayer {
                            shadowElevation = 30.dp.toPx()
                            spotShadowColor = Color(0xFFFFD700)
                            ambientShadowColor = Color(0xFFFF6B35)
                        }
                ) {
                    // Multiple layered backgrounds for depth
                    repeat(3) { layer ->
                        val layerScale = 1f + (layer * 0.2f)
                        val layerAlpha = 0.6f - (layer * 0.15f)

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(layerScale)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFFFD700).copy(alpha = flashIntensity * layerAlpha),
                                            Color(0xFFFFA500).copy(alpha = flashIntensity * layerAlpha * 0.8f),
                                            Color(0xFFFF6B35).copy(alpha = flashIntensity * layerAlpha * 0.6f),
                                            Color(0xFFFF1744).copy(alpha = flashIntensity * layerAlpha * 0.4f)
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .rotate(rotationAngle * (1 + layer * 0.5f))
                        )
                    }

                    // Central icon with glow
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.9f),
                                        Color(0xFFFFD700).copy(alpha = 0.7f),
                                        Color.Transparent
                                    ),
                                    radius = 80f
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_flash_on_24),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(100.dp)
                                .graphicsLayer {
                                    shadowElevation = 20.dp.toPx()
                                    spotShadowColor = Color(0xFFFFD700)
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(50.dp))

            // Enhanced title with 3D effect
            AnimatedVisibility(
                visible = phase >= 2,
                enter = slideInVertically(
                    initialOffsetY = { it }, // Slide in from the bottom
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(animationSpec = tween(600))
            ) {
                Box {
                    // Shadow layers for 3D effect
                    repeat(3) { layer ->
                        Text(
                            text = "FLASHY",
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.Black.copy(alpha = 0.3f - layer * 0.1f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.offset(
                                x = (layer + 1).dp,
                                y = (layer + 1).dp
                            )
                        )
                    }

                    // Main title with gradient
                    Text(
                        text = "FLASHY",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Transparent,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFFFFD700),
                                        Color(0xFFFFA500),
                                        Color(0xFFFF6B35),
                                        Color(0xFFFFD700)
                                    ),
                                    start = Offset(0f, 0f),
                                    end = Offset(200f, 100f)
                                )
                            )
                            .graphicsLayer {
                                shadowElevation = 15.dp.toPx()
                                spotShadowColor = Color(0xFFFFD700)
                            }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Enhanced subtitle with typewriter effect
            AnimatedVisibility(
                visible = phase >= 3,
                enter = slideInVertically(
                    initialOffsetY = { it }, // Slide in from the bottom
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(animationSpec = tween(800))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PREMIUM FLASH ALERTS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700),
                        textAlign = TextAlign.Center,
                        letterSpacing = 3.sp,
                        modifier = Modifier.graphicsLayer {
                            shadowElevation = 10.dp.toPx()
                            spotShadowColor = Color(0xFFFFD700)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "✨ Never Miss a Notification ✨",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.graphicsLayer {
                            shadowElevation = 5.dp.toPx()
                            spotShadowColor = Color.White
                        }
                    )
                }
            }
        }

        // Enhanced loading section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = phase >= 2,
                enter = slideInVertically(animationSpec = tween(800)) + fadeIn(animationSpec = tween(500))
            ) {
                UltraLoadingIndicator(phase)
            }
        }

        // Power-up effects
        if (phase >= 4) {
            PowerUpEffects(explosionScale, rotationAngle)
        }
    }
}

@Composable
fun MeteorShower(rotation: Float, phase: Int) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val meteorCount = 25
        val center = Offset(size.width / 2, size.height / 2)

        repeat(meteorCount) { i ->
            val angle = (rotation + i * (360f / meteorCount)) * PI / 180
            val baseRadius = size.minDimension / 2.5f
            val radius = baseRadius + (i % 5) * 80f
            val meteorSize = (3f + (i % 6) * 2f) * if (phase >= 2) 1.5f else 1f

            val x = center.x + cos(angle).toFloat() * radius
            val y = center.y + sin(angle).toFloat() * radius

            // Meteor trail
            val trailLength = 30f
            val trailEndX = x - cos(angle).toFloat() * trailLength
            val trailEndY = y - sin(angle).toFloat() * trailLength

            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFD700).copy(alpha = 0.8f),
                        Color(0xFFFFA500).copy(alpha = 0.4f),
                        Color.Transparent
                    ),
                    start = Offset(x, y),
                    end = Offset(trailEndX, trailEndY)
                ),
                start = Offset(x, y),
                end = Offset(trailEndX, trailEndY),
                strokeWidth = meteorSize,
                cap = StrokeCap.Round
            )

            // Meteor head
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFFFD700),
                        Color(0xFFFFA500)
                    ),
                    radius = meteorSize
                ),
                radius = meteorSize,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun LightningStorm(intensity: Float, phase: Int, rotation: Float) {
    if (phase >= 2) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val boltCount = 12

            repeat(boltCount) { i ->
                val angle = (rotation * 0.5f + i * (360f / boltCount)) * PI / 180
                val startRadius = 120f
                val endRadius = 300f + (i % 3) * 100f

                val startX = center.x + cos(angle).toFloat() * startRadius
                val startY = center.y + sin(angle).toFloat() * startRadius
                val endX = center.x + cos(angle).toFloat() * endRadius
                val endY = center.y + sin(angle).toFloat() * endRadius

                // Jagged lightning path
                val segments = 5
                val points = mutableListOf<Offset>()
                points.add(Offset(startX, startY))

                for (j in 1 until segments) {
                    val progress = j.toFloat() / segments
                    val baseX = startX + (endX - startX) * progress
                    val baseY = startY + (endY - startY) * progress
                    val randomOffset = (Random.nextFloat() - 0.5f) * 50f

                    points.add(Offset(
                        baseX + cos(angle + PI/2).toFloat() * randomOffset,
                        baseY + sin(angle + PI/2).toFloat() * randomOffset
                    ))
                }
                points.add(Offset(endX, endY))

                // Draw lightning segments
                for (j in 0 until points.size - 1) {
                    val strokeWidth = (4f + (intensity * 6f)) * (1f - j.toFloat() / points.size)
                    drawLine(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFFFFF).copy(alpha = intensity),
                                Color(0xFFFFD700).copy(alpha = intensity * 0.8f),
                                Color(0xFF00BFFF).copy(alpha = intensity * 0.6f)
                            )
                        ),
                        start = points[j],
                        end = points[j + 1],
                        strokeWidth = strokeWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

@Composable
fun ShockwaveEffect(radius: Float, phase: Int) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxRadius = size.minDimension

        if (radius < maxRadius) {
            val alpha = 1f - (radius / maxRadius)

            // Multiple wave rings
            repeat(3) { ring ->
                val ringRadius = radius - (ring * 100f)
                if (ringRadius > 0) {
                    val ringAlpha = alpha * (1f - ring * 0.3f)

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFD700).copy(alpha = ringAlpha * 0.8f),
                                Color(0xFFFFA500).copy(alpha = ringAlpha * 0.6f),
                                Color(0xFFFF6B35).copy(alpha = ringAlpha * 0.4f),
                                Color.Transparent
                            ),
                            radius = ringRadius
                        ),
                        radius = ringRadius,
                        center = center,
                        style = Stroke(width = 3f)
                    )
                }
            }
        }
    }
}

@Composable
fun FloatingOrbs(rotation: Float, phase: Int) {
    if (phase >= 1) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val orbCount = 8

            repeat(orbCount) { i ->
                val angle = (rotation * 0.3f + i * (360f / orbCount)) * PI / 180
                val radius = 250f + sin(rotation * 0.01f + i).toFloat() * 50f
                val orbSize = 15f + cos(rotation * 0.02f + i).toFloat() * 5f

                val x = center.x + cos(angle).toFloat() * radius
                val y = center.y + sin(angle).toFloat() * radius

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF).copy(alpha = 0.9f),
                            Color(0xFFFFD700).copy(alpha = 0.7f),
                            Color(0xFFFFA500).copy(alpha = 0.5f),
                            Color.Transparent
                        ),
                        radius = orbSize
                    ),
                    radius = orbSize,
                    center = Offset(x, y)
                )
            }
        }
    }
}

@Composable
fun UltraLoadingIndicator(phase: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    val glowIntensity by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Enhanced progress bar
        Box(
            modifier = Modifier
                .width(250.dp)
                .height(6.dp)
                .background(
                    Color.White.copy(alpha = 0.1f),
                    RoundedCornerShape(3.dp)
                )
                .border(
                    1.dp,
                    Color(0xFFFFD700).copy(alpha = 0.3f),
                    RoundedCornerShape(3.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width((250 * progress).dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFFD700).copy(alpha = glowIntensity),
                                Color(0xFFFFA500).copy(alpha = glowIntensity),
                                Color(0xFFFF6B35).copy(alpha = glowIntensity),
                                Color(0xFFFFD700).copy(alpha = glowIntensity)
                            )
                        ),
                        shape = RoundedCornerShape(3.dp)
                    )
                    .graphicsLayer {
                        shadowElevation = 10.dp.toPx()
                        spotShadowColor = Color(0xFFFFD700)
                    }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Enhanced loading dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(5) { index ->
                val dotScale by infiniteTransition.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 1.4f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, delayMillis = index * 150),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dot_$index"
                )

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .scale(dotScale)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFFFFF),
                                    Color(0xFFFFD700),
                                    Color(0xFFFFA500)
                                )
                            ),
                            shape = CircleShape
                        )
                        .graphicsLayer {
                            shadowElevation = 8.dp.toPx()
                            spotShadowColor = Color(0xFFFFD700)
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Initializing Premium Experience...",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PowerUpEffects(explosionScale: Float, rotation: Float) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val particleCount = 30

        repeat(particleCount) { i ->
            val angle = (rotation + i * (360f / particleCount)) * PI / 180
            val distance = explosionScale * 200f
            val particleSize = (5f + (i % 4) * 3f) * (1f - explosionScale * 0.5f)

            if (particleSize > 0) {
                val x = center.x + cos(angle).toFloat() * distance
                val y = center.y + sin(angle).toFloat() * distance

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF).copy(alpha = 1f - explosionScale * 0.8f),
                            Color(0xFFFFD700).copy(alpha = 1f - explosionScale),
                            Color.Transparent
                        ),
                        radius = particleSize
                    ),
                    radius = particleSize,
                    center = Offset(x, y)
                )
            }
        }
    }
}