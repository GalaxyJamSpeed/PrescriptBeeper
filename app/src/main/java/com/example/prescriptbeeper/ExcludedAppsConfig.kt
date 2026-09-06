package com.example.prescriptbeeper

import android.content.Context

object ExcludedAppsConfig {
    private const val PREFS_NAME = "prescript_prefs"
    private const val KEY = "excluded_apps"
    private const val SEP = "~~~"

    fun getExcludedApps(context: Context): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY, "") ?: ""
        if (raw.isBlank()) return emptySet()
        return raw.split(SEP).filter { it.isNotBlank() }.toSet()
    }

    fun setExcluded(context: Context, pkg: String, excluded: Boolean) {
        val current = getExcludedApps(context).toMutableSet()
        if (excluded) current.add(pkg) else current.remove(pkg)
        saveSet(context, current)
    }

    private fun saveSet(context: Context, set: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY, set.joinToString(SEP)).apply()
    }
}