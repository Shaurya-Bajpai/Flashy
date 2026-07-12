package com.dsb.flashy.util

import android.content.pm.ApplicationInfo

/**
 * Decides whether the "Selected" (flash-only-for-specific-people) mode makes
 * sense to offer for an app's per-app flash rule.
 *
 * Only apps whose notifications name an individual sender — messaging,
 * social, and mail apps — support per-contact filtering. Utility apps
 * (gallery, recorder, calculator, ...) never have a "who sent this" concept,
 * so offering a contact filter for them just dead-ends the user with an
 * input box they can never usefully fill in.
 *
 * Android has no API that answers "does this app's notifications carry a
 * sender identity" ahead of actually receiving one, so this combines two
 * signals: a maintained list of well-known messaging/social apps, and the
 * app's self-declared `android:appCategory` (CATEGORY_SOCIAL) as a fallback
 * for apps not on the list.
 */
object ContactAwareApps {

    private val KNOWN_MESSAGING_PACKAGES = setOf(
        "com.whatsapp",
        "com.whatsapp.w4b",
        "com.facebook.orca",                 // Messenger
        "com.facebook.katana",                // Facebook
        "com.facebook.lite",
        "com.instagram.android",
        "org.telegram.messenger",
        "org.telegram.messenger.web",
        "org.thoughtcrime.securesms",         // Signal
        "com.snapchat.android",
        "com.discord",
        "com.Slack",
        "com.microsoft.teams",
        "com.skype.raider",
        "com.google.android.apps.messaging",  // Google Messages / default SMS
        "com.google.android.gm",              // Gmail
        "com.microsoft.office.outlook",
        "com.linkedin.android",
        "jp.naver.line.android",
        "com.viber.voip",
        "com.tencent.mm",                     // WeChat
        "com.kakao.talk",
    )

    fun supportsContactFiltering(packageName: String, category: Int): Boolean {
        if (packageName in KNOWN_MESSAGING_PACKAGES) return true
        return category == ApplicationInfo.CATEGORY_SOCIAL
    }
}
