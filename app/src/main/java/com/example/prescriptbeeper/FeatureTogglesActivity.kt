package com.example.prescriptbeeper

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FeatureTogglesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        root.addView(TextView(this).apply {
            text = "ENABLE/DISABLE FEATURES"
            setTextColor(0xFF4be8ff.toInt())
            textSize = 16f
            setTypeface(typeface, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 12)
        })

        root.addView(TextView(this).apply {
            text = "Turn off any trigger you don't want firing prescripts, without needing to touch thresholds."
            setTextColor(0xFF8fa3ad.toInt())
            textSize = 11f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 32)
        })

        addToggleRow(root, "Low Battery Alerts", FeatureToggles.isBatteryLowEnabled(this), "feature_battery_low_enabled")
        addToggleRow(root, "Restored Battery Alerts", FeatureToggles.isBatteryRestoredEnabled(this), "feature_battery_restored_enabled")
        addToggleRow(root, "Low Earbuds Alerts", FeatureToggles.isEarbudsLowEnabled(this), "feature_earbuds_low_enabled")

        val scroll = ScrollView(this).apply {
            fitsSystemWindows = true
            setPadding(0, (32 * resources.displayMetrics.density).toInt(), 0, 0)
            setBackgroundResource(R.drawable.bg_app_wallpaper)
        }
        scroll.addView(root)
        setContentView(scroll)
    }

    private fun addToggleRow(root: LinearLayout, label: String, initialValue: Boolean, prefKey: String) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setBackgroundColor(0xFF10181c.toInt())
            setPadding(24, 20, 24, 20)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 16 }
        }

        row.addView(TextView(this).apply {
            text = label
            setTextColor(0xFFdff1ff.toInt())
            textSize = 13f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })

        val switch = Switch(this).apply { isChecked = initialValue }
        SwitchStyler.applyBlueTint(switch)
        switch.setOnCheckedChangeListener { _, isChecked ->
            FeatureToggles.setEnabled(this, prefKey, isChecked)
        }
        row.addView(switch)

        root.addView(row)
    }
}