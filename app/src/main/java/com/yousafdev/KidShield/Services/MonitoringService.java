package com.yousafdev.KidShield.Services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.CallLog;
import android.provider.Telephony;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yousafdev.KidShield.Activities.BlockedScreenActivity;
import com.yousafdev.KidShield.R;
import com.yousafdev.KidShield.Utils.AlarmReceiver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

public class MonitoringService extends Service {

    // TODO - add screen locking functionality

    private static final String TAG = "MonitoringService";
    public static final String CHANNEL_ID = "KidShieldServiceChannel";
    public static final int NOTIFICATION_ID = 1;

    // Action for the intent that triggers data sync
    public static final String ACTION_SYNC_DATA = "com.yousafdev.KidShield.ACTION_SYNC_DATA";

    private Handler handler = new Handler();
    private Runnable appCheckRunnable;
    private static final long APP_CHECK_INTERVAL = 2000;      // 2 seconds for app blocking check

    private FusedLocationProviderClient fusedLocationClient;
    private DatabaseReference databaseReference;
    private FirebaseUser currentUser;

    private UsageStatsManager usageStatsManager;
    private HashSet<String> blockedApps = new HashSet<>();
    private String lastForegroundApp = "";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service Created");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Log.e(TAG, "No user logged in, stopping service.");
            stopSelf();
            return;
        }

        databaseReference = FirebaseDatabase.getInstance().getReference("users")
                .child(currentUser.getUid());

        appCheckRunnable = () -> {
            checkForegroundApp();
            handler.postDelayed(appCheckRunnable, APP_CHECK_INTERVAL);
        };

        listenForBlockedApps();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service Started");
        createNotificationChannel();

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("KidShield Protection is Active")
                .setContentText("Monitoring for your safety.")
                .setSmallIcon(R.drawable.ic_check)
                .setPriority(NotificationCompat.PRIORITY_MIN) // Use MIN to be less intrusive
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION | ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }

        // Start the continuous app check loop
        handler.removeCallbacks(appCheckRunnable);
        handler.post(appCheckRunnable);

        // Check if the intent is for a data sync
        if (intent != null && ACTION_SYNC_DATA.equals(intent.getAction())) {
            Log.d(TAG, "Received data sync request from AlarmManager.");
            performDataSync();
        }

        // We want this service to continue running until it is explicitly stopped,
        // so we return START_STICKY.
        return START_STICKY;
    }

    private void performDataSync() {
        Log.d(TAG, "Performing background data sync...");
        fetchAndUploadLocation();
        fetchAndUploadCallLogs();
        fetchAndUploadSmsLogs();
        fetchAndUploadInstalledApps();
    }


    private void listenForBlockedApps() {
        databaseReference.child("blocked_apps").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                blockedApps.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (Boolean.TRUE.equals(snapshot.getValue(Boolean.class))) {
                        // Firebase keys cannot contain '.', so we replace them with '_' when storing.
                        // We must revert this change when retrieving.
                        blockedApps.add(snapshot.getKey().replace("_", "."));
                    }
                }
                Log.d(TAG, "Blocked apps list updated: " + blockedApps.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to listen for blocked apps", databaseError.toException());
            }
        });
    }

    private void checkForegroundApp() {
        if (usageStatsManager == null) return;

        long time = System.currentTimeMillis();
        // We query for events in the last 10 seconds.
        List<UsageStats> appList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time);

        if (appList != null && !appList.isEmpty()) {
            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
            for (UsageStats usageStats : appList) {
                mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
            }
            if (!mySortedMap.isEmpty()) {
                String currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();

                // If the foreground app has changed and is in our blocked list
                if (!currentApp.equals(lastForegroundApp) && blockedApps.contains(currentApp)) {
                    Log.d(TAG, "Blocked app detected in foreground: " + currentApp);

                    // Launch the block screen
                    Intent intent = new Intent(getApplicationContext(), BlockedScreenActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
                lastForegroundApp = currentApp;
            }
        }
    }

    private void fetchAndUploadInstalledApps() {
        PackageManager pm = getPackageManager();
        // Get a list of all installed applications.
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        DatabaseReference appsRef = databaseReference.child("installed_apps");
        appsRef.removeValue(); // Clear the list before adding new ones

        for (ApplicationInfo app : apps) {
            // Check if the application has a launch intent. If not, it's a system app.
            if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                // We don't want to block our own app
                if(app.packageName.equals(getPackageName())) {
                    continue;
                }

                String appName = app.loadLabel(pm).toString();
                String packageName = app.packageName;

                HashMap<String, String> appData = new HashMap<>();
                appData.put("appName", appName);
                appData.put("packageName", packageName);

                // Firebase keys can't contain '.', so we replace them.
                appsRef.child(packageName.replace(".", "_")).setValue(appData);
            }
        }
    }


    @SuppressLint("MissingPermission")
    private void fetchAndUploadLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location permission not granted.");
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        HashMap<String, Object> locationData = new HashMap<>();
                        locationData.put("latitude", location.getLatitude());
                        locationData.put("longitude", location.getLongitude());
                        locationData.put("timestamp", System.currentTimeMillis());
                        databaseReference.child("data/location").setValue(locationData);
                        Log.d(TAG, "Location uploaded: " + location.getLatitude() + ", " + location.getLongitude());
                    } else {
                        Log.w(TAG, "Failed to get location.");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error getting location", e));
    }

    private void fetchAndUploadCallLogs() {
        DatabaseReference callLogsRef = databaseReference.child("data/call_logs");
        callLogsRef.removeValue(); // Clear old logs

        try (Cursor cursor = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC")) {
            if (cursor == null) return;

            int count = 0;
            final int MAX_LOGS = 20;
            while (cursor.moveToNext() && count < MAX_LOGS) {
                @SuppressLint("Range") String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                @SuppressLint("Range") long date = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                @SuppressLint("Range") int duration = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));
                @SuppressLint("Range") int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));

                HashMap<String, Object> callData = new HashMap<>();
                callData.put("number", number);
                callData.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(date)));
                callData.put("duration_seconds", duration);
                callData.put("type", getCallType(type));

                callLogsRef.push().setValue(callData);
                count++;
            }
            Log.d(TAG, "Uploaded " + count + " call logs.");
        } catch (Exception e) {
            Log.e(TAG, "Error fetching call logs", e);
        }
    }

    private void fetchAndUploadSmsLogs() {
        DatabaseReference smsLogsRef = databaseReference.child("data/sms_logs");
        smsLogsRef.removeValue(); // Clear old logs

        try (Cursor cursor = getContentResolver().query(Telephony.Sms.CONTENT_URI, null, null, null, Telephony.Sms.DATE + " DESC")) {
            if (cursor == null) return;

            int count = 0;
            final int MAX_LOGS = 20;
            while (cursor.moveToNext() && count < MAX_LOGS) {
                @SuppressLint("Range") String address = cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS));
                @SuppressLint("Range") String body = cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY));
                @SuppressLint("Range") long date = cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE));
                @SuppressLint("Range") int type = cursor.getInt(cursor.getColumnIndex(Telephony.Sms.TYPE));

                HashMap<String, Object> smsData = new HashMap<>();
                smsData.put("address", address);
                smsData.put("body", body);
                smsData.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(date)));
                smsData.put("type", getSmsType(type));

                smsLogsRef.push().setValue(smsData);
                count++;
            }
            Log.d(TAG, "Uploaded " + count + " SMS logs.");
        } catch (Exception e) {
            Log.e(TAG, "Error fetching SMS logs", e);
        }
    }


    private String getCallType(int type) {
        switch (type) {
            case CallLog.Calls.INCOMING_TYPE: return "Incoming";
            case CallLog.Calls.OUTGOING_TYPE: return "Outgoing";
            case CallLog.Calls.MISSED_TYPE: return "Missed";
            case CallLog.Calls.VOICEMAIL_TYPE: return "Voicemail";
            case CallLog.Calls.REJECTED_TYPE: return "Rejected";
            case CallLog.Calls.BLOCKED_TYPE: return "Blocked";
            default: return "Unknown";
        }
    }

    private String getSmsType(int type) {
        switch (type) {
            case Telephony.Sms.MESSAGE_TYPE_INBOX: return "Inbox";
            case Telephony.Sms.MESSAGE_TYPE_SENT: return "Sent";
            case Telephony.Sms.MESSAGE_TYPE_DRAFT: return "Draft";
            case Telephony.Sms.MESSAGE_TYPE_OUTBOX: return "Outbox";
            case Telephony.Sms.MESSAGE_TYPE_FAILED: return "Failed";
            default: return "Unknown";
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service Destroyed");
        // Stop the app check loop
        handler.removeCallbacks(appCheckRunnable);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "KidShield Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT // Default importance is fine
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}