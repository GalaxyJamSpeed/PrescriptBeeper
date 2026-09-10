package com.example.prescriptbeeper

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object WeeklyStats {

    private const val PREFS_NAME = "prescript_prefs"
    private const val STATS_KEY = "weekly_stats"
    private const val ENTRY_SEPARATOR = "~~~"
    private const val KV_SEPARATOR = "="
    private const val DATE_CATEGORY_SEPARATOR = "|"
    private const val RETENTION_DAYS = 400

    private val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)

    data class RangeSummary(
        val total: Int,
        val busiestDayLabel: String,
        val busiestDayCount: Int,
        val topCategoryLabel: String,
        val topCategoryCount: Int,
        val byCategory: Map<String, Int>,
        val dailyTotals: List<Pair<String, Int>>
    )

    data class AllTimeSummary(
        val totalCompleted: Int,
        val totalDismissed: Int,
        val busiestDayLabel: String,
        val busiestDayCount: Int,
        val topCategoryLabel: String,
        val topCategoryCount: Int,
        val byCategory: Map<String, Int>
    )

    fun weekdayInitial(dateKey: String): String {
        return try {
            when (parseKey(dateKey).get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "S"
                Calendar.MONDAY -> "M"
                Calendar.TUESDAY -> "T"
                Calendar.WEDNESDAY -> "W"
                Calendar.THURSDAY -> "T"
                Calendar.FRIDAY -> "F"
                Calendar.SATURDAY -> "S"
                else -> ""
            }
        } catch (e: Exception) { "" }
    }

    fun recordAccept(context: Context, category: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = dateFormat.format(Date())
        val map = loadMap(prefs).toMutableMap()

        val key = "$today$DATE_CATEGORY_SEPARATOR$category"
        map[key] = (map[key] ?: 0) + 1

        pruneOldEntries(map)
        saveMap(prefs, map)

        prefs.edit().putInt("lifetime_completed_total", prefs.getInt("lifetime_completed_total", 0) + 1).apply()
    }

    fun getSummaryForRange(context: Context, startKey: String, endKeyInclusive: String): RangeSummary {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val map = loadMap(prefs)

        val byDay = mutableMapOf<String, Int>()
        val byCategory = mutableMapOf<String, Int>()
        var total = 0

        for ((key, count) in map) {
            val parts = key.split(DATE_CATEGORY_SEPARATOR)
            if (parts.size != 2) continue
            val date = parts[0]
            val category = parts[1]
            if (date < startKey || date > endKeyInclusive) continue

            total += count
            byDay[date] = (byDay[date] ?: 0) + count
            byCategory[category] = (byCategory[category] ?: 0) + count
        }

        val busiestDay = byDay.maxByOrNull { it.value }
        val topCategory = byCategory.maxByOrNull { it.value }
        val dailyTotals = datesBetween(startKey, endKeyInclusive).map { it to (byDay[it] ?: 0) }

        return RangeSummary(
            total = total,
            busiestDayLabel = busiestDay?.let { formatDayLabel(it.key) } ?: "—",
            busiestDayCount = busiestDay?.value ?: 0,
            topCategoryLabel = topCategory?.let { friendlyCategoryName(it.key) } ?: "—",
            topCategoryCount = topCategory?.value ?: 0,
            byCategory = byCategory.mapKeys { friendlyCategoryName(it.key) },
            dailyTotals = dailyTotals
        )
    }

    fun friendlyCategoryName(category: String): String = when (category) {
        "COMMUNICATION" -> "Messages/Calls"
        "BATTERY_LOW" -> "Battery Low"
        "BATTERY_OK" -> "Battery Restored"
        "EARBUDS_LOW" -> "Earbuds"
        "CALENDAR" -> "Calendar"
        "REMINDER" -> "Reminders"
        "CUSTOM" -> "Custom"
        else -> category
    }

    fun formatDayLabel(dateKey: String): String = try {
        SimpleDateFormat("EEEE, MMM d", Locale.US).format(dateFormat.parse(dateKey)!!)
    } catch (e: Exception) { dateKey }

    fun shortDayLabel(dateKey: String): String = try {
        SimpleDateFormat("d", Locale.US).format(dateFormat.parse(dateKey)!!)
    } catch (e: Exception) { dateKey }

    fun chartDayLabel(dateKey: String): String {
        return try {
            val cal = parseKey(dateKey)
            val dayNum = SimpleDateFormat("d", Locale.US).format(cal.time)
            val initial = when (cal.get(Calendar.DAY_OF_WEEK)) {
                Calendar.SUNDAY -> "S"
                Calendar.MONDAY -> "M"
                Calendar.TUESDAY -> "T"
                Calendar.WEDNESDAY -> "W"
                Calendar.THURSDAY -> "T"
                Calendar.FRIDAY -> "F"
                Calendar.SATURDAY -> "S"
                else -> ""
            }
            "$dayNum—$initial"
        } catch (e: Exception) { dateKey }
    }

    fun isWeekend(dateKey: String): Boolean {
        val dow = parseKey(dateKey).get(Calendar.DAY_OF_WEEK)
        return dow == Calendar.SATURDAY || dow == Calendar.SUNDAY
    }

    fun keyFor(cal: Calendar): String = dateFormat.format(cal.time)

    fun parseKey(key: String): Calendar {
        val cal = Calendar.getInstance()
        cal.time = dateFormat.parse(key) ?: Date()
        return cal
    }

    private fun datesBetween(startKey: String, endKeyInclusive: String): List<String> {
        val start = parseKey(startKey)
        val end = parseKey(endKeyInclusive)
        val result = mutableListOf<String>()
        val cursor = start.clone() as Calendar
        while (!cursor.after(end)) {
            result.add(keyFor(cursor))
            cursor.add(Calendar.DAY_OF_YEAR, 1)
        }
        return result
    }

    private fun cutoffDate(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -(RETENTION_DAYS - 1))
        return dateFormat.format(cal.time)
    }

    private fun pruneOldEntries(map: MutableMap<String, Int>) {
        val cutoff = cutoffDate()
        val iterator = map.entries.iterator()
        while (iterator.hasNext()) {
            val date = iterator.next().key.split(DATE_CATEGORY_SEPARATOR).getOrNull(0) ?: continue
            if (date < cutoff) iterator.remove()
        }
    }

    private fun loadMap(prefs: SharedPreferences): Map<String, Int> {
        val raw = prefs.getString(STATS_KEY, "") ?: ""
        if (raw.isBlank()) return emptyMap()
        return raw.split(ENTRY_SEPARATOR).mapNotNull { entry ->
            val idx = entry.lastIndexOf(KV_SEPARATOR)
            if (idx == -1) return@mapNotNull null
            val key = entry.substring(0, idx)
            val value = entry.substring(idx + 1).toIntOrNull() ?: return@mapNotNull null
            key to value
        }.toMap()
    }

    private fun saveMap(prefs: SharedPreferences, map: Map<String, Int>) {
        val raw = map.entries.joinToString(ENTRY_SEPARATOR) { "${it.key}$KV_SEPARATOR${it.value}" }
        prefs.edit().putString(STATS_KEY, raw).apply()
    }

    private const val KARMIC_STATS_KEY = "karmic_daily_stats"

    fun recordKarmic(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = dateFormat.format(Date())
        val map = loadKarmicMap(prefs).toMutableMap()
        map[today] = (map[today] ?: 0) + 1
        pruneOldKarmicEntries(map)
        saveKarmicMap(prefs, map)

        prefs.edit().putInt("lifetime_karmic_total", prefs.getInt("lifetime_karmic_total", 0) + 1).apply()
    }

    fun getKarmicTotalForRange(context: Context, startKey: String, endKeyInclusive: String): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val map = loadKarmicMap(prefs)
        var total = 0
        for ((date, count) in map) {
            if (date < startKey || date > endKeyInclusive) continue
            total += count
        }
        return total
    }

    private fun pruneOldKarmicEntries(map: MutableMap<String, Int>) {
        val cutoff = cutoffDate()
        val iterator = map.entries.iterator()
        while (iterator.hasNext()) {
            if (iterator.next().key < cutoff) iterator.remove()
        }
    }

    private fun loadKarmicMap(prefs: SharedPreferences): Map<String, Int> {
        val raw = prefs.getString(KARMIC_STATS_KEY, "") ?: ""
        if (raw.isBlank()) return emptyMap()
        return raw.split(ENTRY_SEPARATOR).mapNotNull { entry ->
            val idx = entry.lastIndexOf(KV_SEPARATOR)
            if (idx == -1) return@mapNotNull null
            val key = entry.substring(0, idx)
            val value = entry.substring(idx + 1).toIntOrNull() ?: return@mapNotNull null
            key to value
        }.toMap()
    }

    private fun saveKarmicMap(prefs: SharedPreferences, map: Map<String, Int>) {
        val raw = map.entries.joinToString(ENTRY_SEPARATOR) { "${it.key}$KV_SEPARATOR${it.value}" }
        prefs.edit().putString(KARMIC_STATS_KEY, raw).apply()
    }

    fun getAllTimeSummary(context: Context): AllTimeSummary {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        migrateLifetimeTotalsIfNeeded(prefs)
        val todayKey = dateFormat.format(Date())
        val recent = getSummaryForRange(context, cutoffDate(), todayKey)

        return AllTimeSummary(
            totalCompleted = prefs.getInt("lifetime_completed_total", 0),
            totalDismissed = prefs.getInt("lifetime_karmic_total", 0),
            busiestDayLabel = recent.busiestDayLabel,
            busiestDayCount = recent.busiestDayCount,
            topCategoryLabel = recent.topCategoryLabel,
            topCategoryCount = recent.topCategoryCount,
            byCategory = recent.byCategory
        )
    }

    private fun migrateLifetimeTotalsIfNeeded(prefs: SharedPreferences) {
        if (prefs.getBoolean("lifetime_stats_migrated", false)) return

        val completedSum = loadMap(prefs).values.sum()
        val karmicSum = loadKarmicMap(prefs).values.sum()

        val currentCompleted = prefs.getInt("lifetime_completed_total", 0)
        val currentKarmic = prefs.getInt("lifetime_karmic_total", 0)

        prefs.edit()
            .putInt("lifetime_completed_total", currentCompleted + completedSum)
            .putInt("lifetime_karmic_total", currentKarmic + karmicSum)
            .putBoolean("lifetime_stats_migrated", true)
            .apply()
    }
}