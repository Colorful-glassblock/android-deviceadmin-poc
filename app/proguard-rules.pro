# DeviceAdmin PoC - ProGuard Rules
# Keep all classes for research purposes

-keep class com.security.poc.deviceadminreset.** { *; }
-keepclassmembers class * extends android.app.admin.DeviceAdminReceiver { *; }
