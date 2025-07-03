package com.example.flashy.screen

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
import com.example.flashy.R

@Composable
fun FlashyIntroScreen(onComplete: () -> Unit) {
    var phase by remember { mutableIntStateOf(0) }

    val infiniteTransition = rememberInfiniteTransition(label = "intro")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val flashAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flash"
    )

    LaunchedEffect(Unit) {
        delay(500)
        phase = 1
        delay(1000)
        phase = 2
        delay(1500)
        phase = 3
        delay(3000)
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0D1B2A),
                        Color(0xFF1B263B),
                        Color(0xFF415A77)
                    ),
                    radius = 1500f
                )
            )
    ) {
        // Animated Background Particles
        ParticleBackground(rotationAngle)

        // Lightning Effect
        LightningEffect(flashAlpha, phase)

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo with Flash Animation
            AnimatedVisibility(
                visible = phase >= 1,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(animationSpec = tween(800))
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .scale(pulseScale)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD700).copy(alpha = flashAlpha),
                                    Color(0xFFFFA500).copy(alpha = flashAlpha * 0.7f),
                                    Color(0xFFFF6B35).copy(alpha = flashAlpha * 0.5f)
                                )
                            ),
                            shape = CircleShape
                        )
                        .rotate(rotationAngle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_flash_on_24),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Replace slideInUp with slideInVertically
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
                Text(
                    text = "FLASHY",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .graphicsLayer {
                            shadowElevation = 8.dp.toPx()
                            spotShadowColor = Color(0xFFFFD700)
                            ambientShadowColor = Color(0xFFFFD700)
                        }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tagline Animation
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
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700),
                        textAlign = TextAlign.Center,
                        letterSpacing = 2.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Never Miss a Notification",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Bottom Loading Indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = phase >= 2,
                enter = fadeIn(animationSpec = tween(500))
            ) {
                LoadingIndicator()
            }
        }
    }
}

@Composable
fun ParticleBackground(rotation: Float) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val particleCount = 20
        val baseRadius = size.minDimension / 3

        repeat(particleCount) { i ->
            val angle = (rotation + i * (360f / particleCount)) * PI / 180
            val radius = baseRadius + (i % 3) * 50f
            val particleSize = (8f + (i % 4) * 3f)

            val x = center.x + cos(angle).toFloat() * radius
            val y = center.y + sin(angle).toFloat() * radius

            drawCircle(
                color = Color(0xFFFFD700).copy(alpha = 0.3f - (i % 3) * 0.1f),
                radius = particleSize,
                center = Offset(x, y)
            )
        }
    }
}

@Composable
fun LightningEffect(alpha: Float, phase: Int) {
    if (phase >= 2) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val center = Offset(size.width / 2, size.height / 2)
            val strokeWidth = 3f

            // Lightning bolts
            repeat(6) { i ->
                val angle = i * 60f * PI / 180
                val startRadius = 100f
                val endRadius = 200f

                val startX = center.x + cos(angle).toFloat() * startRadius
                val startY = center.y + sin(angle).toFloat() * startRadius
                val endX = center.x + cos(angle).toFloat() * endRadius
                val endY = center.y + sin(angle).toFloat() * endRadius

                drawLine(
                    color = Color(0xFFFFD700).copy(alpha = alpha * 0.7f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun LoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(4.dp)
                .background(
                    Color.White.copy(alpha = 0.2f),
                    RoundedCornerShape(2.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width((200 * progress).dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFFD700),
                                Color(0xFFFFA500)
                            )
                        ),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { index ->
                val dotScale by infiniteTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 1.2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, delayMillis = index * 200),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "dot_$index"
                )

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(dotScale)
                        .background(
                            Color(0xFFFFD700),
                            CircleShape
                        )
                )
            }
        }
    }
}