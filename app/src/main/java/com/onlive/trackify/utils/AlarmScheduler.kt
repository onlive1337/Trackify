package com.onlive.trackify.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
        Log.d(tag, "Alarm scheduled (allow-while-idle) for ${calendar.time}")
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
}