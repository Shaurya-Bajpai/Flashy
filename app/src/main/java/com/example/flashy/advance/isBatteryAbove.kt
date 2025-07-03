package com.example.flashy.advance

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

fun getBatteryLevel(context: Context): Int {
    val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
    return if (level >= 0 && scale > 0) (level * 100) / scale else 100
}
