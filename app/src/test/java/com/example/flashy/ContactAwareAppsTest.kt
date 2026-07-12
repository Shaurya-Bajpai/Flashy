package com.dsb.flashy

import android.content.pm.ApplicationInfo
import com.dsb.flashy.util.ContactAwareApps
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers which apps are offered the "Selected" (per-contact) flash-rule mode.
 * The bug this guards against: a utility app with no sender/contact concept
 * (gallery, recorder, ...) showing the contact picker anyway, which dead-ends
 * the user with a box they can never usefully fill in — flash silently never
 * fires because the contact list stays empty.
 */
class ContactAwareAppsTest {

    @Test
    fun knownMessagingApps_supportContactFiltering() {
        assertTrue(ContactAwareApps.supportsContactFiltering("com.whatsapp", ApplicationInfo.CATEGORY_UNDEFINED))
        assertTrue(ContactAwareApps.supportsContactFiltering("com.instagram.android", ApplicationInfo.CATEGORY_UNDEFINED))
        assertTrue(ContactAwareApps.supportsContactFiltering("org.telegram.messenger", ApplicationInfo.CATEGORY_UNDEFINED))
    }

    @Test
    fun socialCategoryApps_supportContactFiltering_evenIfUnknownPackage() {
        assertTrue(
            ContactAwareApps.supportsContactFiltering("com.some.regional.chat.app", ApplicationInfo.CATEGORY_SOCIAL)
        )
    }

    @Test
    fun utilityApps_doNotSupportContactFiltering() {
        assertFalse(ContactAwareApps.supportsContactFiltering("com.google.android.apps.photos", ApplicationInfo.CATEGORY_IMAGE))
        assertFalse(ContactAwareApps.supportsContactFiltering("com.android.soundrecorder", ApplicationInfo.CATEGORY_AUDIO))
        assertFalse(ContactAwareApps.supportsContactFiltering("com.android.calculator2", ApplicationInfo.CATEGORY_UNDEFINED))
    }

    @Test
    fun unknownPackageWithNoCategory_defaultsToUnsupported() {
        assertFalse(
            ContactAwareApps.supportsContactFiltering("com.unknown.random.app", ApplicationInfo.CATEGORY_UNDEFINED)
        )
    }
}
