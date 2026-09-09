package com.example.prescriptbeeper

import android.content.Context

object FeatureToggles {
    private const val PREFS_NAME = "prescript_prefs"

    fun isBatteryLowEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("feature_battery_low_enabled", true)

    fun isBatteryRestoredEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("feature_battery_restored_enabled", true)

    fun isEarbudsLowEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("feature_earbuds_low_enabled", true)

    fun setEnabled(context: Context, key: String, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putBoolean(key, enabled).apply()
    }
}