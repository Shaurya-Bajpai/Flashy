package com.dsb.flashy.notification

import android.app.Notification
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_CALL_CONTACTS
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_CALL_FILTER_MODE
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_APP_RULES
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_NOTIF_CONTACTS
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_NOTIF_FILTER_MODE
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_SMS_CONTACTS
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_SMS_FILTER_MODE
import com.dsb.flashy.datastore.flashDataStore
import com.dsb.flashy.model.toAppRuleList
import com.dsb.flashy.services.FlashCallService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class NotificationListener : NotificationListenerService() {

    // NotificationListenerService is a Service, so we manage the scope manually.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val pkg = sbn?.packageName ?: return
        if (pkg == applicationContext.packageName) return
        if (sbn.isOngoing) return

        val category = sbn.notification.category

        // Missed call notifications carry no useful action — skip them.
        if (category == Notification.CATEGORY_MISSED_CALL) return

        // The notification title is the sender's display name as stored in the
        // phone's contacts, e.g. "Mom", "John Smith", or a raw number.
        val senderName = sbn.notification.extras
            .getString(Notification.EXTRA_TITLE)?.trim() ?: ""

        scope.launch {
            val prefs = applicationContext.flashDataStore.data.first()

            when (category) {
                Notification.CATEGORY_CALL -> {
                    // In "all" mode CallStateListener fires the instant the phone state
                    // changes to RINGING — before any notification exists. Let it handle
                    // that path; we only take over here when the user has a contact
                    // whitelist active (so we can read the caller name from the notification).
                    val filterMode = prefs[FLASH_CALL_FILTER_MODE] ?: "all"
                    if (filterMode != "selected") return@launch

                    val contacts = prefs[FLASH_CALL_CONTACTS] ?: ""
                    if (!isAllowed(senderName, contacts)) {
                        Log.d("FlashNotif", "Call from \"$senderName\" not in whitelist — skipped")
                        return@launch
                    }
                    triggerFlash("CALL")
                }

                Notification.CATEGORY_MESSAGE -> {
                    val filterMode = prefs[FLASH_SMS_FILTER_MODE] ?: "all"
                    if (filterMode == "selected") {
                        val contacts = prefs[FLASH_SMS_CONTACTS] ?: ""
                        if (!isAllowed(senderName, contacts)) {
                            Log.d("FlashNotif", "SMS from \"$senderName\" not in whitelist — skipped")
                            return@launch
                        }
                    }
                    triggerFlash("SMS")
                }

                else -> {
                    // Per-app rule takes precedence over the global Apps filter.
                    // If a rule exists for this package, use its mode + contacts + speed/count.
                    // If no rule exists, fall back to the global Apps filter.
                    val appRules = (prefs[FLASH_APP_RULES] ?: "").toAppRuleList()
                    val appRule  = appRules.find { it.packageName == pkg }

                    if (appRule != null) {
                        if (appRule.filterMode == "blocked") {
                            Log.d("FlashNotif", "[$pkg] blocked by per-app rule — skipped")
                            return@launch
                        }
                        if (appRule.filterMode == "selected") {
                            if (!isAllowed(senderName, appRule.contacts)) {
                                Log.d("FlashNotif", "[$pkg] \"$senderName\" not in per-app whitelist — skipped")
                                return@launch
                            }
                        }
                        triggerFlash("NOTIF", appRule.flashCount, appRule.flashSpeedMs)
                    } else {
                        val filterMode = prefs[FLASH_NOTIF_FILTER_MODE] ?: "all"
                        if (filterMode == "selected") {
                            val contacts = prefs[FLASH_NOTIF_CONTACTS] ?: ""
                            if (!isAllowed(senderName, contacts)) {
                                Log.d("FlashNotif", "[$pkg] \"$senderName\" not in global Apps whitelist — skipped")
                                return@launch
                            }
                        }
                        triggerFlash("NOTIF")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Returns true if [senderName] matches any entry in [contactsCsv].
     * Matching is case-insensitive and substring-based: adding "John" will
     * match "John Smith" and "John Doe". If the whitelist is empty, no one
     * passes (an empty list means the feature is on but no contacts are set).
     */
    private fun isAllowed(senderName: String, contactsCsv: String): Boolean {
        if (contactsCsv.isBlank()) return false
        val whitelist = contactsCsv
            .split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
        val nameLower = senderName.lowercase()
        return whitelist.any { nameLower.contains(it) }
    }

    private fun triggerFlash(eventType: String, countOverride: Int = -1, speedOverride: Int = -1) {
        Log.d("FlashNotif", "Triggering flash — event: $eventType count=$countOverride speed=$speedOverride")
        val intent = Intent(this, FlashCallService::class.java).apply {
            putExtra("eventType", eventType)
            if (countOverride >= 0) putExtra("flashCountOverride", countOverride)
            if (speedOverride >= 0) putExtra("flashSpeedOverride", speedOverride)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
