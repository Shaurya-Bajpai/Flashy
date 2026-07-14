package com.dsb.flashy.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsb.flashy.R
import com.dsb.flashy.ui.theme.Amber
import com.dsb.flashy.ui.theme.AmberDeep
import com.dsb.flashy.ui.theme.TextDim
import com.dsb.flashy.ui.theme.TextMuted
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Total screen time: ~2.5 s
//   150 ms → icon bursts in
//   450 ms → title slides up  (600 ms from start)
//   350 ms → tagline + bar    (950 ms from start)
//  1600 ms → onComplete       (2550 ms from start)

@Composable
fun FlashyIntroScreen(onComplete: () -> Unit) {
    var phase by remember { mutableIntStateOf(0) }
    var startProgress by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(150)
        phase = 1           // icon
        delay(450)
        phase = 2           // title
        delay(350)
        phase = 3           // tagline
        startProgress = true
        delay(1600)
        onComplete()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "intro_inf")

    // Arc rotating around the icon
    val arcAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing)
        ),
        label = "arc"
    )

    // Slow outer-particle orbit
    val orbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing)
        ),
        label = "orbit"
    )

    // Gentle glow pulse on the icon ring
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Progress bar fill (0 → 1 over 1600 ms, starts when phase 3 triggers)
    val progressTarget = if (startProgress) 1f else 0f
    val progress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(1600, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1A0800),
                        Color(0xFF0C0500),
                        Color(0xFF060401)
                    ),
                    radius = 1800f
                )
            )
    ) {
        // Ambient particle field — subtle amber dots orbiting slowly
        AmbientOrbs(orbitAngle)

        // Main content column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // ── Icon block ───────────────────────────────────────────
            AnimatedVisibility(
                visible = phase >= 1,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    initialScale = 0.4f
                ) + fadeIn(tween(400))
            ) {
                Box(
                    modifier = Modifier.size(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Rotating sweep arc drawn on a Canvas inside the 180 dp box
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val r  = 82.dp.toPx()
                        drawArc(
                            brush = Brush.sweepGradient(
                                colorStops = arrayOf(
                                    0.00f to Color.Transparent,
                                    0.50f to Amber.copy(alpha = 0.55f),
                                    0.75f to AmberDeep.copy(alpha = 0.75f),
                                    1.00f to Color.Transparent
                                ),
                                center = Offset(cx, cy)
                            ),
                            startAngle = arcAngle,
                            sweepAngle = 260f,
                            useCenter  = false,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
                            topLeft = Offset(cx - r, cy - r),
                            size    = Size(r * 2f, r * 2f)
                        )
                    }

                    // Outer glow ring (pulsing)
                    Box(
                        modifier = Modifier
                            .size(136.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Amber.copy(alpha = glowPulse * 0.22f),
                                        Color.Transparent
                                    )
                                ),
                                CircleShape
                            )
                    )

                    // Icon circle
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Amber.copy(alpha = 0.10f), CircleShape)
                            .border(1.5.dp, Amber.copy(alpha = 0.38f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_flash_on_24),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(52.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(44.dp))

            // ── Title ────────────────────────────────────────────────
            AnimatedVisibility(
                visible = phase >= 2,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) + fadeIn(tween(400))
            ) {
                Text(
                    text = "FLASHY",
                    style = TextStyle(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Amber, AmberDeep, Amber)
                        )
                    ),
                    fontSize = 54.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 8.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── Tagline ──────────────────────────────────────────────
            AnimatedVisibility(
                visible = phase >= 3,
                enter = fadeIn(tween(500))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Never miss what matters",
                        fontSize = 15.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FeaturePill("Calls")
                        FeaturePill("Messages")
                        FeaturePill("Apps")
                    }
                }
            }
        }

        // ── Progress bar — bottom of screen ─────────────────────────
        AnimatedVisibility(
            visible = phase >= 3,
            enter = fadeIn(tween(300)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 52.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(200.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Amber.copy(alpha = 0.14f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .background(
                                Brush.horizontalGradient(listOf(Amber, AmberDeep)),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Loading...",
                    fontSize = 11.sp,
                    color = TextDim,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

// ── Small feature label pill ──────────────────────────────────────────────

@Composable
private fun FeaturePill(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Amber.copy(alpha = 0.08f))
            .border(0.5.dp, Amber.copy(alpha = 0.20f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Amber.copy(alpha = 0.75f),
            letterSpacing = 0.5.sp
        )
    }
}

// ── Ambient particle orbs (background layer) ─────────────────────────────

@Composable
private fun AmbientOrbs(orbitAngle: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height / 2f

        val orbits = listOf(
            Triple(size.minDimension * 0.38f, 10, 0.18f),  // inner ring, 10 dots
            Triple(size.minDimension * 0.52f,  7, 0.11f),  // outer ring, 7 dots
        )

        orbits.forEach { (radius, count, alpha) ->
            repeat(count) { i ->
                val angle = (orbitAngle + i * (360f / count)) * PI.toFloat() / 180f
                val x = cx + cos(angle) * radius
                val y = cy + sin(angle) * radius
                drawCircle(
                    color = Amber.copy(alpha = alpha),
                    radius = 3.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
    }
}
