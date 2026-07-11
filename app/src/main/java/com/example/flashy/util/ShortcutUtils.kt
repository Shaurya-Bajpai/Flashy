package com.dsb.flashy.util

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.dsb.flashy.FlashToggleActivity
import com.dsb.flashy.R

const val SHORTCUT_FLASH_TOGGLE = "flash_toggle"

/**
 * Registers or updates the "toggle flash" dynamic shortcut shown on long-press of the app icon.
 * Dynamic shortcut (not static XML) so it works correctly across all product flavors whose
 * applicationId differs — packageName is resolved at runtime, not hardcoded.
 *
 * Label reflects current state so the user knows what the tap will DO:
 *   flash currently ON  → shortcut says "Turn flash off"
 *   flash currently OFF → shortcut says "Turn flash on"
 */
fun updateFlashShortcut(context: Context, flashCurrentlyEnabled: Boolean) {
    val shortLabel = if (flashCurrentlyEnabled) "Flash off" else "Flash on"
    val longLabel  = if (flashCurrentlyEnabled) "Turn flash alerts off" else "Turn flash alerts on"
    val iconRes    = if (flashCurrentlyEnabled) R.drawable.ic_shortcut_flash_off
                     else R.drawable.ic_shortcut_flash_on

    val intent = Intent(context, FlashToggleActivity::class.java).apply {
        // Shortcuts require an explicit action — ACTION_VIEW is the conventional choice
        // for activities that don't process data but just perform an action and finish.
        action = Intent.ACTION_VIEW
    }

    val shortcut = ShortcutInfoCompat.Builder(context, SHORTCUT_FLASH_TOGGLE)
        .setShortLabel(shortLabel)
        .setLongLabel(longLabel)
        .setIcon(IconCompat.createWithResource(context, iconRes))
        .setIntent(intent)
        .build()

    try {
        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
    } catch (_: Exception) {
        // pushDynamicShortcut can throw if the system shortcut limit is exceeded.
        // Non-fatal — the shortcut may simply not appear.
    }
}
