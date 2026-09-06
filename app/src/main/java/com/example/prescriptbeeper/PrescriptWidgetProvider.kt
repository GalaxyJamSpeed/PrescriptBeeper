package com.example.prescriptbeeper

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrescriptWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) {
            updateWidget(context, appWidgetManager, id)
        }
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, PrescriptWidgetProvider::class.java))
            for (id in ids) {
                updateWidget(context, manager, id)
            }
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, widgetId: Int) {
            val prefs = context.getSharedPreferences("prescript_prefs", Context.MODE_PRIVATE)

            val today = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
            val lastDate = prefs.getString("last_reset_date", null)
            val count = if (lastDate != today) 0 else prefs.getInt("prescripts_completed", 0)

            val (stage, icon) = when {
                count >= 20 -> 3 to R.drawable.icunlock3
                count >= 10 -> 2 to R.drawable.icunlock2
                else -> 1 to R.drawable.icunlock1
            }

            val views = RemoteViews(context.packageName, R.layout.widget_prescript)
            views.setTextViewText(R.id.widgetCountText, "$count")
            views.setTextViewText(R.id.widgetLabelText, "STAGE $stage")
            views.setImageViewResource(R.id.widgetStageIcon, icon)

            val launchIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent)

            manager.updateAppWidget(widgetId, views)
        }
    }
}