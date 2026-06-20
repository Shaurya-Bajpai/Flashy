package com.dsb.flashy.notification

import android.content.Intent
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun NotificationPermissionDialog() {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = {},
        confirmButton = {
            TextButton(onClick = {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                context.startActivity(intent)
            }) {
                Text("Go to Settings")
            }
        },
        title = { Text("Enable Notification Access") },
        text = {
            Text(
                "To receive flash alerts for WhatsApp, Messages, and other apps, you need to enable Notification Access for Flashy.\n\nPlease tap 'Go to Settings' and enable Flashy in the list."
            )
        },
        dismissButton = {}
    )
}
