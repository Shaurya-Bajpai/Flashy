package com.dsb.flashy.notification

import android.content.Intent
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun NotificationPermissionDialog(onExit: () -> Unit = {}) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                context.startActivity(intent)
            }) {
                Text("Open Settings")
            }
        },
        title = { Text("Notification Access Required") },
        text = {
            Text(
                "Flashora needs Notification Access to flash your camera light when you receive " +
                        "messages from any app.\n\nTap \"Open Settings\", find Flashora in the list, " +
                        "and enable the toggle. Then return to the app."
            )
        },
        dismissButton = {
            TextButton(onClick = onExit) {
                Text("Exit")
            }
        }
    )
}
