package com.dsb.flashy.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.dsb.flashy.R
import com.dsb.flashy.advance.getBatteryLevel
import com.dsb.flashy.advance.isRingerModeAllowed
import com.dsb.flashy.advance.isScreenOn
import com.dsb.flashy.advance.isWithinDND
import com.dsb.flashy.managers.FlashController
import com.dsb.flashy.call.CallStateListener
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_BATTERY_THRESHOLD
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_CALL
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_CALL_COUNT
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_CALL_SPEED_MS
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_DND_END
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_DND_START
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_GLOBAL
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_NOTIFICATIONS
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_NOTIF_COUNT
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_NOTIF_SPEED_MS
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_RINGER_MODE
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_SCREEN_OFF_ONLY
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_SMS
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_SMS_COUNT
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_SMS_SPEED_MS
import com.dsb.flashy.datastore.flashDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import kotlin.text.get

class FlashCallService : Service() {

    private val CHANNEL_ID = "FlashCallServiceChannel"
    private val NOTIFICATION_ID = 1

    private lateinit var flashController: FlashController
    private lateinit var callListener: CallStateListener
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        flashController = FlashController(this)
        callListener = CallStateListener(this, flashController)
        callListener.register()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("FlashService", "Service started with intent: ${intent?.getStringExtra("eventType")}")
        val eventType = intent?.getStringExtra("eventType") ?: "INIT"

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        if (eventType != "INIT" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Launch a coroutine to call the suspend function
            serviceScope.launch {
                handleEvent(this@FlashCallService, eventType)
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        callListener.unregister()
        flashController.release()
        serviceScope.cancel()
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun handleEvent(context: Context, eventType: String) {
        val prefs = context.flashDataStore.data.first()

        val isGlobalEnabled = prefs[FLASH_GLOBAL] ?: true
        val isCallEnabled = prefs[FLASH_CALL] ?: true
        val isSmsEnabled = prefs[FLASH_SMS] ?: true
        val isNotifEnabled = prefs[FLASH_NOTIFICATIONS] ?: true
        val screenOnly = prefs[FLASH_SCREEN_OFF_ONLY] ?: false
        val dndStart = LocalTime.parse(prefs[FLASH_DND_START] ?: "22:00")
        val dndEnd = LocalTime.parse(prefs[FLASH_DND_END] ?: "07:00")
        val currentTime = LocalTime.now()
        val batteryThreshold = prefs[FLASH_BATTERY_THRESHOLD] ?: 15
        val selectedRingerMode = prefs[FLASH_RINGER_MODE] ?: "All"
        val currentRingerMode = isRingerModeAllowed(context, selectedRingerMode)

        if (!currentRingerMode) {
            Log.d("FlashService", "Ringer mode $selectedRingerMode not allowed, skipping flash")
            return
        }

        if (!isGlobalEnabled) return
        if (isWithinDND(currentTime, dndStart, dndEnd)) return
        if (screenOnly && isScreenOn(context)) return

        val currentBattery = getBatteryLevel(context)
        if (currentBattery < batteryThreshold) {
            Log.d("FlashService", "Battery $currentBattery% < $batteryThreshold%, skipping flash")
            return
        }

        val callCount    = prefs[FLASH_CALL_COUNT]     ?: 0    // 0 = continuous
        val callSpeedMs  = (prefs[FLASH_CALL_SPEED_MS]  ?: 200).toLong()
        val smsCount     = prefs[FLASH_SMS_COUNT]       ?: 5
        val smsSpeedMs   = (prefs[FLASH_SMS_SPEED_MS]   ?: 200).toLong()
        val notifCount   = prefs[FLASH_NOTIF_COUNT]     ?: 5
        val notifSpeedMs = (prefs[FLASH_NOTIF_SPEED_MS] ?: 200).toLong()

        when (eventType) {
            "CALL" -> if (isCallEnabled) {
                if (callCount == 0) flashController.blinkFlashIndefinitely(callSpeedMs)
                else flashController.blinkFlash(callSpeedMs, callCount)
            }
            "SMS" -> if (isSmsEnabled) flashController.blinkFlash(smsSpeedMs,   smsCount)
            "NOTIF" -> if (isNotifEnabled) flashController.blinkFlash(notifSpeedMs, notifCount)
        }

        // Auto-stop after the blink sequence finishes.
        // For continuous call flash, CallStateListener stops it when the call ends.
        if (eventType != "CALL" || callCount != 0) {
            val stopAfterMs = when (eventType) {
                "CALL"  -> callCount  * 2 * callSpeedMs  + 500L
                "SMS"   -> smsCount   * 2 * smsSpeedMs   + 500L
                "NOTIF" -> notifCount * 2 * notifSpeedMs + 500L
                else    -> 2000L
            }
            Handler(Looper.getMainLooper()).postDelayed({
                flashController.stopBlinking()
            }, stopAfterMs)
        }
    }



    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notif_channel_desc)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notif_title))
            .setContentText(getString(R.string.notif_text))
            .setSmallIcon(R.drawable.baseline_flash_on_24)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}

