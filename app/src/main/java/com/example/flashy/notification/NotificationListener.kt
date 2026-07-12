package com.dsb.flashy.notification

import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.dsb.flashy.services.FlashCallService

class NotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val packageName = sbn?.packageName ?: return

        // Skip our own foreground service notification — it would cause a flash loop.
        if (packageName == applicationContext.packageName) return

        // Skip persistent/ongoing notifications (media players, navigation, system status).
        if (sbn.isOngoing) return

        Log.d("FlashNotif", "Notification from $packageName — triggering flash")

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