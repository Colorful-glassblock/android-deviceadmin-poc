package com.security.poc.deviceadminreset;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Device Admin Receiver
 * Handles admin enable/disable events
 */
public class AdminReceiver extends DeviceAdminReceiver {
    
    private static final String TAG = "DeviceAdminPoC";
    
    @Override
    public void onEnabled(Context context, Intent intent) {
        Log.d(TAG, "Device Admin enabled");
        Toast.makeText(context, "Device Admin Enabled - PoC Active", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onDisabled(Context context, Intent intent) {
        Log.d(TAG, "Device Admin disabled");
        Toast.makeText(context, "Device Admin Disabled", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        Log.d(TAG, "Password changed detected");
        Toast.makeText(context, "Password Changed", Toast.LENGTH_SHORT).show();
    }
    
    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return "Warning: Disabling will remove password reset capability.";
    }
}
