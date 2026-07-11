package com.dsb.flashy

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.lifecycleScope
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_GLOBAL
import com.dsb.flashy.datastore.flashDataStore
import com.dsb.flashy.util.updateFlashShortcut
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Transparent no-UI activity launched by the app shortcut (long-press the launcher icon).
 * Reads the current FLASH_GLOBAL state, flips it, updates the shortcut label, shows a toast,
 * then finishes — the user never sees a window.
 */
class FlashToggleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            val current  = flashDataStore.data.map { it[FLASH_GLOBAL] ?: true }.first()
            val newValue = !current
            flashDataStore.edit { it[FLASH_GLOBAL] = newValue }
            // Keep shortcut label in sync so next long-press shows the correct action
            updateFlashShortcut(this@FlashToggleActivity, newValue)
            Toast.makeText(
                this@FlashToggleActivity,
                if (newValue) "Flash alerts on" else "Flash alerts off",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }
    }
}
