package com.example.prescriptbeeper

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context

object ForegroundAppDetector {

    fun getCurrentForegroundApp(context: Context): String? {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager ?: return null

        val endTime = System.currentTimeMillis()
        val beginTime = endTime - (24 * 60 * 60 * 1000L)

        val events = usageStatsManager.queryEvents(beginTime, endTime)
        var lastForegroundPackage: String? = null
        val event = UsageEvents.Event()

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastForegroundPackage = event.packageName
            }
        }
        return lastForegroundPackage
    }
}