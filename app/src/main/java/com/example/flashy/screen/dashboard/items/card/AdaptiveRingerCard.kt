package com.example.flashy.screen.dashboard.items.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashy.R
import com.example.flashy.screen.dash.items.ModeChip
//import com.example.flashy.screen.dashboard.items.ModeChip

@Composable
fun AdaptiveRingerCard(
    selectedMode: String,
    onModeChange: (String) -> Unit
) {
    val modes = listOf(
        Triple("All", painterResource(R.drawable.baseline_notifications_active_24), listOf(Color(0xFF2196F3), Color(0xFF64B5F6))), // Vibrant blue gradient
        Triple("Normal", painterResource(R.drawable.baseline_volume_up_24), listOf(Color(0xFF009688), Color(0xFF4DB6AC))), // Teal gradient
        Triple("Vibrate", painterResource(R.drawable.baseline_vibration_24), listOf(Color(0xFFFFA726), Color(0xFFFFCC80))), // Enhanced orange gradient
        Triple("Silent", painterResource(R.drawable.baseline_volume_off_24), listOf(Color(0xFF757575), Color(0xFFBDBDBD))) // Enhanced gray gradient
    )
//    val modes = listOf("All", "Normal", "Vibrate", "Silent")

    GlassMorphismCard {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Adaptive Modes",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 16.dp)
            )

//            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
//                items(modes.size) { index ->
//                    val mode = modes[index]
//                    val isSelected = mode == selectedMode
//
//                    ModeChip(
//                        label = mode,
//                        isSelected = isSelected,
//                        onClick = { onModeChange(mode) }
//                    )
//                }
//            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                modes.forEach { (mode, icon, colors) ->
                    val isSelected = mode == selectedMode

                    ModeChip(
                        mode = mode,
                        icon = icon,
                        isSelected = isSelected,
                        gradientColors = colors,
                        onClick = { onModeChange(mode) }
                    )
                }
            }
        }
    }
}