package com.dsb.flashy.util

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Fires the platform-appropriate haptic for a toggle state change.
 *
 * API 30+ (Android 11): TOGGLE_ON / TOGGLE_OFF — the system constants designed
 *   specifically for toggle switches; distinct sensations for on vs. off.
 * API 21–29: CLOCK_TICK — a light, crisp tap that suits binary toggles without
 *   the heavy rumble of LONG_PRESS.
 */
fun View.performToggleHaptic(nowEnabled: Boolean) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        performHapticFeedback(
            if (nowEnabled) HapticFeedbackConstants.TOGGLE_ON
            else HapticFeedbackConstants.TOGGLE_OFF
        )
    } else {
        performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }
}
