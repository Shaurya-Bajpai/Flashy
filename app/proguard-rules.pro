# ── Debugging ────────────────────────────────────────────────────────────────
# Keep source file names and line numbers in stack traces reported by Crashlytics.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Strip debug/verbose log calls from release builds ─────────────────────────
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
}

# ── App components registered in AndroidManifest ─────────────────────────────
# Services, receivers, and the notification listener are instantiated by the
# Android framework via reflection — R8 must not rename or remove them.
-keep class com.dsb.flashy.services.** { *; }
-keep class com.dsb.flashy.notification.** { *; }
-keep class com.dsb.flashy.startup.** { *; }
-keep class com.dsb.flashy.call.** { *; }
-keep class com.dsb.flashy.managers.** { *; }

# ── DataStore / Preferences ───────────────────────────────────────────────────
# Preference keys are looked up by name at runtime.
-keep class com.dsb.flashy.datastore.** { *; }
-keepclassmembers class com.dsb.flashy.datastore.** { *; }

# ── Kotlin coroutines ─────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ── Firebase / Crashlytics ────────────────────────────────────────────────────
-keepattributes *Annotation*
-keepattributes Signature
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# ── Jetpack Compose ───────────────────────────────────────────────────────────
# Compose is safe under R8 full mode by default, but keep the entry points.
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ── AndroidX DataStore ────────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }
-keepclassmembers class androidx.datastore.** { *; }

# ── Suppress known-safe warnings ──────────────────────────────────────────────
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
