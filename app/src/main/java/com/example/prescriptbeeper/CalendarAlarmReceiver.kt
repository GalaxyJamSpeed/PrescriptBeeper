package com.example.prescriptbeeper

import android.Manifest
import android.content.BroadcastReceiver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.util.Calendar

class CalendarAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        checkAndFire(context)
        CalendarScheduler.scheduleDailyCheck(context)
    }

    companion object {
        fun checkAndFire(context: Context) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context, Manifest.permission.READ_CALENDAR
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasPermission) return

            val count = countTodaysEvents(context)
            if (count > 0) {
                val baseLine = PrescriptLines.getLine(context, "CALENDAR")
                PrescriptTrigger.fire(
                    context, "CALENDAR",
                    overrideText = "$baseLine ($count)",
                    notificationContent = "$count event(s) scheduled today"
                )
            }
        }

        private fun countTodaysEvents(context: Context): Int {
            val startOfDay = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val endOfDay = (startOfDay.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, 1)
            }

            val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
            ContentUris.appendId(builder, startOfDay.timeInMillis)
            ContentUris.appendId(builder, endOfDay.timeInMillis)

            val projection = arrayOf(CalendarContract.Instances.EVENT_ID)
            val cursor = context.contentResolver.query(builder.build(), projection, null, null, null)

            var count = 0
            cursor?.use { count = it.count }
            return count
        }
    }
}