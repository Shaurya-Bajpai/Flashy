package com.dsb.flashy.screen.dashboard.items.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dsb.flashy.R
import com.dsb.flashy.screen.dashboard.items.AnimatedSwitch
import com.dsb.flashy.ui.theme.Amber
import com.dsb.flashy.ui.theme.ColorScreenOff
import com.dsb.flashy.ui.theme.TextMuted
import com.dsb.flashy.ui.theme.TextWarm

@Composable
fun MasterControlCard(
    flashGlobal: Boolean,
    flashScreenOffOnly: Boolean,
    onFlashGlobalChange: (Boolean) -> Unit,
    onFlashScreenOffOnlyChange: (Boolean) -> Unit
) {
    GlassMorphismCard(accentColor = if (flashGlobal) Amber else Color.Transparent) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Master Control",
                style = MaterialTheme.typography.titleLarge,
                color = TextWarm
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "Global flash system switches",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
            Spacer(Modifier.height(18.dp))

            AnimatedSwitch(
                label = "Flash Alerts",
                sublabel = "Enable or disable all flash notifications",
                isEnabled = flashGlobal,
                onToggle = onFlashGlobalChange,
                icon = painterResource(R.drawable.baseline_flash_on_24),
                color = Amber
            )

            Spacer(Modifier.height(6.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.07f))
            Spacer(Modifier.height(6.dp))

            AnimatedSwitch(
                label = "Screen-Off Only",
                sublabel = "Flash only when the screen is locked",
                isEnabled = flashScreenOffOnly,
                onToggle = onFlashScreenOffOnlyChange,
                icon = painterResource(R.drawable.baseline_phone_locked_24),
                color = ColorScreenOff
            )
        }
    }
}
