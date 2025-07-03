package com.example.flashy

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.example.flashy.call.CallStateListener
import com.example.flashy.managers.FlashController
import com.example.flashy.screen.FlashyIntroScreen
import com.example.flashy.screen.FlashyScreen
import com.example.flashy.screen.dash.FlashDashScreen
import com.example.flashy.screen.dashboard.FlashDashboardScreen
import com.example.flashy.services.FlashCallService

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
        val serviceIntent = Intent(this, FlashCallService::class.java).apply {
            putExtra("eventType", "INIT") // safe startup
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
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
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FlashyIntroScreen(
                        onComplete = {
                            // Navigate to the dashboard screen
                            setContent {
                                FlashDashboardScreen(context = this@MainActivity)
                            }
                        },
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        callListener.unregister()
    }
}