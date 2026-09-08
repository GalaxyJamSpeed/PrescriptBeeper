package com.example.prescriptbeeper

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AppPickerActivity : AppCompatActivity() {

    private val categoryOptions = listOf("Not Watched", "Messages/Calls", "Calendar", "Custom")
    private val categoryKeys = listOf("", "COMMUNICATION", "CALENDAR", "CUSTOM")

    private lateinit var pm: PackageManager
    private lateinit var allApps: List<Pair<String, String>>
    private lateinit var listContainer: LinearLayout
    private var currentQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pm = packageManager

        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        allApps = pm.queryIntentActivities(mainIntent, 0)
            .map { it.activityInfo.packageName to it.activityInfo.loadLabel(pm).toString() }
            .filter { it.first != packageName }
            .distinctBy { it.first }
            .sortedBy { it.second.lowercase() }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        root.addView(TextView(this).apply {
            text = "APP CATEGORIES"
            setTextColor(0xFF4be8ff.toInt())
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
        })

        root.addView(TextView(this).apply {
            text = "Choose which apps trigger prescripts, and what category each one fires under."
            setTextColor(0xFF8fa3ad.toInt())
            textSize = 11f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 24)
        })

        val searchBox = EditText(this).apply {
            hint = "Search apps..."
            setTextColor(0xFFdff1ff.toInt())
            setHintTextColor(0xFF5c6975.toInt())
            setBackgroundColor(0xFF10181c.toInt())
            setPadding(24, 20, 24, 20)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 24 }
        }
        root.addView(searchBox)

        listContainer = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(listContainer)

        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentQuery = s?.toString()?.lowercase()?.trim() ?: ""
                rebuildList()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val scroll = ScrollView(this).apply {
            fitsSystemWindows = true
            setPadding(0, (32 * resources.displayMetrics.density).toInt(), 0, 0)
            setBackgroundColor(0xFF020505.toInt())
        }
        scroll.addView(root)
        setContentView(scroll)

        rebuildList()
    }

    private fun rebuildList() {
        listContainer.removeAllViews()
        val watchedApps = WatchedAppsConfig.getWatchedApps(this)

        val filtered = if (currentQuery.isEmpty()) allApps
        else allApps.filter { it.second.lowercase().contains(currentQuery) }

        val sections = listOf(
            "MESSAGES/CALLS" to filtered.filter { watchedApps[it.first] == "COMMUNICATION" },
            "CALENDAR" to filtered.filter { watchedApps[it.first] == "CALENDAR" },
            "CUSTOM" to filtered.filter { watchedApps[it.first] == "CUSTOM" },
            "NOT WATCHED" to filtered.filter { watchedApps[it.first] == null }
        )

        for ((sectionTitle, apps) in sections) {
            if (apps.isEmpty()) continue

            listContainer.addView(TextView(this).apply {
                text = sectionTitle
                setTextColor(0xFF5c6975.toInt())
                textSize = 10f
                letterSpacing = 0.1f
                setPadding(4, 20, 4, 8)
            })

            for ((pkg, label) in apps) {
                listContainer.addView(buildRow(pkg, label, watchedApps))
            }
        }
    }

    private fun buildRow(pkg: String, label: String, watchedApps: Map<String, String>): LinearLayout {
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

        val icon = try { pm.getApplicationIcon(pkg) } catch (e: PackageManager.NameNotFoundException) { null }
        row.addView(ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(72, 72).apply { marginEnd = 20 }
            setImageDrawable(icon)
        })

        row.addView(TextView(this).apply {
            text = label
            setTextColor(0xFFdff1ff.toInt())
            textSize = 12f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })

        val spinner = Spinner(this)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categoryOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        val currentCategory = watchedApps[pkg] ?: ""
        val currentIndex = categoryKeys.indexOf(currentCategory).let { if (it == -1) 0 else it }
        spinner.setSelection(currentIndex)

        spinner.post {
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedKey = categoryKeys[position]
                    if (selectedKey.isEmpty()) {
                        WatchedAppsConfig.removeApp(this@AppPickerActivity, pkg)
                    } else {
                        WatchedAppsConfig.setCategoryForApp(this@AppPickerActivity, pkg, selectedKey)
                    }
                    rebuildList()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        row.addView(spinner)
        return row
    }
}