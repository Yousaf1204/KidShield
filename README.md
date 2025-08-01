KidShield - Android Parental Control Application
<!-- You can upload your app icon to a service like Imgur and place the link here -->

KidShield is a powerful, open-source parental control application for Android. It is designed to provide parents with essential tools to monitor their children's device usage and ensure their safety in the digital world. The application uses a robust, unstoppable background service to provide reliable, real-time monitoring.

⚠️ Disclaimer: For Educational Purposes Only
This application is intended for educational purposes only. It demonstrates advanced Android concepts, including background services, permissions, and integration with Firebase.

Do not use this application for any illegal or unethical activities. The user of this software is solely responsible for complying with all applicable local, state, and federal laws. The developers of this application assume no liability and are not responsible for any misuse or damage caused by this program. Only install this application on devices that you own or have explicit, legal permission to monitor.

Features
Real-time Location Tracking: Get frequent updates on the child's geographical location, displayed on an interactive map.

App Blocker: View a list of all applications installed on the child's device and block or unblock them with a single tap from the parent's dashboard.

Call & SMS Log Monitoring: Securely access the child's call and SMS history to stay informed about their communications.

Persistent Background Service: The application runs a lightweight, unstoppable foreground service that cannot be easily terminated by the user or the OS, ensuring constant protection.

Automatic Restart: The monitoring service automatically restarts when the device is rebooted.

Architecture Overview
KidShield employs a modern, hybrid architecture to ensure reliability and battery efficiency:

Persistent Foreground Service: A lightweight service runs 24/7 with a non-removable notification. Its only job is to perform a fast, frequent loop (every 2 seconds) to check if the currently used app is on the blocked list. This ensures immediate blocking action with minimal resource usage.

AlarmManager for Data Syncs: All heavy data synchronization tasks (location, call logs, SMS logs) are offloaded to the Android AlarmManager. This core system component wakes the device at a set interval (e.g., every 1-5 minutes), tells the service to perform a data sync, and then lets the device rest. This is the most robust and battery-efficient method for periodic tasks.

This combination prevents the service from being killed by the OS and provides the perfect balance between real-time responsiveness and battery conservation.

Screenshots

Parent Dashboard
<img width="245" height="435" alt="image" src="https://github.com/user-attachments/assets/c34d5b24-09a8-41d0-9b03-fc1af5c20534" />

Child Detail View
<img width="246" height="429" alt="image" src="https://github.com/user-attachments/assets/02582bde-12cd-478e-84ae-e105782d145e" />

App Blocker

<img width="232" height="398" alt="image" src="https://github.com/user-attachments/assets/1323d88a-f1bd-43bf-8482-c0740886a3eb" />
<img width="240" height="433" alt="image" src="https://github.com/user-attachments/assets/e889fb81-ebf5-4f7f-98c6-8e2f26fb0e74" />







#Project Setup Guide
To get this project running, you will need to set up your own Firebase backend and Google Maps API key.

Prerequisites
Android Studio (latest version recommended)

A physical Android device for testing (emulators may not fully support all features like device admin).

Step 1: Clone the Repository
git clone [https://github.com/Yousaf1204/KidShield.git)
cd KidShield

Step 2: Firebase Setup
This project requires a Firebase project to handle authentication and data storage.

Create a Firebase Project:

Go to the Firebase Console.

Click on "Add project" and follow the on-screen instructions to create a new project.

Set up Authentication:

In your new project, go to the Authentication section.

Click on the "Sign-in method" tab.

Enable the "Email/Password" provider.

Set up Realtime Database:

Go to the Realtime Database section.

Click "Create Database" and start in test mode.

Important: After creation, go to the "Rules" tab and paste the following rules. These are more secure and ensure that only authenticated users can read/write their own data.

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

Add Android App to Firebase:

On the project overview page, click the Android icon to add a new Android app.

Package Name: You must use the package name from this project, which is com.yousafdev.KidShield.

Follow the steps and download the google-services.json file.

Add google-services.json to Project:

Switch your Android Studio project view from "Android" to "Project".

Copy the google-services.json file you downloaded and paste it into the app directory of the project (KidShield/app/google-services.json).

Step 3: Google Maps API Key
To display the map in the parent's dashboard, you need a Google Maps API key.

Go to Google Cloud Console:

Navigate to the Google Cloud Console.

Select the same project that was created by Firebase.

Enable Maps SDK for Android:

In the navigation menu, go to "APIs & Services" > "Library".

Search for "Maps SDK for Android" and enable it for your project.

Get Your API Key:

Go to "APIs & Services" > "Credentials".

Click "Create Credentials" > "API key".

Copy the generated API key.

Recommended: Restrict your API key to prevent unauthorized use. Click on the new key and under "Application restrictions," select "Android apps" and add the package name (com.yousafdev.KidShield) and the SHA-1 certificate fingerprint of your debug key.

Add API Key to AndroidManifest.xml:

Open the app/src/main/AndroidManifest.xml file.

Find the following line:

<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_API_KEY_HERE" />

Replace "YOUR_API_KEY_HERE" with the API key you just copied.

Step 4: Build and Run
You are now ready to build and run the application.

Connect your physical Android device.

Click the "Run" button in Android Studio.

The app will install and launch on your device. You can now register a parent and a child account to test the full functionality.

License
This project is licensed under the MIT License. See the LICENSE file for details.

Contributing
Contributions are welcome! If you have ideas for improvements or find any bugs, feel free to open an issue or submit a pull request.
