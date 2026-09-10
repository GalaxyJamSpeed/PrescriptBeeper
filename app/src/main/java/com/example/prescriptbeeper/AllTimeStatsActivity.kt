package com.example.prescriptbeeper

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AllTimeStatsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val summary = WeeklyStats.getAllTimeSummary(this)
        val density = resources.displayMetrics.density

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, (48 + 32 * density).toInt(), 48, 48)
        }

        root.addView(TextView(this).apply {
            text = "ALL-TIME STATS"
            setTextColor(0xFF4be8ff.toInt())
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        })

        fun statCard(label: String, value: String, accentColor: Int = 0xFF7ee8a3.toInt()) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(R.drawable.bg_stat_card)
                setPadding(28, 20, 24, 20)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 12 }
            }
            card.addView(TextView(this).apply {
                text = label
                setTextColor(accentColor)
                textSize = 11f
                setTypeface(typeface, Typeface.BOLD)
            })
            card.addView(TextView(this).apply {
                text = value
                setTextColor(0xFFdff1ff.toInt())
                textSize = 15f
                setPadding(0, 6, 0, 0)
            })
            root.addView(card)
        }

        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 12 }
        }

        fun sideCard(label: String, value: String, accentColor: Int, useRedCard: Boolean = false): LinearLayout {
            return LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundResource(if (useRedCard) R.drawable.bg_stat_card_red else R.drawable.bg_stat_card)
                setPadding(28, 20, 24, 20)
                addView(TextView(this@AllTimeStatsActivity).apply {
                    text = label
                    setTextColor(accentColor)
                    textSize = 11f
                    setTypeface(typeface, Typeface.BOLD)
                })
                addView(TextView(this@AllTimeStatsActivity).apply {
                    text = value
                    setTextColor(0xFFdff1ff.toInt())
                    textSize = 18f
                    setPadding(0, 6, 0, 0)
                })
            }
        }

        val completedCard = sideCard("TOTAL COMPLETED", "${summary.totalCompleted}", 0xFF7ee8a3.toInt()).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = 8 }
        }
        val dismissedCard = sideCard("TOTAL DISMISSED", "${summary.totalDismissed}", 0xFFff3b5c.toInt(), useRedCard = true).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        topRow.addView(completedCard)
        topRow.addView(dismissedCard)
        root.addView(topRow)

        statCard("BUSIEST DAY (LAST ~13 MONTHS)", "${summary.busiestDayLabel} (${summary.busiestDayCount})")
        statCard("TOP CATEGORY (LAST ~13 MONTHS)", "${summary.topCategoryLabel} (${summary.topCategoryCount})")

        root.addView(TextView(this).apply {
            text = "BREAKDOWN (LAST ~13 MONTHS)"
            setTextColor(0xFF4be8ff.toInt())
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, 12, 0, 12)
        })

        if (summary.byCategory.isEmpty()) {
            root.addView(TextView(this).apply {
                text = "No prescripts recorded yet."
                setTextColor(0xFF8fa3ad.toInt())
            })
        } else {
            for ((category, count) in summary.byCategory.entries.sortedByDescending { it.value }) {
                root.addView(TextView(this).apply {
                    text = "$category — $count"
                    setTextColor(0xFFdff1ff.toInt())
                    textSize = 13f
                    setPadding(0, 4, 0, 4)
                })
            }
        }

        val scroll = ScrollView(this).apply {
            fitsSystemWindows = true
            setBackgroundResource(R.drawable.bg_app_wallpaper)
        }
        scroll.addView(root)
        setContentView(scroll)
    }
}