package com.example.flashy.advance

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
