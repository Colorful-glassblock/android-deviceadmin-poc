package com.security.poc.deviceadminreset;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;

import com.rosan.dhizuku.api.Dhizuku;

/**
 * Dhizuku UserService - runs in Dhizuku server process with Device Owner privileges.
 * 
 * Dhizuku loads this class via reflection and passes its own Context,
 * which has DevicePolicyManager with Device Owner authority.
 */
public class DeviceOwnerService extends IDeviceOwnerService.Stub {

    private static final String TAG = "DhizukuPoC";

    private final Context mContext;
    private final DevicePolicyManager mDpm;
    private final ComponentName mAdminComponent;

    /**
     * Constructor with Context - Dhizuku UserService will call this via reflection.
     * The Context is Dhizuku's server context, which has Device Owner privileges.
     */
    public DeviceOwnerService(Context context) {
        mContext = context;
        mDpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        // Use Dhizuku's own admin component (it's the Device Owner)
        mAdminComponent = Dhizuku.getOwnerComponent(context);
        Log.d(TAG, "DeviceOwnerService created, admin: " + mAdminComponent);
    }

    @Override
    public boolean resetPassword(String newPassword) {
        try {
            if (mAdminComponent == null) {
                Log.e(TAG, "Admin component is null - Dhizuku not active?");
                return false;
            }
            // This runs with Device Owner privileges via Dhizuku
            mDpm.resetPassword(newPassword, 0);
            Log.d(TAG, "Password reset via Device Owner successful");
            return true;
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException during password reset: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isActive() {
        return mAdminComponent != null
                && mDpm.isDeviceOwnerApp(mAdminComponent.getPackageName());
    }
}
