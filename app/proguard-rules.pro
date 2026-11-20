# Gson reads these classes by field name, so R8 must not rename or drop them.
-keep class com.venderlive.app.data.model.** { *; }
-keep class com.venderlive.app.data.remote.** { *; }
-keepattributes Signature, InnerClasses, EnclosingMethod, *Annotation*, RuntimeVisibleAnnotations

# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Firebase / Google sign-in / Credential Manager
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class androidx.credentials.** { *; }
-dontwarn com.google.android.gms.**

# Agora (voice rooms)
-keep class io.agora.** { *; }
-dontwarn io.agora.**

# Kotlin
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings { <fields>; }
