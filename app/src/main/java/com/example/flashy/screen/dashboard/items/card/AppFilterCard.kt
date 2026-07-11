package com.dsb.flashy.screen.dashboard.items.card

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dsb.flashy.model.AppFlashRule
import com.dsb.flashy.model.toAppRuleList
import com.dsb.flashy.model.toJsonString
import com.dsb.flashy.ui.theme.Amber
import com.dsb.flashy.ui.theme.TextDim
import com.dsb.flashy.ui.theme.TextMuted
import com.dsb.flashy.ui.theme.TextWarm

private data class InstalledApp(val packageName: String, val name: String)

@Composable
fun AppFilterCard(
    context: Context,
    appRulesJson: String,
    onAppRulesChange: (String) -> Unit,
) {
    val rules = remember(appRulesJson) { appRulesJson.toAppRuleList() }
    var showPicker by remember { mutableStateOf(false) }

    GlassMorphismCard {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = "Per-App Flash Rules",
                style = MaterialTheme.typography.titleLarge,
                color = TextWarm
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "Flash only for specific people in each app",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
            Spacer(Modifier.height(20.dp))

            if (rules.isEmpty()) {
                Text(
                    text = "No app rules yet — uses the global Apps filter above",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDim,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                rules.forEachIndexed { index, rule ->
                    if (index > 0) {
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = Color.White.copy(alpha = 0.07f)
                        )
                    }
                    AppRuleRow(
                        context = context,
                        rule = rule,
                        onUpdate = { updated ->
                            val newRules = rules.toMutableList().also { it[index] = updated }
                            onAppRulesChange(newRules.toJsonString())
                        },
                        onRemove = {
                            val newRules = rules.toMutableList().also { it.removeAt(index) }
                            onAppRulesChange(newRules.toJsonString())
                        }
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Amber.copy(alpha = 0.10f))
                    .border(1.dp, Amber.copy(alpha = 0.30f), RoundedCornerShape(10.dp))
                    .clickable { showPicker = true }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Amber,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Add App",
                        style = MaterialTheme.typography.labelMedium,
                        color = Amber
                    )
                }
            }
        }
    }

    if (showPicker) {
        AppPickerDialog(
            context = context,
            alreadyAdded = rules.map { it.packageName }.toSet(),
            onDismiss = { showPicker = false },
            onAppSelected = { app ->
                val newRules = rules + AppFlashRule(
                    packageName = app.packageName,
                    appName     = app.name,
                    filterMode  = "all",
                    contacts    = ""
                )
                onAppRulesChange(newRules.toJsonString())
                showPicker = false
            }
        )
    }
}

// ── Per-app rule row ─────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppRuleRow(
    context: Context,
    rule: AppFlashRule,
    onUpdate: (AppFlashRule) -> Unit,
    onRemove: () -> Unit,
) {
    val keyboard    = LocalSoftwareKeyboardController.current
    var inputText   by remember { mutableStateOf("") }
    val isSelected  = rule.filterMode == "selected"
    val contactList = remember(rule.contacts) {
        rule.contacts.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIconView(context = context, packageName = rule.packageName, sizeDp = 30)
            Spacer(Modifier.width(10.dp))
            Text(
                text = rule.appName,
                style = MaterialTheme.typography.labelLarge,
                color = TextWarm,
                letterSpacing = 0.5.sp,
                modifier = Modifier.weight(1f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AppModeChip(text = "All",      selected = !isSelected) { onUpdate(rule.copy(filterMode = "all")) }
                AppModeChip(text = "Selected", selected =  isSelected) { onUpdate(rule.copy(filterMode = "selected")) }
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .clickable { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove ${rule.appName}",
                    tint = TextDim,
                    modifier = Modifier.size(11.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = isSelected,
            enter = expandVertically(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
            exit  = shrinkVertically() + fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text("Contact name…", style = MaterialTheme.typography.bodySmall, color = TextDim)
                        },
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall.copy(color = TextWarm),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            appendContact(inputText, rule, contactList, onUpdate) { inputText = "" }
                            keyboard?.hide()
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Amber,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                            cursorColor          = Amber,
                            focusedContainerColor   = Color.White.copy(alpha = 0.05f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                        ),
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .height(52.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (inputText.isNotBlank()) Amber.copy(alpha = 0.20f)
                                else Color.White.copy(alpha = 0.04f)
                            )
                            .border(
                                1.dp,
                                if (inputText.isNotBlank()) Amber.copy(alpha = 0.55f)
                                else Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(enabled = inputText.isNotBlank()) {
                                appendContact(inputText, rule, contactList, onUpdate) { inputText = "" }
                                keyboard?.hide()
                            }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Add",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (inputText.isNotBlank()) Amber else TextDim
                        )
                    }
                }

                if (contactList.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement   = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        contactList.forEach { name ->
                            AppContactChip(name = name, onRemove = {
                                val updated = contactList.filter { it != name }.joinToString(",")
                                onUpdate(rule.copy(contacts = updated))
                            })
                        }
                    }
                } else {
                    Text(
                        text = "No contacts added — flash is paused for ${rule.appName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDim
                    )
                }
            }
        }
    }
}

