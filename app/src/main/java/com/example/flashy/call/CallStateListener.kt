package com.example.flashy.call

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.PowerManager
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import com.example.flashy.managers.FlashController
import com.example.flashy.datastore.GlobalSettingsStore.FLASH_CALL
import com.example.flashy.datastore.GlobalSettingsStore.FLASH_GLOBAL
import com.example.flashy.datastore.GlobalSettingsStore.FLASH_RINGER_MODE
import com.example.flashy.datastore.GlobalSettingsStore.FLASH_SCREEN_OFF_ONLY
import com.example.flashy.datastore.flashDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class CallStateListener(
    private val context: Context,
    private val flashController: FlashController
) {
    private val telephonyManager: TelephonyManager =
        context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    // For Android 12+
    @RequiresApi(Build.VERSION_CODES.S)
    private val telephonyCallback = object : TelephonyCallback(), TelephonyCallback.CallStateListener {
        override fun onCallStateChanged(state: Int) {
            CoroutineScope(Dispatchers.Main).launch {
                handleCallStateChange(state)
            }
        }
    }

    // For older Android versions
    private val phoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            CoroutineScope(Dispatchers.Main).launch {
                handleCallStateChange(state)
            }
        }
    }

    private fun isScreenOn(): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            powerManager.isInteractive
        } else {
            @Suppress("DEPRECATION")
            powerManager.isScreenOn
        }
    }

    private suspend fun isCallFlashAllowed(): Boolean {
        val prefs = context.flashDataStore.data.first()

        val isGlobalEnabled = prefs[FLASH_GLOBAL] ?: true
        val isCallEnabled = prefs[FLASH_CALL] ?: true
        val screenOnly = prefs[FLASH_SCREEN_OFF_ONLY] ?: false
        val ringerModeAllowed = prefs[FLASH_RINGER_MODE] ?: "All"
        val currentRinger = getCurrentRingerMode()

        if (!isGlobalEnabled || !isCallEnabled) return false
        if (screenOnly && isScreenOn()) return false
        if (ringerModeAllowed != "All" && ringerModeAllowed != currentRinger) return false

        return true
    }

    private fun getCurrentRingerMode(): String {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_NORMAL -> "Normal"
            AudioManager.RINGER_MODE_VIBRATE -> "Vibrate"
            AudioManager.RINGER_MODE_SILENT -> "Silent"
            else -> "Unknown"
        }
    }

    private suspend fun handleCallStateChange(state: Int) {
        val prefs = runBlocking { context.flashDataStore.data.first() }
        val isGlobalEnabled = prefs[FLASH_GLOBAL] ?: true
        val isCallEnabled = prefs[FLASH_CALL] ?: true

        if (!isGlobalEnabled || !isCallEnabled) return

        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                if (isCallFlashAllowed()) {
                    flashController.blinkFlashIndefinitely(200L)
                }
            }
            TelephonyManager.CALL_STATE_IDLE, TelephonyManager.CALL_STATE_OFFHOOK -> {
                flashController.stopBlinking()
            }
        }
    }

    fun register() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                telephonyManager.registerTelephonyCallback(context.mainExecutor, telephonyCallback)
            } else {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun unregister() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                telephonyManager.unregisterTelephonyCallback(telephonyCallback)
            } else {
                telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
