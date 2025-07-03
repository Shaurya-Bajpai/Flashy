package com.example.flashy.advance

import android.content.Context
import android.media.AudioManager

fun isRingerModeAllowed(context: Context, allowed: String): Boolean {
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    return when (allowed) {
        "All" -> true
        "Normal" -> audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL
        "Vibrate" -> audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE
        "Silent" -> audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT
        else -> true
    }
}
