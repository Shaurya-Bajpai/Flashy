package com.dsb.flashy.screen.premium

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dsb.flashy.R
import com.dsb.flashy.screen.dashboard.items.AnimatedBackground
import com.dsb.flashy.ui.theme.Amber
import com.dsb.flashy.ui.theme.AmberDark
import com.dsb.flashy.ui.theme.AmberDeep
import com.dsb.flashy.ui.theme.AmberLight
import com.dsb.flashy.ui.theme.TextDim
import com.dsb.flashy.ui.theme.TextMuted
import com.dsb.flashy.ui.theme.TextWarm

// UI state for the restore-purchase flow
sealed class RestoreState {
    object Idle    : RestoreState()
    object Loading : RestoreState()
    object Success : RestoreState()
    data class Error(val message: String) : RestoreState()
}

private data class PremiumFeature(
    val iconRes: Int?,
    val iconVec: androidx.compose.ui.graphics.vector.ImageVector?,
    val name: String,
    val detail: String,
    val accent: Color,
)

@Composable
fun PremiumScreen(
    isPurchasing: Boolean,
    restoreState: RestoreState = RestoreState.Idle,
    onUpgradeClick: () -> Unit,
    onRestoreClick: (paymentId: String) -> Unit = {},
    onBack: () -> Unit,
) {
    val features = listOf(
        PremiumFeature(
            iconRes = R.drawable.baseline_flash_on_24, iconVec = null,
            name = "Flash Pattern",
            detail = "Custom speed & blink count for calls, SMS, and apps",
            accent = Amber
        ),
        PremiumFeature(
            iconRes = R.drawable.baseline_mic_24, iconVec = null,
            name = "Sound Reactive Flash",
            detail = "Flash syncs with music beats and ambient audio",
            accent = Color(0xFF7C3AED)
        ),
        PremiumFeature(
            iconRes = null, iconVec = Icons.Default.Phone,
            name = "Contact Filter",
            detail = "Flash only when specific people call or message you",
            accent = Color(0xFF0EA5E9)
        ),
        PremiumFeature(
            iconRes = R.drawable.baseline_notifications_active_24, iconVec = null,
            name = "Per-App Rules",
            detail = "Block or customize flash behavior per installed app",
            accent = Color(0xFF10B981)
        ),
    )

    val freeFeatures = listOf(
        "Calls, SMS & App alert triggers",
        "Smart schedule, DND & ringer modes",
        "Battery guard & low-battery alert",
        "Screen-off only mode",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF140A00), Color(0xFF0A0600), Color(0xFF060401)),
                    radius = 1600f
                )
            )
    ) {
        AnimatedBackground()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {

            // ── Top bar ───────────────────────────────────────────────────────
            item(key = "topbar") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextMuted
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .background(Amber.copy(alpha = 0.14f), RoundedCornerShape(50))
                            .border(1.dp, Amber.copy(alpha = 0.32f), RoundedCornerShape(50))
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(Icons.Default.Star, null, tint = Amber, modifier = Modifier.size(12.dp))
                            Text(
                                text = "PREMIUM",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.5.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Amber
                            )
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                }
            }

            // ── Hero section ──────────────────────────────────────────────────
            item(key = "hero") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Amber glow orb
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(96.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .drawBehind {
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Amber.copy(alpha = 0.45f),
                                                AmberDeep.copy(alpha = 0.18f),
                                                Color.Transparent
                                            )
                                        ),
                                        radius = size.minDimension * 0.80f
                                    )
                                }
                        )
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(
                                    Brush.radialGradient(listOf(AmberLight, Amber, AmberDark)),
                                    CircleShape
                                )
                                .border(2.dp, Amber.copy(alpha = 0.50f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_flash_on_24),
                                contentDescription = null,
                                tint = Color(0xFF1A0D00),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "FLASHY PREMIUM",
                        style = TextStyle(
                            brush = Brush.horizontalGradient(listOf(AmberLight, Amber, AmberDeep)),
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 4.sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(50))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(50))
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "₹249  ·  One-time  ·  Lifetime access",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // ── "What you unlock" ─────────────────────────────────────────────
            item(key = "unlock_header") {
                SectionHeader("WHAT YOU UNLOCK")
                Spacer(Modifier.height(12.dp))
            }

            items(count = features.size, key = { "feat_$it" }) { idx ->
                FeatureRow(features[idx])
                if (idx < features.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 58.dp),
                        thickness = 0.5.dp,
                        color = Color.White.copy(alpha = 0.06f)
                    )
                }
            }

            item(key = "free_header") {
                Spacer(Modifier.height(28.dp))
                SectionHeader("ALWAYS FREE")
                Spacer(Modifier.height(12.dp))
            }

            items(count = freeFeatures.size, key = { "free_$it" }) { idx ->
                FreeFeatureRow(freeFeatures[idx])
            }

            // ── CTA ───────────────────────────────────────────────────────────
            item(key = "cta") {
                Spacer(Modifier.height(36.dp))
                val btnShape = RoundedCornerShape(16.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(btnShape)
                        .background(
                            Brush.horizontalGradient(listOf(Amber, Color(0xFFE65100)))
                        )
                        .clickable(enabled = !isPurchasing) { onUpgradeClick() }
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPurchasing) {
                        CircularProgressIndicator(
                            color = Color(0xFF1A0D00),
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Star,
                                null,
                                tint = Color(0xFF1A0D00),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Unlock Premium · ₹249",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF1A0D00),
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "Payment secured by Razorpay · No subscription · No renewal",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextDim,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(36.dp))
            }

            // ── Restore purchase ──────────────────────────────────────────────
            item(key = "restore") {
                RestorePurchaseSection(
                    restoreState = restoreState,
                    onRestoreClick = onRestoreClick
                )
                Spacer(Modifier.height(36.dp))
            }
        }
    }
}

