package com.dsb.flashy.notification

import android.app.Notification
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.dsb.flashy.services.FlashCallService

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val pkg = sbn?.packageName ?: return

        // Skip our own foreground service notification — it would cause a flash loop.
        if (pkg == applicationContext.packageName) return

        // Skip persistent/ongoing notifications (media players, navigation, system status).
        if (sbn.isOngoing) return

        val category = sbn.notification.category

        // Skip incoming call notifications — CallStateListener fires on CALL_STATE_RINGING
        // before any notification appears, giving instant response. Handling it again here
        // would double-trigger the flash.
        if (category == Notification.CATEGORY_CALL || category == Notification.CATEGORY_MISSED_CALL) return

        // Messaging apps (Google Messages, WhatsApp, Signal, etc.) set CATEGORY_MESSAGE.
        // Route these to the "SMS" event bucket so they respect the SMS toggle, not the Apps toggle.
        // All other app notifications go to the "NOTIF" bucket.
        val eventType = if (category == Notification.CATEGORY_MESSAGE) "SMS" else "NOTIF"

        Log.d("FlashNotif", "Notification from $pkg [cat=$category] — firing $eventType")

        val serviceIntent = Intent(this, FlashCallService::class.java).apply {
            putExtra("eventType", eventType)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}
