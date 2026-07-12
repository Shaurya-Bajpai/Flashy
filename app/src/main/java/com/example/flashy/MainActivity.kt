package com.dsb.flashy

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
import com.dsb.flashy.notification.NotificationPermissionDialog
import com.dsb.flashy.screen.FlashyIntroScreen
import com.dsb.flashy.screen.dashboard.FlashDashboardScreen
import com.dsb.flashy.services.FlashCallService
import com.dsb.flashy.ui.theme.FlashyTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request only the permissions needed to start the service.
        // READ_PHONE_STATE is requested progressively when the user enables the Calls feature.
        val runtimePermissions = buildList {
            add(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()

        ActivityCompat.requestPermissions(this, runtimePermissions, 101)

        // Start the foreground service after the permission dialog has had a moment
        // to be acted on. The service itself guards against running without CAMERA.
        Handler(Looper.getMainLooper()).postDelayed({ startFlashService() }, 1500)

        setContent {
            val context = LocalContext.current
            var notificationPermissionGranted by remember { mutableStateOf(isNotificationServiceEnabled(context)) }

            // Poll until notification listener access is granted.
            // There is no broadcast for this grant — polling is the standard approach.
            LaunchedEffect(Unit) {
                while (!notificationPermissionGranted) {
                    delay(1000)
                    notificationPermissionGranted = isNotificationServiceEnabled(context)
                }
            }

            FlashyTheme {
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
                        NotificationPermissionDialog(onExit = { finish() })
                    }
                }
            }
        }
    }

    private fun startFlashService() {
        if (!hasPermission(Manifest.permission.CAMERA)) return
        val intent = Intent(this, FlashCallService::class.java).apply {
            putExtra("eventType", "INIT")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    private fun hasPermission(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private fun isNotificationServiceEnabled(context: Context): Boolean =
        NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)
}
