package com.yousafdev.KidShield.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.yousafdev.KidShield.Services.MonitoringService;

import java.util.concurrent.TimeUnit;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";
    private static final long SYNC_INTERVAL = TimeUnit.MINUTES.toMillis(1);

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d(TAG, "Boot completed. Starting services and alarms.");

            // 1. Start the main MonitoringService for app blocking
            Intent serviceIntent = new Intent(context, MonitoringService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }

            // 2. Schedule the periodic data sync using AlarmManager
            scheduleDataSync(context);
        }
    }

    public static void scheduleDataSync(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);

        // Use FLAG_IMMUTABLE for security on newer Android versions
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT : PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, flags);


        if (alarmManager != null) {
            // Cancel any existing alarms to avoid duplicates
            alarmManager.cancel(pendingIntent);

            // Set an inexact repeating alarm. This is more battery-friendly.
            // The alarm will first trigger after the interval and then repeat.
            alarmManager.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + SYNC_INTERVAL,
                    SYNC_INTERVAL,
                    pendingIntent
            );
            Log.d(TAG, "Data sync alarm scheduled to run every " + TimeUnit.MILLISECONDS.toMinutes(SYNC_INTERVAL) + " minute(s).");
        } else {
            Log.e(TAG, "AlarmManager is null. Cannot schedule data sync.");
        }
    }
}