package com.dsb.flashy.payment

import android.content.Context
import androidx.datastore.preferences.core.edit
import com.dsb.flashy.datastore.GlobalSettingsStore.IS_PREMIUM
import com.dsb.flashy.datastore.GlobalSettingsStore.PAYMENT_ID
import com.dsb.flashy.datastore.GlobalSettingsStore.PAYMENT_TIMESTAMP
import com.dsb.flashy.datastore.flashDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

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
    // Call from a debug menu or adb shell; remove from prod builds via ProGuard
    // or guard with BuildConfig.DEBUG.
    suspend fun grantTestPremium(context: Context) {
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
}
