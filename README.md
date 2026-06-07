# DeviceAdmin Password Reset PoC

## ⚠️ LEGAL DISCLAIMER - READ BEFORE USE

**THIS SOFTWARE IS PROVIDED FOR AUTHORIZED SECURITY RESEARCH ONLY.**

### Legal Warnings

1. **Unauthorized Access is Illegal**: Using this tool on devices you do not own or have explicit written authorization to test is a criminal offense under:
   - Computer Fraud and Abuse Act (CFAA) - United States
   - Computer Misuse Act 1990 - United Kingdom
   - Section 202c of the German Criminal Code (StGB)
   - Equivalent laws in most jurisdictions worldwide

2. **Penalties**: Unauthorized use may result in:
   - Criminal prosecution
   - Imprisonment (up to 10 years in some jurisdictions)
   - Substantial fines
   - Civil liability for damages

3. **Authorization Required**: You MUST have:
   - Written permission from the device owner
   - Signed authorization agreement
   - Clear scope of testing defined
   - Legal review of your testing activities

### Intended Use

This Proof of Concept demonstrates a known vulnerability in Android's Device Administration API (API 21-28). It is intended for:

- Security researchers documenting vulnerabilities
- Penetration testers with proper authorization
- Educational purposes in controlled environments
- Vendor security teams assessing their devices

### Responsible Disclosure

If you discover a vulnerability using this tool:

1. Do NOT publicly disclose without vendor notification
2. Follow coordinated disclosure timelines (typically 90 days)
3. Contact the vendor's security team directly
4. Allow time for patches before public disclosure

---

## Technical Details

### Vulnerability: DeviceAdmin Password Reset (API 21-28)

**Affected Versions**: Android 5.0 (API 21) through Android 9 (API 28)

**Fixed In**: Android 10 (API 29) with additional restrictions

**CVSS Score**: 7.8 (High) - Local privilege escalation

**CWE**: CWE-269 (Improper Privilege Management)

### Attack Vector

A malicious application with Device Administrator privileges can:

1. Reset the device lock screen password without user consent
2. Bypass existing authentication mechanisms
3. Gain persistent access to the device

### Mitigation

- Upgrade to Android 10+ where password reset requires additional user interaction
- Only install apps from trusted sources
- Review Device Administrator permissions carefully
- Use enterprise MDM solutions with password policies

---

## Project Structure

```
android-deviceadmin-poc/
├── app/
│   ├── src/main/
│   │   ├── java/com/security/poc/deviceadminreset/
│   │   │   ├── MainActivity.java      # Main exploit logic
│   │   │   └── AdminReceiver.java     # Device admin receiver
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   └── activity_main.xml  # UI layout
│   │   │   └── xml/
│   │   │       └── device_admin_policies.xml
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle                       # Project build config
├── settings.gradle
└── README.md
```

---

## Build & Run

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK with API 21+ installed
- Physical device or emulator (API 21-28 recommended)

### Build Steps

1. Clone this repository
2. Open in Android Studio
3. Sync Gradle files
4. Build → Build Bundle(s) / APK(s) → Build APK(s)

### Testing

1. Install APK on target device/emulator
2. Grant Device Administrator permission when prompted
3. Complete multi-factor verification:
   - Enter PIN (minimum 4 digits)
   - Answer security question
4. Enter new password (minimum 6 characters)
5. Confirm password reset

---

## Multi-Factor Verification

This PoC implements defense-in-depth by requiring multiple verification steps:

1. **Device Admin Activation**: Explicit user consent required
2. **PIN Verification**: User-defined numeric PIN
3. **Security Question**: Knowledge-based authentication
4. **Password Complexity**: Minimum length enforcement
5. **Confirmation Dialog**: Final user confirmation before reset

**Note**: These are PoC-level controls. Production implementations should use:
- Hardware-backed keystore
- Biometric verification
- Server-side validation
- Rate limiting and lockout

---

## Research Notes

### API Behavior by Version

| API Level | Version | Password Reset Behavior |
|-----------|---------|------------------------|
| 21-22 | 5.0-5.1 | Direct reset, no confirmation |
| 23-25 | 6.0-7.1 | Direct reset, no confirmation |
| 26-28 | 8.0-9 | Direct reset, may show warning |
| 29+ | 10+ | Reset blocked without user interaction |

### Exploit Limitations

- Requires Device Administrator privileges
- User must explicitly grant admin access
- Does not work on fully managed enterprise devices
- API 29+ requires factory reset or MDM for password changes

---

## Responsible Use Agreement

By using this software, you agree to:

1. Only use on devices you own or have written authorization to test
2. Not distribute for malicious purposes
3. Report any discovered vulnerabilities responsibly
4. Comply with all applicable laws and regulations
5. Accept full legal responsibility for your actions

---

## CI/CD Pipeline

### GitHub Actions Workflow

The project includes automated CI with the following jobs:

| Job | Trigger | Cache | Description |
|-----|---------|-------|-------------|
| **Build & Lint** | Push/PR to main, develop | Gradle | Compiles debug APK, runs lint |
| **Unit Tests** | After build | Gradle | Runs test suite |
| **Security Scan** | After build | N/A | Secret detection, dependency scan |

### Caching Strategy

- **Gradle Cache**: `~/.gradle/caches` + `~/.gradle/wrapper`
- Cache key based on `*.gradle*` and `gradle-wrapper.properties` hash
- Automatic restore on cache hit

### Artifacts

- Debug APK uploaded on every successful build
- Retention: 30 days

---

## Branch Protection

### `main` branch (Production)

- ✅ **Require PR**: All changes via pull request
- ✅ **CI Checks**: build, test, security-scan must pass
- ✅ **Reviews**: At least 1 approving review required
- ✅ **Stale Reviews**: Dismissed on new commits
- ✅ **Force Push**: Blocked
- ✅ **Deletion**: Blocked
- ✅ **Admin Enforcement**: Enabled (no bypass)

### `develop` branch (Development)

- ✅ **Require PR**: Changes via pull request
- ✅ **CI Checks**: build must pass
- ⚠️ **Reviews**: Not required (faster iteration)
- ✅ **Force Push**: Allowed (for rebasing)
- ✅ **Deletion**: Blocked

### Workflow

```
feature-branch → develop → main
     ↓              ↓         ↓
   (local)      (build)   (full CI + review)
```

---

## References

- [Android Device Administration API](https://developer.android.com/reference/android/app/admin/DevicePolicyManager)
- [CWE-269: Improper Privilege Management](https://cwe.mitre.org/data/definitions/269.html)
- [OWASP Mobile Security Testing Guide](https://owasp.org/www-project-mobile-security-testing-guide/)

---

## License

This project is provided for educational and authorized security research purposes only. 

**NO WARRANTY**: The software is provided "as is" without warranty of any kind.

**LIMITATION OF LIABILITY**: In no event shall the authors be liable for any claim, damages, or other liability arising from use of this software.

---

**Last Updated**: 2026-06-07

**Author**: Security Research PoC

**Contact**: For responsible disclosure inquiries
