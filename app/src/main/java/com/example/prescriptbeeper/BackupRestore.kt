package com.example.prescriptbeeper

import android.content.Context
import android.net.Uri

object BackupRestore {
    private const val PREFS_NAME = "prescript_prefs"

    fun exportToUri(context: Context, uri: Uri): Boolean {
        return try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val sb = StringBuilder()
            sb.append("PRESCRIPTBEEPER_BACKUP_V1\n")
            for ((key, value) in prefs.all) {
                when (value) {
                    is String -> sb.append("STRING\t$key\t${value.replace("\n", "\\n")}\n")
                    is Int -> sb.append("INT\t$key\t$value\n")
                    is Boolean -> sb.append("BOOL\t$key\t$value\n")
                    is Float -> sb.append("FLOAT\t$key\t$value\n")
                    is Long -> sb.append("LONG\t$key\t$value\n")
                    is Set<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        val setVal = value as Set<String>
                        sb.append("STRINGSET\t$key\t${setVal.joinToString("\u0001")}\n")
                    }
                }
            }
            context.contentResolver.openOutputStream(uri)?.use { out ->
                out.write(sb.toString().toByteArray())
            }
            true
        } catch (e: Exception) { false }
    }

    fun importFromUri(context: Context, uri: Uri): Boolean {
        return try {
            val text = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: return false
            val lines = text.split("\n")
            if (lines.isEmpty() || !lines[0].startsWith("PRESCRIPTBEEPER_BACKUP")) return false

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val editor = prefs.edit()
            editor.clear()

            for (i in 1 until lines.size) {
                val line = lines[i]
                if (line.isBlank()) continue
                val parts = line.split("\t", limit = 3)
                if (parts.size < 2) continue
                val type = parts[0]
                val key = parts[1]
                val rawValue = if (parts.size >= 3) parts[2] else ""
                when (type) {
                    "STRING" -> editor.putString(key, rawValue.replace("\\n", "\n"))
                    "INT" -> editor.putInt(key, rawValue.toIntOrNull() ?: 0)
                    "BOOL" -> editor.putBoolean(key, rawValue.toBoolean())
                    "FLOAT" -> editor.putFloat(key, rawValue.toFloatOrNull() ?: 0f)
                    "LONG" -> editor.putLong(key, rawValue.toLongOrNull() ?: 0L)
                    "STRINGSET" -> {
                        val setVal = if (rawValue.isEmpty()) emptySet() else rawValue.split("\u0001").toSet()
                        editor.putStringSet(key, setVal)
                    }
                }
            }
            editor.apply()
            true
        } catch (e: Exception) { false }
    }
}