# KidShield — Android Parental Control Application

KidShield is a powerful, open-source parental control solution designed for Android devices. It empowers parents to monitor their children's digital activity, enforce usage boundaries, and ensure online safety with minimal impact on device performance.

> **⚠️ Educational Use Only**
> This project is intended for educational purposes to demonstrate advanced Android concepts such as persistent background services, permissions, and Firebase integration. Do not use this application for unlawful or unethical monitoring. Only install it on devices you own or have explicit permission to monitor. The developers assume no responsibility for misuse.

---

## ✨ Features

* **Real-time Location Tracking**
  Receive frequent location updates and visualize the child's position on an interactive map.

* **App Blocker**
  View and remotely block/unblock installed applications on the child's device.

* **Call & SMS Log Monitoring**
  Securely access communication logs to stay informed.

* **Persistent Background Service**
  Runs as a foreground service that resists termination by the system or user.

* **Auto-Restart on Boot**
  Ensures the monitoring service restarts automatically after device reboot.

---

## 🚀 Architecture Overview

KidShield is built using a modern hybrid architecture optimized for reliability and battery efficiency:

* **Persistent Foreground Service**
  Executes every 2 seconds to check if the currently active app is in the blocked list and takes immediate action.

* **AlarmManager for Periodic Sync**
  Handles heavier sync tasks (location, SMS, call logs) every 1-5 minutes for optimal power usage.

---

## 🖼️ Screenshots

### Parent Dashboard

<img width="245" height="435" alt="Parent Dashboard" src="https://github.com/user-attachments/assets/c34d5b24-09a8-41d0-9b03-fc1af5c20534" />

### Child Detail View

<img width="246" height="429" alt="Child Detail View" src="https://github.com/user-attachments/assets/02582bde-12cd-478e-84ae-e105782d145e" />

### App Blocker

<img width="232" height="398" alt="App Blocker" src="https://github.com/user-attachments/assets/1323d88a-f1bd-43bf-8482-c0740886a3eb" />
<img width="240" height="433" alt="App Blocker" src="https://github.com/user-attachments/assets/e889fb81-ebf5-4f7f-98c6-8e2f26fb0e74" />

---

## 📖 Project Setup Guide

### Prerequisites

* Android Studio (latest recommended)
* Physical Android device (emulators may not support all features)

### ✅ Step 1: Clone the Repository

```bash
git clone https://github.com/Yousaf1204/KidShield.git
cd KidShield
```

### ✅ Step 2: Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com/).
2. Create a new project.
3. Enable **Email/Password** in Authentication settings.
4. Set up **Realtime Database**:

   * Choose "Start in test mode"
   * Set rules for authenticated access:

```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "auth != null && auth.uid == $uid",
        ".write": "auth != null && auth.uid == $uid"
      }
    }
  }
}
```

5. Register your Android app in Firebase:

   * Use package name: `com.yousafdev.KidShield`
   * Download `google-services.json` and place it in `KidShield/app/`

### ✅ Step 3: Google Maps API Key

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Enable **Maps SDK for Android**
3. Create and restrict an **API Key**:

   * Restrict to package `com.yousafdev.KidShield`
   * Add your debug SHA-1 key
4. Insert API key in `AndroidManifest.xml`:

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_API_KEY_HERE" />
```

### ✅ Step 4: Build & Run

* Connect your Android device
* Run the project from Android Studio
* Register as a parent and child to explore all features

---

## 📝 License

This project is licensed under the [MIT License](LICENSE).

---

## 📈 Contributing

We welcome all contributions! Feel free to:

* Submit issues
* Suggest improvements
* Create pull requests to fix bugs or add features

---
