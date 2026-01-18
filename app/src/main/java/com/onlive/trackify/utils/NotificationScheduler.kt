package com.onlive.trackify.utils

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.onlive.trackify.workers.NotificationWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {

    private val tag = "NotificationScheduler"
    private val periodicWorkName = "subscription_notifications_periodic"
    private val immediateWorkName = "subscription_notifications_immediate"

    private val preferenceManager = PreferenceManager(context)

    fun scheduleNotifications() {
        Log.d(tag, "Scheduling notifications")

        if (!preferenceManager.areNotificationsEnabled()) {
            Log.d(tag, "Notifications disabled, cancelling work")
            cancelNotifications()
            return
        }

        schedulePeriodicWork()
        scheduleImmediateCheck()
    }

    private fun schedulePeriodicWork() {
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

        val periodicWorkRequest = PeriodicWorkRequestBuilder<NotificationWorker>(
            24, TimeUnit.HOURS,
            30, TimeUnit.MINUTES
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                15, TimeUnit.MINUTES
            )
            .addTag("notification_periodic")
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            periodicWorkName,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWorkRequest
        )

        Log.d(tag, "Periodic work scheduled for $hour:$minute, initial delay: ${initialDelay / 1000 / 60} min")
    }

    private fun scheduleImmediateCheck() {
        val immediateWork = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setInitialDelay(5, TimeUnit.MINUTES)
            .addTag("notification_immediate")
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            immediateWorkName,
            ExistingWorkPolicy.REPLACE,
            immediateWork
        )

        Log.d(tag, "Immediate check scheduled in 5 minutes")
    }

    fun triggerImmediateCheck() {
        val expeditedWork = OneTimeWorkRequestBuilder<NotificationWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .addTag("notification_expedited")
            .build()

        WorkManager.getInstance(context).enqueue(expeditedWork)
        Log.d(tag, "Expedited notification check triggered")
    }

    fun cancelNotifications() {
        Log.d(tag, "Cancelling all notification work")
        WorkManager.getInstance(context).cancelUniqueWork(periodicWorkName)
        WorkManager.getInstance(context).cancelUniqueWork(immediateWorkName)
        WorkManager.getInstance(context).cancelAllWorkByTag("notification_expedited")
    }
}