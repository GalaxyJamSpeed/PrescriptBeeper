package com.example.prescriptbeeper

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val notifPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(this, "Notification permission denied.", Toast.LENGTH_LONG).show()
            }
        }

    private val bluetoothPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(this, "Bluetooth permission denied — earbuds alerts won't work.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
        }

        CalendarScheduler.scheduleDailyCheck(this)


        findViewById<Button>(R.id.btnOpenConfigurations).setOnClickListener {
            startActivity(Intent(this, ConfigurationsActivity::class.java))
        }

        findViewById<Button>(R.id.btnViewLog).setOnClickListener {
            startActivity(Intent(this, LogActivity::class.java))
        }

        findViewById<Button>(R.id.btnWeeklySummary).setOnClickListener {
            startActivity(Intent(this, WeeklySummaryActivity::class.java))
        }

        findViewById<Button>(R.id.btnOpenSetup).setOnClickListener {
            startActivity(Intent(this, SetupActivity::class.java))
        }

        findViewById<Button>(R.id.btnOpenReminders).setOnClickListener {
            startActivity(Intent(this, RemindersActivity::class.java))
        }

        val prefs = getSharedPreferences("prescript_prefs", MODE_PRIVATE)
        val switchEnabled = findViewById<Switch>(R.id.switchEnabled)
        switchEnabled.isChecked = prefs.getBoolean("prescript_enabled", true)
        switchEnabled.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("prescript_enabled", isChecked).apply()
            Toast.makeText(
                this,
                if (isChecked) "Prescripts turned on" else "Prescripts turned off",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}