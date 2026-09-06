package com.example.prescriptbeeper

import android.content.Context

object PrescriptLines {

    private const val PREFS_NAME = "prescript_prefs"
    private const val CUSTOM_PREFIX = "custom_lines_"
    private const val SEPARATOR = "~~~"

    val editableCategories = listOf("COMMUNICATION", "BATTERY_LOW", "BATTERY_OK", "EARBUDS_LOW", "CALENDAR")

    private val defaultLines = mapOf(
        "COMMUNICATION" to listOf(
            "A VOICE REACHES OUT. ANSWER OR LET IT FADE.",
            "SOMEONE CALLED. THEY WERE NOT HEARD.",
            "THE CHORUS GROWS LOUD. SILENCE IT OR JOIN IT."
        ),
        "BATTERY_LOW" to listOf(
            "THE VESSEL WEAKENS.",
            "STRENGTH FADES. SEEK A SOURCE."
        ),
        "BATTERY_OK" to listOf(
            "STRENGTH RESTORED."
        ),
        "EARBUDS_LOW" to listOf(
            "THE COMPANION'S STRENGTH FADES.",
            "THE COMPANION GROWS FAINT.",
            "A SILENCE APPROACHES THE COMPANION."
        ),
        "CALENDAR" to listOf(
            "A GATHERING AWAITS YOU TODAY.",
            "THE COUNCIL CONVENES TODAY.",
            "THE DAY DEMANDS YOUR PRESENCE."
        ),
        "GENERIC" to listOf(
            "SOMETHING STIRS."
        )
    )

    fun getLine(context: Context, category: String): String {
        val defaults = defaultLines[category] ?: defaultLines.getValue("GENERIC")
        val customs = getCustomLines(context, category)
        return (defaults + customs).random()
    }

    fun getCustomLines(context: Context, category: String): List<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(CUSTOM_PREFIX + category, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(SEPARATOR).filter { it.isNotBlank() }
    }

    fun addCustomLine(context: Context, category: String, line: String) {
        if (line.isBlank()) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = getCustomLines(context, category).toMutableList()
        current.add(line.trim())
        prefs.edit().putString(CUSTOM_PREFIX + category, current.joinToString(SEPARATOR)).apply()
    }

    fun removeCustomLine(context: Context, category: String, line: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = getCustomLines(context, category).toMutableList()
        current.remove(line)
        prefs.edit().putString(CUSTOM_PREFIX + category, current.joinToString(SEPARATOR)).apply()
    }
}