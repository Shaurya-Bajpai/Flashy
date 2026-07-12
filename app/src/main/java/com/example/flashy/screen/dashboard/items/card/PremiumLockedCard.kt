package com.dsb.flashy.screen.dashboard.items.card

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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsb.flashy.ui.theme.Amber
import com.dsb.flashy.ui.theme.TextDim
import com.dsb.flashy.ui.theme.TextMuted
import com.dsb.flashy.ui.theme.TextWarm

/**
 * Wraps a dashboard card with a premium gate.
 * When [isPremium] is false, shows a locked placeholder card with an upgrade CTA.
 * When [isPremium] is true, renders the [content] normally.
 */
@Composable
fun PremiumGate(
    isPremium: Boolean,
    featureName: String,
    featureIconRes: Int,
    featureDescription: String,
    accentColor: Color,
    onUpgradeClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    if (isPremium) {
        content()
        return
    }

    GlassMorphismCard(accentColor = Amber) {
        Column(modifier = Modifier.padding(20.dp)) {

            // ── Feature identity row ──────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(accentColor.copy(alpha = 0.12f), CircleShape)
                        .border(1.dp, accentColor.copy(alpha = 0.22f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(featureIconRes),
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = featureName,
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWarm
                    )
                    Text(
                        text = featureDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color.White.copy(alpha = 0.07f))
            Spacer(Modifier.height(14.dp))

            // ── Premium badge ─────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(Amber.copy(alpha = 0.14f), RoundedCornerShape(50))
                        .border(1.dp, Amber.copy(alpha = 0.35f), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Amber,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "PREMIUM FEATURE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.1.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Amber
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Upgrade to Flashy Premium to unlock this feature.",
                style = MaterialTheme.typography.bodySmall,
                color = TextDim
            )

            Spacer(Modifier.height(18.dp))

            // ── Upgrade CTA ───────────────────────────────────────────────────
            val btnShape = RoundedCornerShape(12.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(btnShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Amber, Color(0xFFE65100))
                        )
                    )
                    .clickable { onUpgradeClick() }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFF1A0D00),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Unlock Premium · ₹249",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF1A0D00),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
