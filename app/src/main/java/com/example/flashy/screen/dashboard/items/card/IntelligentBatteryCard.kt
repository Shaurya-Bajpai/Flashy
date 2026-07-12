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
import androidx.compose.material3.Slider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsb.flashy.R
import com.dsb.flashy.screen.dashboard.items.AnimatedSwitch
import com.dsb.flashy.screen.dashboard.items.MainCardHeading
import com.dsb.flashy.ui.theme.ColorBattery
import com.dsb.flashy.ui.theme.ColorDanger
import com.dsb.flashy.ui.theme.TextDim
import com.dsb.flashy.ui.theme.TextMuted
import com.dsb.flashy.ui.theme.TextWarm

@Composable
fun IntelligentBatteryCard(
    threshold: Float,
    chargingCompleteFlash: Boolean,
    lowBatteryAlert: Boolean,
    onThresholdChange: (Float) -> Unit,
    onThresholdChangeFinished: () -> Unit,
    onChargingCompleteFlashChange: (Boolean) -> Unit,
    onLowBatteryAlertChange: (Boolean) -> Unit,
) {
    val pct = (threshold * 100).toInt()
    val trackColor = when {
        pct <= 10 -> ColorDanger
        pct <= 18 -> ColorBattery
        else -> Color(0xFF86EFAC)
    }
    val batteryRes = if (pct < 15) R.drawable.baseline_battery_2_bar_24 else R.drawable.baseline_battery_full_24

    GlassMorphismCard {
        Column(modifier = Modifier.padding(24.dp)) {
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
                    MainCardHeading(
                        text = "Smart Battery",
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$pct%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = trackColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Slider(
                value = threshold,
                onValueChange = onThresholdChange,
                onValueChangeFinished = onThresholdChangeFinished,
                valueRange = 0.05f..0.3f,
                colors = SliderDefaults.colors(
                    thumbColor = trackColor,
                    activeTrackColor = trackColor,
                    inactiveTrackColor = trackColor.copy(alpha = 0.3f)
                )
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.07f), modifier = Modifier.padding(vertical = 8.dp))

            AnimatedSwitch(
                label = "Flash on Full Charge",
                sublabel = "Flash 5 times when battery reaches 100%",
                isEnabled = chargingCompleteFlash,
                onToggle = onChargingCompleteFlashChange,
                icon = painterResource(R.drawable.baseline_battery_full_24),
                color = Color(0xFF86EFAC)
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.07f), modifier = Modifier.padding(vertical = 8.dp))

            AnimatedSwitch(
                label = "Low Battery Alert",
                sublabel = "Flash 3 times when battery reaches below",
                isEnabled = lowBatteryAlert,
                onToggle = onLowBatteryAlertChange,
                icon = painterResource(R.drawable.baseline_battery_2_bar_24),
                color = ColorDanger
            )
        }
    }
}

@Preview(showBackground = false)
@Composable
fun IntelligentBatteryCardPreview() {
    IntelligentBatteryCard(
        threshold = 0.15f,
        chargingCompleteFlash = true,
        lowBatteryAlert = true,
        onThresholdChange = {},
        onThresholdChangeFinished = {},
        onChargingCompleteFlashChange = {},
        onLowBatteryAlertChange = {}
    )
}