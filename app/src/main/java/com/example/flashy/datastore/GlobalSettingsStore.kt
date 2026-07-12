package com.dsb.flashy.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.dsb.flashy.model.FlashHistoryEvent
import com.dsb.flashy.model.toFlashHistoryList
import com.dsb.flashy.model.toHistoryJsonString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.flashDataStore by preferencesDataStore(name = "flash_settings")

object GlobalSettingsStore {
    val FLASH_GLOBAL = booleanPreferencesKey("flash_global")
    val FLASH_CALL = booleanPreferencesKey("flash_call")
    val FLASH_SMS = booleanPreferencesKey("flash_sms")
    val FLASH_NOTIFICATIONS = booleanPreferencesKey("flash_notifications")
    val FLASH_DND_START = stringPreferencesKey("flash_dnd_start") // "22:00"
    val FLASH_DND_END = stringPreferencesKey("flash_dnd_end")     // "07:00"
    val FLASH_BATTERY_THRESHOLD = intPreferencesKey("flash_battery_threshold") // default 15
    val FLASH_SCREEN_OFF_ONLY = booleanPreferencesKey("flash_screen_off_only") // true
    val FLASH_RINGER_MODE = stringPreferencesKey("flash_ringer_mode") // "All", "Normal", "Vibrate", "Silent"

    // Flash pattern — speed (ms per half-cycle) and count (0 = continuous) per trigger type
    val FLASH_CALL_COUNT    = intPreferencesKey("flash_call_count")      // 0=∞, 3, 5, 10
    val FLASH_CALL_SPEED_MS = intPreferencesKey("flash_call_speed_ms")   // 100, 200, 400
    val FLASH_SMS_COUNT     = intPreferencesKey("flash_sms_count")        // 3, 5, 10
    val FLASH_SMS_SPEED_MS  = intPreferencesKey("flash_sms_speed_ms")    // 100, 200, 400
    val FLASH_NOTIF_COUNT   = intPreferencesKey("flash_notif_count")     // 3, 5, 10
    val FLASH_NOTIF_SPEED_MS= intPreferencesKey("flash_notif_speed_ms")  // 100, 200, 400

    // Contact filter — "all" flashes for everyone; "selected" only flashes for listed names
    // Contacts stored as comma-separated display names, e.g. "Mom,John Smith,Work"
    // Calls and SMS only — app notifications are filtered by app selection instead
    // (see FLASH_APP_RULES), not by contact name.
    val FLASH_CALL_FILTER_MODE  = stringPreferencesKey("flash_call_filter_mode")   // "all" | "selected"
    val FLASH_CALL_CONTACTS     = stringPreferencesKey("flash_call_contacts")       // "Name1,Name2"
    val FLASH_SMS_FILTER_MODE   = stringPreferencesKey("flash_sms_filter_mode")    // "all" | "selected"
    val FLASH_SMS_CONTACTS      = stringPreferencesKey("flash_sms_contacts")        // "Name1,Name2"

    // Per-app rules — JSON array of AppFlashRule objects.
    // Apps are strictly opt-in: a notification only flashes if its package is in
    // this list. There's no "flash for every app" fallback.
    val FLASH_APP_RULES = stringPreferencesKey("flash_app_rules")

    // Feature 11: respect Android system DND (on by default)
    val FLASH_RESPECT_SYSTEM_DND = booleanPreferencesKey("flash_respect_system_dnd")

    // Feature 12: flash once when battery reaches 100% while charging (off by default)
    val FLASH_CHARGING_COMPLETE = booleanPreferencesKey("flash_charging_complete")

    // Feature 4: sound reactive flash (off by default; requires RECORD_AUDIO)
    val FLASH_SOUND_REACTIVE    = booleanPreferencesKey("flash_sound_reactive")
    val FLASH_SOUND_SENSITIVITY = intPreferencesKey("flash_sound_sensitivity")    // 1–100

    // Feature 7: flash when battery drops to the Battery Guard threshold (off by default)
    val FLASH_LOW_BATTERY_ALERT = booleanPreferencesKey("flash_low_battery_alert")

    // Feature 8: flash event history — JSON array, newest first, capped at 30 entries
    val FLASH_HISTORY = stringPreferencesKey("flash_history")

    // Premium — set to true after a successful Razorpay payment
    val IS_PREMIUM          = booleanPreferencesKey("is_premium")
    val PAYMENT_ID          = stringPreferencesKey("payment_id")        // Razorpay payment ID
    val PAYMENT_TIMESTAMP   = stringPreferencesKey("payment_timestamp") // epoch millis as string

    // Onboarding — false until the user completes the first-launch walkthrough
    val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")

    // In-app review — counts dashboard visits; prompts once on the 5th
    val APP_LAUNCH_COUNT = intPreferencesKey("app_launch_count")
    val REVIEW_PROMPTED  = booleanPreferencesKey("review_prompted")

    fun get(context: Context, key: Preferences.Key<Boolean>): Flow<Boolean> {
        return context.flashDataStore.data.map { prefs ->
            prefs[key] ?: when (key) {
                FLASH_CHARGING_COMPLETE, FLASH_SOUND_REACTIVE, FLASH_LOW_BATTERY_ALERT,
                ONBOARDING_COMPLETE, REVIEW_PROMPTED, IS_PREMIUM -> false   // opt-in / first-launch gates
                else -> true
            }
        }
    }

    suspend fun set(context: Context, key: Preferences.Key<Boolean>, value: Boolean) {
        context.flashDataStore.edit { prefs ->
            prefs[key] = value
        }
    }

    fun getString(context: Context, key: Preferences.Key<String>): Flow<String> {
        return context.flashDataStore.data.map { prefs ->
            prefs[key] ?: when (key) {
                FLASH_DND_START -> "00:00"
                FLASH_DND_END -> "07:00"
                FLASH_CALL_FILTER_MODE, FLASH_SMS_FILTER_MODE -> "all"
                else -> ""
            }
        }
    }

    fun getInt(context: Context, key: Preferences.Key<Int>): Flow<Int> {
        return context.flashDataStore.data.map { prefs ->
            prefs[key] ?: when (key) {
                FLASH_BATTERY_THRESHOLD -> 15
                FLASH_SMS_COUNT, FLASH_NOTIF_COUNT -> 5
                FLASH_CALL_COUNT -> 0   // 0 = continuous (blinks until call ends)
                FLASH_CALL_SPEED_MS, FLASH_SMS_SPEED_MS, FLASH_NOTIF_SPEED_MS -> 200
                FLASH_SOUND_SENSITIVITY -> 50
                else -> 0
            }
        }
    }

    suspend fun edit(context: Context, key: Preferences.Key<String>, value: String) {
        context.flashDataStore.edit { prefs ->
            prefs[key] = value
        }
    }

    suspend fun appendFlashHistory(context: Context, event: FlashHistoryEvent) {
        context.flashDataStore.edit { prefs ->
            val existing = (prefs[FLASH_HISTORY] ?: "").toFlashHistoryList().toMutableList()
            existing.add(0, event)
            prefs[FLASH_HISTORY] = existing.take(30).toHistoryJsonString()
        }
    }

    suspend fun clearFlashHistory(context: Context) {
        context.flashDataStore.edit { prefs ->
            prefs[FLASH_HISTORY] = ""
        }
    }

}
