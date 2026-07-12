package com.dsb.flashy.util

import android.content.Context
import android.content.pm.PackageManager

// A single entry in the app picker for per-app flash rules.
data class InstalledApp(val packageName: String, val name: String)

/**
 * Builds the app list for the per-app flash-rule picker: every app the user
 * can open from their home screen or app drawer — pre-installed/system apps
 * AND apps installed from the Play Store alike — minus Flashy itself.
 *
 * On Android 11+ (API 30+) `PackageManager.getInstalledApplications()` only
 * returns packages the app has "visibility" into. Without the
 * `<queries><intent>` MAIN/LAUNCHER declaration in AndroidManifest.xml, that
 * visibility is limited to system-exempt packages, which is why third-party
 * apps like WhatsApp or Instagram wouldn't show up here otherwise.
 */
object InstalledAppsProvider {

    fun listLaunchableApps(context: Context): List<InstalledApp> {
        val pm = context.packageManager
        val byPackage = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .associateBy { it.packageName }

        return buildAppList(
            packageNames = byPackage.keys.toList(),
            selfPackage = context.packageName,
            hasLaunchIntent = { pm.getLaunchIntentForPackage(it) != null },
            labelOf = { pkg -> pm.getApplicationLabel(byPackage.getValue(pkg)).toString() },
        )
    }

    // Pure core: no PackageManager/Context involved, so it's unit-testable
    // without Robolectric or an instrumented device.
    internal fun buildAppList(
        packageNames: List<String>,
        selfPackage: String,
        hasLaunchIntent: (String) -> Boolean,
        labelOf: (String) -> String,
    ): List<InstalledApp> {
        return packageNames
            .asSequence()
            .distinct()
            .filter { it != selfPackage }
            .filter(hasLaunchIntent)
            .map { InstalledApp(it, labelOf(it)) }
            .sortedBy { it.name.lowercase() }
            .toList()
    }
}
