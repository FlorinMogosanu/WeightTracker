# WeightTracker Android 📱⚖️

A modern, offline-first native Android Weight Tracker built with **Jetpack Compose**, **Material 3**, and **Clean Architecture**. Designed for daily personal tracking and couples, supporting two independent local profiles with zero cloud dependency.

---

## ✨ Features

- 👥 **Dual Local Profiles ("Me" & "Partner"):** Completely isolated weight history, target settings, and stats for two people.
- ⚡ **Smart Weight Logging:**
  - Auto-pre-fills with your current week's average for fast logging.
  - Increment/decrement with `+` and `-` buttons in 0.1 increments.
  - Tap-to-clear: Tapping the weight box instantly clears the auto-filled average for manual entry.
  - Optional notes (e.g., fasted, post-workout).
- 📅 **Monday-First Calendar:** Built-in date picker adhering to Monday-as-first-day-of-the-week.
- 📊 **Weekly Canvas Trend Charts:** Custom hardware-accelerated Canvas line charts displaying 7-day progress and averages.
- 🧹 **Autonomous Data Retention / Auto-Pruning:** Background Room database worker keeping daily entries for 30 days, consolidating weekly past 30 days, and monthly past 1 year.
- ⏰ **Morning Reminders:** WorkManager notification reminders scheduled for morning weigh-ins.
- 🔒 **100% Offline & Private:** Powered by a local Room SQLite database. No tracking, no accounts, and no cloud sync required.

---

## 🛠️ Tech Stack & Architecture

- **UI:** Jetpack Compose, Material 3
- **Language:** Kotlin (Coroutines, StateFlow)
- **Architecture:** Clean Architecture + MVVM (Domain, Data, Presentation layers)
- **Database:** Room (SQLite) with KSP
- **Preferences:** Jetpack DataStore Preferences
- **Background Tasks:** AndroidX WorkManager
- **Navigation:** Navigation Compose

---

## 🚀 Building & Running

### Prerequisites
- Android Studio Iguana / Jellyfish / Ladybug or newer
- JDK 17
- Android SDK (compileSdk 34, minSdk 26)

### CLI Build
```bash
# Clone the repository
git clone <repository-url>
cd WeightTrackerAndroid

# Build debug APK
./gradlew assembleDebug

# Install on connected device (via USB or Wireless ADB)
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 License
MIT License. Free to use and modify for personal or commercial projects.
