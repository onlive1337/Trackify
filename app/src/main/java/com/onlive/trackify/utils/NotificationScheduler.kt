package com.onlive.trackify.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.onlive.trackify.receivers.NotificationReceiver

class NotificationScheduler(private val context: Context) {

    private val tag = "NotificationScheduler"
    private val alarmScheduler = AlarmScheduler(context)
    private val preferenceManager = PreferenceManager(context)

    fun scheduleNotifications() {
        Log.d(tag, "Scheduling notifications")

        if (!preferenceManager.areNotificationsEnabled()) {
            Log.d(tag, "Notifications disabled, cancelling alarms")
            cancelNotifications()
            return
        }

        alarmScheduler.scheduleNextAlarm()
    }

    fun triggerImmediateCheck() {
        val intent = Intent(context, NotificationReceiver::class.java)
        context.sendBroadcast(intent)
        Log.d(tag, "Immediate notification check triggered via broadcast")
    }

    fun cancelNotifications() {
        Log.d(tag, "Cancelling all notification alarms")
        alarmScheduler.cancelAlarm()
    }
}