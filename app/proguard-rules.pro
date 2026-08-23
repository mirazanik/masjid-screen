# Keep line number info for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Firebase Firestore
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firestore transport stack — stripping these leaves the client permanently "offline"
-keep class io.grpc.** { *; }
-keep class com.google.protobuf.** { *; }
-keepclassmembers class io.grpc.** { *; }
-dontwarn io.grpc.**
-dontwarn com.google.protobuf.**
-dontwarn javax.naming.**
-dontwarn javax.annotation.**

# Firebase Auth
-keepattributes Signature
-keepattributes *Annotation*

# Room Database
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-dontwarn androidx.room.**

# Kotlin Coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# Kotlin Serialization / Reflection
-keepattributes RuntimeVisibleAnnotations
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# Adhan prayer times library
-keep class com.batoulapps.adhan.** { *; }
-dontwarn com.batoulapps.adhan.**

# DataStore
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# WorkManager
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# Jetpack Compose (debug tooling only, safe to strip in release)
-dontwarn androidx.compose.ui.tooling.**

# QR pairing (ZXing encode + ML Kit scan)
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**

# App-specific data models (Firebase serialization)
-keep class com.mirazanik.masjidscreen.data.model.** { *; }
-keep class com.mirazanik.masjidscreen.domain.model.** { *; }

# Prevent stripping app's Application class
-keep class com.mirazanik.masjidscreen.MosqueApplication { *; }

# Credential Manager / Google Sign-In
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
  *;
}
-keep class com.google.android.libraries.identity.googleid.** { *; }
