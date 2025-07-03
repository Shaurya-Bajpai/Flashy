package com.example.flashy.datastore

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object FlashUtils {

    private var cameraId: String? = null
    private var isFlashing = false
    private var flashJob: Job? = null

    fun init(context: Context) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (id in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                val isBackFacing = characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
                if (hasFlash && isBackFacing) {
                    cameraId = id
                    break
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    fun startFlashing(context: Context, intervalMs: Long = 300L, repeatCount: Int= 4) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
            cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        } ?: return

        flashJob?.cancel() // Cancel any ongoing flash job
        flashJob = CoroutineScope(Dispatchers.Default).launch {
            repeat(repeatCount) {
                try {
                    cameraManager.setTorchMode(cameraId, true) // Turn on flash
                    delay(intervalMs)
                    cameraManager.setTorchMode(cameraId, false) // Turn off flash
                    delay(intervalMs)
                } catch (e: CameraAccessException) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun stopFlashing(context: Context) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        flashJob?.cancel()
        cameraId?.let {
            try {
                cameraManager.setTorchMode(it, false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
