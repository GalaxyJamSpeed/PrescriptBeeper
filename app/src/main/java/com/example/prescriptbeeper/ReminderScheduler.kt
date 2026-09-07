package com.example.prescriptbeeper

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object ReminderScheduler {

    fun schedule(context: Context, reminder: ReminderConfig.Reminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            putExtra("REMINDER_ID", reminder.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, reminder.id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, reminder.hour)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }

    fun cancel(context: Context, reminder: ReminderConfig.Reminder) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, reminder.id.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun rescheduleAll(context: Context) {
        for (reminder in ReminderConfig.getReminders(context)) {
            if (reminder.enabled) schedule(context, reminder)
        }
    }
}