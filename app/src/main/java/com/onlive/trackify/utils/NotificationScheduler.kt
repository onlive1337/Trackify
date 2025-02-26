package com.onlive.trackify.utils

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.onlive.trackify.workers.SubscriptionReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {

    companion object {
        private const val REMINDER_WORK_NAME = "subscription_reminders"
        private const val DAILY_REMINDER_WORK = "daily_subscription_reminder"
        private const val WEEKLY_REMINDER_WORK = "weekly_subscription_reminder"
        private const val MONTHLY_REMINDER_WORK = "monthly_subscription_reminder"
        private const val CUSTOM_REMINDER_WORK = "custom_subscription_reminder"
    }

    private val preferenceManager = PreferenceManager(context)

    fun rescheduleNotifications() {
        cancelAllReminders()

        if (!preferenceManager.areNotificationsEnabled()) {
            return
        }

        when (preferenceManager.getNotificationFrequency()) {
            NotificationFrequency.DAILY -> scheduleDailyReminder()
            NotificationFrequency.WEEKLY -> scheduleWeeklyReminder()
            NotificationFrequency.MONTHLY -> scheduleMonthlyReminder()
            NotificationFrequency.CUSTOM -> scheduleCustomReminder()
        }
    }

    private fun cancelAllReminders() {
        WorkManager.getInstance(context).apply {
            cancelUniqueWork(DAILY_REMINDER_WORK)
            cancelUniqueWork(WEEKLY_REMINDER_WORK)
            cancelUniqueWork(MONTHLY_REMINDER_WORK)
            cancelUniqueWork(CUSTOM_REMINDER_WORK)
            cancelUniqueWork(REMINDER_WORK_NAME)
        }
    }

    private fun scheduleDailyReminder() {
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

        val reminderWorkRequest = PeriodicWorkRequestBuilder<SubscriptionReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            reminderWorkRequest
        )
    }

    private fun scheduleWeeklyReminder() {
        val (hour, minute) = preferenceManager.getNotificationTime()
        val now = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

            if (before(now)) {
                add(Calendar.WEEK_OF_YEAR, 1)
            }
        }

        val initialDelay = targetTime.timeInMillis - now.timeInMillis

        val reminderWorkRequest = PeriodicWorkRequestBuilder<SubscriptionReminderWorker>(
            7, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WEEKLY_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            reminderWorkRequest
        )
    }

    private fun scheduleMonthlyReminder() {
        val (hour, minute) = preferenceManager.getNotificationTime()
        val now = Calendar.getInstance()
        val targetTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            set(Calendar.DAY_OF_MONTH, 1)

            if (before(now)) {
                add(Calendar.MONTH, 1)
            }
        }

        val initialDelay = targetTime.timeInMillis - now.timeInMillis

        val reminderWorkRequest = PeriodicWorkRequestBuilder<SubscriptionReminderWorker>(
            30, TimeUnit.DAYS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            MONTHLY_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            reminderWorkRequest
        )
    }

    private fun scheduleCustomReminder() {
        val reminderDays = preferenceManager.getReminderDays()

        if (reminderDays.isEmpty()) {
            scheduleDailyReminder()
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

        val reminderWorkRequest = PeriodicWorkRequestBuilder<SubscriptionReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    "reminder_days" to reminderDays.toIntArray()
                )
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CUSTOM_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            reminderWorkRequest
        )
    }
}