// ── App picker dialog ────────────────────────────────────────────────────────

@Composable
private fun AppPickerDialog(
    context: Context,
    alreadyAdded: Set<String>,
    onDismiss: () -> Unit,
    onAppSelected: (InstalledApp) -> Unit,
) {
    var search by remember { mutableStateOf("") }

    val allApps = remember {
        val pm = context.packageManager
        pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
            .filter { it.packageName != context.packageName }
            .map { InstalledApp(it.packageName, pm.getApplicationLabel(it).toString()) }
            .sortedBy { it.name.lowercase() }
    }

    val filtered = remember(search) {
        if (search.isBlank()) allApps
        else allApps.filter { it.name.contains(search, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1A1000),
            tonalElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Select App",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextWarm
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = {
                        Text("Search apps…", style = MaterialTheme.typography.bodySmall, color = TextDim)
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = TextWarm),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Amber,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        cursorColor          = Amber,
                        focusedContainerColor   = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(Modifier.height(12.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(filtered, key = { it.packageName }) { app ->
                        val added = app.packageName in alreadyAdded
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (added) Color.White.copy(alpha = 0.03f)
                                    else Color.Transparent
                                )
                                .clickable(enabled = !added) { onAppSelected(app) }
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AppIconView(context = context, packageName = app.packageName, sizeDp = 32)
                            Text(
                                text = app.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (added) TextDim else TextWarm,
                                modifier = Modifier.weight(1f)
                            )
                            if (added) {
                                Text(
                                    text = "Added",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Amber.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Small composables ────────────────────────────────────────────────────────

@Composable
private fun AppIconView(context: Context, packageName: String, sizeDp: Int) {
    val bitmap = remember(packageName) {
        try { context.packageManager.getApplicationIcon(packageName).toBitmap() }
        catch (_: Exception) { null }
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(RoundedCornerShape(8.dp))
        )
    } else {
        Box(
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.10f))
        )
    }
}

@Composable
private fun AppModeChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val bg       by animateColorAsState(if (selected) Amber.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.04f), label = "amc_bg_$text")
    val border   by animateColorAsState(if (selected) Amber.copy(alpha = 0.60f) else Color.White.copy(alpha = 0.10f), label = "amc_border_$text")
    val textColor by animateColorAsState(if (selected) Amber else TextDim, label = "amc_text_$text")
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
private fun AppContactChip(name: String, onRemove: () -> Unit) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        modifier = Modifier
            .clip(shape)
            .background(Amber.copy(alpha = 0.14f), shape)
            .border(1.dp, Amber.copy(alpha = 0.30f), shape)
            .padding(start = 10.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(text = name, style = MaterialTheme.typography.labelSmall, color = Amber, maxLines = 1)
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(Amber.copy(alpha = 0.15f))
                .clickable { onRemove() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove $name",
                tint = Amber.copy(alpha = 0.80f),
                modifier = Modifier.size(10.dp)
            )
        }
    }
}

// ── Pure helpers ─────────────────────────────────────────────────────────────

private fun Drawable.toBitmap(): Bitmap {
    if (this is BitmapDrawable && bitmap != null) return bitmap
    val bmp = Bitmap.createBitmap(
        intrinsicWidth.coerceAtLeast(1),
        intrinsicHeight.coerceAtLeast(1),
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bmp)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bmp
}

private fun appendContact(
    input: String,
    rule: AppFlashRule,
    contactList: List<String>,
    onUpdate: (AppFlashRule) -> Unit,
    clearInput: () -> Unit,
) {
    val trimmed = input.trim()
    if (trimmed.isBlank()) return
    if (!contactList.any { it.equals(trimmed, ignoreCase = true) }) {
        onUpdate(rule.copy(contacts = (contactList + trimmed).joinToString(",")))
    }
    clearInput()
}
