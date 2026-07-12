package com.dsb.flashy

import com.dsb.flashy.model.AppFlashRule
import com.dsb.flashy.model.toAppRuleList
import com.dsb.flashy.model.toJsonString
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the per-app rule model after removing per-app contact filtering.
 * Presence in the list is now the only signal an app is selected to flash —
 * no "mode" or "contacts" field exists on this model anymore.
 */
class AppFlashRuleTest {

    @Test
    fun roundTripsThroughJson() {
        val rule = AppFlashRule(packageName = "com.whatsapp", appName = "WhatsApp", flashCount = 10, flashSpeedMs = 100)
        val restored = listOf(rule).toJsonString().toAppRuleList()
        assertEquals(listOf(rule), restored)
    }

    @Test
    fun missingCountAndSpeed_defaultTo5And200() {
        val obj = JSONObject().apply {
            put("pkg", "com.instagram.android")
            put("name", "Instagram")
        }
        val rule = AppFlashRule.fromJson(obj)
        assertEquals(5, rule.flashCount)
        assertEquals(200, rule.flashSpeedMs)
    }

    @Test
    fun legacyJsonWithModeAndContacts_parsesAndIgnoresThem() {
        // Rules saved before per-app contact filtering was removed had "mode"
        // and "contacts" keys — old saved data must still load cleanly.
        val legacyJson = """
            [{"pkg":"com.whatsapp","name":"WhatsApp","mode":"selected","contacts":"Mom,Dad","count":3,"speed":400}]
        """.trimIndent()
        val rules = legacyJson.toAppRuleList()
        assertEquals(1, rules.size)
        assertEquals("com.whatsapp", rules[0].packageName)
        assertEquals(3, rules[0].flashCount)
        assertEquals(400, rules[0].flashSpeedMs)
    }

    @Test
    fun blankJson_producesEmptyList() {
        assertTrue("".toAppRuleList().isEmpty())
    }

    @Test
    fun malformedJson_producesEmptyListInsteadOfCrashing() {
        assertTrue("{not valid json".toAppRuleList().isEmpty())
    }

    @Test
    fun serializedJson_containsNoModeOrContactsKeys() {
        val json = listOf(AppFlashRule(packageName = "com.whatsapp", appName = "WhatsApp")).toJsonString()
        assertTrue("mode" !in json)
        assertTrue("contacts" !in json)
    }
}
