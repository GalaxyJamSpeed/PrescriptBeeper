package com.example.prescriptbeeper

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class PermissionsActivity : AppCompatActivity() {

    private val calendarPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(this, "Calendar permission denied — daily summary won't work.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)

        findViewById<Button>(R.id.btnNotifAccess).setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        findViewById<Button>(R.id.btnOverlayPermission).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                )
            } else {
                Toast.makeText(this, "Overlay permission already granted.", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnCalendarAccess).setOnClickListener {
            calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
        }

        findViewById<Button>(R.id.btnBatteryOptimization).setOnClickListener {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !powerManager.isIgnoringBatteryOptimizations(packageName)) {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
            } else {
                Toast.makeText(this, "Battery restriction already disabled.", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnAutostart).setOnClickListener {
            try {
                val intent = Intent().apply {
                    component = ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                }
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "Autostart screen not found on this phone — opening general app settings instead.",
                    Toast.LENGTH_LONG
                ).show()
                val fallback = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(fallback)
            }
        }
    }
}