# Dhizuku Device Owner Password Reset PoC

## ⚠️ LEGAL DISCLAIMER - READ BEFORE USE

**THIS SOFTWARE IS PROVIDED FOR AUTHORIZED SECURITY RESEARCH ONLY.**

### Legal Warnings

1. **Unauthorized Access is Illegal**: Using this tool on devices you do not own or have explicit written authorization to test is a criminal offense under applicable laws.

2. **Authorization Required**: You MUST have:
   - Written permission from the device owner
   - Signed authorization agreement
   - Clear scope of testing defined

### Intended Use

This Proof of Concept demonstrates password reset via **Dhizuku Device Owner** API. It is intended for:

- Security researchers documenting vulnerabilities
- Penetration testers with proper authorization
- Educational purposes in controlled environments

---

## Technical Details

### Approach: Dhizuku Device Owner (replaces local Device Admin)

**Previous version**: Used local `DeviceAdminReceiver` with `DevicePolicyManager.resetPassword()` — limited to API 21-28.

**This version**: Uses [Dhizuku](https://github.com/iamr0s/Dhizuku) to obtain **Device Owner** privileges. The password reset operation runs in Dhizuku's process with full Device Owner authority.

### Architecture

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│   This App      │       │    Dhizuku      │       │    Android      │
│   (Client)      │──────▶│  (DO Server)    │──────▶│   Framework     │
│                 │  API  │                 │  DPM  │                 │
│  Dhizuku.init() │       │ DeviceOwnerSvc  │       │ resetPassword() │
│  bindService()  │       │ DPM access      │       │                 │
└─────────────────┘       └─────────────────┘       └─────────────────┘
```

**Flow:**
1. App initializes Dhizuku API connection
2. Requests permission from Dhizuku
3. Binds to `DeviceOwnerService` (UserService running in Dhizuku's process)
4. Calls `resetPassword()` through the service — executed with Device Owner privileges

### Prerequisites

- **Dhizuku** app installed and activated as Device Owner
  - Activate via ADB: `adb shell dpm set-device-owner com.rosan.dhizuku/.server.DhizukuDAReceiver`
  - Or use Shizuku integration
- **Android 8.0+** (API 26+) — required by Dhizuku API
- This app granted permission in Dhizuku

### Key Differences from Device Admin

| Feature | Device Admin (old) | Dhizuku Device Owner (new) |
|---------|-------------------|---------------------------|
| API Level | 21-28 | 26+ |
| Privilege | Device Admin | Device Owner |
| Password reset | Blocked on API 29+ | Works on all versions |
| Setup | User grants admin | ADB/Shizuku activation |
| Scope | Single app | Shared via Dhizuku API |

---

## Project Structure

```
android-deviceadmin-poc/
├── app/
│   ├── src/main/
│   │   ├── aidl/com/security/poc/deviceadminreset/
│   │   │   └── IDeviceOwnerService.aidl   # AIDL interface for UserService
│   │   ├── java/com/security/poc/deviceadminreset/
│   │   │   ├── MainActivity.java          # Dhizuku client logic
│   │   │   └── DeviceOwnerService.java    # UserService (runs with DO privileges)
│   │   ├── res/
│   │   │   └── layout/
│   │   │       └── activity_main.xml
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
├── settings.gradle
└── README.md
```

---

## Build & Run

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK with API 26+ installed
- Device with Dhizuku installed and activated as Device Owner

### Build Steps

1. Clone this repository
2. Open in Android Studio
3. Sync Gradle files
4. Build → Build Bundle(s) / APK(s) → Build APK(s)

### Testing

1. Install and activate Dhizuku as Device Owner on the test device
2. Install this app
3. Open the app and tap "Initialize Dhizuku"
4. Grant permission when Dhizuku prompts
5. Complete verification steps (PIN + security question)
6. Enter new password and confirm reset

---

## Responsible Use Agreement

By using this software, you agree to:

1. Only use on devices you own or have written authorization to test
2. Not distribute for malicious purposes
3. Report any discovered vulnerabilities responsibly
4. Comply with all applicable laws and regulations

---

## References

- [Dhizuku - Device Owner Permission Sharing](https://github.com/iamr0s/Dhizuku)
- [Dhizuku API](https://github.com/iamr0s/Dhizuku-API)
- [Android DevicePolicyManager](https://developer.android.com/reference/android/app/admin/DevicePolicyManager)

---

## License

This project is provided for educational and authorized security research purposes only.

**Last Updated**: 2026-06-13
