# Moshi
-keep class com.vitalcheck.data.remote.**.dto.** { *; }
-keep class com.vitalcheck.data.local.entity.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