// ── Small composables ────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(13.dp)
                .background(
                    Brush.verticalGradient(listOf(Amber, Amber.copy(alpha = 0.15f))),
                    RoundedCornerShape(2.dp)
                )
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            ),
            color = Amber.copy(alpha = 0.72f)
        )
    }
}

@Composable
private fun FeatureRow(feature: PremiumFeature) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(feature.accent.copy(alpha = 0.12f), CircleShape)
                .border(1.dp, feature.accent.copy(alpha = 0.22f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (feature.iconRes != null) {
                Icon(
                    painter = painterResource(feature.iconRes),
                    contentDescription = null,
                    tint = feature.accent,
                    modifier = Modifier.size(20.dp)
                )
            } else if (feature.iconVec != null) {
                Icon(
                    imageVector = feature.iconVec,
                    contentDescription = null,
                    tint = feature.accent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(feature.name, style = MaterialTheme.typography.bodyLarge, color = TextWarm)
            Spacer(Modifier.height(2.dp))
            Text(feature.detail, style = MaterialTheme.typography.bodySmall, color = TextMuted)
        }
    }
}

@Composable
private fun RestorePurchaseSection(
    restoreState: RestoreState,
    onRestoreClick: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var paymentId by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalDivider(thickness = 0.5.dp, color = Color.White.copy(alpha = 0.07f))
        Spacer(Modifier.height(20.dp))

        // "Already paid on another device?" toggle
        Text(
            text = if (expanded) "Hide restore" else "Already paid on another device?",
            style = MaterialTheme.typography.labelMedium,
            color = if (expanded) TextDim else Amber.copy(alpha = 0.80f),
            modifier = Modifier.clickable { expanded = !expanded }
        )

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = "Enter the Razorpay payment ID from your SMS/email receipt.\nFormat: pay_XXXXXXXXXXXXXX",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(14.dp))

                // Input row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = paymentId,
                        onValueChange = { paymentId = it.trim() },
                        placeholder = {
                            Text(
                                "pay_XXXXXXXXXXXXXX",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextDim
                            )
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = TextWarm),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if (paymentId.isNotBlank()) onRestoreClick(paymentId)
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Amber,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            cursorColor = Amber,
                            focusedContainerColor = Color.White.copy(alpha = 0.04f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(10.dp)
                    )

                    val canRestore = paymentId.isNotBlank() && restoreState !is RestoreState.Loading
                    Box(
                        modifier = Modifier
                            .height(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (canRestore) Amber.copy(alpha = 0.18f)
                                else Color.White.copy(alpha = 0.04f)
                            )
                            .border(
                                1.dp,
                                if (canRestore) Amber.copy(alpha = 0.55f)
                                else Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(enabled = canRestore) { onRestoreClick(paymentId) }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (restoreState is RestoreState.Loading) {
                            CircularProgressIndicator(
                                color = Amber,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                "Restore",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (canRestore) Amber else TextDim
                            )
                        }
                    }
                }

                // Feedback message
                Spacer(Modifier.height(10.dp))
                when (restoreState) {
                    is RestoreState.Success -> Text(
                        text = "✓  Premium restored successfully!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4CAF50),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    is RestoreState.Error -> Text(
                        text = restoreState.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFEF5350),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun FreeFeatureRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .background(Color(0xFF1A3300).copy(alpha = 0.70f), CircleShape)
                .border(1.dp, Color(0xFF4CAF50).copy(alpha = 0.30f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(12.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )
    }
}

