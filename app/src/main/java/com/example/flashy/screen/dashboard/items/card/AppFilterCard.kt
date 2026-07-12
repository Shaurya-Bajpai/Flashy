package com.dsb.flashy.screen.dashboard.items.card

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dsb.flashy.model.AppFlashRule
import com.dsb.flashy.model.toAppRuleList
import com.dsb.flashy.model.toJsonString
import com.dsb.flashy.ui.theme.Amber
import com.dsb.flashy.ui.theme.ColorApp
import com.dsb.flashy.ui.theme.TextDim
import com.dsb.flashy.ui.theme.TextMuted
import com.dsb.flashy.ui.theme.TextWarm
import com.dsb.flashy.util.InstalledApp
import com.dsb.flashy.util.InstalledAppsProvider

@Composable
fun AppFilterCard(
    context: Context,
    appRulesJson: String,
    onAppRulesChange: (String) -> Unit,
) {
    val rules = remember(appRulesJson) { appRulesJson.toAppRuleList() }
    var showPicker by remember { mutableStateOf(false) }

    GlassMorphismCard(accentColor = ColorApp) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = "App Notifications",
                style = MaterialTheme.typography.titleLarge,
                color = TextWarm
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = "Pick which apps are allowed to trigger a flash",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted
            )
            Spacer(Modifier.height(20.dp))

            if (rules.isEmpty()) {
                Text(
                    text = "No apps selected — add one below to get flash alerts for its notifications",
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
                    appName     = app.name
                )
                onAppRulesChange(newRules.toJsonString())
                showPicker = false
            }
        )
    }
}

// ── Per-app row ──────────────────────────────────────────────────────────────
// An app in this list always flashes on notification — there's no per-app
// mode or contact filtering. Removing it from the list is how you exclude it.

@Composable
private fun AppRuleRow(
    context: Context,
    rule: AppFlashRule,
    onUpdate: (AppFlashRule) -> Unit,
    onRemove: () -> Unit,
) {
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

        // Speed row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "SPEED",
                style = MaterialTheme.typography.labelSmall,
                color = TextDim,
                modifier = Modifier.width(44.dp)
            )
            AppPatternChip("Rapid",  rule.flashSpeedMs == 100) { onUpdate(rule.copy(flashSpeedMs = 100)) }
            AppPatternChip("Normal", rule.flashSpeedMs == 200) { onUpdate(rule.copy(flashSpeedMs = 200)) }
            AppPatternChip("Gentle", rule.flashSpeedMs == 400) { onUpdate(rule.copy(flashSpeedMs = 400)) }
        }

        // Count row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "COUNT",
                style = MaterialTheme.typography.labelSmall,
                color = TextDim,
                modifier = Modifier.width(44.dp)
            )
            AppPatternChip("3×",  rule.flashCount == 3)  { onUpdate(rule.copy(flashCount = 3))  }
            AppPatternChip("5×",  rule.flashCount == 5)  { onUpdate(rule.copy(flashCount = 5))  }
            AppPatternChip("10×", rule.flashCount == 10) { onUpdate(rule.copy(flashCount = 10)) }
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

    val allApps = remember { InstalledAppsProvider.listLaunchableApps(context) }

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
private fun AppPatternChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val bg        by animateColorAsState(if (selected) Amber.copy(alpha = 0.20f) else Color.White.copy(alpha = 0.04f), label = "apc_bg_$text")
    val border    by animateColorAsState(if (selected) Amber.copy(alpha = 0.60f) else Color.White.copy(alpha = 0.10f), label = "apc_border_$text")
    val textColor by animateColorAsState(if (selected) Amber else TextDim, label = "apc_text_$text")
    val shape = RoundedCornerShape(6.dp)
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = textColor,
        modifier = Modifier
            .clip(shape)
            .background(bg, shape)
            .border(1.dp, border, shape)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    )
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
