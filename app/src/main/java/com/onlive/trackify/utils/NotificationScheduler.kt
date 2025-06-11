package com.onlive.trackify.utils

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.onlive.trackify.workers.NotificationWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {

    private val tag = "NotificationScheduler"
    private val workName = "subscription_notifications"

    private val preferenceManager = PreferenceManager(context)

    fun scheduleNotifications() {
        Log.d(tag, "Scheduling notifications")

        if (!preferenceManager.areNotificationsEnabled()) {
            Log.d(tag, "Notifications disabled, cancelling work")
            cancelNotifications()
            return
        }

        val (hour, minute) = preferenceManager.getNotificationTime()

        val now = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (before(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val initialDelay = targetTime.timeInMillis - now.timeInMillis

        val workRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )

        Log.d(tag, "Notifications scheduled for $hour:$minute with initial delay ${initialDelay/1000/60} minutes")
    }

    fun cancelNotifications() {
        Log.d(tag, "Cancelling notifications")
        WorkManager.getInstance(context).cancelUniqueWork(workName)
    }
}