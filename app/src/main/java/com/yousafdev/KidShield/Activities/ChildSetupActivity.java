package com.yousafdev.KidShield.Activities;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.yousafdev.KidShield.R;
import com.yousafdev.KidShield.Services.MonitoringService;
import com.yousafdev.KidShield.Utils.BootReceiver; // Import BootReceiver
import com.yousafdev.KidShield.Utils.MyDeviceAdminReceiver;

import java.util.Map;

// Helper class to manage each permission item's view
class PermissionViewHolder {
    View layout;
    ImageView statusIcon;
    TextView title;
    Button enableButton;

    PermissionViewHolder(View layout) {
        this.layout = layout;
        statusIcon = layout.findViewById(R.id.imageView_status);
        title = layout.findViewById(R.id.textView_permission_title);
        enableButton = layout.findViewById(R.id.button_enable);
    }

    void setGranted() {
        statusIcon.setImageResource(R.drawable.ic_check);
        statusIcon.setColorFilter(ContextCompat.getColor(layout.getContext(), R.color.kidshield_blue_primary));
        enableButton.setText("Enabled");
        enableButton.setEnabled(false);
    }
}


public class ChildSetupActivity extends AppCompatActivity {

    private PermissionViewHolder admin, usage, overlay, location, call, sms, notifications, battery;
    private Button finishButton;

    private DevicePolicyManager dpm;
    private ComponentName compName;

    private final ActivityResultLauncher<String[]> requestMultiplePermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::onPermissionsResult);

    private final ActivityResultLauncher<Intent> requestSettingLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                checkAllPermissions();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_setup);

        dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        compName = new ComponentName(this, MyDeviceAdminReceiver.class);

        initializeViews();
        setupClickListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAllPermissions();
    }

    private void initializeViews() {
        admin = new PermissionViewHolder(findViewById(R.id.permission_device_admin));
        usage = new PermissionViewHolder(findViewById(R.id.permission_usage_stats));
        overlay = new PermissionViewHolder(findViewById(R.id.permission_draw_overlay));
        location = new PermissionViewHolder(findViewById(R.id.permission_location));
        call = new PermissionViewHolder(findViewById(R.id.permission_call_logs));
        sms = new PermissionViewHolder(findViewById(R.id.permission_sms));
        notifications = new PermissionViewHolder(findViewById(R.id.permission_notifications));
        battery = new PermissionViewHolder(findViewById(R.id.permission_battery));
        finishButton = findViewById(R.id.button_finish_setup);

        admin.title.setText("Device Administrator (for Screen Lock)");
        usage.title.setText("Usage Access (for App Blocking)");
        overlay.title.setText("Draw Over Apps (for Lock Screen)");
        location.title.setText("Location Access (for Tracking)");
        call.title.setText("Call Log Access (for Monitoring)");
        sms.title.setText("SMS Access (for Monitoring)");
        notifications.title.setText("Notifications (for Service Stability)");
        battery.title.setText("Disable Battery Optimization");
    }

    private void setupClickListeners() {
        admin.enableButton.setOnClickListener(v -> requestDeviceAdmin());
        usage.enableButton.setOnClickListener(v -> requestUsageStats());
        overlay.enableButton.setOnClickListener(v -> requestOverlay());
        location.enableButton.setOnClickListener(v -> requestStandardPermissions(
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}
        ));
        call.enableButton.setOnClickListener(v -> requestStandardPermissions(
                new String[]{Manifest.permission.READ_CALL_LOG}
        ));
        sms.enableButton.setOnClickListener(v -> requestStandardPermissions(
                new String[]{Manifest.permission.READ_SMS}
        ));
        notifications.enableButton.setOnClickListener(v -> requestNotificationPermission());
        battery.enableButton.setOnClickListener(v -> requestIgnoreBatteryOptimizations());

        finishButton.setOnClickListener(v -> {
            // Start the service and schedule the alarm.
            Intent serviceIntent = new Intent(this, MonitoringService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }

            // Schedule the data sync alarm
            BootReceiver.scheduleDataSync(this);

            startActivity(new Intent(this, ChildDashboardActivity.class));
            finish();
        });
    }

    private void checkAllPermissions() {
        if (dpm.isAdminActive(compName)) admin.setGranted();
        if (isUsageStatsAllowed()) usage.setGranted();
        if (Settings.canDrawOverlays(this)) overlay.setGranted();
        if (isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) location.setGranted();
        if (isPermissionGranted(Manifest.permission.READ_CALL_LOG)) call.setGranted();
        if (isPermissionGranted(Manifest.permission.READ_SMS)) sms.setGranted();
        if (isNotificationPermissionGranted()) notifications.setGranted();
        if (isIgnoringBatteryOptimizations()) battery.setGranted();

        // Check if all permissions are granted to enable the finish button
        if (dpm.isAdminActive(compName) && isUsageStatsAllowed() && Settings.canDrawOverlays(this) &&
                isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) && isPermissionGranted(Manifest.permission.READ_CALL_LOG) &&
                isPermissionGranted(Manifest.permission.READ_SMS) && isNotificationPermissionGranted() && isIgnoringBatteryOptimizations()) {
            finishButton.setEnabled(true);
        }
    }

    private void requestDeviceAdmin() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "This permission is required to remotely lock the device.");
        requestSettingLauncher.launch(intent);
    }

    private void requestUsageStats() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        requestSettingLauncher.launch(intent);
    }

    private void requestOverlay() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        requestSettingLauncher.launch(intent);
    }

    private void requestStandardPermissions(String[] permissions) {
        requestMultiplePermissionsLauncher.launch(permissions);
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestStandardPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS});
        }
    }

    private void requestIgnoreBatteryOptimizations() {
        Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
        intent.setData(Uri.parse("package:" + getPackageName()));
        requestSettingLauncher.launch(intent);
    }

    private void onPermissionsResult(Map<String, Boolean> grants) {
        checkAllPermissions();
    }

    private boolean isPermissionGranted(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isUsageStatsAllowed() {
        try {
            android.app.AppOpsManager appOps = (android.app.AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(android.app.AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), getPackageName());
            return mode == android.app.AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isNotificationPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS);
        }
        return true;
    }

    private boolean isIgnoringBatteryOptimizations() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        return pm.isIgnoringBatteryOptimizations(getPackageName());
    }
}