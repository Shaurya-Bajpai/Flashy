package com.dsb.flashy.screen.dashboard.items.card

import android.text.format.DateUtils
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dsb.flashy.R
import com.dsb.flashy.model.FlashHistoryEvent
import com.dsb.flashy.ui.theme.ColorDanger
import com.dsb.flashy.ui.theme.TextDim
import com.dsb.flashy.ui.theme.TextMuted
import com.dsb.flashy.ui.theme.TextWarm

private val ColorCallAccent  = Color(0xFF60A5FA) // blue
private val ColorSmsAccent   = Color(0xFF34D399) // green
private val ColorNotifAccent = Color(0xFFFBBF24) // amber

@Composable
fun FlashHistoryCard(
    events: List<FlashHistoryEvent>,
    onClear: () -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    GlassMorphismCard(accentGlow = Color(0xFF818CF8).copy(alpha = 0.05f)) {
        Column(modifier = Modifier.padding(20.dp)) {
            // ── Header ──────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_history_24),
                    contentDescription = null,
                    tint = Color(0xFF818CF8),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Flash History",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWarm
                    )
                    Text(
                        text = if (events.isEmpty()) "No events yet" else "${events.size} recent events",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
                if (events.isNotEmpty()) {
                    IconButton(onClick = onClear, modifier = Modifier.size(32.dp)) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_delete_sweep_24),
                            contentDescription = "Clear history",
                            tint = TextDim,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                }
                Icon(
                    painter = painterResource(
                        if (expanded) R.drawable.baseline_flash_on_24 else R.drawable.baseline_flash_off_24
                    ),
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = TextDim,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { expanded = !expanded }
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column {
                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.07f))

                    if (events.isEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Events will appear here once the flash triggers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDim,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                    } else {
                        events.forEachIndexed { index, event ->
                            HistoryEventRow(event)
                            if (index < events.lastIndex) {
                                HorizontalDivider(
                                    color = Color.White.copy(alpha = 0.04f),
                                    modifier = Modifier.padding(start = 44.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryEventRow(event: FlashHistoryEvent) {
    val (accentColor, iconRes, typeLabel) = when (event.eventType) {
        "CALL"  -> Triple(ColorCallAccent,  R.drawable.baseline_phone_android_24, "Call")
        "SMS"   -> Triple(ColorSmsAccent,   R.drawable.baseline_sms_24,           "SMS")
        else    -> Triple(ColorNotifAccent, R.drawable.baseline_notifications_active_24, "Notif")
    }

    val primaryText = when {
        event.senderName.isNotBlank() -> event.senderName
        event.appName.isNotBlank()    -> event.appName
        event.eventType == "CALL"     -> "Incoming Call"
        event.eventType == "SMS"      -> "New Message"
        else                          -> "Notification"
    }

    val secondaryText = when {
        event.appName.isNotBlank() && event.senderName.isNotBlank() -> event.appName
        event.eventType == "CALL" && event.senderName.isBlank()     -> "Phone"
        else -> ""
    }

    val timeAgo = if (event.timestampMs > 0L) {
        DateUtils.getRelativeTimeSpanString(
            event.timestampMs,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    } else ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Event type icon bubble
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = primaryText,
                style = MaterialTheme.typography.bodyMedium,
                color = TextWarm,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (secondaryText.isNotBlank()) {
                Text(
                    text = secondaryText,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            // Type badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = typeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor
                )
            }
            if (timeAgo.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextDim
                )
            }
        }
    }
}
