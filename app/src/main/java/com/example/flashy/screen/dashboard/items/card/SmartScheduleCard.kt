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
import com.dsb.flashy.screen.dashboard.items.TimeSelector
import com.example.flashy.screen.dashboard.items.MainCardHeading

@Composable
fun SmartScheduleCard(
    startTime: String,
    endTime: String,
    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit
) {
    GlassMorphismCard {
        Column(modifier = Modifier.padding(24.dp)) {
            MainCardHeading(
                text = "Smart Schedule",
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TimeSelector(
                    label = "Sleep Start",
                    time = startTime,
                    onTimeChange = onStartTimeChange,
                    modifier = Modifier.weight(1f)
                )

                TimeSelector(
                    label = "Sleep End",
                    time = endTime,
                    onTimeChange = onEndTimeChange,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = false)
@Composable
fun SmartScheduleCardPreview() {
    SmartScheduleCard(
        startTime = "22:00",
        endTime = "06:00",
        onStartTimeChange = {},
        onEndTimeChange = {}
    )
}