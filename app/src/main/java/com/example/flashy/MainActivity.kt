package com.example.flashy

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.flashy.call.CallStateListener
import com.example.flashy.managers.FlashController
import com.example.flashy.notification.NotificationPermissionDialog
import com.example.flashy.screen.FlashyIntroScreen
import com.example.flashy.screen.dashboard.FlashDashboardScreen
import com.example.flashy.services.FlashCallService
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var callListener: CallStateListener
    private lateinit var flashController: FlashController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize flash and listener immediately
        flashController = FlashController(this)
        callListener = CallStateListener(this, flashController)
        callListener.register()

        // Start the foreground service ONCE, without triggering flashlight
        // Request permissions first
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.POST_NOTIFICATIONS
            ),
            101
        )

        // Start service only if permissions are granted
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            if (allPermissionsGranted()) {
                val serviceIntent = Intent(this, FlashCallService::class.java).apply {
                    putExtra("eventType", "INIT")
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                } else {
                    startService(serviceIntent)
                }
            }
        }, 1000)
        // Small delay to allow permission dialog to complete

        // Request runtime permissions if not granted
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE,
            ),
            101
        )

        setContent {
            val context = LocalContext.current
            var notificationPermissionGranted by remember { mutableStateOf(isNotificationServiceEnabled(context)) }

            LaunchedEffect(Unit) {
                // Monitor the permission after user returns from settings
                while (!notificationPermissionGranted) {
                    delay(1000)
                    notificationPermissionGranted = isNotificationServiceEnabled(context)
                }
            }

            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (notificationPermissionGranted) {
                        FlashyIntroScreen(
                            onComplete = {
                                // Navigate to the dashboard screen
                                setContent {
                                    FlashDashboardScreen(context = this@MainActivity)
                                }
                            }
                        )
                    } else {
                        NotificationPermissionDialog()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        callListener.unregister()
    }
    private fun allPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
    }

    private fun isNotificationServiceEnabled(context: Context): Boolean {
        val enabledPackages = NotificationManagerCompat.getEnabledListenerPackages(context)
        return enabledPackages.contains(context.packageName)
    }
}