package com.dsb.flashy.notification

import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.dsb.flashy.services.FlashCallService
import android.content.Intent

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val pkg = sbn?.packageName ?: return

        // Skip our own foreground service notification — it would cause a flash loop.
        if (pkg == applicationContext.packageName) return

        // Skip persistent/ongoing notifications (media players, navigation, system status).
        if (sbn.isOngoing) return

        Log.d("FlashNotif", "Notification from $pkg — triggering flash")

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
