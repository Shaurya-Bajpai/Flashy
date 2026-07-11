package com.dsb.flashy.screen.dashboard.items.card

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.dsb.flashy.R
import com.dsb.flashy.screen.dashboard.items.AnimatedSwitch
import com.dsb.flashy.ui.theme.Amber
import com.dsb.flashy.ui.theme.TextDim
import com.dsb.flashy.ui.theme.TextMuted
import com.dsb.flashy.ui.theme.TextWarm

private val ColorSoundReactive = Color(0xFF7C3AED)  // vivid violet

@Composable
fun SoundReactiveCard(
    enabled: Boolean,
    sensitivity: Int,
    onEnabledChange: (Boolean) -> Unit,
    onSensitivityChange: (Int) -> Unit,
    onSensitivityChangeFinished: () -> Unit,
) {
    val context = LocalContext.current
    var showDisclosure by remember { mutableStateOf(false) }

    // Launches the system permission dialog after the user accepts our disclosure.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onEnabledChange(true)
    }

    // Intercept the toggle: if turning ON without the permission, show disclosure first.
    val handleToggle: (Boolean) -> Unit = { checked ->
        if (!checked) {
            onEnabledChange(false)
        } else {
            val alreadyGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (alreadyGranted) {
                onEnabledChange(true)
            } else {
                showDisclosure = true
            }
        }
    }

    // Prominent disclosure required by Google Play before requesting RECORD_AUDIO.
    if (showDisclosure) {
        AlertDialog(
            onDismissRequest = { showDisclosure = false },
            title = { Text("Microphone Access Required") },
            text = {
                Text(
                    "Sound Reactive Flash uses your microphone to detect sound levels in real time " +
                    "so the flash can sync with music or claps.\n\n" +
                    "Audio is processed entirely on your device — it is never recorded, " +
                    "stored, or transmitted anywhere."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDisclosure = false
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }) {
                    Text("Allow", color = Amber)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisclosure = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    GlassMorphismCard(accentGlow = if (enabled) ColorSoundReactive.copy(alpha = 0.08f) else Color.Transparent) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.baseline_volume_up_24),
                    contentDescription = null,
                    tint = ColorSoundReactive,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Sound Reactive Flash",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWarm
                    )
                    Text(
                        text = "Flash syncs with music beats or ambient sound",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            AnimatedSwitch(
                label = "Beat Sync",
                sublabel = "Uses microphone · may increase battery usage",
                isEnabled = enabled,
                onToggle = handleToggle,
                icon = painterResource(R.drawable.baseline_volume_up_24),
                color = ColorSoundReactive
            )

            AnimatedVisibility(
                visible = enabled,
                enter = expandVertically() + fadeIn(),
                exit  = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(Modifier.height(6.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.07f))
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sensitivity",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextWarm,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "$sensitivity%",
                            style = MaterialTheme.typography.labelLarge,
                            color = ColorSoundReactive
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Slider(
                        value = sensitivity / 100f,
                        onValueChange = { onSensitivityChange((it * 100).toInt().coerceIn(1, 100)) },
                        onValueChangeFinished = onSensitivityChangeFinished,
                        valueRange = 0.01f..1f,
                        colors = SliderDefaults.colors(
                            thumbColor = ColorSoundReactive,
                            activeTrackColor = ColorSoundReactive,
                            inactiveTrackColor = ColorSoundReactive.copy(alpha = 0.18f),
                            activeTickColor = Color.Transparent,
                            inactiveTickColor = Color.Transparent
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "Less sensitive",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextDim,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "More sensitive",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextDim
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "Tip: start at 50% for music, raise if it's not triggering, lower if it triggers too easily",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDim
                    )
                }
            }
        }
    }
}
