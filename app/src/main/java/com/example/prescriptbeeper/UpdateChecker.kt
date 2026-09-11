package com.example.prescriptbeeper

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

object UpdateChecker {

    private const val CURRENT_VERSION = "2.5.2"
    private const val REPO_API_URL = "https://api.github.com/repos/GalaxyJamSpeed/PrescriptBeeper/releases/latest"
    private const val CHECK_INTERVAL_MS = 24 * 60 * 60 * 1000L

    data class UpdateInfo(val version: String, val url: String)

    fun checkForUpdate(context: Context, onResult: (UpdateInfo?) -> Unit) {
        val prefs = context.getSharedPreferences("prescript_prefs", Context.MODE_PRIVATE)
        val lastCheck = prefs.getLong("last_update_check", 0L)
        val now = System.currentTimeMillis()

        if (now - lastCheck < CHECK_INTERVAL_MS) {
            onResult(null)
            return
        }

        thread {
            try {
                val connection = URL(REPO_API_URL).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github+json")
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode
                if (responseCode != 200) {
                    onResult(null)
                    return@thread
                }

                val text = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(text)
                val tagName = json.getString("tag_name").removePrefix("v")
                val htmlUrl = json.getString("html_url")

                prefs.edit().putLong("last_update_check", now).apply()

                if (isNewerVersion(tagName, CURRENT_VERSION)) {
                    onResult(UpdateInfo(tagName, htmlUrl))
                } else {
                    onResult(null)
                }
            } catch (e: Exception) {
                onResult(null)
            }
        }
    }

    private fun isNewerVersion(remote: String, current: String): Boolean {
        val remoteParts = remote.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val maxLen = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r != c) return r > c
        }
        return false
    }
}