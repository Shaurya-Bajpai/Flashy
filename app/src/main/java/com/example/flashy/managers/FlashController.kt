package com.dsb.flashy.managers

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
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
            }
        }
    }

    fun blinkFlashIndefinitely(speedMs: Long = 300L) {
        blinkJob?.cancel()
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
            }
        }
    }

    fun stopBlinking() {
        blinkJob?.cancel()
        blinkJob = null
        turnOff()
    }

    /** Call this when the owning Service is destroyed. */
    fun release() {
        stopBlinking()
        scope.cancel()
    }
}
