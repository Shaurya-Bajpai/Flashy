package com.dsb.flashy.screen.dashboard.items.card

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsb.flashy.R
import com.dsb.flashy.datastore.GlobalSettingsStore
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_CALL
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_GLOBAL
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_NOTIFICATIONS
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_SMS
import com.dsb.flashy.ui.theme.ColorApp
import com.dsb.flashy.ui.theme.ColorCall
import com.dsb.flashy.ui.theme.ColorSms
import com.dsb.flashy.ui.theme.TextDim
import com.dsb.flashy.ui.theme.TextWarm
import com.dsb.flashy.screen.dashboard.items.MainCardHeading

// Speed and count option descriptors — kept package-private to this file.
private data class SpeedOption(val label: String, val ms: Int)
private data class CountOption(val label: String, val count: Int)

private val SPEED_OPTIONS = listOf(
    SpeedOption("Rapid",  100),
    SpeedOption("Normal", 200),
    SpeedOption("Gentle", 400),
)

private val COUNT_OPTIONS = listOf(
    CountOption("3×",  3),
    CountOption("5×",  5),
    CountOption("10×", 10),
)

private val COUNT_OPTIONS_CALL = listOf(
    CountOption("3×",  3),
    CountOption("5×",  5),
    CountOption("10×", 10),
    CountOption("∞",   0),   // 0 = blink until call ends
)

@Composable
fun FlashPatternCard(
    flashCall: Boolean,
    flashSms: Boolean,
    flashNotify: Boolean,
    callCount: Int,
    callSpeedMs: Int,
    smsCount: Int,
    smsSpeedMs: Int,
    notifCount: Int,
    notifSpeedMs: Int,
    onCallCountChange: (Int) -> Unit = {},
    onCallSpeedChange: (Int) -> Unit = {},
    onSmsCountChange: (Int) -> Unit = {},
    onSmsSpeedChange: (Int) -> Unit = {},
    onNotifCountChange: (Int) -> Unit = {},
    onNotifSpeedChange: (Int) -> Unit = {},
) {
    GlassMorphismCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            MainCardHeading(
                text = "Flash Pattern",
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // ── Calls ────────────────────────────────────────────────
            if(flashCall) {
                PatternSection(
                    iconVector = Icons.Default.Phone,
                    label = "Calls",
                    accentColor = ColorCall,
                    speedMs = callSpeedMs,
                    count = callCount,
                    countOptions = COUNT_OPTIONS_CALL,
                    onSpeedChange = onCallSpeedChange,
                    onCountChange = onCallCountChange
                )
                Spacer(Modifier.height(14.dp))
            }

            // ── SMS ──────────────────────────────────────────────────
            if(flashSms) {
                PatternSection(
                    iconRes = R.drawable.baseline_sms_24,
                    label = "SMS",
                    accentColor = ColorSms,
                    speedMs = smsSpeedMs,
                    count = smsCount,
                    countOptions = COUNT_OPTIONS,
                    onSpeedChange = onSmsSpeedChange,
                    onCountChange = onSmsCountChange
                )
                Spacer(Modifier.height(14.dp))
            }

            // ── Apps ─────────────────────────────────────────────────
            if(flashNotify) {
                PatternSection(
                    iconRes = R.drawable.baseline_notifications_active_24,
                    label = "Apps",
                    accentColor = ColorApp,
                    speedMs = notifSpeedMs,
                    count = notifCount,
                    countOptions = COUNT_OPTIONS,
                    onSpeedChange = onNotifSpeedChange,
                    onCountChange = onNotifCountChange
                )
            }
        }
    }
}

// ── Section composables ─────────────────────────────────────────────────────

@Composable
private fun PatternSection(
    iconVector: ImageVector? = null,
    iconRes: Int? = null,
    label: String,
    accentColor: Color,
    speedMs: Int,
    count: Int,
    countOptions: List<CountOption>,
    onSpeedChange: (Int) -> Unit,
    onCountChange: (Int) -> Unit,
) {
    // Each alert type now reads as its own soft, tinted card — mirroring the
    // "Alert Types" tiles on the dashboard — instead of a divider-separated row.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        accentColor.copy(alpha = 0.10f),
                        accentColor.copy(alpha = 0.03f)
                    )
                )
            )
            .border(1.dp, accentColor.copy(alpha = 0.16f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section header: rounded-square icon badge + label
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(accentColor.copy(alpha = 0.20f))
                    .border(1.dp, accentColor.copy(alpha = 0.30f), RoundedCornerShape(11.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (iconVector != null) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(17.dp)
                    )
                } else if (iconRes != null) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = TextWarm,
                letterSpacing = 0.5.sp
            )
        }

        // Speed row
        ChipRow(
            rowLabel = "Speed",
            options = SPEED_OPTIONS.map { it.label },
            selectedIndex = SPEED_OPTIONS.indexOfFirst { it.ms == speedMs }.coerceAtLeast(0),
            accentColor = accentColor,
            onSelect = { idx -> onSpeedChange(SPEED_OPTIONS[idx].ms) }
        )

        // Count row
        ChipRow(
            rowLabel = "Count",
            options = countOptions.map { it.label },
            selectedIndex = countOptions.indexOfFirst { it.count == count }.coerceAtLeast(0),
            accentColor = accentColor,
            onSelect = { idx -> onCountChange(countOptions[idx].count) }
        )
    }
}

@Composable
private fun ChipRow(
    rowLabel: String,
    options: List<String>,
    selectedIndex: Int,
    accentColor: Color,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = rowLabel,
            style = MaterialTheme.typography.bodySmall,
            color = TextDim,
            modifier = Modifier.width(44.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEachIndexed { index, label ->
                PatternChip(
                    text = label,
                    selected = index == selectedIndex,
                    accentColor = accentColor,
                    onClick = { onSelect(index) }
                )
            }
        }
    }
}

@Composable
private fun PatternChip(
    text: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "chip_scale_$text"
    )
    val bgColor by animateColorAsState(
        targetValue = if (selected) accentColor.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.05f),
        animationSpec = tween(180),
        label = "chip_bg_$text"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) accentColor.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.10f),
        animationSpec = tween(180),
        label = "chip_border_$text"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else TextDim,
        animationSpec = tween(180),
        label = "chip_text_$text"
    )

    val shape = RoundedCornerShape(10.dp)
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = textColor,
        modifier = Modifier
            .scale(scale)
            .clip(shape)
            .background(bgColor, shape)
            .border(1.dp, borderColor, shape)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    )
}

@Preview(showBackground = false)
@Composable
fun FlashPatternCardPreview() {
    FlashPatternCard(
        flashCall = true,
        flashSms = true,
        flashNotify = true,
        callCount = 0,
        callSpeedMs = 200,
        smsCount = 5,
        smsSpeedMs = 200,
        notifCount = 5,
        notifSpeedMs = 200,
    )
}