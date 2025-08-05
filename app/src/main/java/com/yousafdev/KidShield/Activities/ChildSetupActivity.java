package com.yousafdev.KidShield.Activities;

import android.Manifest;
import android.accessibilityservice.AccessibilityServiceInfo;
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
import android.text.TextUtils;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.yousafdev.KidShield.R;
import com.yousafdev.KidShield.Services.AppAccessibilityService;
import com.yousafdev.KidShield.Services.MonitoringService;
import com.yousafdev.KidShield.Utils.BootReceiver;
import com.yousafdev.KidShield.Utils.MyDeviceAdminReceiver;
import java.util.Map;

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

    private PermissionViewHolder admin, usage, overlay, location, call, sms, notifications, battery, accessibility;
    private Button finishButton;

    private DevicePolicyManager dpm;
    private ComponentName compName;

    private final ActivityResultLauncher<String[]> requestMultiplePermissionsLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), this::onPermissionsResult);

    private final ActivityResultLauncher<Intent> requestSettingLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> checkAllPermissions());

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
        accessibility = new PermissionViewHolder(findViewById(R.id.permission_accessibility));
        finishButton = findViewById(R.id.button_finish_setup);

        admin.title.setText("Device Administrator (for Screen Lock)");
        usage.title.setText("Usage Access (for App Blocking)");
        overlay.title.setText("Draw Over Apps (for Lock Screen)");
        location.title.setText("Location Access (for Tracking)");
        call.title.setText("Call Log Access (for Monitoring)");
        sms.title.setText("SMS Access (for Monitoring)");
        notifications.title.setText("Notifications (for Service Stability)");
        battery.title.setText("Disable Battery Optimization");
        accessibility.title.setText("Accessibility Service (for Faster App Monitoring)");
    }

    private void setupClickListeners() {
        admin.enableButton.setOnClickListener(v -> requestDeviceAdmin());
        usage.enableButton.setOnClickListener(v -> requestUsageStats());
        overlay.enableButton.setOnClickListener(v -> requestOverlay());
        location.enableButton.setOnClickListener(v -> {
            if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
                requestStandardPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !isPermissionGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                requestStandardPermissions(new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION});
            }
        });
        call.enableButton.setOnClickListener(v -> requestStandardPermissions(
                new String[]{Manifest.permission.READ_CALL_LOG}
        ));
        sms.enableButton.setOnClickListener(v -> requestStandardPermissions(
                new String[]{Manifest.permission.READ_SMS}
        ));
        notifications.enableButton.setOnClickListener(v -> requestNotificationPermission());
        battery.enableButton.setOnClickListener(v -> requestIgnoreBatteryOptimizations());
        accessibility.enableButton.setOnClickListener(v -> requestAccessibilityService());

        finishButton.setOnClickListener(v -> {
            Intent serviceIntent = new Intent(this, MonitoringService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }

            BootReceiver.scheduleDataSync(this);

            startActivity(new Intent(this, ChildDashboardActivity.class));
            finish();
        });
    }

    private void checkAllPermissions() {
        if (dpm.isAdminActive(compName)) admin.setGranted();
        if (isUsageStatsAllowed()) usage.setGranted();
        if (Settings.canDrawOverlays(this)) overlay.setGranted();
        if (isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) && (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || isPermissionGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION))) {
            location.setGranted();
        }
        if (isPermissionGranted(Manifest.permission.READ_CALL_LOG)) call.setGranted();
        if (isPermissionGranted(Manifest.permission.READ_SMS)) sms.setGranted();
        if (isNotificationPermissionGranted()) notifications.setGranted();
        if (isIgnoringBatteryOptimizations()) battery.setGranted();
        if (isAccessibilityServiceEnabled(this)) accessibility.setGranted();

        if (dpm.isAdminActive(compName) && isUsageStatsAllowed() && Settings.canDrawOverlays(this) &&
                isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) && (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || isPermissionGranted(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) &&
                isPermissionGranted(Manifest.permission.READ_CALL_LOG) &&
                isPermissionGranted(Manifest.permission.READ_SMS) && isNotificationPermissionGranted() && isIgnoringBatteryOptimizations() && isAccessibilityServiceEnabled(this)) {
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

    private void requestAccessibilityService() {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
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

    public static boolean isAccessibilityServiceEnabled(Context context) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am == null) {
            return false;
        }
        return am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
                .stream()
                .anyMatch(service -> service.getId().equals(context.getPackageName() + "/.Services.AppAccessibilityService"));
    }
}