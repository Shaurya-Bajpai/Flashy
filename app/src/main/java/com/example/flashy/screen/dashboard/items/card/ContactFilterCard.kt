package com.dsb.flashy.screen.dashboard.items.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
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

@Composable
fun ContactFilterCard(
    callFilterMode: String,
    callContacts: String,
    smsFilterMode: String,
    smsContacts: String,
    notifFilterMode: String,
    notifContacts: String,
    onCallFilterModeChange: (String) -> Unit,
    onCallContactsChange: (String) -> Unit,
    onSmsFilterModeChange: (String) -> Unit,
    onSmsContactsChange: (String) -> Unit,
    onNotifFilterModeChange: (String) -> Unit,
    onNotifContactsChange: (String) -> Unit,
) {
    GlassMorphismCard(accentColor = ColorSms) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = "Contact Filter",
                style = MaterialTheme.typography.titleLarge,
                color = TextWarm
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "Flash only when specific people reach you",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
            Spacer(Modifier.height(20.dp))

            ContactSection(
                iconVector = Icons.Default.Phone,
                label = "Calls",
                accentColor = ColorCall,
                filterMode = callFilterMode,
                contacts = callContacts,
                onFilterModeChange = onCallFilterModeChange,
                onContactsChange = onCallContactsChange
            )

            FilterDivider()

            ContactSection(
                iconRes = R.drawable.baseline_sms_24,
                label = "SMS",
                accentColor = ColorSms,
                filterMode = smsFilterMode,
                contacts = smsContacts,
                onFilterModeChange = onSmsFilterModeChange,
                onContactsChange = onSmsContactsChange
            )

            FilterDivider()

            ContactSection(
                iconRes = R.drawable.baseline_notifications_active_24,
                label = "Apps",
                accentColor = ColorApp,
                filterMode = notifFilterMode,
                contacts = notifContacts,
                onFilterModeChange = onNotifFilterModeChange,
                onContactsChange = onNotifContactsChange
            )
        }
    }
}

// ── Per-type section ────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ContactSection(
    iconVector: ImageVector? = null,
    iconRes: Int? = null,
    label: String,
    accentColor: Color,
    filterMode: String,
    contacts: String,
    onFilterModeChange: (String) -> Unit,
    onContactsChange: (String) -> Unit,
) {
    val keyboard = LocalSoftwareKeyboardController.current
    var inputText by remember { mutableStateOf("") }
    val isSelected = filterMode == "selected"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Header row: icon + label + mode chips ──────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                letterSpacing = 0.5.sp,
                modifier = Modifier.weight(1f)
            )

            // "All" / "Selected" toggle chips
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ModeChip(
                    text = "All",
                    selected = !isSelected,
                    accentColor = accentColor,
                    onClick = { onFilterModeChange("all") }
                )
                ModeChip(
                    text = "Selected",
                    selected = isSelected,
                    accentColor = accentColor,
                    onClick = { onFilterModeChange("selected") }
                )
            }
        }

        // ── Expandable contact input — only visible in "selected" mode ─────
        AnimatedVisibility(
            visible = isSelected,
            enter = expandVertically(
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
            ) + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                // Input row: text field + Add button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                "Contact name…",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextDim
                            )
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = TextWarm),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            addContact(inputText, contacts, onContactsChange) { inputText = "" }
                            keyboard?.hide()
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            cursorColor = accentColor,
                            focusedContainerColor = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Add button
                    Box(
                        modifier = Modifier
                            .height(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (inputText.isNotBlank()) accentColor.copy(alpha = 0.20f)
                                else Color.White.copy(alpha = 0.04f)
                            )
                            .border(
                                1.dp,
                                if (inputText.isNotBlank()) accentColor.copy(alpha = 0.55f)
                                else Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(enabled = inputText.isNotBlank()) {
                                addContact(inputText, contacts, onContactsChange) { inputText = "" }
                                keyboard?.hide()
                            }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Add",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (inputText.isNotBlank()) accentColor else TextDim
                        )
                    }
                }

                // Contact chips or empty-state hint
                val contactList = contacts.toContactList()
                if (contactList.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        contactList.forEach { name ->
                            ContactChip(
                                name = name,
                                accentColor = accentColor,
                                onRemove = {
                                    val updated = contactList
                                        .filter { it != name }
                                        .toContactsString()
                                    onContactsChange(updated)
                                }
                            )
                        }
                    }
                } else {
                    Text(
                        text = "No contacts added — flash is paused for $label",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDim
                    )
                }
            }
        }
    }
}

// ── Small composables ───────────────────────────────────────────────────────

@Composable
private fun ModeChip(
    text: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(
        targetValue = if (selected) accentColor.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.04f),
        label = "mode_chip_bg_$text"
    )
    val border by animateColorAsState(
        targetValue = if (selected) accentColor.copy(alpha = 0.60f) else Color.White.copy(alpha = 0.10f),
        label = "mode_chip_border_$text"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) accentColor else TextDim,
        label = "mode_chip_text_$text"
    )
    val shape = RoundedCornerShape(8.dp)
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = textColor,
        modifier = Modifier
            .clip(shape)
            .background(bg, shape)
            .border(1.dp, border, shape)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp)
    )
}

@Composable
private fun ContactChip(
    name: String,
    accentColor: Color,
    onRemove: () -> Unit,
) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(accentColor.copy(alpha = 0.14f), shape)
            .border(1.dp, accentColor.copy(alpha = 0.30f), shape)
            .padding(start = 10.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = accentColor,
            maxLines = 1
        )
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f))
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove $name",
                tint = accentColor.copy(alpha = 0.80f),
                modifier = Modifier.size(10.dp)
            )
        }
    }
}

@Composable
private fun FilterDivider() {
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 0.5.dp,
        color = Color.White.copy(alpha = 0.07f)
    )
}

// ── Pure helpers ────────────────────────────────────────────────────────────

private fun String.toContactList(): List<String> =
    split(",").map { it.trim() }.filter { it.isNotEmpty() }

private fun List<String>.toContactsString(): String =
    joinToString(",")

private fun addContact(
    input: String,
    existing: String,
    onUpdate: (String) -> Unit,
    clearInput: () -> Unit,
) {
    val trimmed = input.trim()
    if (trimmed.isBlank()) return
    val list = existing.toContactList().toMutableList()
    if (!list.any { it.equals(trimmed, ignoreCase = true) }) {
        list.add(trimmed)
        onUpdate(list.toContactsString())
    }
    clearInput()
}
