package com.dsb.flashy.screen.dashboard.items.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dsb.flashy.R
import com.dsb.flashy.ui.theme.ColorBattery
import com.dsb.flashy.ui.theme.ColorDanger
import com.dsb.flashy.ui.theme.TextDim
import com.dsb.flashy.ui.theme.TextMuted
import com.dsb.flashy.ui.theme.TextWarm

@Composable
fun IntelligentBatteryCard(
    threshold: Float,
    onThresholdChange: (Float) -> Unit,
    onThresholdChangeFinished: () -> Unit
) {
    val pct = (threshold * 100).toInt()
    val trackColor = when {
        pct <= 10 -> ColorDanger
        pct <= 18 -> ColorBattery
        else -> Color(0xFF86EFAC)
    }
    val batteryRes = if (pct < 15) R.drawable.baseline_battery_2_bar_24 else R.drawable.baseline_battery_full_24

    GlassMorphismCard(accentGlow = trackColor.copy(alpha = 0.05f)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(batteryRes),
                        contentDescription = null,
                        tint = trackColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Battery Guard",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextWarm
                        )
                        Text(
                            text = "Mute flash below threshold",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$pct%",
                        style = MaterialTheme.typography.headlineMedium,
                        color = trackColor
                    )
                    Text(
                        text = "limit",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextDim
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Slider(
                value = threshold,
                onValueChange = onThresholdChange,
                onValueChangeFinished = onThresholdChangeFinished,
                valueRange = 0.05f..0.30f,
                colors = SliderDefaults.colors(
                    thumbColor = trackColor,
                    activeTrackColor = trackColor,
                    inactiveTrackColor = trackColor.copy(alpha = 0.18f),
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("5%", style = MaterialTheme.typography.labelSmall, color = TextDim)
                Text("30%", style = MaterialTheme.typography.labelSmall, color = TextDim)
            }
        }
    }
}
