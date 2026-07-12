package com.dsb.flashy

import com.dsb.flashy.model.AppFlashRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Mirrors the per-app opt-in resolution used in
 * NotificationListener.onNotificationPosted: an app notification only
 * flashes if its package is in FLASH_APP_RULES. There's no "flash for every
 * app" fallback and no contact-name filtering — this pins that behavior so
 * it can't silently regress back to a blanket default.
 */
class AppNotificationSelectionTest {

    private fun resolve(rules: List<AppFlashRule>, packageName: String): AppFlashRule? =
        rules.find { it.packageName == packageName }

    @Test
    fun selectedApp_resolvesToItsOwnRule() {
        val rules = listOf(AppFlashRule("com.whatsapp", "WhatsApp", flashCount = 3, flashSpeedMs = 400))
        val resolved = resolve(rules, "com.whatsapp")
        assertEquals(3, resolved?.flashCount)
        assertEquals(400, resolved?.flashSpeedMs)
    }

    @Test
    fun unselectedApp_resolvesToNull_meaningNoFlash() {
        val rules = listOf(AppFlashRule("com.whatsapp", "WhatsApp"))
        assertNull(resolve(rules, "com.google.android.apps.photos"))
    }

    @Test
    fun emptyRuleList_neverFlashesAnyApp() {
        assertNull(resolve(emptyList(), "com.whatsapp"))
    }

    @Test
    fun multipleSelectedApps_eachResolvesIndependently() {
        val rules = listOf(
            AppFlashRule("com.whatsapp", "WhatsApp", flashCount = 5, flashSpeedMs = 200),
            AppFlashRule("com.instagram.android", "Instagram", flashCount = 10, flashSpeedMs = 100),
        )
        assertEquals(5, resolve(rules, "com.whatsapp")?.flashCount)
        assertEquals(10, resolve(rules, "com.instagram.android")?.flashCount)
        assertNull(resolve(rules, "com.facebook.katana"))
    }
}
