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
                    "Beeper from Limbus Company. It turns real phone events (texts, calls, calendar entries, " +
                    "low battery, low earbuds battery, and your own custom reminders) into stylized full-screen " +
                    "\"Prescripts,\" styled after the in-universe device's cyan-on-black display.\n\nThis is an " +
                    "unofficial, non-commercial fan project. All game-related names and concepts belong to " +
                    "Project Moon."
        )

        addSection(
            "PRESCRIPTS",
            "A Prescript is the popup itself - a scramble-to-reveal message tied to whatever triggered " +
                    "it. Accept opens the source app (if there is one) and counts as completed. Dismiss, or " +
                    "letting the countdown run out, counts as a Karmic Consequence instead.\n\nEach category " +
                    "(Messages/Calls, Battery, Earbuds, Calendar, Custom) pulls from its own pool of lines - a " +
                    "mix of built-in flavor text and anything you've added yourself in Configurations."
        )

        addSection(
            "UNLOCK STAGE",
            "Your Stage reflects how well you're keeping up with Prescripts, not how many you get. " +
                    "Once you've had at least 5 interactions in a day, your completion rate (accepted ÷ total) " +
                    "decides whether you climb: 50%+ moves you from Stage 1 to Stage 2, and 75%+ moves you from " +
                    "Stage 2 to Stage 3.\n\nStages always move one step at a time and never skip (you can't " +
                    "jump straight from Stage 1 to 3) and once reached, a Stage never drops back down for the " +
                    "rest of the day. Everything resets to Stage 1 at midnight."
        )

        addSection(
            "KARMIC CONSEQUENCE",
            "Every Dismiss or expired Prescript counts as a Karmic Consequence, shown on the popup and " +
                    "the widget. If your dismissed count for the day ever exceeds your completed count, the icon " +
                    "shifts to its more severe [Fortuna] form as a visual warning that you're falling behind."
        )

        addSection(
            "STATS",
            "The Stats screen tracks Day, Week, and Month views, each with a bar chart, a Total " +
                    "Completed vs. Total Dismissed comparison, and a breakdown by category. All-Time Stats, " +
                    "accessible from the top of that screen, shows your true lifetime totals alongside recent " +
                    "trends covering roughly the last 13 months of detailed history."
        )

        addSection(
            "APP CATEGORIES & EXCLUSIONS",
            "In Configurations, App Categories lets you assign any installed app to Messages/Calls, " +
                    "Calendar, or Custom (only apps you assign actually trigger anything).\n\nExclusions works " +
                    "differently: apps added there won't show a popup for ANY notification (from any app) " +
                    "while you're currently inside them. It's meant for games or focus apps where you don't want " +
                    "interruptions, regardless of what's triggering them. This requires the Usage Access " +
                    "permission to detect which app is currently open."
        )

        addSection(
            "FEATURE TOGGLES",
            "Configurations → Enable/Disable Features lets you turn off Low Battery, Restored Battery, " +
                    "or Low Earbuds alerts individually if you don't want them at all, without needing to touch " +
                    "any thresholds."
        )

        addSection(
            "WIDGET & REMINDERS",
            "The home screen widget shows today's completions, current Stage, and Karmic Consequence " +
                    "count without opening the app. Reminders let you schedule your own custom Prescripts at any " +
                    "time of day, independent of any real-world trigger."
        )

        addSection(
            "BACKUP & RESTORE",
            "Found in the main menu's Setup area. Export saves every setting, watched app, custom line, " +
                    "and reminder to a single file. Import restores from that file, completely replacing your " +
                    "current settings - useful when reinstalling or moving to a new phone."
        )

        addSection(
            "PERMISSIONS",
            "Notification Access lets the app see incoming notifications from apps you've chosen to " +
                    "watch. Overlay permission is what allows the popup to draw on screen. Usage Access powers " +
                    "Exclusions and the \"skip popup if already in that app\" option. Calendar, Battery " +
                    "Exemption, and Autostart are each optional and only needed for their specific features - " +
                    "check the Permissions screen for details on each."
        )

        val scroll = ScrollView(this).apply {
            fitsSystemWindows = true
            setPadding(0, (32 * resources.displayMetrics.density).toInt(), 0, 0)
            setBackgroundResource(R.drawable.bg_app_wallpaper)
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