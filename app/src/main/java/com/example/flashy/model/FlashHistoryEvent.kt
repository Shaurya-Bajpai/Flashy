package com.dsb.flashy.model

import org.json.JSONArray
import org.json.JSONObject

data class FlashHistoryEvent(
    val eventType: String,   // "CALL" | "SMS" | "NOTIF"
    val senderName: String,
    val appPackage: String,
    val appName: String,
    val timestampMs: Long
) {
    companion object {
        fun fromJson(obj: JSONObject) = FlashHistoryEvent(
            eventType   = obj.getString("type"),
            senderName  = obj.optString("sender", ""),
            appPackage  = obj.optString("pkg", ""),
            appName     = obj.optString("app", ""),
            timestampMs = obj.optLong("ts", 0L)
        )
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("type", eventType)
        put("sender", senderName)
        put("pkg", appPackage)
        put("app", appName)
        put("ts", timestampMs)
    }
}

fun List<FlashHistoryEvent>.toHistoryJsonString(): String =
    JSONArray(map { it.toJson() }).toString()

fun String.toFlashHistoryList(): List<FlashHistoryEvent> {
    if (isBlank()) return emptyList()
    return try {
        val arr = JSONArray(this)
        (0 until arr.length()).map { FlashHistoryEvent.fromJson(arr.getJSONObject(it)) }
    } catch (_: Exception) { emptyList() }
}
