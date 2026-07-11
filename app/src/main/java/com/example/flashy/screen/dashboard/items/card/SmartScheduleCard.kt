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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dsb.flashy.R
import com.dsb.flashy.screen.dashboard.items.TimeSelector
import com.dsb.flashy.ui.theme.ColorSchedule
import com.dsb.flashy.ui.theme.TextMuted
import com.dsb.flashy.ui.theme.TextWarm

@Composable
fun SmartScheduleCard(
    startTime: String,
    endTime: String,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit
) {
    GlassMorphismCard {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.baseline_bedtime_24),
                    contentDescription = null,
                    tint = ColorSchedule,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Sleep Mode",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWarm
                    )
                    Text(
                        text = "Mute flash during these hours",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TimeSelector(
                    label = "SLEEP",
                    time = startTime,
                    icon = painterResource(R.drawable.baseline_bedtime_24),
                    accentColor = ColorSchedule,
                    onTimeChange = onStartTimeChange,
                    modifier = Modifier.weight(1f)
                )
                TimeSelector(
                    label = "WAKE",
                    time = endTime,
                    icon = painterResource(R.drawable.baseline_wb_sunny_24),
                    accentColor = Color(0xFFFFA726),
                    onTimeChange = onEndTimeChange,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
