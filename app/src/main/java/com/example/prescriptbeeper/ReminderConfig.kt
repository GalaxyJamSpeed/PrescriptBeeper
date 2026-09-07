package com.example.prescriptbeeper

import android.content.Context

object ReminderConfig {

    private const val PREFS_NAME = "prescript_prefs"
    private const val KEY = "reminders_list"
    private const val ENTRY_SEP = "~~~"
    private const val FIELD_SEP = "|||"

    data class Reminder(
        val id: String,
        val hour: Int,
        val minute: Int,
        val text: String,
        val enabled: Boolean
    )

    fun getReminders(context: Context): List<Reminder> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(ENTRY_SEP).mapNotNull { entry ->
            val parts = entry.split(FIELD_SEP)
            if (parts.size != 5) return@mapNotNull null
            Reminder(parts[0], parts[1].toIntOrNull() ?: return@mapNotNull null,
                parts[2].toIntOrNull() ?: return@mapNotNull null, parts[3], parts[4] == "1")
        }
    }

    fun addReminder(context: Context, hour: Int, minute: Int, text: String): Reminder {
        val id = "R${System.currentTimeMillis()}"
        val reminder = Reminder(id, hour, minute, text, true)
        val current = getReminders(context).toMutableList()
        current.add(reminder)
        saveAll(context, current)
        return reminder
    }

    fun setEnabled(context: Context, id: String, enabled: Boolean) {
        val current = getReminders(context).map {
            if (it.id == id) it.copy(enabled = enabled) else it
        }
        saveAll(context, current)
    }

    fun deleteReminder(context: Context, id: String) {
        val current = getReminders(context).filter { it.id != id }
        saveAll(context, current)
    }

    fun getById(context: Context, id: String): Reminder? =
        getReminders(context).find { it.id == id }

    private fun saveAll(context: Context, list: List<Reminder>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = list.joinToString(ENTRY_SEP) {
            listOf(it.id, it.hour, it.minute, it.text, if (it.enabled) "1" else "0").joinToString(FIELD_SEP)
        }
        prefs.edit().putString(KEY, raw).apply()
    }
}