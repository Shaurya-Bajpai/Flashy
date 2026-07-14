package com.dsb.flashy.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dsb.flashy.R
import com.dsb.flashy.datastore.GlobalSettingsStore
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_CALL
import com.dsb.flashy.datastore.GlobalSettingsStore.ONBOARDING_COMPLETE
import com.dsb.flashy.ui.theme.Amber
import com.dsb.flashy.ui.theme.AmberDeep
import com.dsb.flashy.ui.theme.ColorCall
import com.dsb.flashy.ui.theme.TextDim
import com.dsb.flashy.ui.theme.TextMuted
import com.dsb.flashy.ui.theme.TextWarm
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val STEP_WELCOME = 0
private const val STEP_NOTIF   = 1
private const val STEP_CALLS   = 2
private const val STEP_BATTERY = 3
private const val STEP_DONE    = 4

@Composable
fun OnboardingScreen(context: Context, onComplete: () -> Unit) {
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf(STEP_WELCOME) }

    // ── Notification listener access ──────────────────────────────────────
    fun isNotifGranted() = NotificationManagerCompat
        .getEnabledListenerPackages(context).contains(context.packageName)

    var notifGranted by remember { mutableStateOf(isNotifGranted()) }

    // Poll while on the notification step; auto-advance when granted
    LaunchedEffect(step) {
        if (step == STEP_NOTIF) {
            while (!notifGranted) {
                delay(500)
                notifGranted = isNotifGranted()
            }
            step = STEP_CALLS
        }
    }

    // ── Phone call permission ─────────────────────────────────────────────
    var callGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                    == PackageManager.PERMISSION_GRANTED
        )
    }

    val callPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        callGranted = granted
        if (granted) scope.launch { GlobalSettingsStore.set(context, FLASH_CALL, true) }
        step = STEP_BATTERY
    }

    // ── Battery optimization ──────────────────────────────────────────────
    val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as PowerManager }
    var batteryExempt by remember {
        mutableStateOf(powerManager.isIgnoringBatteryOptimizations(context.packageName))
    }

    // Re-check both after returning from any settings screen
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                batteryExempt = powerManager.isIgnoringBatteryOptimizations(context.packageName)
                notifGranted  = isNotifGranted()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    fun finish() {
        scope.launch { GlobalSettingsStore.set(context, ONBOARDING_COMPLETE, true) }
        onComplete()
    }

    // ── Layout ────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF140A00),
                        Color(0xFF0A0600),
                        Color(0xFF060401)
                    ),
                    radius = 1800f
                )
            )
    ) {
        AnimatedContent(
            targetState = step,
            transitionSpec = {
                val dir = if (targetState > initialState)
                    AnimatedContentTransitionScope.SlideDirection.Start
                else
                    AnimatedContentTransitionScope.SlideDirection.End
                (slideIntoContainer(dir, spring(stiffness = Spring.StiffnessMediumLow)) +
                        fadeIn(tween(200))) togetherWith
                        (slideOutOfContainer(dir, spring(stiffness = Spring.StiffnessMediumLow)) +
                                fadeOut(tween(160)))
            },
            label = "onboarding_step"
        ) { currentStep ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (currentStep) {
                    STEP_WELCOME -> WelcomeStep(onNext = { step = STEP_NOTIF })
                    STEP_NOTIF   -> NotifStep(
                        notifGranted   = notifGranted,
                        onOpenSettings = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
                    )
                    STEP_CALLS   -> CallsStep(
                        callGranted = callGranted,
                        onAllow     = { callPermLauncher.launch(Manifest.permission.READ_PHONE_STATE) },
                        onSkip      = { step = STEP_BATTERY }
                    )
                    STEP_BATTERY -> BatteryStep(
                        batteryExempt = batteryExempt,
                        onFix = {
                            context.startActivity(
                                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                                    .setData(Uri.parse("package:${context.packageName}"))
                            )
                        },
                        onContinue = { step = STEP_DONE }
                    )
                    STEP_DONE    -> DoneStep(onStart = { finish() })
                }
            }
        }

        // Step progress dots (visible only on steps 1–3)
        if (step in STEP_NOTIF..STEP_BATTERY) {
            StepDots(
                total   = 3,
                current = step - 1,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 52.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step progress dots
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepDots(total: Int, current: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { i ->
            val active = i == current
            val width by animateDpAsState(
                targetValue = if (active) 24.dp else 8.dp,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
                label = "dot_$i"
            )
            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (active) Amber else Amber.copy(alpha = 0.22f))
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared composables
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun StepIconBubble(iconRes: Int, accentColor: Color) {
    val infinite = rememberInfiniteTransition(label = "icon_glow")
    val glowAlpha by infinite.animateFloat(
        initialValue = 0.12f, targetValue = 0.30f,
        animationSpec = infiniteRepeatable(
            tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "glow"
    )
    Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(accentColor.copy(alpha = glowAlpha), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(70.dp)
                .background(accentColor.copy(alpha = 0.10f), CircleShape)
                .border(1.5.dp, accentColor.copy(alpha = 0.32f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Amber, AmberDeep),
                    start = Offset(0f, 0f),
                    end = Offset(900f, 0f)
                )
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF1A0D00),
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SkipButton(text: String = "Skip for now", onClick: () -> Unit) {
    TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = TextDim)
    }
}

@Composable
private fun InfoBox(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = TextDim, lineHeight = 18.sp)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 0 — Welcome
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "welcome_pulse")
    val pulse by infinite.animateFloat(
        initialValue = 0.90f, targetValue = 1.10f,
        animationSpec = infiniteRepeatable(
            tween(1300, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .scale(pulse)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Amber.copy(alpha = 0.28f), Color.Transparent)
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .background(Amber.copy(alpha = 0.12f), CircleShape)
                    .border(2.dp, Amber.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_flash_on_24),
                    contentDescription = null,
                    tint = Amber,
                    modifier = Modifier.size(46.dp)
                )
            }
        }

        Spacer(Modifier.height(36.dp))

        Text(
            text = "Welcome to Flashy",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextWarm,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(14.dp))

        Text(
            text = "Your camera flash becomes your personal alert system. Never miss a call, message, or notification — even on silent mode.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(Modifier.height(52.dp))

        PrimaryButton(text = "Let's Set Up", onClick = onNext)

        Spacer(Modifier.height(14.dp))

        Text(
            text = "Takes about 30 seconds",
            style = MaterialTheme.typography.bodySmall,
            color = TextDim,
            textAlign = TextAlign.Center
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 1 — Notification Access
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun NotifStep(notifGranted: Boolean, onOpenSettings: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        StepIconBubble(R.drawable.baseline_notifications_active_24, Amber)

        Spacer(Modifier.height(28.dp))

        Text(
            text = "Allow Notification Access",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextWarm,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Flashy needs to read your notifications to detect messages from WhatsApp, Instagram, Gmail, and any app you choose.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(Modifier.height(16.dp))

        InfoBox("🔒  Flashy only reads notification titles to trigger your flash. It never stores, uploads, or shares your messages.")

        Spacer(Modifier.height(32.dp))

        if (notifGranted) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(ColorCall.copy(alpha = 0.14f), CircleShape)
                        .border(1.dp, ColorCall.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_flash_on_24),
                        contentDescription = null,
                        tint = ColorCall,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Access granted — continuing...",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = ColorCall
                )
            }
        } else {
            PrimaryButton(text = "Grant Notification Access", onClick = onOpenSettings)
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Find Flashy in the list and turn it on",
                style = MaterialTheme.typography.bodySmall,
                color = TextDim,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 2 — Call Alerts
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CallsStep(callGranted: Boolean, onAllow: () -> Unit, onSkip: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        StepIconBubble(R.drawable.baseline_phone_android_24, ColorCall)

        Spacer(Modifier.height(28.dp))

        Text(
            text = "Flash on Incoming Calls",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextWarm,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Allow Flashy to detect incoming phone calls so your flash blinks whenever someone calls you.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(Modifier.height(16.dp))

        InfoBox("🔒  Only detects that the phone is ringing. Flashy cannot read your call log, contacts, or phone numbers.")

        Spacer(Modifier.height(32.dp))

        if (callGranted) {
            PrimaryButton(text = "Continue", onClick = onSkip)
        } else {
            PrimaryButton(text = "Allow Call Detection", onClick = onAllow)
            Spacer(Modifier.height(6.dp))
            SkipButton(onClick = onSkip)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 3 — Battery Optimization
// ─────────────────────────────────────────────────────────────────────────────

private val BatteryOrange = Color(0xFFFF8C00)

@Composable
private fun BatteryStep(batteryExempt: Boolean, onFix: () -> Unit, onContinue: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        StepIconBubble(R.drawable.baseline_battery_alert_24, BatteryOrange)

        Spacer(Modifier.height(28.dp))

        Text(
            text = "Keep Flash Running",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextWarm,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "On most phones — Xiaomi, Oppo, Realme, Samsung — the OS kills apps when the screen turns off. One tap prevents this permanently.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(Modifier.height(16.dp))

        // Live status box
        val statusColor = if (batteryExempt) ColorCall else BatteryOrange
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(statusColor.copy(alpha = 0.06f))
                .border(1.dp, statusColor.copy(alpha = 0.22f), RoundedCornerShape(10.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(
                        if (batteryExempt) R.drawable.baseline_flash_on_24
                        else R.drawable.baseline_battery_alert_24
                    ),
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (batteryExempt)
                        "Battery optimization is off — flash will run reliably."
                    else
                        "Battery optimization is active — flash may stop when screen is off.",
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        if (batteryExempt) {
            PrimaryButton(text = "Continue", onClick = onContinue)
        } else {
            PrimaryButton(text = "Disable Battery Optimization", onClick = onFix)
            Spacer(Modifier.height(6.dp))
            SkipButton(text = "Skip — I'll fix it later", onClick = onContinue)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Step 4 — Done
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DoneStep(onStart: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "done_pulse")
    val pulse by infinite.animateFloat(
        initialValue = 0.94f, targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(pulse)
                .background(
                    Brush.radialGradient(
                        colors = listOf(ColorCall.copy(alpha = 0.22f), Color.Transparent)
                    ),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .background(ColorCall.copy(alpha = 0.10f), CircleShape)
                    .border(2.dp, ColorCall.copy(alpha = 0.38f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_flash_on_24),
                    contentDescription = null,
                    tint = ColorCall,
                    modifier = Modifier.size(42.dp)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text = "You're All Set!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextWarm,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(14.dp))

        Text(
            text = "Flash alerts are ready. Head to the dashboard to customize exactly how and when your flash triggers.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(Modifier.height(52.dp))

        PrimaryButton(text = "Start Using Flashy", onClick = onStart)
    }
}
