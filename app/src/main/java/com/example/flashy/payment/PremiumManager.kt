package com.dsb.flashy.payment

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.dsb.flashy.BuildConfig
import com.dsb.flashy.analytics.FlashyAnalytics
import com.dsb.flashy.datastore.GlobalSettingsStore.IS_PREMIUM
import com.dsb.flashy.datastore.GlobalSettingsStore.PAYMENT_ID
import com.dsb.flashy.datastore.GlobalSettingsStore.PAYMENT_TIMESTAMP
import com.dsb.flashy.datastore.flashDataStore
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

// ─── Result type for restore-purchase flow ────────────────────────────────────
sealed class RestoreResult {
    object Success        : RestoreResult()
    object NotFound       : RestoreResult()  // valid format but not in Firestore
    object InvalidFormat  : RestoreResult()  // doesn't start with pay_
    data class NetworkError(val message: String) : RestoreResult()
}

// ─── Pricing constants (update these to change pricing) ──────────────────────
const val PREMIUM_PRICE_PAISE = 24900   // ₹249 in paise (100 paise = 1 rupee)
const val PREMIUM_PRICE_DISPLAY = "₹249"
const val PREMIUM_CURRENCY = "INR"

object PremiumManager {

    // ── Activate premium after a successful Razorpay payment ─────────────────
    suspend fun activate(context: Context, paymentId: String) {
        context.flashDataStore.edit { prefs ->
            prefs[IS_PREMIUM]          = true
            prefs[PAYMENT_ID]          = paymentId
            prefs[PAYMENT_TIMESTAMP]   = System.currentTimeMillis().toString()
        }
        FlashyAnalytics.onPremiumActivated(context, paymentId)
    }

    // ── One-shot check (use inside a coroutine) ───────────────────────────────
    suspend fun isPremium(context: Context): Boolean {
        return context.flashDataStore.data
            .map { it[IS_PREMIUM] ?: false }
            .first()
    }

    // ── Reactive flow (use with collectAsState in Compose) ───────────────────
    // Example:
    //   val isPremium by PremiumManager.isPremiumFlow(context).collectAsState(initial = false)
    fun isPremiumFlow(context: Context): Flow<Boolean> {
        return context.flashDataStore.data.map { it[IS_PREMIUM] ?: false }
    }

    // ── Returns the stored Razorpay payment ID (empty string if not premium) ──
    suspend fun getPaymentId(context: Context): String {
        return context.flashDataStore.data
            .map { it[PAYMENT_ID] ?: "" }
            .first()
    }

    // ── DEV ONLY — grants premium without payment for testing ─────────────────
    // Call from a debug menu or adb shell. No-ops in release builds.
    suspend fun grantTestPremium(context: Context) {
        if (!BuildConfig.DEBUG) return
        activate(context, "test_payment_${System.currentTimeMillis()}")
    }

    // ── Revokes premium (useful for testing the locked state) ─────────────────
    suspend fun revokePremium(context: Context) {
        context.flashDataStore.edit { prefs ->
            prefs[IS_PREMIUM]        = false
            prefs[PAYMENT_ID]        = ""
            prefs[PAYMENT_TIMESTAMP] = ""
        }
    }

    /**
     * Cross-device restore: look up the payment ID in Firestore.
     * Razorpay sends the payment ID (pay_XXXXXXXXXXXXXX) via SMS/email receipt
     * to the user after purchase. They enter it here on a new device.
     *
     * Security: the /payments collection is only writable by the app after a
     * successful Razorpay SDK callback — a random guess of pay_XXXX won't exist.
     */
    suspend fun restorePurchase(context: Context, enteredId: String): RestoreResult {
        val cleanId = enteredId.trim()

        // Razorpay payment IDs always start with "pay_" and are 18–20 chars total
        if (!cleanId.startsWith("pay_") || cleanId.length < 10) {
            return RestoreResult.InvalidFormat
        }

        return try {
            val doc = FirebaseFirestore.getInstance()
                .collection("payments")
                .document(cleanId)
                .get()
                .await()

            if (doc.exists()) {
                activate(context, cleanId)
                RestoreResult.Success
            } else {
                RestoreResult.NotFound
            }
        } catch (e: Exception) {
            RestoreResult.NetworkError(e.message ?: "Network error")
        }
    }
}
