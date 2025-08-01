KidShield - Android Parental Control App
 
KidShield is an open-source Android parental control app designed to help parents monitor their children's device usage and ensure their digital safety. Powered by a robust background service, it provides real-time monitoring with minimal battery impact.
⚠️ Disclaimer: For Educational Purposes OnlyThis app is intended for educational purposes to demonstrate advanced Android concepts like background services, permissions, and Firebase integration.Do not use for illegal or unethical activities. Users are solely responsible for complying with all applicable laws. Developers assume no liability for misuse. Install only on devices you own or have explicit legal permission to monitor.
Features

Real-time Location Tracking: View your child's location on an interactive map with frequent updates.
App Blocker: List all installed apps on the child's device and block/unblock them from the parent's dashboard.
Call & SMS Monitoring: Securely access call and SMS logs to stay informed about communications.
Persistent Service: Runs a lightweight, unstoppable foreground service for constant protection.
Auto-Restart: Monitoring service restarts automatically on device reboot.

Architecture
KidShield uses a modern, battery-efficient hybrid architecture:

Persistent Foreground Service: Runs 24/7 with a non-removable notification, checking for blocked apps every 2 seconds for instant action with minimal resource usage.
AlarmManager for Syncs: Handles heavy tasks (location, call/SMS logs) via Android’s AlarmManager, syncing every 1-5 minutes for robust, battery-friendly operation.

This ensures the service resists OS termination while balancing responsiveness and power efficiency.
Screenshots



Parent Dashboard
Child Detail View
App Blocker








Setup Guide
Prerequisites

Android Studio (latest version recommended)
Physical Android device (emulators may not support all features)
Firebase account
Google Cloud account

Step 1: Clone the Repository
git clone https://github.com/Yousaf1204/KidShield.git
cd KidShield

Step 2: Firebase Setup

Create a Firebase Project:
Go to Firebase Console.
Click "Add project" and follow the steps.


Enable Authentication:
In the Authentication section, enable the "Email/Password" provider.


Set up Realtime Database:
Create a Realtime Database in test mode.
Update the "Rules" tab with:{
  "rules": {
    "users": {
      "$uid": {
        ".read": "auth != null && auth.uid == $uid",
        ".write": "auth != null && auth.uid == $uid"
      }
    }
  }
}




Add Android App:
In Firebase, add an Android app with package name: com.yousafdev.KidShield.
Download google-services.json and place it in KidShield/app/.



Step 3: Google Maps API Key

Enable Maps SDK:
In Google Cloud Console, select your Firebase project.
Enable "Maps SDK for Android" in APIs & Services > Library.


Get API Key:
Go to APIs & Services > Credentials, create an API key.
Restrict it to Android apps with package name com.yousafdev.KidShield and your debug SHA-1 fingerprint.


Add API Key:
In app/src/main/AndroidManifest.xml, replace YOUR_API_KEY_HERE in:<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_API_KEY_HERE" />





Step 4: Build and Run

Connect a physical Android device.
In Android Studio, click "Run" to install and launch the app.
Register parent and child accounts to test functionality.

License
This project is licensed under the MIT License.
Contributing
Contributions are welcome! Please open an issue or submit a pull request for bug fixes or enhancements.
