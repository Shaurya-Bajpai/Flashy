package com.dsb.flashy.screen.dashboard.items.card

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsb.flashy.R
import com.dsb.flashy.ui.theme.TextDim
import com.dsb.flashy.ui.theme.TextDisabled
import com.dsb.flashy.ui.theme.TextWarm

@Composable
fun AlertTypeCard(
    painter: Painter = painterResource(R.drawable.baseline_sms_24),
    icon: ImageVector = Icons.Default.Notifications,
    pain: Boolean = false,
    label: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.91f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "scale"
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (isEnabled) 1f else 0.48f,
        animationSpec = tween(250), label = "alpha"
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (isEnabled) 0.55f else 0.10f,
        animationSpec = tween(250), label = "border"
    )
    val bgAlpha by animateFloatAsState(
        targetValue = if (isEnabled) 0.18f else 0.05f,
        animationSpec = tween(250), label = "bg"
    )

    val shape = RoundedCornerShape(18.dp)

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .then(
                if (isEnabled) Modifier.drawBehind {
                    drawCircle(
                        color = color.copy(alpha = 0.12f),
                        radius = size.maxDimension * 0.52f,
                        center = Offset(size.width / 2f, size.height / 2f)
                    )
                } else Modifier
            )
            .clip(shape)
            .background(color.copy(alpha = bgAlpha))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        color.copy(alpha = borderAlpha),
                        color.copy(alpha = borderAlpha * 0.25f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(200f, 200f)
                ),
                shape = shape
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                isPressed = true
                onToggle(!isEnabled)
            }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        color.copy(alpha = if (isEnabled) 0.22f else 0.08f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (pain) {
                    Icon(
                        painter = painter,
                        contentDescription = null,
                        tint = if (isEnabled) color else color.copy(alpha = 0.35f),
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isEnabled) color else color.copy(alpha = 0.35f),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (isEnabled) TextWarm else TextDim,
                letterSpacing = 0.5.sp
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text = if (isEnabled) "ON" else "OFF",
                style = MaterialTheme.typography.labelSmall,
                color = if (isEnabled) color else TextDisabled,
                letterSpacing = 1.2.sp
            )
        }
    }
}
