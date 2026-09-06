package com.example.prescriptbeeper

import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.util.Log

class PrescriptActivity : AppCompatActivity() {

    private val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789#_@%&"
    private val handler = Handler(Looper.getMainLooper())
    private var frame = 0
    private val totalFrames = 26

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("PrescriptDebug", "PrescriptActivity launched!")
        setContentView(R.layout.activity_prescript)

        val target = intent.getStringExtra("PRESCRIPT_TEXT") ?: "SOMETHING STIRS."
        val textView = findViewById<TextView>(R.id.tvPrescript)

        playBeep()
        runScramble(textView, target)

        findViewById<Button>(R.id.btnAccept).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnDismiss).setOnClickListener { finish() }
    }

    private fun playBeep() {
        val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
        tone.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
    }

    private fun runScramble(textView: TextView, target: String) {
        val runnable = object : Runnable {
            override fun run() {
                frame++
                val revealCount = (frame.toFloat() / totalFrames * target.length).toInt()
                if (revealCount >= target.length) {
                    textView.text = "_${target}._"
                    return
                }
                val revealed = target.substring(0, revealCount)
                val scrambled = (0 until target.length - revealCount)
                    .map { chars.random() }
                    .joinToString("")
                textView.text = "_$revealed$scrambled._"
                handler.postDelayed(this, 90)
            }
        }
        handler.post(runnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}