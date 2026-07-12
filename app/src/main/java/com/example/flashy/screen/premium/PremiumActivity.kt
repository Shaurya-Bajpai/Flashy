package com.dsb.flashy.screen.premium

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.dsb.flashy.BuildConfig
import com.dsb.flashy.payment.PREMIUM_CURRENCY
import com.dsb.flashy.payment.PREMIUM_PRICE_PAISE
import com.dsb.flashy.payment.PremiumManager
import com.dsb.flashy.payment.RestoreResult
import com.dsb.flashy.ui.theme.FlashyTheme
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.launch
import org.json.JSONObject

class PremiumActivity : ComponentActivity(), PaymentResultListener {

    private var isPurchasing  by mutableStateOf(false)
    private var restoreState  by mutableStateOf<RestoreState>(RestoreState.Idle)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Pre-warm the Razorpay WebView so the payment sheet opens faster
        Checkout.preload(applicationContext)

        setContent {
            FlashyTheme {
                PremiumScreen(
                    isPurchasing  = isPurchasing,
                    restoreState  = restoreState,
                    onUpgradeClick = { startPayment() },
                    onRestoreClick = { paymentId -> restorePurchase(paymentId) },
                    onBack = { finish() }
                )
            }
        }
    }

    private fun startPayment() {
        if (isPurchasing) return
        isPurchasing = true

        val checkout = Checkout()
        checkout.setKeyID(BuildConfig.RAZORPAY_KEY_ID)

        try {
            val options = JSONObject().apply {
                put("name", "Flashy")
                put("description", "Premium – Lifetime Access")
                put("currency", PREMIUM_CURRENCY)
                put("amount", PREMIUM_PRICE_PAISE)
                put("send_sms_hash", true)
                put("prefill", JSONObject().apply {
                    put("email", "")
                    put("contact", "")
                })
                put("theme", JSONObject().apply {
                    put("color", "#FFB300")
                })
            }
            checkout.open(this, options)
        } catch (e: Exception) {
            isPurchasing = false
            Toast.makeText(this, "Could not start payment. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun restorePurchase(paymentId: String) {
        restoreState = RestoreState.Loading
        lifecycleScope.launch {
            restoreState = when (val result = PremiumManager.restorePurchase(this@PremiumActivity, paymentId)) {
                is RestoreResult.Success -> {
                    Toast.makeText(
                        this@PremiumActivity,
                        "Premium restored! All features are unlocked.",
                        Toast.LENGTH_LONG
                    ).show()
                    setResult(RESULT_OK)
                    finish()
                    RestoreState.Success
                }
                is RestoreResult.NotFound -> RestoreState.Error(
                    "Payment ID not found. Check the ID from your Razorpay SMS/email."
                )
                is RestoreResult.InvalidFormat -> RestoreState.Error(
                    "Invalid format. Payment IDs start with 'pay_' — e.g. pay_AbCd1234XyZw"
                )
                is RestoreResult.NetworkError -> RestoreState.Error(
                    "Network error. Check your connection and try again."
                )
            }
        }
    }

    // ── PaymentResultListener callbacks ──────────────────────────────────────

    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        isPurchasing = false
        if (razorpayPaymentID.isNullOrBlank()) {
            Toast.makeText(this, "Payment received but ID was empty. Contact support.", Toast.LENGTH_LONG).show()
            return
        }
        lifecycleScope.launch {
            PremiumManager.activate(this@PremiumActivity, razorpayPaymentID)
            Toast.makeText(
                this@PremiumActivity,
                "Welcome to Flashy Premium! All features are now unlocked.",
                Toast.LENGTH_LONG
            ).show()
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onPaymentError(code: Int, description: String?) {
        isPurchasing = false
        // code 2 = user dismissed the payment sheet — not an error worth showing
        if (code == 2) return
        val msg = when (code) {
            0 -> "Network error. Check your connection and try again."
            1 -> "Invalid payment configuration. Please contact support."
            else -> description ?: "Payment failed. Please try again."
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
