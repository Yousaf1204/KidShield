package com.yousafdev.KidShield.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.yousafdev.KidShield.Services.MonitoringService;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm received, triggering data sync.");

        // Create an intent to start the MonitoringService
        Intent serviceIntent = new Intent(context, MonitoringService.class);
        // Add our custom action to tell the service to sync data
        serviceIntent.setAction(MonitoringService.ACTION_SYNC_DATA);

        // Start the service correctly based on the Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}