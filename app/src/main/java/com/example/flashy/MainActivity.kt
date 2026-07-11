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
import androidx.datastore.preferences.core.edit
import com.dsb.flashy.datastore.GlobalSettingsStore.APP_LAUNCH_COUNT
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_GLOBAL
import com.dsb.flashy.datastore.GlobalSettingsStore.ONBOARDING_COMPLETE
import com.dsb.flashy.datastore.GlobalSettingsStore.REVIEW_PROMPTED
import com.dsb.flashy.datastore.flashDataStore
import com.dsb.flashy.util.updateFlashShortcut
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager
import com.dsb.flashy.screen.FlashyIntroScreen
import com.dsb.flashy.screen.dashboard.FlashDashboardScreen
import com.dsb.flashy.screen.onboarding.OnboardingScreen
import com.dsb.flashy.services.FlashCallService
import com.dsb.flashy.ui.theme.FlashyTheme
import kotlinx.coroutines.flow.first

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
                val prefs = context.flashDataStore.data.first()
                val onboardingDone = prefs[ONBOARDING_COMPLETE] ?: false
                val flashEnabled   = prefs[FLASH_GLOBAL]        ?: true
                // Register/sync the long-press shortcut on every launch so its label
                // always reflects the current flash state.
                updateFlashShortcut(context, flashEnabled)
                screen = if (onboardingDone) AppScreen.INTRO else AppScreen.ONBOARDING
            }

            // Counts each time the user reaches the dashboard (= one "use").
            // On the 5th use, triggers the Play Store in-app review prompt once.
            LaunchedEffect(screen) {
                if (screen != AppScreen.DASHBOARD) return@LaunchedEffect
                val prefs      = context.flashDataStore.data.first()
                val count      = (prefs[APP_LAUNCH_COUNT] ?: 0) + 1
                val prompted   = prefs[REVIEW_PROMPTED]   ?: false
                context.flashDataStore.edit { it[APP_LAUNCH_COUNT] = count }
                if (count >= 5 && !prompted) {
                    context.flashDataStore.edit { it[REVIEW_PROMPTED] = true }
                    val mgr = if (BuildConfig.DEBUG) FakeReviewManager(context)
                              else ReviewManagerFactory.create(context)
                    mgr.requestReviewFlow().addOnCompleteListener { task ->
                        if (task.isSuccessful) mgr.launchReviewFlow(this@MainActivity, task.result)
                    }
                }
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
