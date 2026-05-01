# --- General Android & Kotlin ---
-keepattributes Signature, *Annotation*, SourceFile, LineNumberTable, InnerClasses, EnclosingMethod
-dontwarn javax.annotation.**

# --- R8 / ProGuard Optimization ---
-repackageclasses ''
-allowaccessmodification

# Remove debug logs in release build
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# --- Trackify Core Components ---
-keep class com.onlive.trackify.TrackifyApplication { *; }
-keep class com.onlive.trackify.MainActivity { *; }

# --- Data Models (CRITICAL for GSON & Room) ---
# Prevent renaming fields that are serialized to JSON or stored in DB
-keepclassmembers class com.onlive.trackify.data.model.** { <fields>; }
-keep class com.onlive.trackify.data.model.** { *; }

# --- ViewModels & Repositories ---
-keep class com.onlive.trackify.viewmodel.** { *; }
-keep class com.onlive.trackify.data.repository.** { public <methods>; }

# --- Room Persistence ---
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }
-keepclassmembers @androidx.room.Entity class * { *; }
-keepclassmembers @androidx.room.Dao class * { *; }
-dontwarn androidx.room.paging.**

# --- Gson Serialization ---
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.gson.reflect.TypeToken
-keep class * extends com.google.gson.reflect.TypeToken
-keep public class * implements com.google.gson.TypeAdapterFactory
-keep public class * implements com.google.gson.JsonSerializer
-keep public class * implements com.google.gson.JsonDeserializer
-keep public class * implements com.google.gson.TypeAdapter

-keep class com.onlive.trackify.utils.DataExportImportManager$** { *; }

# --- Jetpack Compose ---
-keep @androidx.compose.runtime.Composable class * { *; }
-keep class * {
    @androidx.compose.runtime.Composable <methods>;
}

# --- Firebase & Google Services ---
# Rely on library-provided ProGuard rules
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# --- Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# --- Enums & Sealed Classes ---
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- Utils ---
-keep class com.onlive.trackify.utils.** {
    public <methods>;
}
