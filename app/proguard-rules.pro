# Keep line number info for crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes RuntimeVisibleAnnotations

# Firestore transport stack — stripping these leaves the client permanently "offline".
-keep class io.grpc.** { *; }
-keep class com.google.protobuf.** { *; }
-dontwarn io.grpc.**
-dontwarn com.google.protobuf.**
-dontwarn javax.naming.**
-dontwarn javax.annotation.**
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Firebase loads these by class name from manifest <meta-data>. R8 full mode can
# drop them even with the library consumer rule, which crashes at launch:
# "FirebaseCrashlytics component is not present."
-keep class * implements com.google.firebase.components.ComponentRegistrar { *; }
-keep class com.google.firebase.components.** { *; }
-keep class com.google.firebase.crashlytics.** { *; }
-keep class com.google.firebase.sessions.** { *; }
-keep class com.google.firebase.installations.** { *; }

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

-keep class kotlin.Metadata { *; }

# Jetpack Compose (debug tooling only, safe to strip in release)
-dontwarn androidx.compose.ui.tooling.**

# App-specific data models (Firestore field maps / Room)
-keep class com.mirazanik.masjidscreen.data.model.** { *; }

# Prevent stripping app's Application class
-keep class com.mirazanik.masjidscreen.MosqueApplication { *; }

# Credential Manager / Google Sign-In
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
  *;
}
-keep class com.google.android.libraries.identity.googleid.** { *; }
