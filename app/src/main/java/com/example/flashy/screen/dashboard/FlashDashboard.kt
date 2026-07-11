package com.dsb.flashy.screen.dashboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.dsb.flashy.datastore.GlobalSettingsStore
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
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_CALL_FILTER_MODE
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_CALL_CONTACTS
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_SMS_FILTER_MODE
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_SMS_CONTACTS
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_NOTIF_FILTER_MODE
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_NOTIF_CONTACTS
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_APP_RULES
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_RESPECT_SYSTEM_DND
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_CHARGING_COMPLETE
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_LOW_BATTERY_ALERT
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_SOUND_REACTIVE
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_SOUND_SENSITIVITY
import com.dsb.flashy.datastore.flashDataStore
import com.dsb.flashy.screen.dashboard.items.AlertGrid
import com.dsb.flashy.screen.dashboard.items.AnimatedBackground
import com.dsb.flashy.screen.dashboard.items.PremiumHeader
import com.dsb.flashy.screen.dashboard.items.StatusAlert
import com.dsb.flashy.screen.dashboard.items.card.AdaptiveRingerCard
import com.dsb.flashy.screen.dashboard.items.card.AppFilterCard
import com.dsb.flashy.screen.dashboard.items.card.ContactFilterCard
import com.dsb.flashy.screen.dashboard.items.card.FlashPatternCard
import com.dsb.flashy.screen.dashboard.items.card.SoundReactiveCard
import com.dsb.flashy.screen.dashboard.items.card.IntelligentBatteryCard
import com.dsb.flashy.screen.dashboard.items.card.MasterControlCard
import com.dsb.flashy.screen.dashboard.items.card.QuickAccessCard
import com.dsb.flashy.screen.dashboard.items.card.SmartScheduleCard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FlashDashboardScreen(context: Context) {
    val scope = rememberCoroutineScope()
    val prefsFlow = context.flashDataStore.data.collectAsState(initial = emptyPreferences())
    val batteryThreshold = prefsFlow.value[FLASH_BATTERY_THRESHOLD] ?: 15
    val ringerMode = prefsFlow.value[FLASH_RINGER_MODE] ?: "All"

    var batterySlider by remember { mutableFloatStateOf(batteryThreshold.toFloat()) }
    var showPulse by remember { mutableStateOf(false) }

    val flashGlobal by GlobalSettingsStore.get(context, FLASH_GLOBAL).collectAsState(initial = true)
    val flashCall by GlobalSettingsStore.get(context, FLASH_CALL).collectAsState(initial = true)
    val flashSms by GlobalSettingsStore.get(context, FLASH_SMS).collectAsState(initial = true)
    val flashNotify by GlobalSettingsStore.get(context, FLASH_NOTIFICATIONS).collectAsState(initial = true)
    val flashDndStart by GlobalSettingsStore.getString(context, FLASH_DND_START).collectAsState(initial = "00:00")
    val flashDndEnd by GlobalSettingsStore.getString(context, FLASH_DND_END).collectAsState(initial = "07:00")
    val flashScreenOffOnly by GlobalSettingsStore.get(context, FLASH_SCREEN_OFF_ONLY).collectAsState(initial = true)

    // Contact filter settings
    val callFilterMode   by GlobalSettingsStore.getString(context, FLASH_CALL_FILTER_MODE).collectAsState(initial = "all")
    val callContacts     by GlobalSettingsStore.getString(context, FLASH_CALL_CONTACTS).collectAsState(initial = "")
    val smsFilterMode    by GlobalSettingsStore.getString(context, FLASH_SMS_FILTER_MODE).collectAsState(initial = "all")
    val smsContacts      by GlobalSettingsStore.getString(context, FLASH_SMS_CONTACTS).collectAsState(initial = "")
    val notifFilterMode  by GlobalSettingsStore.getString(context, FLASH_NOTIF_FILTER_MODE).collectAsState(initial = "all")
    val notifContacts    by GlobalSettingsStore.getString(context, FLASH_NOTIF_CONTACTS).collectAsState(initial = "")
    val appRulesJson        by GlobalSettingsStore.getString(context, FLASH_APP_RULES).collectAsState(initial = "")
    val respectSystemDnd      by GlobalSettingsStore.get(context, FLASH_RESPECT_SYSTEM_DND).collectAsState(initial = true)
    val chargingCompleteFlash by GlobalSettingsStore.get(context, FLASH_CHARGING_COMPLETE).collectAsState(initial = false)
    val lowBatteryAlert       by GlobalSettingsStore.get(context, FLASH_LOW_BATTERY_ALERT).collectAsState(initial = false)
    val soundReactive         by GlobalSettingsStore.get(context, FLASH_SOUND_REACTIVE).collectAsState(initial = false)
    val soundSensitivity      by GlobalSettingsStore.getInt(context, FLASH_SOUND_SENSITIVITY).collectAsState(initial = 50)
    var soundSensitivitySlider by remember { mutableStateOf(50) }

    // Flash pattern settings
    val callCount    by GlobalSettingsStore.getInt(context, FLASH_CALL_COUNT).collectAsState(initial = 0)
    val callSpeedMs  by GlobalSettingsStore.getInt(context, FLASH_CALL_SPEED_MS).collectAsState(initial = 200)
    val smsCount     by GlobalSettingsStore.getInt(context, FLASH_SMS_COUNT).collectAsState(initial = 5)
    val smsSpeedMs   by GlobalSettingsStore.getInt(context, FLASH_SMS_SPEED_MS).collectAsState(initial = 200)
    val notifCount   by GlobalSettingsStore.getInt(context, FLASH_NOTIF_COUNT).collectAsState(initial = 5)
    val notifSpeedMs by GlobalSettingsStore.getInt(context, FLASH_NOTIF_SPEED_MS).collectAsState(initial = 200)

    // Request READ_PHONE_STATE only when the user explicitly enables the Calls feature.
    // This way the system permission dialog appears in context — the user understands why
    // the app needs it because they just tapped "Flash on Calls".
    val callPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch { GlobalSettingsStore.set(context, FLASH_CALL, true) }
        }
    }

    val soundReactiveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch { GlobalSettingsStore.set(context, FLASH_SOUND_REACTIVE, true) }
        }
    }

    // Keep local slider in sync with DataStore (e.g. first load)
    LaunchedEffect(soundSensitivity) { soundSensitivitySlider = soundSensitivity }

    LaunchedEffect(Unit) {
        while (true) {
            showPulse = !showPulse
            delay(2000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF140A00),
                        Color(0xFF0A0600),
                        Color(0xFF060401)
                    ),
                    radius = 1600f
                )
            )
    ) {
        AnimatedBackground()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { PremiumHeader(showPulse, flashGlobal) }

            if (!flashGlobal) {
                item { StatusAlert() }
            }

            item {
                MasterControlCard(
                    flashGlobal = flashGlobal,
                    flashScreenOffOnly = flashScreenOffOnly,
                    onFlashGlobalChange = { scope.launch { GlobalSettingsStore.set(context, FLASH_GLOBAL, it) } },
                    onFlashScreenOffOnlyChange = { scope.launch { GlobalSettingsStore.set(context, FLASH_SCREEN_OFF_ONLY, it) } }
                )
            }

            item {
                AlertGrid(
                    flashCall = flashCall,
                    flashSms = flashSms,
                    flashNotify = flashNotify,
                    onFlashCallChange = { enabled ->
                        if (!enabled) {
                            scope.launch { GlobalSettingsStore.set(context, FLASH_CALL, false) }
                        } else if (ContextCompat.checkSelfPermission(
                                context, Manifest.permission.READ_PHONE_STATE
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            scope.launch { GlobalSettingsStore.set(context, FLASH_CALL, true) }
                        } else {
                            callPermLauncher.launch(Manifest.permission.READ_PHONE_STATE)
                        }
                    },
                    onFlashSmsChange = { scope.launch { GlobalSettingsStore.set(context, FLASH_SMS, it) } },
                    onFlashNotifyChange = { scope.launch { GlobalSettingsStore.set(context, FLASH_NOTIFICATIONS, it) } }
                )
            }

            item {
                SmartScheduleCard(
                    startTime = flashDndStart,
                    endTime = flashDndEnd,
                    respectSystemDnd = respectSystemDnd,
                    onStartTimeChange = { newTime ->
                        scope.launch { context.flashDataStore.edit { it[FLASH_DND_START] = newTime } }
                    },
                    onEndTimeChange = { newTime ->
                        scope.launch { context.flashDataStore.edit { it[FLASH_DND_END] = newTime } }
                    },
                    onRespectSystemDndChange = { v ->
                        scope.launch { GlobalSettingsStore.set(context, FLASH_RESPECT_SYSTEM_DND, v) }
                    }
                )
            }

            item {
                IntelligentBatteryCard(
                    threshold = batterySlider / 100f,
                    chargingCompleteFlash = chargingCompleteFlash,
                    lowBatteryAlert = lowBatteryAlert,
                    onThresholdChange = { batterySlider = it * 100f },
                    onThresholdChangeFinished = {
                        scope.launch {
                            context.flashDataStore.edit { it[FLASH_BATTERY_THRESHOLD] = batterySlider.toInt() }
                        }
                    },
                    onChargingCompleteFlashChange = { v ->
                        scope.launch { GlobalSettingsStore.set(context, FLASH_CHARGING_COMPLETE, v) }
                    },
                    onLowBatteryAlertChange = { v ->
                        scope.launch { GlobalSettingsStore.set(context, FLASH_LOW_BATTERY_ALERT, v) }
                    }
                )
            }

            item {
                AdaptiveRingerCard(
                    selectedMode = ringerMode,
                    onModeChange = { mode ->
                        scope.launch { context.flashDataStore.edit { it[FLASH_RINGER_MODE] = mode } }
                    }
                )
            }

            item {
                SoundReactiveCard(
                    enabled = soundReactive,
                    sensitivity = soundSensitivitySlider,
                    onEnabledChange = { enabled ->
                        if (!enabled) {
                            scope.launch { GlobalSettingsStore.set(context, FLASH_SOUND_REACTIVE, false) }
                        } else if (ContextCompat.checkSelfPermission(
                                context, Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            scope.launch { GlobalSettingsStore.set(context, FLASH_SOUND_REACTIVE, true) }
                        } else {
                            soundReactiveLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onSensitivityChange = { soundSensitivitySlider = it },
                    onSensitivityChangeFinished = {
                        scope.launch {
                            context.flashDataStore.edit { it[FLASH_SOUND_SENSITIVITY] = soundSensitivitySlider }
                        }
                    }
                )
            }

            item {
                FlashPatternCard(
                    callCount    = callCount,
                    callSpeedMs  = callSpeedMs,
                    smsCount     = smsCount,
                    smsSpeedMs   = smsSpeedMs,
                    notifCount   = notifCount,
                    notifSpeedMs = notifSpeedMs,
                    onCallCountChange    = { v -> scope.launch { context.flashDataStore.edit { it[FLASH_CALL_COUNT]     = v } } },
                    onCallSpeedChange    = { v -> scope.launch { context.flashDataStore.edit { it[FLASH_CALL_SPEED_MS]  = v } } },
                    onSmsCountChange     = { v -> scope.launch { context.flashDataStore.edit { it[FLASH_SMS_COUNT]      = v } } },
                    onSmsSpeedChange     = { v -> scope.launch { context.flashDataStore.edit { it[FLASH_SMS_SPEED_MS]   = v } } },
                    onNotifCountChange   = { v -> scope.launch { context.flashDataStore.edit { it[FLASH_NOTIF_COUNT]    = v } } },
                    onNotifSpeedChange   = { v -> scope.launch { context.flashDataStore.edit { it[FLASH_NOTIF_SPEED_MS] = v } } },
                )
            }

            item {
                ContactFilterCard(
                    callFilterMode   = callFilterMode,
                    callContacts     = callContacts,
                    smsFilterMode    = smsFilterMode,
                    smsContacts      = smsContacts,
                    notifFilterMode  = notifFilterMode,
                    notifContacts    = notifContacts,
                    onCallFilterModeChange  = { v -> scope.launch { GlobalSettingsStore.edit(context, FLASH_CALL_FILTER_MODE,  v) } },
                    onCallContactsChange    = { v -> scope.launch { GlobalSettingsStore.edit(context, FLASH_CALL_CONTACTS,     v) } },
                    onSmsFilterModeChange   = { v -> scope.launch { GlobalSettingsStore.edit(context, FLASH_SMS_FILTER_MODE,   v) } },
                    onSmsContactsChange     = { v -> scope.launch { GlobalSettingsStore.edit(context, FLASH_SMS_CONTACTS,      v) } },
                    onNotifFilterModeChange = { v -> scope.launch { GlobalSettingsStore.edit(context, FLASH_NOTIF_FILTER_MODE, v) } },
                    onNotifContactsChange   = { v -> scope.launch { GlobalSettingsStore.edit(context, FLASH_NOTIF_CONTACTS,    v) } },
                )
            }

            item {
                AppFilterCard(
                    context = context,
                    appRulesJson = appRulesJson,
                    onAppRulesChange = { v -> scope.launch { GlobalSettingsStore.edit(context, FLASH_APP_RULES, v) } }
                )
            }

            item {
                QuickAccessCard {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }
            }
        }
    }
}
