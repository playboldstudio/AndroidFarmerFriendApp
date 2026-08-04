# Retrofit rules
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Gson rules
-keep class com.google.gson.** { *; }
-keepattributes *Annotation*
-keep class com.example.androidfarmerfriend.data.model.** { *; }

# --- Keep all app Gson/Retrofit DTOs (data.api + data.scraper) so field
# --- names survive R8 obfuscation. Gson reads them reflectively by field
# --- name; R8 renaming a field silently breaks JSON parsing on those
# --- screens in the release build.
-keep class com.example.androidfarmerfriend.data.api.** { *; }
-keep class com.example.androidfarmerfriend.data.scraper.** { *; }
-keep class com.example.androidfarmerfriend.data.location.** { *; }

# Jsoup rules
-keep class org.jsoup.** { *; }

# Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable

# --- WorkManager ---
# WorkManager looks up WorkManagerImplExtKt.createWorkManager(Context, Configuration)
# reflectively by hard-coded name at startup (WorkManagerImpl.initialize).
# R8 renames it to something like "m", which throws
# NoSuchMethodException and crashes the app on launch. Keep the method name
# (and the class) so the reflective lookup still resolves in release builds.
-keep class androidx.work.impl.WorkManagerImplExtKt {
    public static androidx.work.impl.WorkManagerImpl createWorkManager(android.content.Context, androidx.work.Configuration);
}

# WorkManager instantiates Worker subclasses reflectively (must keep the
# (Context, WorkerParameters) constructor + class name).
-keep class * extends androidx.work.Worker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.CoroutineWorker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.ListenableWorker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}

# FirebaseMessagingService is registered by class name in the manifest and
# instantiated by the system — never rename it.
-keep class com.example.androidfarmerfriend.notifications.FarmerMessagingService { *; }

# Compose + CustomTabs animations are referenced by resource ID only; no keep needed.
# Keep annotated serializers/models for Firebase/Firestore too.
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
