package com.example.flashy.screen.dash

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
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
import com.example.flashy.screen.dash.items.GlobalDisabledWarning
import com.example.flashy.screen.dash.items.HeaderSection
import com.example.flashy.screen.dash.items.card.AlertTypesCard
import com.example.flashy.screen.dash.items.card.BatteryThresholdCard
import com.example.flashy.screen.dash.items.card.BottomActionsCard
import com.example.flashy.screen.dash.items.card.DoNotDisturbCard
import com.example.flashy.screen.dash.items.card.MainSettingsCard
import com.example.flashy.screen.dash.items.card.EnhancedRingerModeCard
import kotlinx.coroutines.launch
import kotlin.math.sin

@Composable
fun FlashDashScreen(context: Context) {
    val scope = rememberCoroutineScope()

    // Collect DataStore preferences
    val prefsFlow = context.flashDataStore.data.collectAsState(initial = emptyPreferences())
    val batteryThreshold = prefsFlow.value[FLASH_BATTERY_THRESHOLD] ?: 15
    val ringerMode = prefsFlow.value[FLASH_RINGER_MODE] ?: "All"

    // State variables
    var batterySlider by remember { mutableFloatStateOf(batteryThreshold.toFloat()) }

    // Collect settings from GlobalSettingsStore
    val flashGlobal by GlobalSettingsStore.get(context, FLASH_GLOBAL).collectAsState(initial = true)
    val flashCall by GlobalSettingsStore.get(context, FLASH_CALL).collectAsState(initial = true)
    val flashSms by GlobalSettingsStore.get(context, FLASH_SMS).collectAsState(initial = true)
    val flashNotify by GlobalSettingsStore.get(context, FLASH_NOTIFICATIONS).collectAsState(initial = true)
    val flashDndStart by GlobalSettingsStore.getString(context, FLASH_DND_START).collectAsState(initial = "00:00")
    val flashDndEnd by GlobalSettingsStore.getString(context, FLASH_DND_END).collectAsState(initial = "07:00")
    val flashScreenOffOnly by GlobalSettingsStore.get(context, FLASH_SCREEN_OFF_ONLY).collectAsState(initial = true)

    // Enhanced animated background
    val infiniteTransition = rememberInfiniteTransition(label = "background_animation")
    val backgroundOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "background_offset"
    )

    val dynamicGradient = Brush.radialGradient(
        colors = listOf(
            Color(0xFF667eea).copy(alpha = 0.9f),
            Color(0xFF764ba2).copy(alpha = 0.8f),
            Color(0xFF667eea).copy(alpha = 0.7f),
            Color(0xFF2D1B69).copy(alpha = 0.9f)
        ),
        center = Offset(
            x = 0.5f + 0.3f * sin(Math.toRadians(backgroundOffset.toDouble())).toFloat(),
            y = 0.5f + 0.2f * sin(Math.toRadians(backgroundOffset * 0.7).toDouble()).toFloat()
        ),
        radius = 1200f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(dynamicGradient)
    ) {
        // Floating particles background
        FloatingParticles()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                HeaderSection(flashGlobal)
            }

            item {
                MainSettingsCard(
                    flashScreenOffOnly = flashScreenOffOnly,
                    onFlashScreenOffOnlyChange = {
                        scope.launch { GlobalSettingsStore.set(context, FLASH_SCREEN_OFF_ONLY, it) }
                    },
                    flashGlobal = flashGlobal,
                    onFlashGlobalChange = {
                        scope.launch { GlobalSettingsStore.set(context, FLASH_GLOBAL, it) }
                    }
                )
            }

            item {
                AlertTypesCard(
                    flashCall = flashCall,
                    onFlashCallChange = {
                        scope.launch { GlobalSettingsStore.set(context, FLASH_CALL, it) }
                    },
                    flashSms = flashSms,
                    onFlashSmsChange = {
                        scope.launch { GlobalSettingsStore.set(context, FLASH_SMS, it) }
                    },
                    flashNotify = flashNotify,
                    onFlashNotifyChange = {
                        scope.launch { GlobalSettingsStore.set(context, FLASH_NOTIFICATIONS, it) }
                    }
                )
            }

            item {
                DoNotDisturbCard(
                    startTime = flashDndStart,
                    endTime = flashDndEnd,
                    onStartTimeChange = { time ->
                        scope.launch {
                            context.flashDataStore.edit { it[FLASH_DND_START] = time }
                        }
                    },
                    onEndTimeChange = { time ->
                        scope.launch {
                            context.flashDataStore.edit { it[FLASH_DND_END] = time }
                        }
                    }
                )
            }

            item {
                BatteryThresholdCard(
                    threshold = batterySlider / 100f,
                    onThresholdChange = {
                        batterySlider = it * 100f
                    },
                    onThresholdChangeFinished = {
                        scope.launch {
                            context.flashDataStore.edit {
                                it[FLASH_BATTERY_THRESHOLD] = batterySlider.toInt()
                            }
                        }
                    }
                )
            }

            item {
                EnhancedRingerModeCard(
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
                BottomActionsCard(
                    onNotificationSettingsClick = {
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    }
                )
            }

            if (!flashGlobal) {
                item {
                    GlobalDisabledWarning()
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun FloatingParticles() {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")

    repeat(15) { index ->
        val offsetX by infiniteTransition.animateFloat(
            initialValue = (-100).dp.value,
            targetValue = 400.dp.value,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (8000..15000).random(),
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "particle_x_$index"
        )

        val offsetY by infiniteTransition.animateFloat(
            initialValue = (50 * index).dp.value,
            targetValue = (50 * index + 100).dp.value,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (3000..6000).random(),
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "particle_y_$index"
        )

        Box(
            modifier = Modifier
                .offset(offsetX.dp, offsetY.dp)
                .size((4..12).random().dp)
                .background(Color.White.copy(alpha = 0.3f), CircleShape)
                .blur(2.dp, BlurredEdgeTreatment.Unbounded)
        )
    }
}