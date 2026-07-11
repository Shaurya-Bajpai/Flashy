package com.dsb.flashy.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.datastore.preferences.core.edit
import com.dsb.flashy.R
import com.dsb.flashy.datastore.GlobalSettingsStore.FLASH_GLOBAL
import com.dsb.flashy.datastore.flashDataStore
import com.dsb.flashy.util.updateFlashShortcut
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class FlashToggleWidget : AppWidgetProvider() {

    // Called when the widget is first placed and whenever the system needs a refresh.
    // updatePeriodMillis = 0, so this only fires on placement and manual refresh.
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val enabled = context.flashDataStore.data
                .map { it[FLASH_GLOBAL] ?: true }
                .first()
            appWidgetIds.forEach { id ->
                applyViews(context, appWidgetManager, id, enabled)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_TOGGLE) {
            val async = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val current  = context.flashDataStore.data.map { it[FLASH_GLOBAL] ?: true }.first()
                    val newValue = !current
                    context.flashDataStore.edit { it[FLASH_GLOBAL] = newValue }
                    // Keep the long-press shortcut label in sync too
                    updateFlashShortcut(context, newValue)
                    // Redraw every placed instance of this widget
                    refreshAll(context, newValue)
                } finally {
                    async.finish()
                }
            }
        }
    }

    companion object {
        const val ACTION_TOGGLE = "com.dsb.flashy.widget.ACTION_TOGGLE_FLASH"

        /**
         * Redraws all placed instances of this widget to reflect [flashEnabled].
         * Call this from any code path that changes FLASH_GLOBAL so the widget
         * stays in sync (shortcut, in-app toggle, widget tap).
         */
        fun refreshAll(context: Context, flashEnabled: Boolean) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, FlashToggleWidget::class.java)
            )
            ids.forEach { id -> applyViews(context, manager, id, flashEnabled) }
        }

        private fun applyViews(
            context: Context,
            manager: AppWidgetManager,
            widgetId: Int,
            flashEnabled: Boolean
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_flash_toggle)

            // Icon swaps between amber bolt (ON) and grey slashed bolt (OFF)
            views.setImageViewResource(
                R.id.widget_icon,
                if (flashEnabled) R.drawable.ic_shortcut_flash_on
                else R.drawable.ic_shortcut_flash_off
            )

            // State label below the icon
            views.setTextViewText(R.id.widget_state, if (flashEnabled) "ON" else "OFF")

            // Tap anywhere on the widget → broadcast ACTION_TOGGLE back to this provider
            val pi = PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, FlashToggleWidget::class.java).apply { action = ACTION_TOGGLE },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pi)

            manager.updateAppWidget(widgetId, views)
        }
    }
}
