package com.example.prescriptbeeper

import android.content.Context

object WatchedAppsConfig {
    private const val PREFS_NAME = "prescript_prefs"
    private const val KEY = "watched_apps_config"
    private const val ENTRY_SEP = "~~~"
    private const val KV_SEP = "="

    fun getWatchedApps(context: Context): Map<String, String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY, "") ?: ""
        if (raw.isBlank()) return emptyMap()
        return raw.split(ENTRY_SEP).mapNotNull { entry ->
            val idx = entry.indexOf(KV_SEP)
            if (idx == -1) return@mapNotNull null
            entry.substring(0, idx) to entry.substring(idx + 1)
        }.toMap()
    }

    fun setCategoryForApp(context: Context, pkg: String, category: String) {
        val current = getWatchedApps(context).toMutableMap()
        current[pkg] = category
        saveMap(context, current)
    }

    fun removeApp(context: Context, pkg: String) {
        val current = getWatchedApps(context).toMutableMap()
        current.remove(pkg)
        saveMap(context, current)
    }

    private fun saveMap(context: Context, map: Map<String, String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = map.entries.joinToString(ENTRY_SEP) { "${it.key}$KV_SEP${it.value}" }
        prefs.edit().putString(KEY, raw).apply()
    }
}