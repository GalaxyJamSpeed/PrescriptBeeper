package com.example.prescriptbeeper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getStringExtra("REMINDER_ID") ?: return
        val reminder = ReminderConfig.getById(context, id) ?: return

        if (reminder.enabled) {
            PrescriptTrigger.fire(context, "REMINDER", overrideText = reminder.text)
            ReminderScheduler.schedule(context, reminder)
        }
    }
}