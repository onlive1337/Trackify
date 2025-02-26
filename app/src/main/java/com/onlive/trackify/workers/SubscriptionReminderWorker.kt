package com.onlive.trackify.workers

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
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

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            if (!preferenceManager.areNotificationsEnabled()) {
                return@withContext Result.success()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

                if (!hasPermission) {
                    return@withContext Result.success()
                }
            }

            val subscriptionDao = AppDatabase.getDatabase(applicationContext).subscriptionDao()
            val activeSubscriptions = subscriptionDao.getActiveSubscriptionsSync()

            val today = Calendar.getInstance()
            var reminderDays = preferenceManager.getReminderDays()

            val inputDays = inputData.getIntArray("reminder_days")
            if (inputDays != null && inputDays.isNotEmpty()) {
                reminderDays = inputDays.toSet()
            }

            if (preferenceManager.getNotificationFrequency() == NotificationFrequency.WEEKLY) {
                if (today.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                    return@withContext Result.success()
                }
            }

            if (preferenceManager.getNotificationFrequency() == NotificationFrequency.MONTHLY) {
                if (today.get(Calendar.DAY_OF_MONTH) != 1) {
                    return@withContext Result.success()
                }
            }

            for (subscription in activeSubscriptions) {
                val nextPaymentDate = calculateNextPaymentDate(subscription.startDate, subscription.billingFrequency)

                val daysUntilPayment = getDaysDifference(today.time, nextPaymentDate)

                if (daysUntilPayment in reminderDays) {
                    notificationHelper.showUpcomingPaymentNotification(subscription, daysUntilPayment)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun calculateNextPaymentDate(startDate: Date, frequency: BillingFrequency): Date {
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        val today = Calendar.getInstance()

        while (calendar.before(today)) {
            when (frequency) {
                BillingFrequency.MONTHLY -> calendar.add(Calendar.MONTH, 1)
                BillingFrequency.YEARLY -> calendar.add(Calendar.YEAR, 1)
            }
        }

        return calendar.time
    }

    private fun getDaysDifference(date1: Date, date2: Date): Int {
        val diff = date2.time - date1.time
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
    }
}