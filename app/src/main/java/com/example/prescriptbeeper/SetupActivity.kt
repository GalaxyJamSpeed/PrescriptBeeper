package com.example.prescriptbeeper

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SetupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        findViewById<Button>(R.id.btnOpenPermissions).setOnClickListener {
            startActivity(Intent(this, PermissionsActivity::class.java))
        }

        findViewById<Button>(R.id.btnOpenTests).setOnClickListener {
            startActivity(Intent(this, TestsActivity::class.java))
        }

        findViewById<Button>(R.id.btnOpenAbout).setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }
    }
}