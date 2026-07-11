package com.dsb.flashy.screen.dashboard.items

import android.content.Context
import android.hardware.camera2.CameraManager
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsb.flashy.R
import com.dsb.flashy.ui.theme.Amber
import com.dsb.flashy.ui.theme.AmberDark
import com.dsb.flashy.ui.theme.AmberDeep
import com.dsb.flashy.ui.theme.AmberLight
import com.dsb.flashy.ui.theme.ColorCall
import com.dsb.flashy.ui.theme.ColorDanger
import com.dsb.flashy.ui.theme.TextDim
import com.dsb.flashy.ui.theme.TextMuted
import com.dsb.flashy.ui.theme.TextWarm
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PremiumHeader(showPulse: Boolean, flashGlobal: Boolean) {
    val context = LocalContext.current
    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    val cameraId = remember { cameraManager.cameraIdList.firstOrNull() }
    val isFlashOn = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val infinite = rememberInfiniteTransition(label = "header")

    val glowPulse by infinite.animateFloat(
        initialValue = 0.30f, targetValue = 0.70f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )
    val outerRing by infinite.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing)),
        label = "outer"
    )
    val innerRing by infinite.animateFloat(
        initialValue = 360f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "inner"
    )
    val statusDot by infinite.animateFloat(
        initialValue = 0.35f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "dot"
    )

    DisposableEffect(Unit) {
        onDispose { cameraId?.let { cameraManager.setTorchMode(it, false) } }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Status badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(
                    color = if (flashGlobal) ColorCall.copy(alpha = 0.12f) else ColorDanger.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(50)
                )
                .border(
                    width = 1.dp,
                    color = if (flashGlobal) ColorCall.copy(alpha = 0.35f) else ColorDanger.copy(alpha = 0.28f),
                    shape = RoundedCornerShape(50)
                )
                .padding(horizontal = 16.dp, vertical = 7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(
                        color = (if (flashGlobal) ColorCall else ColorDanger).copy(alpha = statusDot),
                        shape = CircleShape
                    )
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = if (flashGlobal) "ACTIVE" else "DISABLED",
                style = MaterialTheme.typography.labelMedium,
                color = if (flashGlobal) ColorCall else ColorDanger,
                letterSpacing = 1.8.sp
            )
        }

        Spacer(Modifier.height(32.dp))

        // Flash orb — the hero element
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(168.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        scope.launch {
                            cameraId?.let {
                                isFlashOn.value = !isFlashOn.value
                                cameraManager.setTorchMode(it, isFlashOn.value)
                            }
                        }
                    })
                }
        ) {
            // Ambient glow halo behind everything
            Box(
                modifier = Modifier
                    .size(168.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Amber.copy(alpha = glowPulse * 0.45f),
                                    AmberDeep.copy(alpha = glowPulse * 0.18f),
                                    Color.Transparent
                                )
                            ),
                            radius = size.minDimension * 0.85f
                        )
                    }
            )

            // Outer dashed ring
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .rotate(outerRing)
                    .drawBehind {
                        drawDashedRing(
                            color = Amber.copy(alpha = if (flashGlobal) glowPulse * 0.85f else 0.18f),
                            radius = size.minDimension / 2f - 3f,
                            dashCount = 14,
                            dashArcDeg = 11f,
                            strokeWidth = 2.5f
                        )
                    }
            )

            // Inner dashed ring (opposite direction, tighter)
            Box(
                modifier = Modifier
                    .size(122.dp)
                    .rotate(innerRing)
                    .drawBehind {
                        drawDashedRing(
                            color = AmberDeep.copy(alpha = if (flashGlobal) glowPulse * 0.65f else 0.12f),
                            radius = size.minDimension / 2f - 2f,
                            dashCount = 9,
                            dashArcDeg = 8f,
                            strokeWidth = 1.8f
                        )
                    }
            )

            // Core orb
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .drawBehind {
                        if (flashGlobal) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Amber.copy(alpha = glowPulse * 0.5f),
                                        Color.Transparent
                                    )
                                ),
                                radius = size.minDimension * 1.1f
                            )
                        }
                    }
                    .background(
                        brush = Brush.radialGradient(
                            colors = if (flashGlobal) {
                                listOf(AmberLight, Amber, AmberDark)
                            } else {
                                listOf(Color(0xFF2A1E00), Color(0xFF191200))
                            }
                        ),
                        shape = CircleShape
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (flashGlobal) Amber.copy(alpha = 0.55f) else Color(0xFF3A2800).copy(alpha = 0.45f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_flashlight_on_24),
                    contentDescription = "Tap to test flash",
                    modifier = Modifier.size(34.dp),
                    tint = if (flashGlobal) Color(0xFF1A0D00) else Amber.copy(alpha = 0.30f)
                )
            }
        }

        Spacer(Modifier.height(30.dp))

        // Title — amber gradient, wide tracking
        Text(
            text = "FLASHY",
            style = TextStyle(
                brush = Brush.horizontalGradient(
                    colors = listOf(AmberLight, Amber, AmberDeep)
                ),
                fontSize = 40.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 8.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Smart flash notification system",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Tap the orb to test your flash",
            style = MaterialTheme.typography.labelSmall,
            color = TextDim,
            textAlign = TextAlign.Center
        )
    }
}

private fun DrawScope.drawDashedRing(
    color: Color,
    radius: Float,
    dashCount: Int,
    dashArcDeg: Float,
    strokeWidth: Float
) {
    val cx = size.width / 2f
    val cy = size.height / 2f
    repeat(dashCount) { i ->
        val startAngle = i * (360f / dashCount) * (PI.toFloat() / 180f)
        val endAngle = startAngle + (dashArcDeg * PI.toFloat() / 180f)
        drawLine(
            color = color,
            start = Offset(cx + cos(startAngle) * radius, cy + sin(startAngle) * radius),
            end = Offset(cx + cos(endAngle) * radius, cy + sin(endAngle) * radius),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}
