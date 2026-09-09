package com.example.prescriptbeeper

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class BackupRestoreActivity : AppCompatActivity() {

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri: Uri? ->
        if (uri == null) return@registerForActivityResult
        val success = BackupRestore.exportToUri(this, uri)
        Toast.makeText(this, if (success) "Backup saved" else "Backup failed", Toast.LENGTH_SHORT).show()
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@registerForActivityResult
        AlertDialog.Builder(this)
            .setTitle("Restore Backup?")
            .setMessage("This will overwrite ALL current settings, watched apps, custom lines, and reminders with the contents of this backup. This cannot be undone.")
            .setPositiveButton("Restore") { _, _ ->
                val success = BackupRestore.importFromUri(this, uri)
                if (success) {
                    CalendarScheduler.scheduleDailyCheck(this)
                    ReminderScheduler.rescheduleAll(this)
                    PrescriptWidgetProvider.updateAll(this)
                    Toast.makeText(this, "Backup restored", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Restore failed — file may not be a valid backup", Toast.LENGTH_LONG).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
            setBackgroundResource(R.drawable.bg_app_wallpaper)
            fitsSystemWindows = true
        }

        root.addView(TextView(this).apply {
            text = "BACKUP & RESTORE"
            setTextColor(0xFF4be8ff.toInt())
            textSize = 16f
            setPadding(0, (32 * resources.displayMetrics.density).toInt(), 0, 12)
        })

        root.addView(TextView(this).apply {
            text = "Save every setting, watched app, custom line, and reminder to a file or restore from one."
            setTextColor(0xFF8fa3ad.toInt())
            textSize = 11f
            setPadding(0, 0, 0, 32)
        })

        root.addView(Button(this).apply {
            text = "Export Backup"
            setBackgroundResource(R.drawable.bg_widget_dark)
            setTextColor(0xFF4be8ff.toInt())
            setOnClickListener { exportLauncher.launch("prescriptbeeper_backup.txt") }
        })

        root.addView(Button(this).apply {
            text = "Import Backup"
            setBackgroundResource(R.drawable.bg_widget_dark)
            setTextColor(0xFF4be8ff.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 16 }
            setOnClickListener { importLauncher.launch(arrayOf("text/plain")) }
        })

        setContentView(root)
    }
}