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
    }
}