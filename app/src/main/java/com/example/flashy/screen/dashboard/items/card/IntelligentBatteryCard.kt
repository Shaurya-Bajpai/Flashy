package com.dsb.flashy.screen.dashboard.items.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
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
import com.example.flashy.screen.dashboard.items.MainCardHeading

@Composable
fun IntelligentBatteryCard(
    threshold: Float,
    onThresholdChange: (Float) -> Unit,
    onThresholdChangeFinished: () -> Unit
) {
    GlassMorphismCard {
        Column(modifier = Modifier.padding(24.dp)) {
            MainCardHeading(
                text = "Smart Battery: ${(threshold * 100).toInt()}%",
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Slider(
                value = threshold,
                onValueChange = onThresholdChange,
                onValueChangeFinished = onThresholdChangeFinished,
                valueRange = 0.05f..0.3f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFFF9800),
                    activeTrackColor = Color(0xFFFF9800),
                    inactiveTrackColor = Color(0xFFFF9800).copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Preview(showBackground = false)
@Composable
fun IntelligentBatteryCardPreview() {
    IntelligentBatteryCard(
        threshold = 0.2f,
        onThresholdChange = {},
        onThresholdChangeFinished = {}
    )
}