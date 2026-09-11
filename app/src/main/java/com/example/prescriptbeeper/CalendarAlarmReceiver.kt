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
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

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
            val todayDateStr = SimpleDateFormat("yyyyMMdd", Locale.US).format(Calendar.getInstance().time)

            val startOfWindow = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val endOfWindow = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 2)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }

            val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
            ContentUris.appendId(builder, startOfWindow.timeInMillis)
            ContentUris.appendId(builder, endOfWindow.timeInMillis)

            val projection = arrayOf(
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.ALL_DAY
            )
            val cursor = context.contentResolver.query(builder.build(), projection, null, null, null)

            var count = 0
            cursor?.use {
                val beginIdx = it.getColumnIndex(CalendarContract.Instances.BEGIN)
                val allDayIdx = it.getColumnIndex(CalendarContract.Instances.ALL_DAY)

                while (it.moveToNext()) {
                    val begin = it.getLong(beginIdx)
                    val isAllDay = it.getInt(allDayIdx) != 0

                    val eventDateStr = if (isAllDay) {
                        val utcFormat = SimpleDateFormat("yyyyMMdd", Locale.US).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }
                        utcFormat.format(java.util.Date(begin))
                    } else {
                        SimpleDateFormat("yyyyMMdd", Locale.US).format(java.util.Date(begin))
                    }

                    if (eventDateStr == todayDateStr) count++
                }
            }
            return count
        }
    }
}