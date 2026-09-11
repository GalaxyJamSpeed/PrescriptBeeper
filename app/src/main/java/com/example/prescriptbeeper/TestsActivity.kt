package com.example.prescriptbeeper

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class TestsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tests)

        findViewById<Button>(R.id.btnTestPopup).setOnClickListener {
            PrescriptTrigger.fire(this, "COMMUNICATION", isTest = true)
        }

        findViewById<Button>(R.id.btnTestCalendar).setOnClickListener {
            CalendarAlarmReceiver.checkAndFire(this)
        }

        findViewById<Button>(R.id.btnTestBatteryLow).setOnClickListener {
            PrescriptTrigger.fire(
                this, "BATTERY_LOW",
                overrideText = "${PrescriptLines.getLine(this, "BATTERY_LOW")} (TEST)",
                isTest = true
            )
        }

        findViewById<Button>(R.id.btnTestBatteryOk).setOnClickListener {
            PrescriptTrigger.fire(
                this, "BATTERY_OK",
                overrideText = "${PrescriptLines.getLine(this, "BATTERY_OK")} (TEST)",
                isTest = true
            )
        }

        findViewById<Button>(R.id.btnTestEarbuds).setOnClickListener {
            PrescriptTrigger.fire(
                this, "EARBUDS_LOW",
                overrideText = "${PrescriptLines.getLine(this, "EARBUDS_LOW")} (Test Earbuds, 15%)",
                isTest = true
            )
        }

        findViewById<Button>(R.id.btnTestReminder).setOnClickListener {
            PrescriptTrigger.fire(this, "REMINDER", overrideText = "THIS IS A TEST REMINDER.", isTest = true)
        }
    }
}