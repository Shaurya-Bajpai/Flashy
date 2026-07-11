package com.dsb.flashy.screen.dashboard

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_HISTORY
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_SOUND_REACTIVE
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_SOUND_SENSITIVITY
import com.dsb.flashy.datastore.flashDataStore
import com.dsb.flashy.model.toFlashHistoryList
import com.dsb.flashy.screen.dashboard.items.AlertGrid
import com.dsb.flashy.screen.dashboard.items.AnimatedBackground
import com.dsb.flashy.screen.dashboard.items.BatteryOptimizationBanner
import com.dsb.flashy.screen.dashboard.items.PremiumHeader
import com.dsb.flashy.screen.dashboard.items.StatusAlert
import com.dsb.flashy.screen.dashboard.items.card.AdaptiveRingerCard
import com.dsb.flashy.screen.dashboard.items.card.FlashHistoryCard
import com.dsb.flashy.screen.dashboard.items.card.AppFilterCard
import com.dsb.flashy.screen.dashboard.items.card.ContactFilterCard
import com.dsb.flashy.screen.dashboard.items.card.FlashPatternCard
import com.dsb.flashy.screen.dashboard.items.card.SoundReactiveCard
import com.dsb.flashy.screen.dashboard.items.card.IntelligentBatteryCard
import com.dsb.flashy.screen.dashboard.items.card.MasterControlCard
import com.dsb.flashy.screen.dashboard.items.card.QuickAccessCard
import com.dsb.flashy.screen.dashboard.items.card.SmartScheduleCard
import com.dsb.flashy.ui.theme.TextDim
import com.dsb.flashy.util.updateFlashShortcut
import com.dsb.flashy.widget.FlashToggleWidget
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Replace with your hosted privacy policy URL before submitting to Play Store ──
private const val PRIVACY_POLICY_URL = "https://yoursite.com/privacy-policy"

