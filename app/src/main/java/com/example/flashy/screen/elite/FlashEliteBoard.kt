package com.example.flashy.screen.elite

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.flashy.datastore.GlobalSettingsStore.FLASH_SMS
import com.example.flashy.datastore.flashDataStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*
import com.example.flashy.R

@Composable
fun FlashEliteBoardScreen(context: Context) {
    val scope = rememberCoroutineScope()
    val prefs = context.flashDataStore.data.collectAsState(initial = emptyPreferences())
    val flashGlobal by GlobalSettingsStore.get(context, FLASH_GLOBAL).collectAsState(initial = true)
    val flashCall by GlobalSettingsStore.get(context, FLASH_CALL).collectAsState(initial = true)
    val flashSms by GlobalSettingsStore.get(context, FLASH_SMS).collectAsState(initial = true)
    val flashNotify by GlobalSettingsStore.get(context, FLASH_NOTIFICATIONS).collectAsState(initial = true)
    val flashDndStart by GlobalSettingsStore.getString(context, FLASH_DND_START).collectAsState(initial = "22:00")
    val flashDndEnd by GlobalSettingsStore.getString(context, FLASH_DND_END).collectAsState(initial = "07:00")
    val batteryThreshold = prefs.value[FLASH_BATTERY_THRESHOLD] ?: 15
    val ringerMode = prefs.value[FLASH_RINGER_MODE] ?: "All"

    var batterySlider by remember { mutableFloatStateOf(batteryThreshold / 100f) }
    var selectedMode by remember { mutableStateOf(ringerMode) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(
                colors = listOf(Color(0xFF0D0D1E), Color(0xFF1A1A2E), Color(0xFF16213E))
            ))
    ) {
        UltraBackground()

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { UltraHeader() }
            item { SuperMasterCard(flashGlobal) { scope.launch { GlobalSettingsStore.set(context, FLASH_GLOBAL, it) } } }
            item { AlertMatrix(flashCall, flashSms, flashNotify,
                { scope.launch { GlobalSettingsStore.set(context, FLASH_CALL, it) } },
                { scope.launch { GlobalSettingsStore.set(context, FLASH_SMS, it) } },
                { scope.launch { GlobalSettingsStore.set(context, FLASH_NOTIFICATIONS, it) } }
            ) }
            item { TimeWarpCard(flashDndStart, flashDndEnd,
                { scope.launch { context.flashDataStore.edit { prefs -> prefs[FLASH_DND_START] = it } } },
                { scope.launch { context.flashDataStore.edit { prefs -> prefs[FLASH_DND_END] = it } } }
            ) }
            item { BatteryGenius(batterySlider, { batterySlider = it }) {
                scope.launch { context.flashDataStore.edit { prefs -> prefs[FLASH_BATTERY_THRESHOLD] = (batterySlider * 100).toInt() } }
            } }
            item { ModeSelector(selectedMode) { selectedMode = it; scope.launch { context.flashDataStore.edit { prefs -> prefs[FLASH_RINGER_MODE] = it } } } }
            item { QuantumAccess { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) } }
            if (!flashGlobal) item { StatusBeacon() }
        }
    }
}

@Composable
fun UltraBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val rotation by infiniteTransition.animateFloat(0f, 360f, infiniteRepeatable(tween(30000, easing = LinearEasing)), label = "rot")
    val pulse by infiniteTransition.animateFloat(0.3f, 1f, infiniteRepeatable(tween(4000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "pulse")

    Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)

        // Animated gradient orbs
        repeat(8) { i ->
            val angle = (rotation + i * 45) * PI / 180
            val radius = size.minDimension / 2.5f
            val orbOffset = Offset(
                center.x + cos(angle).toFloat() * radius * pulse,
                center.y + sin(angle).toFloat() * radius * pulse
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF4A90E2).copy(alpha = 0.3f),
                        Color(0xFF9C27B0).copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    radius = 200f
                ),
                radius = 120f,
                center = orbOffset
            )
        }

        // Central energy core
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFD700).copy(alpha = 0.4f),
                    Color(0xFFFF6B35).copy(alpha = 0.2f),
                    Color.Transparent
                ),
                radius = 300f
            ),
            radius = 200f * pulse,
            center = center
        )
    }
}

