package com.dsb.flashy.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.dsb.flashy.R
import com.dsb.flashy.advance.getBatteryLevel
import com.dsb.flashy.model.FlashHistoryEvent
import com.dsb.flashy.advance.isRingerModeAllowed
import com.dsb.flashy.advance.isScreenOn
import com.dsb.flashy.advance.isSystemDndActive
import com.dsb.flashy.advance.isWithinDND
import com.dsb.flashy.managers.FlashController
import com.dsb.flashy.call.CallStateListener
import com.dsb.flashy.datastore.GlobalSettingsStore
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_BATTERY_THRESHOLD
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_CALL
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_CALL_COUNT
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_CALL_SPEED_MS
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_CHARGING_COMPLETE
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_LOW_BATTERY_ALERT
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_DND_END
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_DND_START
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_GLOBAL
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_NOTIFICATIONS
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_NOTIF_COUNT
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_NOTIF_SPEED_MS
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_RESPECT_SYSTEM_DND
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_RINGER_MODE
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_SOUND_REACTIVE
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_SOUND_SENSITIVITY
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_SCREEN_OFF_ONLY
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_SMS
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_SMS_COUNT
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_SMS_SPEED_MS
import com.dsb.flashy.datastore.flashDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalTime
import kotlin.text.get

class FlashCallService : Service() {

    private val CHANNEL_ID = "FlashCallServiceChannel"
    private val NOTIFICATION_ID = 1

    private lateinit var flashController: FlashController
    private lateinit var callListener: CallStateListener
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Tracks the previous battery percentage to detect single-fire transitions.
    private var prevBatteryPct = -1

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level  = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale  = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            if (level < 0 || scale <= 0) return

            val pct         = level * 100 / scale
            val isCharging  = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                              status == BatteryManager.BATTERY_STATUS_FULL
            val isDischarging = !isCharging

            // ── Charging complete: 99 → 100 while on charger ─────────────────
            if (pct == 100 && isCharging && prevBatteryPct in 0..99) {
                serviceScope.launch {
                    val prefs = context.flashDataStore.data.first()
                    if ((prefs[FLASH_GLOBAL] ?: true) && (prefs[FLASH_CHARGING_COMPLETE] ?: false)) {
                        Log.d("FlashService", "Battery full — charging-complete flash")
                        flashController.blinkFlash(200L, 5)
                    }
                }
            }

            // ── Low battery alert: first crossing of the guard threshold downward ──
            // Only fires once per discharge cycle (prevBatteryPct was above threshold).
            if (prevBatteryPct > 0 && isDischarging) {
                serviceScope.launch {
                    val prefs     = context.flashDataStore.data.first()
                    val threshold = prefs[FLASH_BATTERY_THRESHOLD] ?: 15
                    if ((prefs[FLASH_GLOBAL] ?: true) &&
                        (prefs[FLASH_LOW_BATTERY_ALERT] ?: false) &&
                        pct <= threshold && prevBatteryPct > threshold
                    ) {
                        Log.d("FlashService", "Battery hit $pct% threshold — low-battery alert flash")
                        // 3 slow blinks at 500 ms — distinct from normal notification flashes
                        flashController.blinkFlash(500L, 3)
                    }
                }
            }

            prevBatteryPct = pct
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        flashController = FlashController(this)
        callListener = CallStateListener(this, flashController)
        callListener.register()

        registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        // Reactively start/stop sound reactive flash whenever the setting changes.
        serviceScope.launch {
            flashDataStore.data
                .map { prefs ->
                    (prefs[FLASH_SOUND_REACTIVE] ?: false) to (prefs[FLASH_SOUND_SENSITIVITY] ?: 50)
                }
                .distinctUntilChanged()
                .collect { (enabled, sensitivity) ->
                    if (enabled) flashController.startSoundReactive(sensitivity)
                    else flashController.stopSoundReactive()
                }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("FlashService", "Service started with intent: ${intent?.getStringExtra("eventType")}")
        val eventType = intent?.getStringExtra("eventType") ?: "INIT"

        val notification = createNotification()
        // On targetSdk 36 the 2-arg startForeground() activates ALL types declared in the
        // manifest, including microphone — which crashes if RECORD_AUDIO isn't granted yet.
        // Use the 3-arg form and only add the microphone type when the permission is held.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            var fgType = ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
            ) {
                @Suppress("InlinedApi")
                fgType = fgType or ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            }
            startForeground(NOTIFICATION_ID, notification, fgType)
        } else {
            @Suppress("DEPRECATION")
            startForeground(NOTIFICATION_ID, notification)
        }

        val countOverride  = intent?.getIntExtra("flashCountOverride", -1) ?: -1
        val speedOverride  = intent?.getIntExtra("flashSpeedOverride", -1) ?: -1
        val senderName     = intent?.getStringExtra("senderName") ?: ""
        val appPackage     = intent?.getStringExtra("appPackage") ?: ""
        val appName        = intent?.getStringExtra("appName") ?: ""

        if (eventType != "INIT" && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            serviceScope.launch {
                handleEvent(this@FlashCallService, eventType, countOverride, speedOverride,
                    senderName, appPackage, appName)
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        unregisterReceiver(batteryReceiver)
        callListener.unregister()
        flashController.release()
        serviceScope.cancel()
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun handleEvent(
        context: Context,
        eventType: String,
        countOverride: Int = -1,
        speedOverride: Int = -1,
        senderName: String = "",
        appPackage: String = "",
        appName: String = ""
    ) {
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

        val respectSystemDnd = prefs[FLASH_RESPECT_SYSTEM_DND] ?: true
        if (respectSystemDnd && isSystemDndActive(context)) {
            Log.d("FlashService", "System DND active — skipping flash")
            return
        }

        val currentBattery = getBatteryLevel(context)
        if (currentBattery < batteryThreshold) {
            Log.d("FlashService", "Battery $currentBattery% < $batteryThreshold%, skipping flash")
            return
        }

        val callCount    = prefs[FLASH_CALL_COUNT]     ?: 0    // 0 = continuous
        val callSpeedMs  = (prefs[FLASH_CALL_SPEED_MS]  ?: 200).toLong()
        val smsCount     = prefs[FLASH_SMS_COUNT]       ?: 5
        val smsSpeedMs   = (prefs[FLASH_SMS_SPEED_MS]   ?: 200).toLong()
        // Per-app overrides take precedence over global NOTIF settings when present.
        val notifCount   = if (countOverride >= 0) countOverride else (prefs[FLASH_NOTIF_COUNT]   ?: 5)
        val notifSpeedMs = if (speedOverride >= 0) speedOverride.toLong() else (prefs[FLASH_NOTIF_SPEED_MS] ?: 200).toLong()

        var flashFired = false
        when (eventType) {
            "CALL"  -> if (isCallEnabled) {
                if (callCount == 0) flashController.blinkFlashIndefinitely(callSpeedMs)
                else flashController.blinkFlash(callSpeedMs, callCount)
                flashFired = true
            }
            "SMS"   -> if (isSmsEnabled)  { flashController.blinkFlash(smsSpeedMs,   smsCount);   flashFired = true }
            "NOTIF" -> if (isNotifEnabled){ flashController.blinkFlash(notifSpeedMs, notifCount); flashFired = true }
        }

        if (flashFired) {
            GlobalSettingsStore.appendFlashHistory(
                context,
                FlashHistoryEvent(
                    eventType   = eventType,
                    senderName  = senderName,
                    appPackage  = appPackage,
                    appName     = appName,
                    timestampMs = System.currentTimeMillis()
                )
            )
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
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
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

