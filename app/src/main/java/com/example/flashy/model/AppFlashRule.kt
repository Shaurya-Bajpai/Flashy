package com.dsb.flashy.model

import org.json.JSONArray
import org.json.JSONObject

data class AppFlashRule(
    val packageName: String,
    val appName: String,
    val filterMode: String = "all",
    val contacts: String = ""
) {
    companion object {
        fun fromJson(obj: JSONObject) = AppFlashRule(
            packageName = obj.getString("pkg"),
            appName     = obj.getString("name"),
            filterMode  = obj.optString("mode", "all"),
            contacts    = obj.optString("contacts", "")
        )
    }

    fun toJson(): JSONObject = JSONObject().apply {
        put("pkg",      packageName)
        put("name",     appName)
        put("mode",     filterMode)
        put("contacts", contacts)
    }
}

fun List<AppFlashRule>.toJsonString(): String =
    JSONArray(map { it.toJson() }).toString()

fun String.toAppRuleList(): List<AppFlashRule> {
    if (isBlank()) return emptyList()
    return try {
        val arr = JSONArray(this)
        (0 until arr.length()).map { AppFlashRule.fromJson(arr.getJSONObject(it)) }
    } catch (_: Exception) {
        emptyList()
    }
}
