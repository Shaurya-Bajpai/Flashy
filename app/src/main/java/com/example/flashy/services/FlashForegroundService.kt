package com.example.flashy.services

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
import com.example.flashy.R
import com.example.flashy.advance.getBatteryLevel
import com.example.flashy.advance.isRingerModeAllowed
import com.example.flashy.advance.isScreenOn
import com.example.flashy.advance.isWithinDND
import com.example.flashy.managers.FlashController
import com.example.flashy.call.CallStateListener
import com.example.flashy.datastore.GlobalSettingsStore.FLASH_BATTERY_THRESHOLD
import com.example.flashy.datastore.GlobalSettingsStore.FLASH_CALL
import com.example.flashy.datastore.GlobalSettingsStore.FLASH_DND_END
import com.example.flashy.datastore.GlobalSettingsStore.FLASH_DND_START
import com.example.flashy.datastore.GlobalSettingsStore.FLASH_GLOBAL
import com.example.flashy.datastore.GlobalSettingsStore.FLASH_NOTIFICATIONS
import com.example.flashy.datastore.GlobalSettingsStore.FLASH_RINGER_MODE
import com.example.flashy.datastore.GlobalSettingsStore.FLASH_SCREEN_OFF_ONLY
import com.example.flashy.datastore.GlobalSettingsStore.FLASH_SMS
import com.example.flashy.datastore.flashDataStore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import kotlin.text.get

class FlashCallService : Service() {

    private val CHANNEL_ID = "FlashCallServiceChannel"
    private val NOTIFICATION_ID = 1

    private lateinit var flashController: FlashController
    private lateinit var callListener: CallStateListener

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
            GlobalScope.launch {
                handleEvent(this@FlashCallService, eventType)
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        callListener.unregister()
        flashController.stopBlinking()
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
        when (eventType) {
            "CALL" -> if (isCallEnabled) flashController.blinkFlash(200)
            "SMS" -> if (isSmsEnabled) flashController.blinkFlash(200)
            "NOTIF" -> if (isNotifEnabled) flashController.blinkFlash(200)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            flashController.stopBlinking()
        }, 2000)
    }



    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Flash Call Alert Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Flash Alert Active")
            .setContentText("Managing incoming call, SMS, and app notifications")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}

