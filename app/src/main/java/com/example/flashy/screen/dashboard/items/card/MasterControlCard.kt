package com.example.flashy.screen.dashboard.items.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.flashy.R
import com.example.flashy.screen.dashboard.items.AnimatedSwitch

@Composable
fun MasterControlCard(
    flashGlobal: Boolean,
    flashScreenOffOnly: Boolean,
    onFlashGlobalChange: (Boolean) -> Unit,
    onFlashScreenOffOnlyChange: (Boolean) -> Unit
) {
    GlassMorphismCard {
        Column(modifier = Modifier.padding(24.dp)) {
            AnimatedSwitch(
                label = "Master Control",
                sublabel = "Global flash system",
                isEnabled = flashGlobal,
                onToggle = onFlashGlobalChange,
                icon = painterResource(R.drawable.baseline_flash_on_24),
                color = Color(0xFF4CAF50)
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedSwitch(
                label = "Screen Off Mode",
                sublabel = "Flash when screen is locked",
                isEnabled = flashScreenOffOnly,
                onToggle = onFlashScreenOffOnlyChange,
                icon = painterResource(R.drawable.baseline_phone_locked_24),
                color = Color(0xFF2196F3)
            )
        }
    }
}