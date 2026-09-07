package com.example.prescriptbeeper

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class TestsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tests)

        findViewById<Button>(R.id.btnTestPopup).setOnClickListener {
            PrescriptTrigger.fire(this, "COMMUNICATION")
        }

        findViewById<Button>(R.id.btnTestCalendar).setOnClickListener {
            CalendarAlarmReceiver.checkAndFire(this)
        }

        findViewById<Button>(R.id.btnTestBatteryLow).setOnClickListener {
            PrescriptTrigger.fire(
                this, "BATTERY_LOW",
                overrideText = "${PrescriptLines.getLine(this, "BATTERY_LOW")} (TEST)"
            )
        }

        findViewById<Button>(R.id.btnTestBatteryOk).setOnClickListener {
            PrescriptTrigger.fire(
                this, "BATTERY_OK",
                overrideText = "${PrescriptLines.getLine(this, "BATTERY_OK")} (TEST)"
            )
        }

        findViewById<Button>(R.id.btnTestEarbuds).setOnClickListener {
            PrescriptTrigger.fire(
                this, "EARBUDS_LOW",
                overrideText = "${PrescriptLines.getLine(this, "EARBUDS_LOW")} (Test Earbuds, 15%)"
            )
        }

        findViewById<Button>(R.id.btnTestReminder).setOnClickListener {
            PrescriptTrigger.fire(this, "REMINDER", overrideText = "THIS IS A TEST REMINDER.")
        }
    }
}