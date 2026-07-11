package com.dsb.flashy.managers

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
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
import kotlin.math.sqrt

class FlashController(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val cameraId: String? = findFlashCamera()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var blinkJob: Job? = null
    private var soundReactiveJob: Job? = null

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

    /**
     * Start continuously sampling the microphone and flash when the amplitude
     * crosses a threshold derived from [sensitivityPct] (1–100).
     * Higher sensitivity = lower threshold = triggers on quieter sounds.
     *
     * Call/SMS/notification blink patterns have automatic priority: while
     * [blinkJob] is active the sound reactive loop simply skips torch control
     * so the blink pattern runs uninterrupted. It resumes automatically once
     * the blink finishes.
     */
    fun startSoundReactive(sensitivityPct: Int) {
        stopSoundReactive()
        // Safety timeout: max 2 hours of continuous recording
        if (!wakeLock.isHeld) wakeLock.acquire(2 * 60 * 60 * 1000L)

        soundReactiveJob = scope.launch(Dispatchers.IO) {
            val sampleRate = 44100
            val minBuf = AudioRecord.getMinBufferSize(
                sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = minBuf.coerceAtLeast(2048)

            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                Log.e("FlashController", "AudioRecord failed to initialize")
                if (wakeLock.isHeld) wakeLock.release()
                return@launch
            }

            // threshold: (101 - sensitivity) * 130 + 200
            //   1%   → ~13070  (very hard to trigger)
            //   50%  → ~6700   (triggers on music beats)
            //   100% → ~200    (triggers on ambient noise)
            val threshold = (101f - sensitivityPct.coerceIn(1, 100)) * 130f + 200f
            val buffer = ShortArray(bufferSize / 2)

            audioRecord.startRecording()
            try {
                while (isActive) {
                    val read = audioRecord.read(buffer, 0, buffer.size)
                    if (read <= 0) continue

                    var sum = 0.0
                    for (i in 0 until read) sum += buffer[i].toLong() * buffer[i]
                    val rms = sqrt(sum / read).toFloat()

                    // Yield priority to any running blink pattern
                    if (rms > threshold && blinkJob?.isActive != true) {
                        setTorch(true)
                        delay(60)
                        setTorch(false)
                        delay(40)
                    }
                }
            } finally {
                setTorch(false)
                audioRecord.stop()
                audioRecord.release()
                if (wakeLock.isHeld) wakeLock.release()
            }
        }
    }

    fun stopSoundReactive() {
        soundReactiveJob?.cancel()
        soundReactiveJob = null
        setTorch(false)
    }

    /** Call this when the owning Service is destroyed. */
    fun release() {
        stopBlinking()
        stopSoundReactive()
        scope.cancel()
    }
}
