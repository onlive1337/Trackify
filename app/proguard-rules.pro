# Saving models
-keep class com.onlive.trackify.data.model.** { *; }

 # Room rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }