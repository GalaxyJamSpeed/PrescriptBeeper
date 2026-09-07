package com.example.prescriptbeeper

import android.app.TimePickerDialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class RemindersActivity : AppCompatActivity() {

    private lateinit var listContainer: LinearLayout
    private var selectedHour = 8
    private var selectedMinute = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        root.addView(TextView(this).apply {
            text = "REMINDERS"
            setTextColor(0xFF4be8ff.toInt())
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        })

        val timeButton = Button(this).apply {
            text = "Set Time: 08:00"
            setOnClickListener {
                TimePickerDialog(this@RemindersActivity, { _, hour, minute ->
                    selectedHour = hour
                    selectedMinute = minute
                    text = String.format("Set Time: %02d:%02d", hour, minute)
                }, selectedHour, selectedMinute, true).show()
            }
        }
        root.addView(timeButton)

        val textInput = EditText(this).apply {
            hint = "Reminder text..."
            setTextColor(0xFFdff1ff.toInt())
            setHintTextColor(0xFF5c6975.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 16; bottomMargin = 16 }
        }
        root.addView(textInput)

        root.addView(Button(this).apply {
            text = "Add Reminder"
            setOnClickListener {
                val text = textInput.text.toString().trim()
                if (text.isNotEmpty()) {
                    val reminder = ReminderConfig.addReminder(this@RemindersActivity, selectedHour, selectedMinute, text.uppercase())
                    ReminderScheduler.schedule(this@RemindersActivity, reminder)
                    textInput.text.clear()
                    refreshList()
                }
            }
        })

        root.addView(TextView(this).apply {
            text = "YOUR REMINDERS"
            setTextColor(0xFF4be8ff.toInt())
            textSize = 12f
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, 32, 0, 12)
        })

        listContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(listContainer)

        val scroll = ScrollView(this).apply {
            fitsSystemWindows = true
            setPadding(0, (32 * resources.displayMetrics.density).toInt(), 0, 0)
            setBackgroundColor(0xFF020505.toInt())
        }
        scroll.addView(root)
        setContentView(scroll)

        refreshList()
    }

    private fun refreshList() {
        listContainer.removeAllViews()
        val reminders = ReminderConfig.getReminders(this).sortedBy { it.hour * 60 + it.minute }

        if (reminders.isEmpty()) {
            listContainer.addView(TextView(this).apply {
                text = "No reminders set yet."
                setTextColor(0xFF8fa3ad.toInt())
                gravity = Gravity.CENTER
            })
            return
        }

        for (reminder in reminders) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(0xFF10181c.toInt())
                setPadding(24, 20, 24, 20)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 16 }
            }

            val topRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            topRow.addView(TextView(this).apply {
                text = String.format("%02d:%02d", reminder.hour, reminder.minute)
                setTextColor(0xFF4be8ff.toInt())
                textSize = 16f
                setTypeface(typeface, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })

            topRow.addView(Switch(this).apply {
                isChecked = reminder.enabled
                setOnCheckedChangeListener { _, isChecked ->
                    ReminderConfig.setEnabled(this@RemindersActivity, reminder.id, isChecked)
                    if (isChecked) ReminderScheduler.schedule(this@RemindersActivity, reminder.copy(enabled = true))
                    else ReminderScheduler.cancel(this@RemindersActivity, reminder)
                }
            })

            topRow.addView(Button(this).apply {
                text = "Delete"
                textSize = 10f
                setOnClickListener {
                    ReminderScheduler.cancel(this@RemindersActivity, reminder)
                    ReminderConfig.deleteReminder(this@RemindersActivity, reminder.id)
                    refreshList()
                }
            })

            row.addView(topRow)
            row.addView(TextView(this).apply {
                text = reminder.text
                setTextColor(0xFFdff1ff.toInt())
                textSize = 12f
                setPadding(0, 8, 0, 0)
            })

            listContainer.addView(row)
        }
    }
}