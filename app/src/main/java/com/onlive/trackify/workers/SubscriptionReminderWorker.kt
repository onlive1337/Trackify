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
import com.onlive.trackify.data.model.PaymentStatus
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

            val today = Calendar.getInstance()

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

            val database = AppDatabase.getDatabase(applicationContext)
            val subscriptions = database.subscriptionDao().getActiveSubscriptionsSync()
            val payments = database.paymentDao().getAllPaymentsSync()

            if (subscriptions.isEmpty()) {
                Log.d(TAG, "No active subscriptions found")
                return@withContext Result.success()
            }

            var notificationCount = 0
            val reminderDays = preferenceManager.getReminderDays()

            for (payment in payments) {
                if (payment.status == PaymentStatus.CONFIRMED) {
                    continue
                }

                val daysUntilPayment = getDaysDifference(today.time, payment.date)

                if (daysUntilPayment in reminderDays) {
                    val subscription = subscriptions.find { it.subscriptionId == payment.subscriptionId }
                    if (subscription != null && subscription.active) {
                        notificationHelper.showUpcomingPaymentNotification(subscription, daysUntilPayment)
                        notificationCount++
                        Log.d(TAG, "Sent payment notification for ${subscription.name} in $daysUntilPayment days")
                    }
                }
            }

            for (subscription in subscriptions) {
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

    private fun getDaysDifference(date1: Date, date2: Date): Int {
        val diffMillis = date2.time - date1.time
        return TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS).toInt()
    }
}