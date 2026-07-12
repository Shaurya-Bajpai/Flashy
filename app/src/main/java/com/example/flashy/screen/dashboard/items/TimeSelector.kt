package com.dsb.flashy.screen.dashboard.items

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsb.flashy.ui.theme.ColorSchedule
import com.dsb.flashy.ui.theme.TextDim
import com.dsb.flashy.ui.theme.TextMuted

@Composable
fun TimeSelector(
    label: String,
    time: String,
    icon: Painter? = null,
    accentColor: Color = ColorSchedule,
    onTimeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val shape = RoundedCornerShape(14.dp)

    Column(
        modifier = modifier
            .clip(shape)
            .background(accentColor.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.32f),
                        accentColor.copy(alpha = 0.07f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(300f, 300f)
                ),
                shape = shape
            )
            .clickable {
                val parts = time.split(":").mapNotNull { it.toIntOrNull() }
                val h = if (parts.size > 0) parts[0] else 0
                val m = if (parts.size > 1) parts[1] else 0
                TimePickerDialog(context, { _, hour, minute ->
                    onTimeChange(String.format("%02d:%02d", hour, minute))
                }, h, m, true).show()
            }
            .padding(vertical = 14.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (icon != null) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = accentColor.copy(alpha = 0.80f),
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.height(6.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            letterSpacing = 0.8.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = time,
            style = MaterialTheme.typography.headlineSmall,
            color = accentColor
        )
    }
}
