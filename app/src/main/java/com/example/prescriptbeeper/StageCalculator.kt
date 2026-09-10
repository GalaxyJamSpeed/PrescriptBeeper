package com.example.prescriptbeeper

import android.content.SharedPreferences

object StageCalculator {
    private const val MIN_SAMPLE_STAGE_2 = 5
    private const val MIN_SAMPLE_STAGE_3 = 15
    private const val RATIO_STAGE_2 = 0.5f
    private const val RATIO_STAGE_3 = 0.75f

    fun computeAndAdvanceStage(prefs: SharedPreferences): Int {
        val completed = prefs.getInt("prescripts_completed", 0)
        val karmic = prefs.getInt("karmic_consequences", 0)
        val total = completed + karmic
        val ratio = if (total == 0) 0f else completed.toFloat() / total
        val currentStage = prefs.getInt("highest_stage_today", 1)

        var newStage = currentStage
        if (currentStage == 1 && total >= MIN_SAMPLE_STAGE_2 && ratio >= RATIO_STAGE_2) {
            newStage = 2
        } else if (currentStage == 2 && total >= MIN_SAMPLE_STAGE_3 && ratio >= RATIO_STAGE_3) {
            newStage = 3
        }
        if (newStage != currentStage) {
            prefs.edit().putInt("highest_stage_today", newStage).apply()
        }
        return newStage
    }

    fun getStageIcon(stage: Int): Int = when (stage) {
        3 -> R.drawable.icunlock3
        2 -> R.drawable.icunlock2
        else -> R.drawable.icunlock1
    }

    fun getProgressText(prefs: SharedPreferences): String {
        val completed = prefs.getInt("prescripts_completed", 0)
        val karmic = prefs.getInt("karmic_consequences", 0)
        val total = completed + karmic
        val ratio = if (total == 0) 0f else completed.toFloat() / total
        val currentStage = prefs.getInt("highest_stage_today", 1)
        val ratioPercent = (ratio * 100).toInt()

        return when (currentStage) {
            1 -> {
                val remaining = (MIN_SAMPLE_STAGE_2 - total).coerceAtLeast(0)
                if (remaining > 0) "NEXT: STAGE 2 — $remaining MORE NEEDED"
                else "NEXT: STAGE 2 — NEED 50%+ (AT $ratioPercent%)"
            }
            2 -> {
                val remaining = (MIN_SAMPLE_STAGE_3 - total).coerceAtLeast(0)
                if (remaining > 0) "NEXT: STAGE 3 — $remaining MORE NEEDED"
                else "NEXT: STAGE 3 — NEED 75%+ (AT $ratioPercent%)"
            }
            else -> "MAX STAGE REACHED"
        }
    }

    fun getCompactProgressText(prefs: SharedPreferences): String {
        val completed = prefs.getInt("prescripts_completed", 0)
        val karmic = prefs.getInt("karmic_consequences", 0)
        val total = completed + karmic
        val ratio = if (total == 0) 0f else completed.toFloat() / total
        val currentStage = prefs.getInt("highest_stage_today", 1)

        return when (currentStage) {
            1 -> {
                val remaining = (MIN_SAMPLE_STAGE_2 - total).coerceAtLeast(0)
                if (remaining > 0) "$total/$MIN_SAMPLE_STAGE_2→S2" else "${(ratio * 100).toInt()}%→S2"
            }
            2 -> {
                val remaining = (MIN_SAMPLE_STAGE_3 - total).coerceAtLeast(0)
                if (remaining > 0) "$total/$MIN_SAMPLE_STAGE_3→S3" else "${(ratio * 100).toInt()}%→S3"
            }
            else -> "MAX"
        }
    }
}