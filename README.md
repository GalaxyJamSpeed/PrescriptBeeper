<div align="center">

<img src="app/src/main/res/drawable/icdeviceicon.png" width="110" alt="PrescriptBeeper device icon">

# PrescriptBeeper

**A fan-made Android notification system inspired by the Index's Prescript Beeper from *Limbus Company***

![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF?logo=kotlin&logoColor=white)
![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Min SDK](https://img.shields.io/badge/Min%20SDK-26-blue)
![License](https://img.shields.io/badge/License-Personal%20Project-lightgrey)

</div>

---

## What is this?

PrescriptBeeper turns ordinary phone events — texts, calls, calendar events, low battery, low earbuds battery — into stylized, full-screen **"Prescripts"**, mimicking the in-universe Index device from *Limbus Company*. Instead of a normal Android notification, you get a floating popup with a scramble-to-reveal text animation, a custom beep, and an Accept/Dismiss choice, styled after the device's cyan-on-black display.

<p align="center">
  <img src="app/src/main/res/drawable/ictargetflower.png" width="70">
  &nbsp;&nbsp;&nbsp;
  <img src="app/src/main/res/drawable/icunlock1.png" width="70">
  <img src="app/src/main/res/drawable/icunlock2.png" width="70">
  <img src="app/src/main/res/drawable/icunlock3.png" width="70">
</p>

This started as a first-ever Kotlin project, built entirely feature-by-feature — see [Tech Notes](#tech-notes--hyperos-quirks) for the Android/HyperOS-specific issues solved along the way.

---

## Features

### 🔔 The Prescript Popup
- Full-screen floating overlay (draws over any app, not just the lock screen)
- Character-scramble reveal animation before the final line locks in
- Custom beep sound, with an in-app volume slider
- Configurable auto-dismiss timer with a live countdown badge
- Shows the sending app's icon + sender name for messages/calls
- Accept opens the source app directly; Dismiss just closes it
- A footer tracks daily completions and displays your current **Unlock Stage** (1–3), resetting at midnight

### 📡 Real-World Triggers
- **Messages & Calls** — via notification listening, fully configurable per app
- **Calendar** — a daily check against your phone's synced Google Calendar, once each morning
- **Phone Battery** — low-battery and restored-battery alerts, both thresholds adjustable
- **Bluetooth Earbuds/Headphones** — low-battery alerts for *any* connected Bluetooth audio device, not tied to one brand
- Per-app cooldowns and duplicate-notification protection, so one message never spams multiple popups

### 🗂️ App Categories & Exclusions
- Assign any installed app to **Messages/Calls** or **Calendar**, or leave it untouched
- **Exclusions** — mark specific apps (games, sensitive apps, etc.) to be silently tracked with a small on-screen flower badge, with no popup or sound at all
- Both screens support live search across every installed app

### 📊 Stats & History
- **Prescript Log** — the last 20 prescripts, with real notification content, sender, and outcome (Accepted / Dismissed / Expired)
- **Stats** — Day, Week, and Month views with a custom bar chart, busiest-day tracking, and a category breakdown
- **Home Screen Widget** — today's completion count and current stage, at a glance

### 💬 Speak With The Index
- An in-app chatbot themed entirely around the Index — always in-character, always in caps, powered by Google's Gemini API (free tier)

### ⚙️ Fully Configurable
- Editable prescript line pools per category — add or remove your own custom lines anytime
- Adjustable volume, popup duration, per-app cooldown, and battery thresholds
- A single switch to pause the entire system without uninstalling anything

---

## Setup

1. Download the APK from [Releases](../../releases)
2. Tap the file on your Android phone and approve the one-time "install from this source" prompt
3. Open the app and walk through the **Permissions** screen (in Setup & Testing):
   - Notification Access
   - Display Over Other Apps
   - Calendar Access
   - Bluetooth & Notifications (requested automatically)

> **Note:** the shared APK includes a Gemini API key baked in for the chatbot feature, using the maintainer's free-tier key. If you'd rather use your own, build from source below and supply your own key.

### A note for Xiaomi/HyperOS devices
Several permissions on MIUI/HyperOS are hidden behind extra manufacturer-specific toggles beyond stock Android's settings — if popups aren't appearing, check:
- **Autostart** permission for the app
- **Display pop-up windows while running in the background**
- **Battery saver** set to "No restrictions" for this app

---

## Tech Notes / HyperOS Quirks

This project intentionally avoids two "expected" approaches that turned out to be unreliable on real hardware:

- **Full-screen intent notifications** (the usual way to show a popup over the lock screen) were silently blocked by HyperOS's internal allowlist. The app uses a `WindowManager` overlay service instead, which works regardless of OEM restrictions.
- **`ACTION_BATTERY_LOW`** is a fixed, OS-defined threshold (~15%, not adjustable). Battery monitoring instead reads the live percentage directly and compares it to a user-set threshold.

---

## Disclaimer

This is an unofficial, non-commercial fan project inspired by *Limbus Company*, created by Project Moon. All game-related names, concepts, and imagery referenced belong to their respective owners. PrescriptBeeper is a personal utility app and is not affiliated with or endorsed by Project Moon.
