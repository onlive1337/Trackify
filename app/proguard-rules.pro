-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes InnerClasses,EnclosingMethod

-keep class com.onlive.trackify.data.model.** { *; }
-keepclassmembers class com.onlive.trackify.data.model.** { *; }

-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class *
-keepclassmembers @androidx.room.Dao class * { *; }
-keep @androidx.room.Database class *
-keepclassmembers @androidx.room.Database class * { *; }
-dontwarn androidx.room.paging.**

-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class com.google.gson.stream.** { *; }
-keep class com.onlive.trackify.utils.DataExportImportManager$** { *; }

-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keep class kotlin.reflect.jvm.internal.** { *; }
-dontwarn kotlin.reflect.jvm.internal.**

-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembers class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }
-keep @androidx.compose.runtime.Composable class * { *; }
-keep class * {
    @androidx.compose.runtime.Composable <methods>;
}

-keep class androidx.navigation.** { *; }

-keep class androidx.work.** { *; }
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context,androidx.work.WorkerParameters);
}
-keep class com.onlive.trackify.workers.** { *; }

-keepclassmembers class * implements androidx.lifecycle.LifecycleObserver {
    <init>(...);
}
-keep class * implements androidx.lifecycle.LifecycleObserver {
    <init>(...);
}
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class com.onlive.trackify.viewmodel.** { *; }

-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

-keep class coil.** { *; }
-keep interface coil.** { *; }
-dontwarn coil.**

-keep class com.google.accompanist.** { *; }
-dontwarn com.google.accompanist.**

-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**
-keep class com.patrykandpatrick.vico.** { *; }
-dontwarn com.patrykandpatrick.vico.**

-keep class com.onlive.trackify.TrackifyApplication { *; }
-keep class com.onlive.trackify.MainActivity { *; }

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-repackageclasses 'a'
-allowaccessmodification
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# Firebase Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# remove in release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

-keepclassmembers class com.onlive.trackify.utils.** {
    public <methods>;
}

-keep class com.onlive.trackify.data.repository.** { 
    public <methods>; 
}
-keep class com.onlive.trackify.data.database.** { 
    public <methods>; 
}

-dontwarn java.lang.invoke.**
-dontwarn **$$serializer
-keepclassmembers class **$Companion {
    public <methods>;
}