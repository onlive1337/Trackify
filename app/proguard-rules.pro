# --- Crash Stacktrace Metadata ---
-keepattributes SourceFile, LineNumberTable, Signature, *Annotation*, InnerClasses, EnclosingMethod

# Remove debug/verbose/info logs in release builds (preserve warn/error for CrashLogger diagnostics)
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
}

# --- Kotlinx Serialization (Navigation Compose Routes & Backup DTOs) ---
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.onlive.trackify.**$$serializer { *; }
-keepclassmembers class com.onlive.trackify.** {
    *** Companion;
}
-keepclasseswithmembers class com.onlive.trackify.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep @kotlinx.serialization.Serializable class com.onlive.trackify.** { *; }

# --- Room Entities & Enums ---
-keep @androidx.room.Entity class com.onlive.trackify.data.model.** { *; }
-keepclassmembers enum com.onlive.trackify.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
