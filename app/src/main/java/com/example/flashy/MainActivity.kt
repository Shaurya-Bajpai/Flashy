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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dsb.flashy.datastore.GlobalSettingsStore.ONBOARDING_COMPLETE
import com.dsb.flashy.datastore.flashDataStore
import com.dsb.flashy.screen.FlashyIntroScreen
import com.dsb.flashy.screen.dashboard.FlashDashboardScreen
import com.dsb.flashy.screen.onboarding.OnboardingScreen
import com.dsb.flashy.services.FlashCallService
import com.dsb.flashy.ui.theme.FlashyTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private enum class AppScreen { LOADING, ONBOARDING, INTRO, DASHBOARD }

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val runtimePermissions = buildList {
            add(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
        ActivityCompat.requestPermissions(this, runtimePermissions, 101)

        Handler(Looper.getMainLooper()).postDelayed({ startFlashService() }, 1500)

        setContent {
            val context = LocalContext.current
            var screen by remember { mutableStateOf(AppScreen.LOADING) }

            LaunchedEffect(Unit) {
                val onboardingDone = context.flashDataStore.data
                    .map { prefs -> prefs[ONBOARDING_COMPLETE] ?: false }
                    .first()
                screen = if (onboardingDone) AppScreen.INTRO else AppScreen.ONBOARDING
            }

            // Single FlashyTheme wrapper — all screens share the same theme context.
            FlashyTheme {
                when (screen) {
                    AppScreen.LOADING -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF060401))
                    )

                    AppScreen.ONBOARDING -> OnboardingScreen(
                        context = context,
                        onComplete = { screen = AppScreen.INTRO }
                    )

                    AppScreen.INTRO -> FlashyIntroScreen(
                        onComplete = { screen = AppScreen.DASHBOARD }
                    )

                    AppScreen.DASHBOARD -> FlashDashboardScreen(
                        context = context
                    )
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
}
