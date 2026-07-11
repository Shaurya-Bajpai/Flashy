package com.dsb.flashy.advance

import android.app.NotificationManager
import android.content.Context

fun isSystemDndActive(context: Context): Boolean {
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return nm.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
}
