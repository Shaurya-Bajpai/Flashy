package com.example.flashy.notification

import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.example.flashy.services.FlashCallService

class NotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val packageName = sbn?.packageName ?: return

        // Filter: only WhatsApp or Messages etc (optional)
        if (packageName.contains("whatsapp") || packageName.contains("messaging")) {
            Log.d("FlashNotif", "Notification from $packageName")

            val serviceIntent = Intent(this, FlashCallService::class.java).apply {
                putExtra("eventType", "NOTIF")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }
    }
}