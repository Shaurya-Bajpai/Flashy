package com.example.flashy.screen.dash.items.card

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.example.flashy.R
import com.example.flashy.screen.dash.items.SettingItem
import com.example.flashy.screen.dash.items.GlowingDivider

@Composable
fun MainSettingsCard(
    flashScreenOffOnly: Boolean,
    onFlashScreenOffOnlyChange: (Boolean) -> Unit,
    flashGlobal: Boolean,
    onFlashGlobalChange: (Boolean) -> Unit
) {
    PremiumGlassCard {
        Column {
            SettingItem(
                icon = Icons.Default.Lock,
                title = "Screen Off Flash",
                subtitle = "Flash only when display is off",
                isEnabled = flashScreenOffOnly,
                onToggle = onFlashScreenOffOnlyChange,
                iconColor = Color(0xFF2196F3),
                gradientColors = listOf(Color(0xFF2196F3), Color(0xFF21CBF3))
            )

            GlowingDivider()

            SettingItem(
                painter = painterResource(R.drawable.baseline_flash_on_24),
                title = "Master Flash Control",
                subtitle = "Enable all flash notifications",
                isEnabled = flashGlobal,
                onToggle = onFlashGlobalChange,
                iconColor = Color(0xFFFFC107),
                gradientColors = listOf(Color(0xFFFFC107), Color(0xFFFF8F00)),
                isPrimary = true
            )
        }
    }
}