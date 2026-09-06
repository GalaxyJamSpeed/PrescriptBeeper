package com.example.prescriptbeeper

import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

class MarkedAppsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("prescript_prefs", MODE_PRIVATE)
        val unread = prefs.getStringSet("unread_packages", emptySet()) ?: emptySet()
        val pm = packageManager

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFF020505.toInt())
            setPadding(48, 48, 48, 48)
        }

        val title = TextView(this).apply {
            text = "MARKED APPS"
            setTextColor(0xFF4be8ff.toInt())
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 40)
        }
        root.addView(title)

        if (unread.isEmpty()) {
            root.addView(TextView(this).apply {
                text = "Nothing marked right now."
                setTextColor(0xFF8fa3ad.toInt())
                gravity = Gravity.CENTER
            })
        } else {
            for (pkg in unread) {
                val row = FrameLayout(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = 24 }
                }

                val inner = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(24, 24, 24, 24)
                    setBackgroundColor(0xFF10181c.toInt())
                }

                val icon = try { pm.getApplicationIcon(pkg) } catch (e: PackageManager.NameNotFoundException) { null }
                inner.addView(ImageView(this).apply {
                    layoutParams = LinearLayout.LayoutParams(96, 96)
                    setImageDrawable(icon)
                })

                val label = try {
                    pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
                } catch (e: Exception) { pkg }

                inner.addView(TextView(this).apply {
                    text = label
                    setTextColor(0xFFdff1ff.toInt())
                    textSize = 14f
                    setPadding(32, 0, 0, 0)
                })

                row.setOnClickListener {
                    val launchIntent = pm.getLaunchIntentForPackage(pkg)
                    launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    launchIntent?.let { startActivity(it) }
                }

                row.addView(inner)
                row.addView(ImageView(this).apply {
                    layoutParams = FrameLayout.LayoutParams(56, 56).apply {
                        gravity = Gravity.TOP or Gravity.END
                    }
                    setImageResource(R.drawable.ictargetflower)
                })

                root.addView(row)
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