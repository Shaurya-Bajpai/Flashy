package com.dsb.flashy.screen.dashboard.items

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.dsb.flashy.ui.theme.TextDim
import com.dsb.flashy.ui.theme.TextMuted

private val BannerOrange = Color(0xFFFF8C00)

@Composable
fun BatteryOptimizationBanner(
    onFixClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "battery_opt_pulse")
    val pulse by infinite.animateFloat(
        initialValue = 0.75f, targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BannerOrange.copy(alpha = 0.07f))
            .border(1.dp, BannerOrange.copy(alpha = 0.30f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.Top) {
                // Pulsing icon bubble
                Box(
                    modifier = Modifier.size(32.dp).padding(top = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .scale(pulse)
                            .background(BannerOrange.copy(alpha = 0.18f), CircleShape)
                    )
                    Icon(
                        painter = painterResource(R.drawable.baseline_battery_alert_24),
                        contentDescription = null,
                        tint = BannerOrange,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Battery Optimization Active",
                        style = MaterialTheme.typography.titleSmall,
                        color = BannerOrange
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Flash alerts may stop working when the screen is off. This affects most phones — Xiaomi, Oppo, Realme, Samsung, and others.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = "Tap Fix Now to exempt this app from battery restrictions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDim
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(Modifier.weight(1f))

                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Not Now",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextDim
                    )
                }

                Spacer(Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(BannerOrange.copy(alpha = 0.18f))
                        .border(1.dp, BannerOrange.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                        .clickable { onFixClick() }
                        .padding(horizontal = 18.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_power_24),
                            contentDescription = null,
                            tint = BannerOrange,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Fix Now",
                            style = MaterialTheme.typography.labelMedium,
                            color = BannerOrange
                        )
                    }
                }
            }
        }
    }
}
