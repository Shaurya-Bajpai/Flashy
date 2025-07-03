package com.example.flashy.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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

    fun get(context: Context, key: Preferences.Key<Boolean>): Flow<Boolean> {
        return context.flashDataStore.data.map { prefs ->
            prefs[key] ?: true
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
                else -> ""
            }
        }
    }

    fun getInt(context: Context, key: Preferences.Key<Int>): Flow<Int> {
        return context.flashDataStore.data.map { prefs ->
            prefs[key] ?: 0
        }
    }

    suspend fun edit(context: Context, key: Preferences.Key<String>, value: String) {
        context.flashDataStore.edit { prefs ->
            prefs[key] = value
        }
    }

}
