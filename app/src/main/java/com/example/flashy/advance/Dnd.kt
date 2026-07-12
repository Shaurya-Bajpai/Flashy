package com.dsb.flashy.advance

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalTime

@RequiresApi(Build.VERSION_CODES.O)
fun isWithinDND(current: LocalTime, start: LocalTime, end: LocalTime): Boolean {
    return if (start.isBefore(end)) {
        current.isAfter(start) && current.isBefore(end)
    } else {
        current.isAfter(start) || current.isBefore(end)
    }
}

fun isSystemDndActive(context: Context): Boolean {
    val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    return nm.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL
}