@Composable
fun FlashDashboardScreen(context: Context) {
    val scope = rememberCoroutineScope()
    // Single DataStore subscription — all 28 values come from one atomic snapshot.
    // Replaces 26 individual collectAsState() calls: 1 recomposition per change, not 26.
    val prefs by context.flashDataStore.data.collectAsState(initial = emptyPreferences())

    val flashGlobal           = prefs[FLASH_GLOBAL]              ?: true
    val flashCall             = prefs[FLASH_CALL]                ?: true
    val flashSms              = prefs[FLASH_SMS]                 ?: true
    val flashNotify           = prefs[FLASH_NOTIFICATIONS]       ?: true
    val flashDndStart         = prefs[FLASH_DND_START]           ?: "00:00"
    val flashDndEnd           = prefs[FLASH_DND_END]             ?: "07:00"
    val flashScreenOffOnly    = prefs[FLASH_SCREEN_OFF_ONLY]     ?: true
    val callFilterMode        = prefs[FLASH_CALL_FILTER_MODE]    ?: "all"
    val callContacts          = prefs[FLASH_CALL_CONTACTS]       ?: ""
    val smsFilterMode         = prefs[FLASH_SMS_FILTER_MODE]     ?: "all"
    val smsContacts           = prefs[FLASH_SMS_CONTACTS]        ?: ""
    val notifFilterMode       = prefs[FLASH_NOTIF_FILTER_MODE]   ?: "all"
    val notifContacts         = prefs[FLASH_NOTIF_CONTACTS]      ?: ""
    val appRulesJson          = prefs[FLASH_APP_RULES]           ?: ""
    val respectSystemDnd      = prefs[FLASH_RESPECT_SYSTEM_DND]  ?: true
    val chargingCompleteFlash = prefs[FLASH_CHARGING_COMPLETE]   ?: false
    val lowBatteryAlert       = prefs[FLASH_LOW_BATTERY_ALERT]   ?: false
    val flashHistoryJson      = prefs[FLASH_HISTORY]             ?: ""
    val soundReactive         = prefs[FLASH_SOUND_REACTIVE]      ?: false
    val soundSensitivity      = prefs[FLASH_SOUND_SENSITIVITY]   ?: 50
    val callCount             = prefs[FLASH_CALL_COUNT]          ?: 0
    val callSpeedMs           = prefs[FLASH_CALL_SPEED_MS]       ?: 200
    val smsCount              = prefs[FLASH_SMS_COUNT]           ?: 5
    val smsSpeedMs            = prefs[FLASH_SMS_SPEED_MS]        ?: 200
    val notifCount            = prefs[FLASH_NOTIF_COUNT]         ?: 5
    val notifSpeedMs          = prefs[FLASH_NOTIF_SPEED_MS]      ?: 200
    val batteryThreshold      = prefs[FLASH_BATTERY_THRESHOLD]   ?: 15
    val ringerMode            = prefs[FLASH_RINGER_MODE]         ?: "All"

    var batterySlider by remember { mutableFloatStateOf(batteryThreshold.toFloat()) }
    var showPulse by remember { mutableStateOf(true) }

    // Battery optimization state — re-checked every time the user returns to the app
    val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as PowerManager }
    var batteryOptimizationActive by remember {
        mutableStateOf(!powerManager.isIgnoringBatteryOptimizations(context.packageName))
    }
    var batteryBannerDismissed by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                batteryOptimizationActive =
                    !powerManager.isIgnoringBatteryOptimizations(context.packageName)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var soundSensitivitySlider by remember { mutableStateOf(50) }

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

    // Keep local sliders in sync with DataStore (e.g. first load after initial emptyPreferences())
    LaunchedEffect(soundSensitivity) { soundSensitivitySlider = soundSensitivity }
    LaunchedEffect(batteryThreshold) { batterySlider = batteryThreshold.toFloat() }

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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item(key = "header") { PremiumHeader(showPulse, flashGlobal) }

            if (!flashGlobal) {
                item(key = "status_alert") { StatusAlert() }
            }

            if (batteryOptimizationActive && !batteryBannerDismissed) {
                item(key = "battery_banner") {
                    BatteryOptimizationBanner(
                        onFixClick = {
                            context.startActivity(
                                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                                    .setData(Uri.parse("package:${context.packageName}"))
                            )
                        },
                        onDismiss = { batteryBannerDismissed = true }
                    )
                }
            }

            // ── MASTER CONTROL ────────────────────────────────────────────────
            item(key = "sec_master") { SectionLabel("MASTER CONTROL") }
            item(key = "master_card") {
                MasterControlCard(
                    flashGlobal = flashGlobal,
                    flashScreenOffOnly = flashScreenOffOnly,
                    onFlashGlobalChange = { v ->
                        scope.launch {
                            GlobalSettingsStore.set(context, FLASH_GLOBAL, v)
                            updateFlashShortcut(context, v)
                            FlashToggleWidget.refreshAll(context, v)
                        }
                    },
                    onFlashScreenOffOnlyChange = { scope.launch { GlobalSettingsStore.set(context, FLASH_SCREEN_OFF_ONLY, it) } }
                )
            }

            // ── ALERT TRIGGERS ────────────────────────────────────────────────
            item(key = "sec_alerts") { SectionLabel("ALERT TRIGGERS") }
            item(key = "alert_grid") {
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

            // ── SMART BEHAVIOR ────────────────────────────────────────────────
            item(key = "sec_behavior") { SectionLabel("SMART BEHAVIOR") }
            item(key = "schedule_card") {
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
            item(key = "ringer_card") {
                AdaptiveRingerCard(
                    selectedMode = ringerMode,
                    onModeChange = { mode ->
                        scope.launch { context.flashDataStore.edit { it[FLASH_RINGER_MODE] = mode } }
                    }
                )
            }

            // ── BATTERY ───────────────────────────────────────────────────────
            item(key = "sec_battery") { SectionLabel("BATTERY") }
            item(key = "battery_card") {
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

            // ── ADVANCED ──────────────────────────────────────────────────────
            item(key = "sec_advanced") { SectionLabel("ADVANCED") }
            item(key = "sound_card") {
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
            item(key = "pattern_card") {
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

            // ── FILTERING ─────────────────────────────────────────────────────
            item(key = "sec_filtering") { SectionLabel("FILTERING") }
            item(key = "contact_card") {
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
            item(key = "app_filter_card") {
                AppFilterCard(
                    context = context,
                    appRulesJson = appRulesJson,
                    onAppRulesChange = { v -> scope.launch { GlobalSettingsStore.edit(context, FLASH_APP_RULES, v) } }
                )
            }

            // ── HISTORY & SETTINGS ────────────────────────────────────────────
            item(key = "sec_history") { SectionLabel("HISTORY & SETTINGS") }
            item(key = "history_card") {
                FlashHistoryCard(
                    events = flashHistoryJson.toFlashHistoryList(),
                    onClear = { scope.launch { GlobalSettingsStore.clearFlashHistory(context) } }
                )
            }
            item(key = "quick_access_card") {
                QuickAccessCard {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }
            }

            item(key = "privacy_footer") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Privacy Policy",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextDim,
                        modifier = Modifier.clickable {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL))
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
        ),
        color = TextDim,
        modifier = Modifier.padding(start = 2.dp, top = 6.dp, bottom = 0.dp)
    )
}
