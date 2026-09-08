package com.example.prescriptbeeper

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

class ConfigurationsActivity : AppCompatActivity() {

    private fun setupCollapsible(header: Button, container: LinearLayout, title: String) {
        header.setOnClickListener {
            val expanding = container.visibility != View.VISIBLE
            container.visibility = if (expanding) View.VISIBLE else View.GONE
            header.text = if (expanding) "▾ $title" else "▸ $title"
        }
    }

    private val categoryKeys = PrescriptLines.editableCategories
    private val categoryLabels = listOf("Messages/Calls", "Battery Low", "Battery Restored", "Earbuds Low", "Calendar", "Custom")

    private lateinit var spinnerCategory: Spinner
    private lateinit var customLinesContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_configurations)

        setupCollapsible(findViewById(R.id.headerAudio), findViewById(R.id.audioContainer), "AUDIO")
        setupCollapsible(findViewById(R.id.headerTiming), findViewById(R.id.timingContainer), "TIMING")
        setupCollapsible(findViewById(R.id.headerThresholds), findViewById(R.id.thresholdsContainer), "THRESHOLDS")

        findViewById<Button>(R.id.btnAppCategories).setOnClickListener {
            startActivity(Intent(this, AppPickerActivity::class.java))
        }

        findViewById<Button>(R.id.btnExclusions).setOnClickListener {
            startActivity(Intent(this, ExclusionsActivity::class.java))
        }

        val prefs = getSharedPreferences("prescript_prefs", MODE_PRIVATE)

        val seekBarVolume = findViewById<SeekBar>(R.id.seekBarVolume)
        val tvVolumeValue = findViewById<TextView>(R.id.tvVolumeValue)
        val savedVolume = (prefs.getFloat("beep_volume", 1.0f) * 100).toInt()
        seekBarVolume.progress = savedVolume
        tvVolumeValue.text = "${savedVolume}%"
        seekBarVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                tvVolumeValue.text = "${progress}%"
                prefs.edit().putFloat("beep_volume", progress / 100f).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val seekBarDuration = findViewById<SeekBar>(R.id.seekBarDuration)
        val tvDurationValue = findViewById<TextView>(R.id.tvDurationValue)
        val savedDuration = prefs.getInt("popup_duration_seconds", 5)
        seekBarDuration.progress = if (savedDuration == 0) 16 else savedDuration - 5
        tvDurationValue.text = if (savedDuration == 0) "Infinite" else "${savedDuration}s"
        seekBarDuration.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress == 16) {
                    tvDurationValue.text = "Infinite"
                    prefs.edit().putInt("popup_duration_seconds", 0).apply()
                } else {
                    val seconds = progress + 5
                    tvDurationValue.text = "${seconds}s"
                    prefs.edit().putInt("popup_duration_seconds", seconds).apply()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val seekBarCooldown = findViewById<SeekBar>(R.id.seekBarCooldown)
        val tvCooldownValue = findViewById<TextView>(R.id.tvCooldownValue)
        val savedCooldown = prefs.getInt("app_cooldown_seconds", 20)
        seekBarCooldown.progress = savedCooldown - 10
        tvCooldownValue.text = "${savedCooldown}s"
        seekBarCooldown.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val seconds = progress + 10
                tvCooldownValue.text = "${seconds}s"
                prefs.edit().putInt("app_cooldown_seconds", seconds).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val seekBarBatteryThreshold = findViewById<SeekBar>(R.id.seekBarBatteryThreshold)
        val tvBatteryThresholdValue = findViewById<TextView>(R.id.tvBatteryThresholdValue)
        val savedThreshold = prefs.getInt("battery_low_threshold", 20)
        seekBarBatteryThreshold.progress = savedThreshold - 10
        tvBatteryThresholdValue.text = "${savedThreshold}%"
        seekBarBatteryThreshold.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val percent = progress + 10
                tvBatteryThresholdValue.text = "${percent}%"
                prefs.edit().putInt("battery_low_threshold", percent).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val seekBarBatteryRestored = findViewById<SeekBar>(R.id.seekBarBatteryRestored)
        val tvBatteryRestoredValue = findViewById<TextView>(R.id.tvBatteryRestoredValue)
        val savedRestoredThreshold = prefs.getInt("battery_restored_threshold", 80)
        seekBarBatteryRestored.progress = savedRestoredThreshold - 50
        tvBatteryRestoredValue.text = "${savedRestoredThreshold}%"
        seekBarBatteryRestored.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val percent = progress + 50
                tvBatteryRestoredValue.text = "${percent}%"
                prefs.edit().putInt("battery_restored_threshold", percent).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val seekBarEarbudsThreshold = findViewById<SeekBar>(R.id.seekBarEarbudsThreshold)
        val tvEarbudsThresholdValue = findViewById<TextView>(R.id.tvEarbudsThresholdValue)
        val savedEarbudsThreshold = prefs.getInt("earbuds_low_threshold", 30)
        seekBarEarbudsThreshold.progress = savedEarbudsThreshold - 10
        tvEarbudsThresholdValue.text = "${savedEarbudsThreshold}%"
        seekBarEarbudsThreshold.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val percent = progress + 10
                tvEarbudsThresholdValue.text = "${percent}%"
                prefs.edit().putInt("earbuds_low_threshold", percent).apply()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        spinnerCategory = findViewById(R.id.spinnerCategory)
        customLinesContainer = findViewById(R.id.customLinesContainer)

        val adapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categoryLabels) {
            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(0xFFdff1ff.toInt())
                return view
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent) as TextView
                view.setTextColor(0xFFdff1ff.toInt())
                view.setBackgroundColor(0xFF10181c.toInt())
                return view
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        val removeDefaultsButton = findViewById<Button>(R.id.btnRemoveDefaults)

        fun updateRemoveDefaultsButton() {
            val category = categoryKeys[spinnerCategory.selectedItemPosition]
            val hidden = PrescriptLines.areDefaultsHidden(this, category)
            removeDefaultsButton.text = if (hidden) "Restore Default Lines" else "Remove Default Lines"
        }

        spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                refreshCustomLinesList()
                updateRemoveDefaultsButton()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        removeDefaultsButton.setOnClickListener {
            val category = categoryKeys[spinnerCategory.selectedItemPosition]
            val currentlyHidden = PrescriptLines.areDefaultsHidden(this, category)
            PrescriptLines.setDefaultsHidden(this, category, !currentlyHidden)
            updateRemoveDefaultsButton()
            Toast.makeText(this, if (!currentlyHidden) "Default lines removed" else "Default lines restored", Toast.LENGTH_SHORT).show()
        }

        updateRemoveDefaultsButton()

        findViewById<Button>(R.id.btnAddLine).setOnClickListener {
            val editText = findViewById<EditText>(R.id.etNewLine)
            val newLine = editText.text.toString().trim()
            if (newLine.isNotEmpty()) {
                val category = categoryKeys[spinnerCategory.selectedItemPosition]
                PrescriptLines.addCustomLine(this, category, newLine.uppercase())
                editText.text.clear()
                refreshCustomLinesList()
                Toast.makeText(this, "Line added", Toast.LENGTH_SHORT).show()
            }
        }

        refreshCustomLinesList()
    }

    private fun refreshCustomLinesList() {
        customLinesContainer.removeAllViews()
        val category = categoryKeys[spinnerCategory.selectedItemPosition]
        val customLines = PrescriptLines.getCustomLines(this, category)

        if (customLines.isEmpty()) {
            customLinesContainer.addView(TextView(this).apply {
                text = "No custom lines added yet for this category."
                setTextColor(0xFF8fa3ad.toInt())
                textSize = 12f
                setPadding(0, 8, 0, 8)
            })
            return
        }

        for (line in customLines) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setBackgroundColor(0xFF10181c.toInt())
                setPadding(20, 16, 20, 16)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 12 }
            }

            row.addView(TextView(this).apply {
                text = line
                setTextColor(0xFFdff1ff.toInt())
                textSize = 12f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })

            row.addView(Button(this).apply {
                text = "Delete"
                textSize = 10f
                setOnClickListener {
                    PrescriptLines.removeCustomLine(this@ConfigurationsActivity, category, line)
                    refreshCustomLinesList()
                }
            })

            customLinesContainer.addView(row)
        }
    }
}