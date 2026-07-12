package com.dsb.flashy.analytics

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.dsb.flashy.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

/**
 * Central analytics & observability hub.
 *
 * Firebase Console locations:
 *   Crashes   → Crashlytics  (custom keys show free vs premium context)
 *   Events    → Analytics → Events  (feature usage, upgrade funnel)
 *   Users     → Firestore → /users/{deviceId}  (premium user list)
 *
 * To query premium users in Firestore:
 *   Collection: users
 *   Filter: isPremium == true
 */
object FlashyAnalytics {

    // ── Event name constants ──────────────────────────────────────────────────
    object Event {
        const val PREMIUM_ACTIVATED      = "premium_activated"
        const val UPGRADE_SCREEN_OPENED  = "upgrade_screen_opened"
        const val PAYMENT_INITIATED      = "payment_initiated"
        const val PAYMENT_FAILED         = "payment_failed"
        const val FEATURE_TOGGLED        = "feature_toggled"
        const val FLASH_TEST_TAPPED      = "flash_test_tapped"
        const val PREMIUM_GATE_TAPPED    = "premium_gate_tapped"
    }

    // ── User property keys ────────────────────────────────────────────────────
    object Prop {
        const val IS_PREMIUM   = "is_premium"
        const val FLAVOR       = "flavor"
    }

    // ─── Called once per session — sets user context on all tools ─────────────
    fun init(context: Context, isPremium: Boolean) {
        val analytics = FirebaseAnalytics.getInstance(context)
        val crashlytics = FirebaseCrashlytics.getInstance()

        // Firebase Analytics user properties (visible in Audiences + funnels)
        analytics.setUserProperty(Prop.IS_PREMIUM, if (isPremium) "true" else "false")
        analytics.setUserProperty(Prop.FLAVOR, BuildConfig.ENVIRONMENT)

        // Crashlytics custom keys — every crash report now shows these fields
        crashlytics.setCustomKey("is_premium", isPremium)
        crashlytics.setCustomKey("flavor", BuildConfig.ENVIRONMENT)
        crashlytics.setCustomKey("android_sdk", Build.VERSION.SDK_INT)
        crashlytics.setCustomKey("device_model", "${Build.MANUFACTURER} ${Build.MODEL}")
        crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)

        // Upsert a minimal Firestore record for every active installation
        // (even free users — so you can see total install count)
        updateFirestorePresence(context, isPremium)
    }

    // ─── Called when Razorpay payment succeeds ────────────────────────────────
    fun onPremiumActivated(context: Context, paymentId: String) {
        val analytics = FirebaseAnalytics.getInstance(context)
        val crashlytics = FirebaseCrashlytics.getInstance()

        // Update runtime context immediately
        analytics.setUserProperty(Prop.IS_PREMIUM, "true")
        crashlytics.setCustomKey("is_premium", true)
        crashlytics.setCustomKey("payment_id", paymentId)

        analytics.logEvent(Event.PREMIUM_ACTIVATED) {
            param("payment_id", paymentId)
            param("price_inr", 249L)
        }

        // Write a permanent record to Firestore
        writeFirestorePremiumRecord(context, paymentId)
    }

    // ─── Upgrade screen opened (conversion funnel entry) ──────────────────────
    fun logUpgradeScreenOpened(context: Context) {
        FirebaseAnalytics.getInstance(context).logEvent(Event.UPGRADE_SCREEN_OPENED) {}
    }

    // ─── Razorpay checkout tapped (funnel step 2) ─────────────────────────────
    fun logPaymentInitiated(context: Context) {
        FirebaseAnalytics.getInstance(context).logEvent(Event.PAYMENT_INITIATED) {}
    }

    // ─── Payment failed or cancelled ─────────────────────────────────────────
    fun logPaymentFailed(context: Context, code: Int, reason: String) {
        FirebaseAnalytics.getInstance(context).logEvent(Event.PAYMENT_FAILED) {
            param("error_code", code.toLong())
            param("reason", reason.take(100))
        }
    }

    // ─── Feature toggle (calls, SMS, battery guard, sound reactive, etc.) ─────
    fun logFeatureToggled(context: Context, featureName: String, enabled: Boolean) {
        FirebaseAnalytics.getInstance(context).logEvent(Event.FEATURE_TOGGLED) {
            param("feature", featureName)
            param("enabled", if (enabled) "true" else "false")
        }
    }

    // ─── Premium gate tapped by a free user ───────────────────────────────────
    fun logPremiumGateTapped(context: Context, featureName: String) {
        FirebaseAnalytics.getInstance(context).logEvent(Event.PREMIUM_GATE_TAPPED) {
            param("feature", featureName)
        }
    }

    // ─── Log a non-fatal error to Crashlytics ────────────────────────────────
    // Use this for caught exceptions that shouldn't crash the app but are worth knowing about.
    // Example: FlashyAnalytics.logNonFatal(context, Exception("DataStore read returned null"))
    fun logNonFatal(context: Context, throwable: Throwable, message: String = "") {
        val crashlytics = FirebaseCrashlytics.getInstance()
        if (message.isNotBlank()) crashlytics.log(message)
        crashlytics.recordException(throwable)
    }

    // ── Firestore helpers ─────────────────────────────────────────────────────

    private fun deviceId(context: Context): String =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "unknown"

    private fun updateFirestorePresence(context: Context, isPremium: Boolean) {
        val db = FirebaseFirestore.getInstance()
        val data = hashMapOf(
            "isPremium"      to isPremium,
            "deviceModel"    to "${Build.MANUFACTURER} ${Build.MODEL}",
            "androidVersion" to Build.VERSION.SDK_INT,
            "appVersion"     to BuildConfig.VERSION_NAME,
            "flavor"         to BuildConfig.ENVIRONMENT,
            "lastSeen"       to FieldValue.serverTimestamp(),
        )
        db.collection("users")
            .document(deviceId(context))
            .set(data, SetOptions.merge())
    }

    private fun writeFirestorePremiumRecord(context: Context, paymentId: String) {
        val db = FirebaseFirestore.getInstance()
        val userRecord = hashMapOf(
            "isPremium"           to true,
            "paymentId"           to paymentId,
            "premiumActivatedAt"  to FieldValue.serverTimestamp(),
            "deviceModel"         to "${Build.MANUFACTURER} ${Build.MODEL}",
            "androidVersion"      to Build.VERSION.SDK_INT,
            "appVersion"          to BuildConfig.VERSION_NAME,
            "flavor"              to BuildConfig.ENVIRONMENT,
            "lastSeen"            to FieldValue.serverTimestamp(),
        )
        // Per-device record (for total user counts + device info)
        db.collection("users")
            .document(deviceId(context))
            .set(userRecord, SetOptions.merge())

        // Per-payment record — this is what enables cross-device restore.
        // Any device can look up this document by payment ID to verify purchase.
        val paymentRecord = hashMapOf(
            "paymentId"           to paymentId,
            "verifiedAt"          to FieldValue.serverTimestamp(),
            "appVersion"          to BuildConfig.VERSION_NAME,
            "androidVersion"      to Build.VERSION.SDK_INT,
        )
        db.collection("payments")
            .document(paymentId)
            .set(paymentRecord, SetOptions.merge())
    }
}
