package com.example.prescriptbeeper

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LogActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val entries = PrescriptLog.getEntries(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bg_app_wallpaper)
            setPadding(48, 48, 48, 48)
        }

        root.addView(TextView(this).apply {
            text = "PRESCRIPT LOG"
            setTextColor(0xFF4be8ff.toInt())
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 40)
        })

        if (entries.isEmpty()) {
            root.addView(TextView(this).apply {
                text = "No prescripts logged yet."
                setTextColor(0xFF8fa3ad.toInt())
                gravity = Gravity.CENTER
            })
        } else {
            for (entry in entries) {
                val statusColor = when (entry.status) {
                    "ACCEPTED" -> 0xFF4be8ff.toInt()
                    "DISMISSED" -> 0xFF8fa3ad.toInt()
                    "EXPIRED" -> 0xFF6b7680.toInt()
                    else -> 0xFFd8dee3.toInt()
                }

                val card = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    setBackgroundColor(0xFF10181c.toInt())
                    setPadding(24, 20, 24, 20)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = 20 }
                }

                card.addView(TextView(this).apply {
                    text = "${entry.appName}  •  ${entry.timestamp}"
                    setTextColor(0xFF7ee8a3.toInt())
                    textSize = 11f
                    setTypeface(typeface, Typeface.BOLD)
                })

                card.addView(TextView(this).apply {
                    text = entry.text
                    setTextColor(0xFFdff1ff.toInt())
                    textSize = 13f
                    setPadding(0, 8, 0, 8)
                })

                if (entry.content.isNotBlank()) {
                    card.addView(TextView(this).apply {
                        text = "\"${entry.content}\""
                        setTextColor(0xFF9fb3bd.toInt())
                        textSize = 11f
                        setPadding(0, 0, 0, 8)
                    })
                }

                card.addView(TextView(this).apply {
                    text = entry.status
                    setTextColor(statusColor)
                    textSize = 10f
                    setTypeface(typeface, Typeface.BOLD)
                })

                root.addView(card)
            }
        }

        val density = resources.displayMetrics.density
        val scroll = ScrollView(this).apply {
            fitsSystemWindows = true
            setPadding(0, (32 * density).toInt(), 0, 0)
        }
        scroll.addView(root)
        setContentView(scroll)
    }
}