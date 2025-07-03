package com.example.flashy.screen.dash.items.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flashy.R
import com.example.flashy.screen.dash.items.SettingItem
import com.example.flashy.screen.dash.items.GlowingDivider

@Composable
fun AlertTypesCard(
    flashCall: Boolean,
    onFlashCallChange: (Boolean) -> Unit,
    flashSms: Boolean,
    onFlashSmsChange: (Boolean) -> Unit,
    flashNotify: Boolean,
    onFlashNotifyChange: (Boolean) -> Unit
) {
    PremiumGlassCard {
        Column {
            Row(
                modifier = Modifier.padding(24.dp, 20.dp, 24.dp, 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF667eea), Color(0xFF764ba2))
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Alert Types",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.3f),
                            offset = Offset(1f, 1f),
                            blurRadius = 4f
                        )
                    )
                )
            }

            SettingItem(
                icon = Icons.Default.Phone,
                title = "Incoming Calls",
                subtitle = "Flash for incoming calls",
                isEnabled = flashCall,
                onToggle = onFlashCallChange,
                iconColor = Color(0xFF4CAF50),
                gradientColors = listOf(Color(0xFF4CAF50), Color(0xFF8BC34A))
            )

            GlowingDivider()

            SettingItem(
                painter = painterResource(R.drawable.baseline_sms_24),
                title = "Text Messages",
                subtitle = "Double flash for SMS",
                isEnabled = flashSms,
                onToggle = onFlashSmsChange,
                iconColor = Color(0xFF9C27B0),
                gradientColors = listOf(Color(0xFF9C27B0), Color(0xFFE91E63))
            )

            GlowingDivider()

            SettingItem(
                icon = Icons.Default.Notifications,
                title = "App Notifications",
                subtitle = "Flash for app alerts",
                isEnabled = flashNotify,
                onToggle = onFlashNotifyChange,
                iconColor = Color(0xFFFF5722),
                gradientColors = listOf(Color(0xFFFF5722), Color(0xFFFF9800))
            )
        }
    }
}