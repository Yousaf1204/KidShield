package com.yousafdev.KidShield.Services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;

public class AppAccessibilityService extends AccessibilityService {

    private static final String TAG = "AppAccessibilityService";
    public static final String ACTION_FOREGROUND_APP = "com.yousafdev.KidShield.ACTION_FOREGROUND_APP";
    public static final String EXTRA_PACKAGE_NAME = "packageName";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (event.getPackageName() != null && event.getClassName() != null) {
                ComponentName componentName = new ComponentName(
                        event.getPackageName().toString(),
                        event.getClassName().toString()
                );
                String packageName = componentName.getPackageName();
                Log.d(TAG, "Foreground App: " + packageName);

                Intent intent = new Intent(ACTION_FOREGROUND_APP);
                intent.putExtra(EXTRA_PACKAGE_NAME, packageName);
                sendBroadcast(intent);
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "Accessibility Service Interrupted");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        setServiceInfo(info);
        Log.d(TAG, "Accessibility Service Connected");
    }
}