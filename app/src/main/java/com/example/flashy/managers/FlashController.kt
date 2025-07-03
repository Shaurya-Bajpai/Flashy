package com.example.flashy.managers

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import kotlinx.coroutines.*

class FlashController(private val context: Context) {
    private var cameraManager: CameraManager =
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private var cameraId: String? = null
    private var blinkJob: Job? = null

    init {
        try {
            cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun turnOn() {
        try {
            cameraId?.let {
                cameraManager.setTorchMode(it, true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun turnOff() {
        try {
            cameraId?.let {
                cameraManager.setTorchMode(it, false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun blinkFlashIndefinitely(speedMs: Long = 300L) {
        blinkJob?.cancel()
        blinkJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                while (isActive) {
                    turnOn()
                    delay(speedMs)
                    turnOff()
                    delay(speedMs)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun blinkFlash(speedMs: Long = 300L, repeatCount: Int = 2) {
        blinkJob?.cancel()
        blinkJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                repeat(repeatCount) {
                    turnOn()
                    delay(speedMs)
                    turnOff()
                    delay(speedMs)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun stopBlinking() {
        blinkJob?.cancel()
        blinkJob = null
        turnOff()
    }
}