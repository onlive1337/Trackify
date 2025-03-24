package com.onlive.trackify.workers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.onlive.trackify.data.database.AppDatabase
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.utils.NotificationFrequency
import com.onlive.trackify.utils.NotificationHelper
import com.onlive.trackify.utils.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class SubscriptionReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val notificationHelper = NotificationHelper(appContext)
    private val preferenceManager = PreferenceManager(appContext)
    private val TAG = "SubscriptionReminder"

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "SubscriptionReminderWorker started")

            if (!preferenceManager.areNotificationsEnabled()) {
                Log.d(TAG, "Notifications disabled in settings")
                return@withContext Result.success()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

                if (!hasPermission) {
                    Log.d(TAG, "No permission to send notifications")
                    return@withContext Result.success()
                }
            }

            val subscriptionDao = AppDatabase.getDatabase(applicationContext).subscriptionDao()
            val activeSubscriptions = subscriptionDao.getActiveSubscriptionsSync()

            if (activeSubscriptions.isEmpty()) {
                Log.d(TAG, "No active subscriptions found")
                return@withContext Result.success()
            }

            val today = Calendar.getInstance()
            var reminderDays = preferenceManager.getReminderDays()

            if (reminderDays.isEmpty()) {
                reminderDays = setOf(0, 1, 3, 7)
                Log.d(TAG, "No reminder days set, using defaults")
            }

            val inputDays = inputData.getIntArray("reminder_days")
            if (inputDays != null && inputDays.isNotEmpty()) {
                reminderDays = inputDays.toSet()
                Log.d(TAG, "Using reminder days from input data: $reminderDays")
            }

            if (preferenceManager.getNotificationFrequency() == NotificationFrequency.WEEKLY) {
                if (today.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                    Log.d(TAG, "Today is not Monday, skipping for weekly frequency")
                    return@withContext Result.success()
                }
            }

            if (preferenceManager.getNotificationFrequency() == NotificationFrequency.MONTHLY) {
                if (today.get(Calendar.DAY_OF_MONTH) != 1) {
                    Log.d(TAG, "Today is not the first day of the month, skipping for monthly frequency")
                    return@withContext Result.success()
                }
            }

            var notificationCount = 0

            for (subscription in activeSubscriptions) {
                if (subscription.endDate != null && subscription.endDate.before(today.time)) {
                    Log.d(TAG, "Subscription ${subscription.name} has expired, skipping")
                    continue
                }

                val nextPaymentDate = calculateNextPaymentDate(subscription.startDate, subscription.billingFrequency)
                val daysUntilPayment = getDaysDifference(today.time, nextPaymentDate)

                if (daysUntilPayment in reminderDays) {
                    notificationHelper.showUpcomingPaymentNotification(subscription, daysUntilPayment)
                    notificationCount++
                    Log.d(TAG, "Sent payment notification for ${subscription.name} in $daysUntilPayment days")
                }

                subscription.endDate?.let { endDate ->
                    if (endDate.after(today.time)) {
                        val daysUntilExpiration = getDaysDifference(today.time, endDate)

                        if (daysUntilExpiration == 0 || daysUntilExpiration == 1 ||
                            daysUntilExpiration == 3 || daysUntilExpiration == 7 ||
                            daysUntilExpiration == 14 || daysUntilExpiration == 30) {

                            notificationHelper.showExpirationNotification(subscription, daysUntilExpiration)
                            notificationCount++
                            Log.d(TAG, "Sent expiration notification for ${subscription.name} in $daysUntilExpiration days")
                        }
                    }
                }
            }

            Log.d(TAG, "SubscriptionReminderWorker completed successfully, sent $notificationCount notifications")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in SubscriptionReminderWorker: ${e.message}", e)
            Result.failure()
        }
    }

    private fun calculateNextPaymentDate(startDate: Date, frequency: BillingFrequency): Date {
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        val today = Calendar.getInstance()

        while (calendar.before(today) || calendar.timeInMillis == today.timeInMillis) {
            when (frequency) {
                BillingFrequency.MONTHLY -> calendar.add(Calendar.MONTH, 1)
                BillingFrequency.YEARLY -> calendar.add(Calendar.YEAR, 1)
            }
        }

        return calendar.time
    }

    private fun getDaysDifference(date1: Date, date2: Date): Int {
        val diffMillis = date2.time - date1.time
        return TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS).toInt()
    }
}