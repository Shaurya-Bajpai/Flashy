package com.example.flashy.screen.dashboard

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.example.flashy.datastore.GlobalSettingsStore
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
import com.example.flashy.screen.dashboard.items.AlertGrid
import com.example.flashy.screen.dashboard.items.AnimatedBackground
import com.example.flashy.screen.dashboard.items.PremiumHeader
import com.example.flashy.screen.dashboard.items.StatusAlert
import com.example.flashy.screen.dashboard.items.card.AdaptiveRingerCard
import com.example.flashy.screen.dashboard.items.card.IntelligentBatteryCard
import com.example.flashy.screen.dashboard.items.card.MasterControlCard
import com.example.flashy.screen.dashboard.items.card.QuickAccessCard
import com.example.flashy.screen.dashboard.items.card.SmartScheduleCard
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

    LaunchedEffect(Unit) {
        while (true) {
            showPulse = !showPulse
            delay(2000)
        }
    }

    Box(modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    ),
                    radius = 1200f
                )
            )) {
        AnimatedBackground()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { PremiumHeader(showPulse, flashGlobal) }

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
                    onFlashCallChange = { scope.launch { GlobalSettingsStore.set(context, FLASH_CALL, it) } },
                    onFlashSmsChange = { scope.launch { GlobalSettingsStore.set(context, FLASH_SMS, it) } },
                    onFlashNotifyChange = { scope.launch { GlobalSettingsStore.set(context, FLASH_NOTIFICATIONS, it) } }
                )
            }

            item {
                SmartScheduleCard(
                    startTime = flashDndStart,
                    endTime = flashDndEnd,
                    onStartTimeChange = { scope.launch { context.flashDataStore.edit { it[FLASH_DND_START] = flashDndStart } } },
                    onEndTimeChange = { scope.launch { context.flashDataStore.edit { it[FLASH_DND_END] = flashDndEnd } } }
                )
            }

            item {
                IntelligentBatteryCard(
                    threshold = batterySlider / 100f,
                    onThresholdChange = { batterySlider = it * 100f },
                    onThresholdChangeFinished = {
                        scope.launch { context.flashDataStore.edit { it[FLASH_BATTERY_THRESHOLD] = batterySlider.toInt() } }
                    }
                )
            }

            item {
                AdaptiveRingerCard(
                    selectedMode = ringerMode,
                    onModeChange = { mode ->
                        scope.launch {
                            context.flashDataStore.edit {
                                it[FLASH_RINGER_MODE] = mode
                            }
                        }
                    }
                )
            }

            item {
                QuickAccessCard {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }
            }

            if (!flashGlobal) {
                item { StatusAlert() }
            }
        }
    }
}