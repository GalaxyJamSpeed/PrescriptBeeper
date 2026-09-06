package com.example.prescriptbeeper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.NotificationCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.LinearLayout

class PrescriptOverlayService : Service() {

    private var overlayView: View? = null
    private var windowManager: WindowManager? = null
    private val handler = Handler(Looper.getMainLooper())
    private var frame = 0
    private val totalFrames = 26
    private val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789#_@%&"
    private var sourcePackage: String? = null
    private var currentLogId: String = ""
    private var notificationContent: String = ""

    private var senderName: String? = null

    private var category: String = "GENERIC"

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startAsForeground()
        senderName = intent?.getStringExtra("SENDER_NAME")
        category = intent?.getStringExtra("CATEGORY") ?: "GENERIC"
        val text = intent?.getStringExtra("PRESCRIPT_TEXT") ?: "SOMETHING STIRS."
        sourcePackage = intent?.getStringExtra("SOURCE_PACKAGE")
        notificationContent = intent?.getStringExtra("NOTIFICATION_CONTENT") ?: ""
        showOverlay(text)
        return START_NOT_STICKY
    }

    private fun removeCurrentOverlay() {
        handler.removeCallbacksAndMessages(null)
        overlayView?.let {
            try { windowManager?.removeView(it) } catch (e: Exception) {}
        }
        overlayView = null
    }

    private fun startAsForeground() {
        val channelId = "prescript_overlay_channel"
        val manager = getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(
                channelId, "Prescript Overlay", NotificationManager.IMPORTANCE_MIN
            ).apply {
                setSound(null, null)
                enableVibration(false)
            }
            manager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("")
            .setContentText("")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
        startForeground(2001, notification)
    }

    private fun showOverlay(target: String) {
        resetCounterIfNewDay()
        removeCurrentOverlay()

        val appName = sourcePackage?.let { pkg ->
            try { packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString() }
            catch (e: Exception) { pkg }
        } ?: "System"

        currentLogId = PrescriptLog.addEntry(this, appName, target, notificationContent)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val view = LayoutInflater.from(this).inflate(R.layout.overlay_prescript, null)
        overlayView = view

        val overlayType =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.CENTER

        windowManager?.addView(view, params)

        playBeep()

        val textView = view.findViewById<TextView>(R.id.tvPrescript)
        runScramble(textView, target)

        if (category == "COMMUNICATION" && sourcePackage != null) {
            val appLabel = try {
                packageManager.getApplicationLabel(packageManager.getApplicationInfo(sourcePackage!!, 0)).toString()
            } catch (e: Exception) { sourcePackage }
            val icon = try { packageManager.getApplicationIcon(sourcePackage!!) } catch (e: Exception) { null }

            view.findViewById<ImageView>(R.id.ivSenderIcon).setImageDrawable(icon)
            view.findViewById<TextView>(R.id.tvSenderInfo).text =
                if (senderName.isNullOrBlank()) appLabel else "$appLabel • $senderName"
            view.findViewById<LinearLayout>(R.id.senderInfoRow).visibility = View.VISIBLE
        }

        view.findViewById<Button>(R.id.btnAccept).setOnClickListener {
            resetCounterIfNewDay()
            val p = getSharedPreferences("prescript_prefs", MODE_PRIVATE)
            p.edit().putInt("prescripts_completed", p.getInt("prescripts_completed", 0) + 1).apply()
            PrescriptLog.updateStatus(applicationContext, currentLogId, "ACCEPTED")
            WeeklyStats.recordAccept(applicationContext, category)
            PrescriptWidgetProvider.updateAll(applicationContext)

            sourcePackage?.let { pkg ->
                val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
                launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                launchIntent?.let { startActivity(it) }
            }
            dismiss()
        }

        view.findViewById<Button>(R.id.btnDismiss).setOnClickListener {
            PrescriptLog.updateStatus(applicationContext, currentLogId, "DISMISSED")
            dismiss()
        }

        val prefs = getSharedPreferences("prescript_prefs", MODE_PRIVATE)

        val completedCount = prefs.getInt("prescripts_completed", 0)
        val (stage, stageIcon) = stageFor(completedCount)
        view.findViewById<TextView>(R.id.tvFooterCount).text = "$completedCount prescripts completed — Stage $stage"
        view.findViewById<ImageView>(R.id.ivStageIcon).setImageResource(stageIcon)

        val duration = prefs.getInt("popup_duration_seconds", 5)
        startCountdown(duration)
    }

    private fun startCountdown(totalSeconds: Int) {
        var remaining = totalSeconds
        val countdownView = overlayView?.findViewById<TextView>(R.id.tvCountdown)
        countdownView?.text = "${remaining}s"

        val countdownRunnable = object : Runnable {
            override fun run() {
                remaining--
                if (remaining <= 0) {
                    PrescriptLog.updateStatus(applicationContext, currentLogId, "EXPIRED")
                    dismiss()
                    return
                }
                countdownView?.text = "${remaining}s"
                handler.postDelayed(this, 1000)
            }
        }
        handler.postDelayed(countdownRunnable, 1000)
    }

    private fun playBeep() {
        val prefs = getSharedPreferences("prescript_prefs", MODE_PRIVATE)
        val volume = prefs.getFloat("beep_volume", 1.0f)

        val mediaPlayer = MediaPlayer.create(this, R.raw.prescript_1)
        mediaPlayer?.setVolume(volume, volume)
        mediaPlayer?.setOnCompletionListener { it.release() }
        mediaPlayer?.start()
    }

    private fun runScramble(textView: TextView, target: String) {
        frame = 0
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
                    .map { chars.random() }.joinToString("")
                textView.text = "_$revealed$scrambled._"
                handler.postDelayed(this, 90)
            }
        }
        handler.post(runnable)
    }

    private fun dismiss() {
        removeCurrentOverlay()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        overlayView?.let {
            try { windowManager?.removeView(it) } catch (e: Exception) {}
        }
    }

    private fun stageFor(count: Int): Pair<Int, Int> {
        return when {
            count >= 20 -> 3 to R.drawable.icunlock3
            count >= 10 -> 2 to R.drawable.icunlock2
            else -> 1 to R.drawable.icunlock1
        }
    }

    private fun resetCounterIfNewDay() {
        val prefs = getSharedPreferences("prescript_prefs", MODE_PRIVATE)
        val today = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val lastDate = prefs.getString("last_reset_date", null)

        if (lastDate != today) {
            prefs.edit()
                .putInt("prescripts_completed", 0)
                .putString("last_reset_date", today)
                .apply()
            PrescriptWidgetProvider.updateAll(this)
        }
    }
}