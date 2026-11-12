# prevent proguard / R8 from obfuscating entity fields
-keep class com.flagsmith.entities.** {
    <fields>;
}

# Retrofit Proguard rules to prevent crashes when using Proguard/R8
# Keep Retrofit interfaces and their methods from being obfuscated
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep Retrofit Call and Response classes
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# Keep Retrofit annotations
-keep class retrofit2.** { *; }

# Keep Retrofit converter classes
-keep class * extends retrofit2.Converter$Factory

# Keep Retrofit call adapter classes
-keep class * extends retrofit2.CallAdapter$Factory

# Keep Retrofit service interfaces
-keep interface * extends retrofit2.Call

# Keep Retrofit HTTP annotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep Retrofit service method parameters and return types
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    <methods>;
}

# Keep Retrofit service interfaces used by Flagsmith
-keep interface com.flagsmith.internal.FlagsmithRetrofitService { *; }

# Keep OkHttp classes used by Retrofit
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Keep Gson classes used by Retrofit converter
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer