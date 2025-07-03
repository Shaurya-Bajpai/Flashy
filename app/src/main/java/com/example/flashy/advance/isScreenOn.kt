package com.example.flashy.advance

import android.content.Context
import android.os.Build
import android.os.PowerManager

fun isScreenOn(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
        powerManager.isInteractive
    } else {
        powerManager.isScreenOn
    }
}
