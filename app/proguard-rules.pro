# SQLCipher
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keep class com.sodapop.app.data.remote.dto.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}

# Keep serializable classes
-keep,includedescriptorclasses class com.sodapop.app.**$$serializer { *; }
-keepclassmembers class com.sodapop.app.** {
    *** Companion;
}
-keepclasseswithmembers class com.sodapop.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Google Tink / ErrorProne (used by EncryptedSharedPreferences)
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-dontwarn com.google.auto.value.**
