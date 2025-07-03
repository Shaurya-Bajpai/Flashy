package com.example.flashy.screen.dashboard.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashy.screen.dashboard.items.card.AlertTypeCard
import com.example.flashy.screen.dashboard.items.card.GlassMorphismCard

@Composable
fun AlertGrid(
    flashCall: Boolean,
    flashSms: Boolean,
    flashNotify: Boolean,
    onFlashCallChange: (Boolean) -> Unit,
    onFlashSmsChange: (Boolean) -> Unit,
    onFlashNotifyChange: (Boolean) -> Unit
) {
    GlassMorphismCard {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = "Alert Types",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AlertTypeCard(
                    icon = Icons.Default.Phone,
                    label = "Calls",
                    isEnabled = flashCall,
                    onToggle = onFlashCallChange,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )

                AlertTypeCard(
                    pain = true,
                    label = "SMS",
                    isEnabled = flashSms,
                    onToggle = onFlashSmsChange,
                    color = Color(0xFF9C27B0),
                    modifier = Modifier.weight(1f)
                )

                AlertTypeCard(
                    label = "Apps",
                    isEnabled = flashNotify,
                    onToggle = onFlashNotifyChange,
                    color = Color(0xFFFF5722),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}