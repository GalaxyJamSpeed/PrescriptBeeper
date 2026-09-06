package com.example.prescriptbeeper

import android.app.Notification
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.graphics.PixelFormat
import android.bluetooth.BluetoothClass

class NotificationForwarderService : NotificationListenerService() {

    private val lastFiredPerPackage = mutableMapOf<String, Long>()

    private val lastFiredKeyPerPackage = mutableMapOf<String, String>()

    private var badgeView: View? = null
    private var windowManager: WindowManager? = null

    private val earbudsBatteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            val level = intent.getIntExtra("android.bluetooth.device.extra.BATTERY_LEVEL", -1)
            if (device == null || level == -1) return

            val isAudioDevice = try {
                device.bluetoothClass?.majorDeviceClass == BluetoothClass.Device.Major.AUDIO_VIDEO
            } catch (e: SecurityException) { false }
            if (!isAudioDevice) return

            val deviceAddress = try { device.address } catch (e: SecurityException) { null } ?: return
            val deviceName = try { device.name } catch (e: SecurityException) { null } ?: "Earbuds"

            val prefs = getSharedPreferences("prescript_prefs", MODE_PRIVATE)
            val threshold = prefs.getInt("earbuds_low_threshold", 30)
            val alertedDevices = (prefs.getStringSet("earbuds_alerted_devices", emptySet()) ?: emptySet()).toMutableSet()

            if (level < threshold && deviceAddress !in alertedDevices) {
                alertedDevices.add(deviceAddress)
                prefs.edit().putStringSet("earbuds_alerted_devices", alertedDevices).apply()
                PrescriptTrigger.fire(
                    applicationContext, "EARBUDS_LOW",
                    overrideText = "${PrescriptLines.getLine(applicationContext, "EARBUDS_LOW")} ($deviceName, $level%)"
                )
            } else if (level >= threshold && deviceAddress in alertedDevices) {
                alertedDevices.remove(deviceAddress)
                prefs.edit().putStringSet("earbuds_alerted_devices", alertedDevices).apply()
            }
        }
    }

    private val phoneBatteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level == -1 || scale == -1) return

            val percent = (level * 100) / scale

            val prefs = getSharedPreferences("prescript_prefs", MODE_PRIVATE)
            val threshold = prefs.getInt("battery_low_threshold", 20)
            val alreadyAlerted = prefs.getBoolean("phone_battery_alerted", false)

            val restoredThreshold = prefs.getInt("battery_restored_threshold", 80)

            if (percent <= threshold && !alreadyAlerted) {
                prefs.edit().putBoolean("phone_battery_alerted", true).apply()
                PrescriptTrigger.fire(
                    applicationContext, "BATTERY_LOW",
                    overrideText = "${PrescriptLines.getLine(applicationContext, "BATTERY_LOW")} ($percent%)"
                )
            } else if (percent >= restoredThreshold && alreadyAlerted) {
                prefs.edit().putBoolean("phone_battery_alerted", false).apply()
                PrescriptTrigger.fire(
                    applicationContext, "BATTERY_OK",
                    overrideText = "${PrescriptLines.getLine(applicationContext, "BATTERY_OK")} ($percent%)"
                )
            }
        }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        registerReceiver(
            earbudsBatteryReceiver,
            IntentFilter("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED")
        )
        registerReceiver(phoneBatteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        updateUnreadBadge()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        try { unregisterReceiver(earbudsBatteryReceiver) } catch (e: Exception) {}
        try { unregisterReceiver(phoneBatteryReceiver) } catch (e: Exception) {}
        hideBadge()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        super.onNotificationPosted(sbn)
        if (sbn.packageName == packageName) return

        val excludedApps = ExcludedAppsConfig.getExcludedApps(applicationContext)
        val watchedApps = WatchedAppsConfig.getWatchedApps(applicationContext)

        val isExcluded = sbn.packageName in excludedApps
        val category = watchedApps[sbn.packageName]

        if (!isExcluded && category == null) return

        val prefs = getSharedPreferences("prescript_prefs", MODE_PRIVATE)

        val unread = (prefs.getStringSet("unread_packages", emptySet()) ?: emptySet()).toMutableSet()
        unread.add(sbn.packageName)
        prefs.edit().putStringSet("unread_packages", unread).apply()
        updateUnreadBadge()

        if (isExcluded) return

        if (lastFiredKeyPerPackage[sbn.packageName] == sbn.key) return

        val cooldownSeconds = prefs.getInt("app_cooldown_seconds", 20)
        val now = SystemClock.elapsedRealtime()
        val lastFired = lastFiredPerPackage[sbn.packageName]

        if (lastFired == null || now - lastFired >= cooldownSeconds * 1000L) {
            lastFiredPerPackage[sbn.packageName] = now
            lastFiredKeyPerPackage[sbn.packageName] = sbn.key

            val extras = sbn.notification.extras
            val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
            val body = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
            val content = listOf(title, body).filter { it.isNotBlank() }.joinToString(": ")

            PrescriptTrigger.fire(
                applicationContext, category!!,
                sourcePackage = sbn.packageName,
                notificationContent = content,
                senderName = title.ifBlank { null }
            )
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        super.onNotificationRemoved(sbn)
        val excludedApps = ExcludedAppsConfig.getExcludedApps(applicationContext)
        val watchedApps = WatchedAppsConfig.getWatchedApps(applicationContext)

        if (sbn.packageName in excludedApps || sbn.packageName in watchedApps.keys) {
            lastFiredPerPackage.remove(sbn.packageName)
            lastFiredPerPackage.remove(sbn.packageName)
            lastFiredKeyPerPackage.remove(sbn.packageName)

            val prefs = getSharedPreferences("prescript_prefs", MODE_PRIVATE)
            val unread = (prefs.getStringSet("unread_packages", emptySet()) ?: emptySet()).toMutableSet()
            unread.remove(sbn.packageName)
            prefs.edit().putStringSet("unread_packages", unread).apply()
            updateUnreadBadge()
        }
    }

    private fun updateUnreadBadge() {
        val prefs = getSharedPreferences("prescript_prefs", MODE_PRIVATE)
        val unread = prefs.getStringSet("unread_packages", emptySet()) ?: emptySet()
        if (unread.isNotEmpty()) showBadge() else hideBadge()
    }

    private fun showBadge() {
        if (badgeView != null) return
        if (!Settings.canDrawOverlays(this)) return

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val view = ImageView(this).apply {
            setImageResource(R.drawable.ictargetflower)
            setOnClickListener {
                val intent = Intent(this@NotificationForwarderService, MarkedAppsActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        badgeView = view

        val overlayType =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            140, 140,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.END
        params.x = 20
        params.y = 200

        windowManager?.addView(view, params)
    }

    private fun hideBadge() {
        badgeView?.let {
            try { windowManager?.removeView(it) } catch (e: Exception) {}
        }
        badgeView = null
    }
}