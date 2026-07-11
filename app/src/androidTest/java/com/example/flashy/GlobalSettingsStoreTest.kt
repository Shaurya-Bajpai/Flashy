package com.dsb.flashy

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dsb.flashy.datastore.GlobalSettingsStore
import com.dsb.flashy.datastore.flashDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for GlobalSettingsStore.
 * These run on a real device or emulator.
 *
 * Run via: ./gradlew connectedDebugAndroidTest
 * Or in Android Studio: right-click this file → Run
 */
@RunWith(AndroidJUnit4::class)
class GlobalSettingsStoreTest {

    private lateinit var context: Context

    @Before
    fun clearDataStore() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        // Wipe all preferences before every test so tests are fully independent.
        runBlocking {
            context.flashDataStore.edit { it.clear() }
        }
    }

    // ── Default values ──────────────────────────────────────────────────────

    @Test
    fun default_flashGlobal_isTrue() = runBlocking {
        val value = GlobalSettingsStore.get(context, GlobalSettingsStore.FLASH_GLOBAL).first()
        assertTrue("Flash global should default to on", value)
    }

    @Test
    fun default_flashCall_isTrue() = runBlocking {
        val value = GlobalSettingsStore.get(context, GlobalSettingsStore.FLASH_CALL).first()
        assertTrue("Flash on calls should default to on", value)
    }

    @Test
    fun default_flashSms_isTrue() = runBlocking {
        val value = GlobalSettingsStore.get(context, GlobalSettingsStore.FLASH_SMS).first()
        assertTrue("Flash on SMS should default to on", value)
    }

    @Test
    fun default_flashNotifications_isTrue() = runBlocking {
        val value = GlobalSettingsStore.get(context, GlobalSettingsStore.FLASH_NOTIFICATIONS).first()
        assertTrue("Flash on notifications should default to on", value)
    }

    @Test
    fun default_batteryThreshold_is15() = runBlocking {
        val value = GlobalSettingsStore.getInt(context, GlobalSettingsStore.FLASH_BATTERY_THRESHOLD).first()
        assertEquals("Battery threshold should default to 15%", 15, value)
    }

    @Test
    fun default_dndStart_isCorrect() = runBlocking {
        val value = GlobalSettingsStore.getString(context, GlobalSettingsStore.FLASH_DND_START).first()
        assertEquals("DND start should default to 00:00", "00:00", value)
    }

    @Test
    fun default_dndEnd_is7am() = runBlocking {
        val value = GlobalSettingsStore.getString(context, GlobalSettingsStore.FLASH_DND_END).first()
        assertEquals("DND end should default to 07:00", "07:00", value)
    }

    // ── Persist and read back ───────────────────────────────────────────────

    @Test
    fun setFlashGlobal_toFalse_persists() = runBlocking {
        GlobalSettingsStore.set(context, GlobalSettingsStore.FLASH_GLOBAL, false)
        val value = GlobalSettingsStore.get(context, GlobalSettingsStore.FLASH_GLOBAL).first()
        assertFalse("Setting flash global to false should persist", value)
    }

    @Test
    fun setFlashCall_toFalse_persists() = runBlocking {
        GlobalSettingsStore.set(context, GlobalSettingsStore.FLASH_CALL, false)
        val value = GlobalSettingsStore.get(context, GlobalSettingsStore.FLASH_CALL).first()
        assertFalse("Disabling call flash should persist", value)
    }

    @Test
    fun setFlashSms_toFalse_thenTrue_reflectsLatestValue() = runBlocking {
        GlobalSettingsStore.set(context, GlobalSettingsStore.FLASH_SMS, false)
        GlobalSettingsStore.set(context, GlobalSettingsStore.FLASH_SMS, true)
        val value = GlobalSettingsStore.get(context, GlobalSettingsStore.FLASH_SMS).first()
        assertTrue("Last write should win", value)
    }

    @Test
    fun setDndStart_persistsCustomTime() = runBlocking {
        GlobalSettingsStore.edit(context, GlobalSettingsStore.FLASH_DND_START, "23:30")
        val value = GlobalSettingsStore.getString(context, GlobalSettingsStore.FLASH_DND_START).first()
        assertEquals("Custom DND start time should persist", "23:30", value)
    }

    @Test
    fun setDndEnd_persistsCustomTime() = runBlocking {
        GlobalSettingsStore.edit(context, GlobalSettingsStore.FLASH_DND_END, "08:00")
        val value = GlobalSettingsStore.getString(context, GlobalSettingsStore.FLASH_DND_END).first()
        assertEquals("Custom DND end time should persist", "08:00", value)
    }

    // ── Independence: settings don't bleed into each other ─────────────────

    @Test
    fun disablingFlashCall_doesNotAffectFlashSms() = runBlocking {
        GlobalSettingsStore.set(context, GlobalSettingsStore.FLASH_CALL, false)
        val smsValue = GlobalSettingsStore.get(context, GlobalSettingsStore.FLASH_SMS).first()
        assertTrue("Disabling call flash must not touch SMS flash setting", smsValue)
    }

    @Test
    fun disablingFlashGlobal_doesNotAffectIndividualToggles() = runBlocking {
        GlobalSettingsStore.set(context, GlobalSettingsStore.FLASH_GLOBAL, false)
        val callValue = GlobalSettingsStore.get(context, GlobalSettingsStore.FLASH_CALL).first()
        val smsValue  = GlobalSettingsStore.get(context, GlobalSettingsStore.FLASH_SMS).first()
        assertTrue("Individual call toggle should remain at its default when global is disabled", callValue)
        assertTrue("Individual SMS toggle should remain at its default when global is disabled", smsValue)
    }
}
