package com.onlive.trackify.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.onlive.trackify.receivers.NotificationReceiver
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val tag = "AlarmScheduler"
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val preferenceManager = PreferenceManager(context)

    fun scheduleNextAlarm() {
        if (!preferenceManager.areNotificationsEnabled()) {
            Log.d(tag, "Notifications disabled, cancelling alarm")
            cancelAlarm()
            return
        }

        val (hour, minute) = preferenceManager.getNotificationTime()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        if (canScheduleExactAlarms()) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
                Log.d(tag, "Exact alarm scheduled for ${calendar.time}")
            } catch (e: SecurityException) {
                Log.e(tag, "Failed to schedule exact alarm, falling back to windowed alarm", e)
                scheduleWindowedAlarm(calendar.timeInMillis, pendingIntent)
            }
        } else {
            Log.d(tag, "Exact alarm permission missing, scheduling windowed alarm")
            scheduleWindowedAlarm(calendar.timeInMillis, pendingIntent)
        }
    }

    private fun scheduleWindowedAlarm(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        val windowLengthMillis = 10 * 60 * 1000L
        alarmManager.setWindow(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            windowLengthMillis,
            pendingIntent
        )
        Log.d(tag, "Windowed alarm scheduled (10 min window)")
    }

    fun cancelAlarm() {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d(tag, "Alarm cancelled")
        }
    }

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun openAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = android.net.Uri.fromParts("package", context.packageName, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Log.e(tag, "Failed to open exact alarm settings", e)
            }
        }
    }
}