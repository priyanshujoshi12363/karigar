# Keep annotations & generic signatures (needed by Gson/Retrofit)
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes EnclosingMethod

# Gson DTOs — serialized by reflection, must keep field names
-keep class com.karigar.worker.data.remote.** { *; }
-keep class com.karigar.worker.data.** { <fields>; }

# Retrofit / OkHttp
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keepattributes Exceptions

# Firebase (messaging) ships its own rules; keep models just in case
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
