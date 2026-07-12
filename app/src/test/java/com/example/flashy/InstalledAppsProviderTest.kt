package com.dsb.flashy

import com.dsb.flashy.util.InstalledApp
import com.dsb.flashy.util.InstalledAppsProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the pure list-building logic behind the per-app flash-rule picker.
 * The real bug this guards against: only pre-installed/system apps were
 * showing up, because third-party apps (WhatsApp, Instagram, etc.) need
 * Android 11+ package-visibility exemption via a launch-intent check — if
 * that filter or the self-package exclusion regresses, this list silently
 * drops or duplicates apps again.
 */
class InstalledAppsProviderTest {

    private val labels = mapOf(
        "com.dsb.flashy" to "Flashy",
        "com.android.settings" to "Settings",          // pre-installed/system
        "com.whatsapp" to "WhatsApp",                  // Play Store app
        "com.instagram.android" to "Instagram",         // Play Store app
        "com.facebook.katana" to "Facebook",             // Play Store app
        "com.some.backgroundservice" to "Background Service", // no launcher UI
    )

    private fun build(
        packageNames: List<String> = labels.keys.toList(),
        selfPackage: String = "com.dsb.flashy",
        launchable: Set<String> = labels.keys - "com.some.backgroundservice",
    ): List<InstalledApp> {
        return InstalledAppsProvider.buildAppList(
            packageNames = packageNames,
            selfPackage = selfPackage,
            hasLaunchIntent = { it in launchable },
            labelOf = { labels.getValue(it) },
        )
    }

    @Test
    fun includesBothSystemAndPlayStoreApps() {
        val result = build().map { it.packageName }
        assertTrue("system app missing", "com.android.settings" in result)
        assertTrue("WhatsApp missing", "com.whatsapp" in result)
        assertTrue("Instagram missing", "com.instagram.android" in result)
        assertTrue("Facebook missing", "com.facebook.katana" in result)
    }

    @Test
    fun excludesOwnApp() {
        val result = build().map { it.packageName }
        assertTrue("Flashy should not list itself", "com.dsb.flashy" !in result)
    }

    @Test
    fun excludesAppsWithNoLaunchIntent() {
        val result = build().map { it.packageName }
        assertTrue(
            "apps without a launch intent shouldn't be pickable",
            "com.some.backgroundservice" !in result
        )
    }

    @Test
    fun sortsByNameCaseInsensitively() {
        val result = build().map { it.name }
        assertEquals(result.sortedBy { it.lowercase() }, result)
    }

    @Test
    fun deduplicatesByPackageName() {
        val result = build(packageNames = listOf("com.whatsapp", "com.whatsapp", "com.instagram.android"))
        assertEquals(2, result.size)
    }

    @Test
    fun emptyInstalledListProducesEmptyResult() {
        assertEquals(emptyList<InstalledApp>(), build(packageNames = emptyList()))
    }
}