@Composable
fun UltraHeader() {
    var showPulse by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        while (true) {
            showPulse = !showPulse
            delay(3000)
        }
    }

    val scale by animateFloatAsState(if (showPulse) 1.15f else 1f, tween(1500, easing = FastOutSlowInEasing), label = "scale")
    val glow by animateFloatAsState(if (showPulse) 1f else 0.6f, tween(1500), label = "glow")

    UltraGlass {
        Box(
            modifier = Modifier.fillMaxWidth().padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD700).copy(alpha = glow),
                                    Color(0xFFFF6B35).copy(alpha = glow * 0.8f),
                                    Color(0xFF9C27B0).copy(alpha = glow * 0.6f)
                                )
                            ),
                            CircleShape
                        )
                        .blur(if (showPulse) 8.dp else 0.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_flash_on_24),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "FLASH ELITE",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 4.sp
                )

                Text(
                    "Next-Gen Flash Control",
                    fontSize = 16.sp,
                    color = Color(0xFFFFD700).copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SuperMasterCard(isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "master")
    val shimmer by infiniteTransition.animateFloat(0f, 1f, infiniteRepeatable(tween(2000)), label = "shimmer")

    UltraGlass {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clickable { onToggle(!isEnabled) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        if (isEnabled)
                            Brush.sweepGradient(
                                colors = listOf(
                                    Color(0xFF4CAF50),
                                    Color(0xFF81C784),
                                    Color(0xFF4CAF50)
                                ),
                                center = Offset(0.5f, 0.5f)
                            )
                        else
                            Brush.linearGradient(colors = listOf(Color.Gray, Color.DarkGray)),
                        CircleShape
                    )
                    .then(if (isEnabled) Modifier.rotate(shimmer * 360f) else Modifier),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_power_24),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "MASTER CONTROL",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 2.sp
                )
                Text(
                    if (isEnabled) "System Active" else "System Inactive",
                    fontSize = 14.sp,
                    color = if (isEnabled) Color(0xFF4CAF50) else Color.Gray
                )
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF4CAF50),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.Gray
                )
            )
        }
    }
}

@Composable
fun AlertMatrix(call: Boolean, sms: Boolean, notify: Boolean, onCall: (Boolean) -> Unit, onSms: (Boolean) -> Unit, onNotify: (Boolean) -> Unit) {
    UltraGlass {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "ALERT MATRIX",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                HoloCard(icon = Icons.Default.Phone, label = "CALLS", isEnabled = call, onToggle = onCall, color = Color(0xFF4CAF50), modifier = Modifier.weight(1f))
                HoloCard(pain = true, label = "SMS", isEnabled = sms, onToggle = onSms, color = Color(0xFF9C27B0), modifier = Modifier.weight(1f))
                HoloCard(label = "APPS", isEnabled = notify, onToggle = onNotify, color = Color(0xFFFF5722), modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun HoloCard(
    painter: Painter = painterResource(R.drawable.baseline_sms_24),
    pain: Boolean = false,
    icon: ImageVector = Icons.Default.Notifications,
    label: String,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    color: Color,
    modifier: Modifier
) {
    val animatedColor by animateColorAsState(if (isEnabled) color else Color.Gray, tween(500), label = "holo")
    val scale by animateFloatAsState(if (isEnabled) 1f else 0.9f, tween(300), label = "scale")

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .scale(scale)
            .clickable { onToggle(!isEnabled) },
        colors = CardDefaults.cardColors(containerColor = animatedColor.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if(pain) {
                Icon(painter, contentDescription = null, tint = animatedColor, modifier = Modifier.size(36.dp))
            } else {
                Icon(imageVector = icon, contentDescription = null, tint = animatedColor, modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun TimeWarpCard(start: String, end: String, onStartChange: (String) -> Unit, onEndChange: (String) -> Unit) {
    val context = LocalContext.current

    UltraGlass {
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = painterResource(R.drawable.baseline_schedule_24), contentDescription = null, tint = Color(0xFFE91E63), modifier = Modifier.size(32.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("TIME WARP", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                Text("Sleep Mode Schedule", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
            }

            TimeChip(start, "START") {
                TimePickerDialog(context, { _, h, m -> onStartChange(String.format("%02d:%02d", h, m)) }, 0, 0, true).show()
            }

            TimeChip(end, "END") {
                TimePickerDialog(context, { _, h, m -> onEndChange(String.format("%02d:%02d", h, m)) }, 0, 0, true).show()
            }
        }
    }
}

@Composable
fun TimeChip(time: String, label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE91E63).copy(alpha = 0.3f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.7f))
            Text(time, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun BatteryGenius(threshold: Float, onChange: (Float) -> Unit, onFinished: () -> Unit) {
    UltraGlass {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(R.drawable.baseline_battery_full_24), contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text("BATTERY GENIUS: ${(threshold * 100).toInt()}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Slider(
                value = threshold,
                onValueChange = onChange,
                onValueChangeFinished = onFinished,
                valueRange = 0.05f..0.5f,
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFFF9800),
                    activeTrackColor = Color(0xFFFF9800),
                    inactiveTrackColor = Color(0xFFFF9800).copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun ModeSelector(selected: String, onSelect: (String) -> Unit) {
    val modes = listOf("All", "Normal", "Vibrate", "Silent")

    UltraGlass {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("ADAPTIVE MODES", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                modes.forEach { mode ->
                    ModeChip(mode, selected == mode, { onSelect(mode) }, Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun ModeChip(label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val animatedColor by animateColorAsState(
        if (isSelected) Color(0xFF3F51B5) else Color.White.copy(alpha = 0.1f),
        tween(300), label = "mode"
    )

    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = animatedColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            label,
            modifier = Modifier.padding(8.dp),
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.7f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun QuantumAccess(onClick: () -> Unit) {
    UltraGlass {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EA).copy(alpha = 0.8f)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text("QUANTUM ACCESS", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun StatusBeacon() {
    UltraGlass {
        Row(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text("SYSTEM OFFLINE", color = Color(0xFFFF5252), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun UltraGlass(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f)
                    )
                )
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                ),
                RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        content()
    }
}