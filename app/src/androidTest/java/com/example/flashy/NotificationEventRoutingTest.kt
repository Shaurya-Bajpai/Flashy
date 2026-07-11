package com.dsb.flashy

import android.app.Notification
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Validates the category → event-type routing logic used in NotificationListener.
 *
 * The logic (mirrored here from NotificationListener.onNotificationPosted):
 *   CATEGORY_CALL / CATEGORY_MISSED_CALL → null (skip; CallStateListener handles these)
 *   CATEGORY_MESSAGE                      → "SMS"
 *   anything else                         → "NOTIF"
 *
 * Keeping this logic as a pure function makes it easy to test without
 * a full service context.
 */
@RunWith(AndroidJUnit4::class)
class NotificationEventRoutingTest {

    private fun categoryToEventType(category: String?): String? = when (category) {
        Notification.CATEGORY_CALL,
        Notification.CATEGORY_MISSED_CALL -> null
        Notification.CATEGORY_MESSAGE     -> "SMS"
        else                              -> "NOTIF"
    }

    // ── Call categories are skipped ─────────────────────────────────────────

    @Test
    fun incomingCallCategory_returnsNull() {
        assertNull(categoryToEventType(Notification.CATEGORY_CALL))
    }

    @Test
    fun missedCallCategory_returnsNull() {
        assertNull(categoryToEventType(Notification.CATEGORY_MISSED_CALL))
    }

    // ── Messaging category maps to SMS ──────────────────────────────────────

    @Test
    fun messageCategory_returnsSmsEvent() {
        // Notification.CATEGORY_MESSAGE = "msg"
        // Used by Google Messages, WhatsApp, Signal, Telegram, etc.
        assertEquals("SMS", categoryToEventType(Notification.CATEGORY_MESSAGE))
    }

    // ── Everything else maps to NOTIF ───────────────────────────────────────

    @Test
    fun emailCategory_returnsNotifEvent() {
        assertEquals("NOTIF", categoryToEventType(Notification.CATEGORY_EMAIL))
    }

    @Test
    fun socialCategory_returnsNotifEvent() {
        assertEquals("NOTIF", categoryToEventType(Notification.CATEGORY_SOCIAL))
    }

    @Test
    fun promoCategory_returnsNotifEvent() {
        assertEquals("NOTIF", categoryToEventType(Notification.CATEGORY_PROMO))
    }

    @Test
    fun reminderCategory_returnsNotifEvent() {
        assertEquals("NOTIF", categoryToEventType(Notification.CATEGORY_REMINDER))
    }

    @Test
    fun nullCategory_returnsNotifEvent() {
        // Apps that don't set a category still trigger the Apps flash
        assertEquals("NOTIF", categoryToEventType(null))
    }

    @Test
    fun unknownCategory_returnsNotifEvent() {
        assertEquals("NOTIF", categoryToEventType("some_future_category"))
    }
}
