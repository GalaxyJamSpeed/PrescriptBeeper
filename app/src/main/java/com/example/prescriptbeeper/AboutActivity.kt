package com.example.prescriptbeeper

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {

    private lateinit var root: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        root.addView(TextView(this).apply {
            text = "ABOUT"
            setTextColor(0xFF4be8ff.toInt())
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        })

        addSection(
            "APP INFO",
            "PrescriptBeeper is a fan-made notification system inspired by the Index's Prescript " +
                    "Beeper from Limbus Company. It turns real phone events - texts, calls, calendar entries, " +
                    "low battery, low earbuds battery - into stylized full-screen \"Prescripts,\" styled after " +
                    "the in-universe device's cyan-on-black display.\n\nThis is an unofficial, non-commercial " +
                    "fan project. All game-related names and concepts belong to Project Moon."
        )

        addSection(
            "PRESCRIPTS",
            "A Prescript is the popup itself - a scramble-to-reveal message tied to whatever triggered " +
                    "it. Accept opens the source app (if there is one) and counts toward your daily total. " +
                    "Dismiss, or letting the countdown run out, counts as a Karmic Consequence instead.\n\n" +
                    "Each category (Messages/Calls, Battery, Earbuds, Calendar, Custom) pulls from its own pool " +
                    "of lines - a mix of built-in flavor text and anything you've added yourself in " +
                    "Configurations."
        )

        addSection(
            "STATS",
            "The Stats screen tracks your history in Day, Week, and Month views, each with a bar chart " +
                    "and a breakdown by category. Total Completed and Total Dismissed sit side by side so you " +
                    "can see the balance at a glance, alongside your busiest day and most common trigger."
        )

        addSection(
            "KARMIC CONSEQUENCE",
            "Every time you Dismiss a Prescript, or let it expire on its own, it counts as a Karmic " +
                    "Consequence - shown on the popup itself and on the home screen widget. If your dismissed " +
                    "count for the day ever exceeds your completed count, the icon shifts to its more severe " +
                    "[Fortuna] form as a visual warning that you're ignoring too many Prescripts."
        )

        addSection(
            "UNLOCK STAGE",
            "Completing Prescripts (by pressing Accept) raises your Stage for the day (Stage 1 = 1/Stage 2 = 15/Stage 3 = 30) - a simple " +
                    "visual sense of progress shown in the popup's footer and on the widget. Both this and " +
                    "your Karmic Consequence count reset automatically at midnight."
        )

        addSection(
            "WIDGET & REMINDERS",
            "The home screen widget shows today's completions, current Stage, and Karmic Consequence " +
                    "count without needing to open the app. Reminders let you schedule your own custom " +
                    "Prescripts at any time of day, independent of any real-world trigger."
        )

        addSection(
            "PERMISSIONS",
            "Notification Access lets the app see (not read aloud, not store elsewhere) incoming " +
                    "notifications from apps you've chosen to watch. Overlay permission is what allows the " +
                    "popup to draw on screen. Calendar, Battery, Autostart, and Usage Access are each optional " +
                    "and only needed for the specific features tied to them - check the Permissions screen for " +
                    "details on each."
        )

        val scroll = ScrollView(this).apply {
            fitsSystemWindows = true
            setPadding(0, (32 * resources.displayMetrics.density).toInt(), 0, 0)
            setBackgroundColor(0xFF020505.toInt())
        }
        scroll.addView(root)
        setContentView(scroll)
    }

    private fun addSection(title: String, body: String) {
        val header = Button(this).apply {
            text = "▸ $title"
            setBackgroundResource(R.drawable.bg_widget_dark)
            setTextColor(0xFF4be8ff.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 8 }
        }

        val container = TextView(this).apply {
            text = body
            setTextColor(0xFFdff1ff.toInt())
            textSize = 12f
            setLineSpacing(6f, 1.1f)
            setPadding(20, 16, 20, 16)
            setBackgroundColor(0xFF10181c.toInt())
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }

        header.setOnClickListener {
            val expanding = container.visibility != View.VISIBLE
            container.visibility = if (expanding) View.VISIBLE else View.GONE
            header.text = if (expanding) "▾ $title" else "▸ $title"
        }

        root.addView(header)
        root.addView(container)
    }
}