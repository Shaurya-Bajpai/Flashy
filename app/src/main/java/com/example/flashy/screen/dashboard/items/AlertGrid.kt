package com.dsb.flashy.screen.dashboard.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dsb.flashy.R
import com.dsb.flashy.screen.dashboard.items.card.AlertTypeCard
import com.dsb.flashy.screen.dashboard.items.card.GlassMorphismCard
import com.dsb.flashy.ui.theme.ColorApp
import com.dsb.flashy.ui.theme.ColorCall
import com.dsb.flashy.ui.theme.ColorSms
import com.dsb.flashy.ui.theme.TextMuted
import com.dsb.flashy.ui.theme.TextWarm

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
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Alert Triggers",
                style = MaterialTheme.typography.titleLarge,
                color = TextWarm
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "Choose what activates your flash",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AlertTypeCard(
                    icon = Icons.Default.Phone,
                    label = "Calls",
                    isEnabled = flashCall,
                    onToggle = onFlashCallChange,
                    color = ColorCall,
                    modifier = Modifier.weight(1f)
                )
                AlertTypeCard(
                    painter = painterResource(R.drawable.baseline_sms_24),
                    pain = true,
                    label = "SMS",
                    isEnabled = flashSms,
                    onToggle = onFlashSmsChange,
                    color = ColorSms,
                    modifier = Modifier.weight(1f)
                )
                AlertTypeCard(
                    painter = painterResource(R.drawable.baseline_notifications_active_24),
                    pain = true,
                    label = "Apps",
                    isEnabled = flashNotify,
                    onToggle = onFlashNotifyChange,
                    color = ColorApp,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
