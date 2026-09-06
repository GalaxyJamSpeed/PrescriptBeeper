package com.example.prescriptbeeper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class SimpleBarChartView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private var data: List<Pair<String, Int>> = emptyList()

    private val barPaint = Paint().apply { color = Color.parseColor("#35d7f0") }
    private val labelPaint = Paint().apply {
        color = Color.parseColor("#5c6975")
        textSize = 22f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }
    private val valuePaint = Paint().apply {
        color = Color.parseColor("#dff1ff")
        textSize = 22f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private var highlightFlags: List<Boolean> = emptyList()
    private val weekendBgPaint = Paint().apply { color = Color.parseColor("#15242a") }

    fun setData(newData: List<Pair<String, Int>>, highlights: List<Boolean> = emptyList()) {
        data = newData
        highlightFlags = highlights
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (data.isEmpty()) return

        val maxValue = (data.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1)
        val chartHeight = height - 60f
        val slotWidth = width.toFloat() / data.size
        val barWidth = (slotWidth * 0.5f).coerceAtMost(60f)

        data.forEachIndexed { index, (label, value) ->
            if (highlightFlags.getOrNull(index) == true) {
                val slotLeft = slotWidth * index
                canvas.drawRect(slotLeft, 0f, slotLeft + slotWidth, height.toFloat(), weekendBgPaint)
            }

            val barHeight = (value.toFloat() / maxValue) * (chartHeight - 30f)
            val centerX = slotWidth * index + slotWidth / 2
            val top = chartHeight - barHeight

            canvas.drawRoundRect(
                centerX - barWidth / 2, top,
                centerX + barWidth / 2, chartHeight,
                8f, 8f, barPaint
            )

            if (value > 0) canvas.drawText(value.toString(), centerX, top - 8f, valuePaint)
            canvas.drawText(label, centerX, height.toFloat() - 10f, labelPaint)
        }
    }
}