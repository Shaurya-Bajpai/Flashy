package com.example.flashy.screen.dashboard.items

import android.content.Context
import android.hardware.camera2.CameraManager
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashy.R
import com.example.flashy.screen.dashboard.items.card.GlassMorphismCard
import kotlinx.coroutines.launch

@Composable
fun PremiumHeader(showPulse: Boolean, flashGlobal: Boolean) {
    val context = LocalContext.current
    val cameraManager = remember { context.getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    val cameraId = remember { cameraManager.cameraIdList.firstOrNull() }
    val isFlashOn = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val scale by animateFloatAsState(
        targetValue = if (showPulse) 1.1f else 1f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "pulse"
    )

    DisposableEffect(Unit) {
        onDispose {
            // Ensure flashlight is turned off when the app is closed
            cameraId?.let { cameraManager.setTorchMode(it, false) }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "header_animation")

    val flashAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flash_alpha"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (flashGlobal) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    GlassMorphismCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        scope.launch {
                            cameraId?.let {
                                isFlashOn.value = !isFlashOn.value
                                cameraManager.setTorchMode(it, isFlashOn.value)
                            }
                        }
                    })
                }) {
                // Outer glow ring
                Box(modifier = Modifier
                    .size(100.dp)
                    .scale(pulseScale)
                    .background(
                        Color(0xFFFFC107).copy(alpha = flashAlpha * 0.3f),
                        CircleShape
                    )
                    .blur(20.dp, BlurredEdgeTreatment.Unbounded))

                // Middle ring
                Box(modifier = Modifier
                    .size(80.dp)
                    .rotate(rotationAngle)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFC107).copy(alpha = flashAlpha),
                                Color(0xFFFF8F00).copy(alpha = flashAlpha * 0.7f)
                            )
                        ),
                        CircleShape
                    )
                    .shadow(16.dp, CircleShape))

                // Inner icon
                Icon(
                    painter = painterResource(R.drawable.baseline_flashlight_on_24),
                    contentDescription = "Flash",
                    modifier = Modifier
                        .size(40.dp)
                        .rotate(-rotationAngle), // Counter-rotate to keep icon upright
                    tint = Color.White
                )
            }

//            Box(
//                modifier = Modifier
//                    .size(80.dp)
//                    .scale(scale)
//                    .background(
//                        brush = Brush.radialGradient(
//                            colors = listOf(
//                                Color(0xFFFFD700),
//                                Color(0xFFFF6B35)
//                            )
//                        ),
//                        shape = CircleShape
//                    )
//                    .padding(16.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Icon(
//                    painter = painterResource(R.drawable.baseline_flash_on_24),
//                    contentDescription = null,
//                    tint = Color.White,
//                    modifier = Modifier.size(32.dp)
//                )
//            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "⚡ Flashy Dashboard",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium
            )

            Text(
                text = "Premium Flash Notifications",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}