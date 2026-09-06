package com.example.prescriptbeeper

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WeeklySummaryActivity : AppCompatActivity() {

    private enum class Mode { DAY, WEEK, MONTH }

    private var mode = Mode.WEEK
    private var anchor = Calendar.getInstance()

    private lateinit var root: LinearLayout
    private lateinit var periodLabel: TextView
    private lateinit var chart: SimpleBarChartView
    private lateinit var statsContainer: LinearLayout
    private lateinit var breakdownContainer: LinearLayout
    private lateinit var dayButton: Button
    private lateinit var weekButton: Button
    private lateinit var monthButton: Button

    private lateinit var weekdayHeaderRow: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val density = resources.displayMetrics.density

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF020505.toInt())
            setPadding(48, (48 + 32 * density).toInt(), 48, 48)
        }

        root.addView(TextView(this).apply {
            text = "STATS"
            setTextColor(0xFF4be8ff.toInt())
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        })

        val modeRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 20 }
        }
        dayButton = Button(this).apply {
            text = "Day"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { mode = Mode.DAY; refresh() }
        }
        weekButton = Button(this).apply {
            text = "Week"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { mode = Mode.WEEK; refresh() }
        }
        monthButton = Button(this).apply {
            text = "Month"
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setOnClickListener { mode = Mode.MONTH; refresh() }
        }
        modeRow.addView(dayButton)
        modeRow.addView(weekButton)
        modeRow.addView(monthButton)
        root.addView(modeRow)

        val navRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 20 }
        }
        navRow.addView(Button(this).apply {
            text = "<"
            setOnClickListener { shift(-1); refresh() }
        })
        periodLabel = TextView(this).apply {
            setTextColor(0xFFdff1ff.toInt())
            textSize = 13f
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        navRow.addView(periodLabel)
        navRow.addView(Button(this).apply {
            text = ">"
            setOnClickListener { shift(1); refresh() }
        })
        root.addView(navRow)

        weekdayHeaderRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (16 * density).toInt()
            ).apply { bottomMargin = 0 }
            visibility = android.view.View.GONE
        }
        root.addView(weekdayHeaderRow)

        chart = SimpleBarChartView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (180 * density).toInt()
            ).apply { bottomMargin = 24 }
        }
        root.addView(chart)

        statsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }
        root.addView(statsContainer)

        root.addView(TextView(this).apply {
            text = "BREAKDOWN"
            setTextColor(0xFF4be8ff.toInt())
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, 12, 0, 12)
        })

        breakdownContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(breakdownContainer)

        val scroll = ScrollView(this).apply { fitsSystemWindows = true }
        scroll.addView(root)
        setContentView(scroll)

        refresh()
    }

    private fun shift(direction: Int) {
        when (mode) {
            Mode.DAY -> anchor.add(Calendar.DAY_OF_YEAR, direction)
            Mode.WEEK -> anchor.add(Calendar.DAY_OF_YEAR, direction * 7)
            Mode.MONTH -> anchor.add(Calendar.MONTH, direction)
        }
    }

    private fun computeRange(): Pair<String, String> {
        return when (mode) {
            Mode.DAY -> {
                val key = WeeklyStats.keyFor(anchor)
                key to key
            }
            Mode.WEEK -> {
                val start = anchor.clone() as Calendar
                val currentDayOfWeek = start.get(Calendar.DAY_OF_WEEK)
                val daysSinceMonday = (currentDayOfWeek + 5) % 7
                start.add(Calendar.DAY_OF_YEAR, -daysSinceMonday)
                val end = start.clone() as Calendar
                end.add(Calendar.DAY_OF_YEAR, 6)
                WeeklyStats.keyFor(start) to WeeklyStats.keyFor(end)
            }
            Mode.MONTH -> {
                val start = anchor.clone() as Calendar
                start.set(Calendar.DAY_OF_MONTH, 1)
                val end = start.clone() as Calendar
                end.set(Calendar.DAY_OF_MONTH, end.getActualMaximum(Calendar.DAY_OF_MONTH))
                WeeklyStats.keyFor(start) to WeeklyStats.keyFor(end)
            }
        }
    }

    private fun periodLabelText(): String {
        return when (mode) {
            Mode.DAY -> SimpleDateFormat("EEEE, MMM d, yyyy", Locale.US).format(anchor.time)
            Mode.WEEK -> {
                val (start, end) = computeRange()
                "${SimpleDateFormat("MMM d", Locale.US).format(WeeklyStats.parseKey(start).time)} – " +
                        SimpleDateFormat("MMM d, yyyy", Locale.US).format(WeeklyStats.parseKey(end).time)
            }
            Mode.MONTH -> SimpleDateFormat("MMMM yyyy", Locale.US).format(anchor.time)
        }
    }

    private fun refresh() {
        dayButton.isEnabled = mode != Mode.DAY
        weekButton.isEnabled = mode != Mode.WEEK
        monthButton.isEnabled = mode != Mode.MONTH

        val (startKey, endKey) = computeRange()
        val summary = WeeklyStats.getSummaryForRange(this, startKey, endKey)

        periodLabel.text = periodLabelText()

        updateWeekdayHeader(summary.dailyTotals.map { it.first })

        val chartLabels = if (mode == Mode.DAY) {
            summary.byCategory.entries.map { it.key.take(4) to it.value }
        } else {
            summary.dailyTotals.map { WeeklyStats.shortDayLabel(it.first) to it.second }
        }

        val weekendFlags = if (mode == Mode.WEEK || mode == Mode.MONTH) {
            summary.dailyTotals.map { WeeklyStats.isWeekend(it.first) }
        } else emptyList()

        chart.setData(chartLabels, weekendFlags)

        statsContainer.removeAllViews()
        fun statCard(label: String, value: String) {
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(0xFF10181c.toInt())
                setPadding(24, 20, 24, 20)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 12 }
            }
            card.addView(TextView(this).apply {
                text = label
                setTextColor(0xFF7ee8a3.toInt())
                textSize = 11f
                setTypeface(typeface, Typeface.BOLD)
            })
            card.addView(TextView(this).apply {
                text = value
                setTextColor(0xFFdff1ff.toInt())
                textSize = 15f
                setPadding(0, 6, 0, 0)
            })
            statsContainer.addView(card)
        }

        statCard("TOTAL COMPLETED", "${summary.total}")
        if (mode != Mode.DAY) {
            statCard("BUSIEST DAY", "${summary.busiestDayLabel} (${summary.busiestDayCount})")
        }
        statCard("TOP CATEGORY", "${summary.topCategoryLabel} (${summary.topCategoryCount})")

        breakdownContainer.removeAllViews()
        if (summary.byCategory.isEmpty()) {
            breakdownContainer.addView(TextView(this).apply {
                text = "No prescripts completed in this period."
                setTextColor(0xFF8fa3ad.toInt())
            })
        } else {
            for ((category, count) in summary.byCategory.entries.sortedByDescending { it.value }) {
                breakdownContainer.addView(TextView(this).apply {
                    text = "$category — $count"
                    setTextColor(0xFFdff1ff.toInt())
                    textSize = 13f
                    setPadding(0, 4, 0, 4)
                })
            }
        }
    }

    private fun updateWeekdayHeader(dateKeys: List<String>) {
        weekdayHeaderRow.removeAllViews()
        if (mode == Mode.DAY) {
            weekdayHeaderRow.visibility = android.view.View.GONE
            return
        }
        weekdayHeaderRow.visibility = android.view.View.VISIBLE
        val size = if (mode == Mode.WEEK) 12f else 8f
        for (key in dateKeys) {
            weekdayHeaderRow.addView(TextView(this).apply {
                text = WeeklyStats.weekdayInitial(key)
                setTextColor(0xFF5c6975.toInt())
                textSize = size
                gravity = Gravity.CENTER
                includeFontPadding = false
                setPadding(0, 0, 0, 0)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            })
        }
    }
}