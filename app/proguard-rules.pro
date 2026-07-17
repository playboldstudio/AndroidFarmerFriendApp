# Retrofit rules
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Gson rules
-keep class com.google.gson.** { *; }
-keepattributes *Annotation*
-keep class com.example.androidfarmerfriend.data.model.** { *; }

# Jsoup rules
-keep class org.jsoup.** { *; }

# Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable
