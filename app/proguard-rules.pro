# Keep core attributes
-keepattributes Signature,*Annotation*,SourceFile,LineNumberTable,InnerClasses,EnclosingMethod

# Basic obfuscation
-repackageclasses 'a'
-allowaccessmodification

# Remove debug logging
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

-keep class com.onlive.trackify.TrackifyApplication { *; }
-keep class com.onlive.trackify.MainActivity { *; }

# Keep all data models (used by Room and Gson serialization)
-keep class com.onlive.trackify.data.model.** { *; }
-keepclassmembers class com.onlive.trackify.data.model.** { *; }

# Keep ViewModels (reflection access)
-keep class com.onlive.trackify.viewmodel.** { *; }

# Keep repositories (public API methods)
-keep class com.onlive.trackify.data.repository.** {
    public <methods>;
}

# Keep utility classes
-keep class com.onlive.trackify.utils.** {
    public <methods>;
}

# Keep WorkManager workers
-keep class com.onlive.trackify.workers.** { *; }

-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class * { *; }
-keepclassmembers @androidx.room.Entity class * { *; }
-keepclassmembers @androidx.room.Dao class * { *; }
-dontwarn androidx.room.paging.**

-keep class androidx.compose.** { *; }
-keep @androidx.compose.runtime.Composable class * { *; }
-keep class * {
    @androidx.compose.runtime.Composable <methods>;
}

# ViewModel
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }

# WorkManager
-keep class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# Navigation
-keep class androidx.navigation.** { *; }

-keep class com.google.gson.** { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.onlive.trackify.utils.DataExportImportManager$** { *; }

-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.jvm.internal.** { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-dontwarn kotlin.reflect.jvm.internal.**

# Coroutines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# Enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-dontwarn java.lang.invoke.**
-dontwarn **$$serializer
-dontwarn javax.annotation.**