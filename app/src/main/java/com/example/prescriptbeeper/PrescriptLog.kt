package com.example.prescriptbeeper

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PrescriptLog {

    private const val PREFS_NAME = "prescript_prefs"
    private const val LOG_KEY = "prescript_log"
    private const val MAX_ENTRIES = 20
    private const val ENTRY_SEPARATOR = "~~~"
    private const val FIELD_SEPARATOR = "|||"

    data class LogEntry(
        val timestamp: String,
        val appName: String,
        val text: String,
        val status: String,
        val content: String
    )

    fun addEntry(context: Context, appName: String, text: String, content: String = ""): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val timestamp = SimpleDateFormat("MMM d, h:mm a", Locale.US).format(Date())
        val id = System.currentTimeMillis().toString()

        val safeContent = content.replace(FIELD_SEPARATOR, "").replace(ENTRY_SEPARATOR, "")
        val newEntry = listOf(id, timestamp, appName, text, "PENDING", safeContent).joinToString(FIELD_SEPARATOR)

        val existing = prefs.getString(LOG_KEY, "") ?: ""
        val entries = if (existing.isBlank()) mutableListOf() else existing.split(ENTRY_SEPARATOR).toMutableList()

        entries.add(0, newEntry)
        while (entries.size > MAX_ENTRIES) entries.removeAt(entries.size - 1)

        prefs.edit().putString(LOG_KEY, entries.joinToString(ENTRY_SEPARATOR)).apply()
        return id
    }

    fun updateStatus(context: Context, id: String, status: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getString(LOG_KEY, "") ?: return
        if (existing.isBlank()) return

        val entries = existing.split(ENTRY_SEPARATOR).toMutableList()
        for (i in entries.indices) {
            val parts = entries[i].split(FIELD_SEPARATOR)
            if (parts.isNotEmpty() && parts[0] == id) {
                val content = if (parts.size >= 6) parts[5] else ""
                entries[i] = listOf(parts[0], parts[1], parts[2], parts[3], status, content).joinToString(FIELD_SEPARATOR)
                break
            }
        }
        prefs.edit().putString(LOG_KEY, entries.joinToString(ENTRY_SEPARATOR)).apply()
    }

    fun getEntries(context: Context): List<LogEntry> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getString(LOG_KEY, "") ?: return emptyList()
        if (existing.isBlank()) return emptyList()

        return existing.split(ENTRY_SEPARATOR).mapNotNull { raw ->
            val parts = raw.split(FIELD_SEPARATOR)
            when {
                parts.size >= 6 -> LogEntry(parts[1], parts[2], parts[3], parts[4], parts[5])
                parts.size == 5 -> LogEntry(parts[1], parts[2], parts[3], parts[4], "")
                else -> null
            }
        }
    }
}