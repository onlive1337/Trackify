package com.onlive.trackify.utils

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.onlive.trackify.workers.SubscriptionReminderWorker
import java.util.Calendar
import java.util.concurrent.TimeUnit

class NotificationScheduler(private val context: Context) {

    companion object {
        private const val TAG = "NotificationScheduler"
        private const val REMINDER_WORK_NAME = "subscription_reminders"
        private const val DAILY_REMINDER_WORK = "daily_subscription_reminder"
        private const val WEEKLY_REMINDER_WORK = "weekly_subscription_reminder"
        private const val MONTHLY_REMINDER_WORK = "monthly_subscription_reminder"
        private const val CUSTOM_REMINDER_WORK = "custom_subscription_reminder"
    }

    private val preferenceManager = PreferenceManager(context)

    fun rescheduleNotifications() {
        Log.d(TAG, "Перепланирование уведомлений")
        cancelAllReminders()

        if (!preferenceManager.areNotificationsEnabled()) {
            Log.d(TAG, "Уведомления отключены в настройках")
            return
        }

        when (preferenceManager.getNotificationFrequency()) {
            NotificationFrequency.DAILY -> scheduleDailyReminder()
            NotificationFrequency.WEEKLY -> scheduleWeeklyReminder()
            NotificationFrequency.MONTHLY -> scheduleMonthlyReminder()
            NotificationFrequency.CUSTOM -> scheduleCustomReminder()
        }
    }

    fun cancelAllReminders() {
        Log.d(TAG, "Отмена всех напоминаний")
        WorkManager.getInstance(context).apply {
            cancelUniqueWork(DAILY_REMINDER_WORK)
            cancelUniqueWork(WEEKLY_REMINDER_WORK)
            cancelUniqueWork(MONTHLY_REMINDER_WORK)
            cancelUniqueWork(CUSTOM_REMINDER_WORK)
            cancelUniqueWork(REMINDER_WORK_NAME)
        }
    }

    private fun scheduleDailyReminder() {
        Log.d(TAG, "Планирование ежедневных напоминаний")
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

        Log.d(TAG, "Ежедневное напоминание запланировано на ${hour}:${minute}, " +
                "начальная задержка: ${initialDelay/1000/60} минут")
    }

    private fun scheduleWeeklyReminder() {
        Log.d(TAG, "Планирование еженедельных напоминаний")
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

        Log.d(TAG, "Еженедельное напоминание запланировано на понедельник ${hour}:${minute}")
    }

    private fun scheduleMonthlyReminder() {
        Log.d(TAG, "Планирование ежемесячных напоминаний")
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

        Log.d(TAG, "Ежемесячное напоминание запланировано на 1-е число ${hour}:${minute}")
    }

    private fun scheduleCustomReminder() {
        Log.d(TAG, "Планирование пользовательских напоминаний")
        val reminderDays = preferenceManager.getReminderDays()

        if (reminderDays.isEmpty()) {
            Log.d(TAG, "Не выбраны дни для напоминаний, используем ежедневные напоминания")
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
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CUSTOM_REMINDER_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            reminderWorkRequest
        )

        Log.d(TAG, "Пользовательские напоминания запланированы с выбранными днями: ${reminderDays.joinToString()}")
    }
}