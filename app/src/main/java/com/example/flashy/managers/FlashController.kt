package com.dsb.flashy.managers

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.PowerManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FlashController(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val cameraId: String? = findFlashCamera()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var blinkJob: Job? = null

    // PARTIAL_WAKE_LOCK keeps the CPU running while the screen stays off.
    // This is essential on budget devices (Xiaomi, Oppo, Vivo) that aggressively
    // suspend background processes — without it, a 2-second blink sequence may
    // be cut short before it finishes.
    private val wakeLock: PowerManager.WakeLock =
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Flashy:FlashController")

    private fun findFlashCamera(): String? = try {
        cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }
    } catch (e: Exception) {
        Log.e("FlashController", "Failed to find flash camera", e)
        null
    }

    fun turnOn() = setTorch(true)
    fun turnOff() = setTorch(false)

    private fun setTorch(on: Boolean) {
        val id = cameraId ?: return
        try {
            cameraManager.setTorchMode(id, on)
        } catch (e: Exception) {
            Log.e("FlashController", "setTorchMode($on) failed", e)
        }
    }

    fun blinkFlash(speedMs: Long = 300L, repeatCount: Int = 5) {
        blinkJob?.cancel()
        // Safety timeout: repeatCount blinks × 2 intervals × speedMs, plus a 1-second buffer.
        val timeout = repeatCount * 2 * speedMs + 1_000L
        if (!wakeLock.isHeld) wakeLock.acquire(timeout)
        blinkJob = scope.launch {
            try {
                repeat(repeatCount) {
                    if (!isActive) return@launch
                    turnOn()
                    delay(speedMs)
                    turnOff()
                    delay(speedMs)
                }
            } finally {
                turnOff()
                if (wakeLock.isHeld) wakeLock.release()
            }
        }
    }

    fun blinkFlashIndefinitely(speedMs: Long = 300L) {
        blinkJob?.cancel()
        // 60-second safety timeout — longer than any realistic ringtone.
        if (!wakeLock.isHeld) wakeLock.acquire(60_000L)
        blinkJob = scope.launch {
            try {
                while (isActive) {
                    turnOn()
                    delay(speedMs)
                    turnOff()
                    delay(speedMs)
                }
            } finally {
                turnOff()
                if (wakeLock.isHeld) wakeLock.release()
            }
        }
    }

    fun stopBlinking() {
        blinkJob?.cancel()
        blinkJob = null
        turnOff()
        if (wakeLock.isHeld) wakeLock.release()
    }

    /** Call this when the owning Service is destroyed. */
    fun release() {
        stopBlinking()
        scope.cancel()
    }
}
