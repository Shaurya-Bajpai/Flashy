package com.dsb.flashy.screen.dashboard.items.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
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
import com.dsb.flashy.screen.dashboard.items.AnimatedSwitch
import com.dsb.flashy.ui.theme.Amber
import com.dsb.flashy.ui.theme.TextDim
import com.dsb.flashy.ui.theme.TextMuted
import com.dsb.flashy.ui.theme.TextWarm

private val ColorSoundReactive = Color(0xFF7C3AED)  // vivid violet

@Composable
fun SoundReactiveCard(
    enabled: Boolean,
    sensitivity: Int,
    onEnabledChange: (Boolean) -> Unit,
    onSensitivityChange: (Int) -> Unit,
    onSensitivityChangeFinished: () -> Unit,
) {
    GlassMorphismCard(accentGlow = if (enabled) ColorSoundReactive.copy(alpha = 0.08f) else Color.Transparent) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.baseline_volume_up_24),
                    contentDescription = null,
                    tint = ColorSoundReactive,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Sound Reactive Flash",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWarm
                    )
                    Text(
                        text = "Flash syncs with music beats or ambient sound",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            AnimatedSwitch(
                label = "Beat Sync",
                sublabel = "Uses microphone · may increase battery usage",
                isEnabled = enabled,
                onToggle = onEnabledChange,
                icon = painterResource(R.drawable.baseline_volume_up_24),
                color = ColorSoundReactive
            )

            AnimatedVisibility(
                visible = enabled,
                enter = expandVertically() + fadeIn(),
                exit  = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(Modifier.height(6.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.07f))
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sensitivity",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextWarm,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "$sensitivity%",
                            style = MaterialTheme.typography.labelLarge,
                            color = ColorSoundReactive
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Slider(
                        value = sensitivity / 100f,
                        onValueChange = { onSensitivityChange((it * 100).toInt().coerceIn(1, 100)) },
                        onValueChangeFinished = onSensitivityChangeFinished,
                        valueRange = 0.01f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = ColorSoundReactive,
                            activeTrackColor = ColorSoundReactive,
                            inactiveTrackColor = ColorSoundReactive.copy(alpha = 0.18f),
                            activeTickColor = Color.Transparent,
                            inactiveTickColor = Color.Transparent
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Less sensitive",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextDim,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "More sensitive",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextDim
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "Tip: start at 50% for music, raise if it's not triggering, lower if it triggers too easily",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDim
                    )
                }
            }
        }
    }
}
