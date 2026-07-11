package com.dsb.flashy.screen.dashboard.items.card

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dsb.flashy.R
import com.dsb.flashy.ui.theme.Amber
import com.dsb.flashy.ui.theme.TextDim
import com.dsb.flashy.ui.theme.TextMuted
import com.dsb.flashy.ui.theme.TextWarm

private data class RingerMode(val key: String, val label: String, val iconRes: Int)

@Composable
fun AdaptiveRingerCard(
    selectedMode: String,
    onModeChange: (String) -> Unit
) {
    val modes = listOf(
        RingerMode("All", "All", R.drawable.baseline_notifications_active_24),
        RingerMode("Normal", "Sound", R.drawable.baseline_volume_up_24),
        RingerMode("Vibrate", "Vibrate", R.drawable.baseline_vibration_24),
        RingerMode("Silent", "Silent", R.drawable.baseline_volume_off_24)
    )

    GlassMorphismCard {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Ringer Mode",
                style = MaterialTheme.typography.titleLarge,
                color = TextWarm
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "Flash only in selected ringer states",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
            Spacer(Modifier.height(16.dp))

            // Segmented control — one design choice, executed cleanly
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                modes.forEach { mode ->
                    val isSelected = mode.key == selectedMode
                    val chipScale by animateFloatAsState(
                        targetValue = if (isSelected) 1f else 0.94f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "scale_${mode.key}"
                    )
                    val chipBg by animateColorAsState(
                        targetValue = if (isSelected) Amber else Color.Transparent,
                        animationSpec = tween(200),
                        label = "bg_${mode.key}"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF1A0D00) else TextDim,
                        animationSpec = tween(200),
                        label = "fg_${mode.key}"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .scale(chipScale)
                            .clip(RoundedCornerShape(9.dp))
                            .background(chipBg)
                            .clickable { onModeChange(mode.key) }
                            .padding(vertical = 10.dp, horizontal = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(mode.iconRes),
                            contentDescription = mode.key,
                            tint = contentColor,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}
