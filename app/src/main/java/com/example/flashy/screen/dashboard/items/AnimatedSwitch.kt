package com.dsb.flashy.screen.dashboard.items

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.dsb.flashy.ui.theme.TextDim
import com.dsb.flashy.ui.theme.TextMuted
import com.dsb.flashy.ui.theme.TextWarm

@Composable
fun AnimatedSwitch(
    label: String,
    sublabel: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    icon: Painter,
    color: Color
) {
    val haptic = LocalHapticFeedback.current

    val iconScale by animateFloatAsState(
        targetValue = if (isEnabled) 1f else 0.85f,
        animationSpec = tween(200),
        label = "icon_scale"
    )
    val contentAlpha by animateFloatAsState(
        targetValue = if (isEnabled) 1f else 0.60f,
        animationSpec = tween(250),
        label = "alpha"
    )
    val iconBg by animateColorAsState(
        targetValue = if (isEnabled) color.copy(alpha = 0.20f) else color.copy(alpha = 0.07f),
        animationSpec = tween(250),
        label = "icon_bg"
    )
    val iconBorder by animateColorAsState(
        targetValue = if (isEnabled) color.copy(alpha = 0.42f) else color.copy(alpha = 0.10f),
        animationSpec = tween(250),
        label = "icon_border"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggle(!isEnabled)
            }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .scale(iconScale)
                .background(iconBg, CircleShape)
                .border(1.dp, iconBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = if (isEnabled) color else color.copy(alpha = 0.38f),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .alpha(contentAlpha)
        ) {
            Text(text = label, style = MaterialTheme.typography.titleMedium, color = TextWarm)
            Spacer(Modifier.height(2.dp))
            Text(text = sublabel, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        }

        Switch(
            checked = isEnabled,
            onCheckedChange = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggle(it)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF1A0D00),
                checkedTrackColor = color,
                checkedBorderColor = color.copy(alpha = 0.55f),
                uncheckedThumbColor = TextDim,
                uncheckedTrackColor = Color.White.copy(alpha = 0.07f),
                uncheckedBorderColor = Color.White.copy(alpha = 0.14f)
            )
        )
    }
}
