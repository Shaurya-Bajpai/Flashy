package com.dsb.flashy

import com.dsb.flashy.datastore.GlobalSettingsStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Validates that every DataStore preference key has the correct name and
 * that all keys are unique. These tests catch accidental key renames that
 * would silently wipe saved user settings on the next app launch.
 */
class SettingsKeysTest {

    @Test
    fun flashGlobalKey_hasCorrectName() {
        assertEquals("flash_global", GlobalSettingsStore.FLASH_GLOBAL.name)
    }

    @Test
    fun flashCallKey_hasCorrectName() {
        assertEquals("flash_call", GlobalSettingsStore.FLASH_CALL.name)
    }

    @Test
    fun flashSmsKey_hasCorrectName() {
        assertEquals("flash_sms", GlobalSettingsStore.FLASH_SMS.name)
    }

    @Test
    fun flashNotificationsKey_hasCorrectName() {
        assertEquals("flash_notifications", GlobalSettingsStore.FLASH_NOTIFICATIONS.name)
    }

    @Test
    fun flashScreenOffOnlyKey_hasCorrectName() {
        assertEquals("flash_screen_off_only", GlobalSettingsStore.FLASH_SCREEN_OFF_ONLY.name)
    }

    @Test
    fun flashDndStartKey_hasCorrectName() {
        assertEquals("flash_dnd_start", GlobalSettingsStore.FLASH_DND_START.name)
    }

    @Test
    fun flashDndEndKey_hasCorrectName() {
        assertEquals("flash_dnd_end", GlobalSettingsStore.FLASH_DND_END.name)
    }

    @Test
    fun flashBatteryThresholdKey_hasCorrectName() {
        assertEquals("flash_battery_threshold", GlobalSettingsStore.FLASH_BATTERY_THRESHOLD.name)
    }

    @Test
    fun flashRingerModeKey_hasCorrectName() {
        assertEquals("flash_ringer_mode", GlobalSettingsStore.FLASH_RINGER_MODE.name)
    }

    @Test
    fun allKeys_haveUniqueNames() {
        val allKeys = listOf(
            GlobalSettingsStore.FLASH_GLOBAL.name,
            GlobalSettingsStore.FLASH_CALL.name,
            GlobalSettingsStore.FLASH_SMS.name,
            GlobalSettingsStore.FLASH_NOTIFICATIONS.name,
            GlobalSettingsStore.FLASH_SCREEN_OFF_ONLY.name,
            GlobalSettingsStore.FLASH_DND_START.name,
            GlobalSettingsStore.FLASH_DND_END.name,
            GlobalSettingsStore.FLASH_BATTERY_THRESHOLD.name,
            GlobalSettingsStore.FLASH_RINGER_MODE.name
        )
        assertEquals(
            "Duplicate DataStore key found — this would silently corrupt saved settings",
            allKeys.size,
            allKeys.toSet().size
        )
    }

    @Test
    fun dndStartAndEndKeys_areDifferent() {
        assertNotEquals(GlobalSettingsStore.FLASH_DND_START.name, GlobalSettingsStore.FLASH_DND_END.name)
    }

    @Test
    fun callAndSmsKeys_areDifferent() {
        assertNotEquals(GlobalSettingsStore.FLASH_CALL.name, GlobalSettingsStore.FLASH_SMS.name)
    }
}
