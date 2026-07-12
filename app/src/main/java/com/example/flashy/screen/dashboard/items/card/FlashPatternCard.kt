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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsb.flashy.R
import com.dsb.flashy.ui.theme.Amber
import com.dsb.flashy.ui.theme.ColorApp
import com.dsb.flashy.ui.theme.ColorCall
import com.dsb.flashy.ui.theme.ColorSms
import com.dsb.flashy.ui.theme.TextDim
import com.dsb.flashy.ui.theme.TextMuted
import com.dsb.flashy.ui.theme.TextWarm

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
    callCount: Int,
    callSpeedMs: Int,
    smsCount: Int,
    smsSpeedMs: Int,
    notifCount: Int,
    notifSpeedMs: Int,
    onCallCountChange: (Int) -> Unit,
    onCallSpeedChange: (Int) -> Unit,
    onSmsCountChange: (Int) -> Unit,
    onSmsSpeedChange: (Int) -> Unit,
    onNotifCountChange: (Int) -> Unit,
    onNotifSpeedChange: (Int) -> Unit,
) {
    GlassMorphismCard(accentColor = Amber) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.baseline_flash_on_24),
                    contentDescription = null,
                    tint = Amber,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Flash Pattern",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWarm
                    )
                    Text(
                        text = "Customize speed and count per alert type",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            // ── Calls ────────────────────────────────────────────────────────
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

            SectionDivider()

            // ── SMS ──────────────────────────────────────────────────────────
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

            SectionDivider()

            // ── Apps ─────────────────────────────────────────────────────────
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section header: icon + label
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(accentColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (iconVector != null) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                } else if (iconRes != null) {
                    Icon(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(Modifier.width(10.dp))
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
        targetValue = if (selected) accentColor.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.04f),
        animationSpec = tween(180),
        label = "chip_bg_$text"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) accentColor.copy(alpha = 0.60f) else Color.White.copy(alpha = 0.10f),
        animationSpec = tween(180),
        label = "chip_border_$text"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) accentColor else TextDim,
        animationSpec = tween(180),
        label = "chip_text_$text"
    )

    val shape = RoundedCornerShape(8.dp)
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

@Composable
private fun SectionDivider() {
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 0.5.dp,
        color = Color.White.copy(alpha = 0.07f)
    )
}
