package com.example.prescriptbeeper

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock

object PrescriptTrigger {

    private var lastFiredAt = 0L
    private const val COOLDOWN_MS = 4000L

    fun fire(
        context: Context,
        category: String,
        overrideText: String? = null,
        sourcePackage: String? = null,
        notificationContent: String? = null,
        senderName: String? = null,
        isTest: Boolean = false
    ) {
        val prefs = context.getSharedPreferences("prescript_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("prescript_enabled", true)) return

        val now = SystemClock.elapsedRealtime()
        if (now - lastFiredAt < COOLDOWN_MS) return
        lastFiredAt = now

        val text = overrideText ?: PrescriptLines.getLine(context, category)

        val serviceIntent = Intent(context, PrescriptOverlayService::class.java).apply {
            putExtra("PRESCRIPT_TEXT", text)
            putExtra("SOURCE_PACKAGE", sourcePackage)
            putExtra("NOTIFICATION_CONTENT", notificationContent)
            putExtra("CATEGORY", category)
            putExtra("SENDER_NAME", senderName)
            putExtra("IS_TEST", isTest)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}