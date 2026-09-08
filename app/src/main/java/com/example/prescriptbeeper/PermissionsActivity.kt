package com.example.prescriptbeeper

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS

class PermissionsActivity : AppCompatActivity() {

    private val calendarPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(this, "Calendar permission denied — daily summary won't work.", Toast.LENGTH_LONG).show()
            }
            refreshButtonColors()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permissions)

        findViewById<MaterialButton>(R.id.btnNotifAccess).setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        findViewById<MaterialButton>(R.id.btnOverlayPermission).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                )
            } else {
                Toast.makeText(this, "Overlay permission already granted.", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<MaterialButton>(R.id.btnCalendarAccess).setOnClickListener {
            calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
        }

        findViewById<MaterialButton>(R.id.btnBatteryOptimization).setOnClickListener {
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

        findViewById<MaterialButton>(R.id.btnAutostart).setOnClickListener {
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

        findViewById<MaterialButton>(R.id.btnUsageAccess).setOnClickListener {
            startActivity(Intent(ACTION_USAGE_ACCESS_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshButtonColors()
    }

    private fun setButtonGranted(button: MaterialButton, granted: Boolean) {
        val color = if (granted) 0xFF4be8ff.toInt() else 0xFFff3b5c.toInt()
        button.setTextColor(color)
        button.strokeColor = ColorStateList.valueOf(color)
    }

    private fun refreshButtonColors() {
        setButtonGranted(findViewById(R.id.btnNotifAccess), PermissionStatus.hasNotificationAccess(this))
        setButtonGranted(findViewById(R.id.btnOverlayPermission), PermissionStatus.hasOverlayPermission(this))
        setButtonGranted(findViewById(R.id.btnCalendarAccess), PermissionStatus.hasCalendarPermission(this))
        setButtonGranted(findViewById(R.id.btnBatteryOptimization), PermissionStatus.hasBatteryExemption(this))
        setButtonGranted(findViewById(R.id.btnUsageAccess), PermissionStatus.hasUsageAccess(this))
    }
}