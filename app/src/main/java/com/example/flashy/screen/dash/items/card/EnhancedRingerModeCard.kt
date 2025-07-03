package com.example.flashy.screen.dash.items.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashy.R
import com.example.flashy.screen.dash.items.ModeChip

@Composable
fun EnhancedRingerModeCard(selectedMode: String, onModeChange: (String) -> Unit) {
    val modes = listOf(
        Triple("All", painterResource(R.drawable.baseline_notifications_active_24), listOf(Color(0xFF3F51B5), Color(0xFF5C6BC0))),
        Triple("Normal", painterResource(R.drawable.baseline_volume_up_24), listOf(Color(0xFF4CAF50), Color(0xFF8BC34A))),
        Triple("Vibrate", painterResource(R.drawable.baseline_vibration_24), listOf(Color(0xFFFF9800), Color(0xFFFFC107))),
        Triple("Silent", painterResource(R.drawable.baseline_volume_off_24), listOf(Color(0xFF9E9E9E), Color(0xFFBDBDBD)))
    )

    PremiumGlassCard {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF3F51B5), Color(0xFF5C6BC0))
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_phone_android_24),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Ringer Mode",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

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