# Keep annotations & generic signatures (needed by Gson/Retrofit)
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes EnclosingMethod

# Gson DTOs — serialized by reflection, must keep field names
-keep class com.karigar.app.data.remote.** { *; }
-keep class com.karigar.app.data.** { <fields>; }

# Retrofit / OkHttp
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepattributes Exceptions

# osmdroid
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**

# Firebase (auth + messaging) ship their own rules; keep models just in case
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
