package com.onlive.trackify.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.onlive.trackify.data.database.AppDatabase
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.utils.NotificationHelper
import com.onlive.trackify.utils.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class NotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val notificationHelper = NotificationHelper(appContext)
    private val preferenceManager = PreferenceManager(appContext)
    private val database = AppDatabase.getDatabase(appContext)

    companion object {
        private const val TAG = "NotificationWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting notification check")

            if (!preferenceManager.areNotificationsEnabled()) {
                Log.d(TAG, "Notifications disabled")
                return@withContext Result.success()
            }

            val subscriptions = database.subscriptionDao().getActiveSubscriptionsSync()
            val reminderDays = preferenceManager.getReminderDays()
            val today = Calendar.getInstance().time

            Log.d(TAG, "Checking ${subscriptions.size} subscriptions with reminder days: $reminderDays")

            for (subscription in subscriptions) {
                checkPaymentReminders(subscription, today, reminderDays)
                checkExpirationReminders(subscription, today, reminderDays)
            }

            Log.d(TAG, "Notification check completed")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in notification worker", e)
            Result.failure()
        }
    }

    private fun checkPaymentReminders(subscription: Subscription, today: Date, reminderDays: Set<Int>) {
        val nextPaymentDate = calculateNextPaymentDate(subscription, today) ?: return
        val daysUntil = getDaysDifference(today, nextPaymentDate)

        if (daysUntil in reminderDays) {
            notificationHelper.showPaymentReminderNotification(subscription, daysUntil)
            Log.d(TAG, "Payment reminder sent for ${subscription.name}, days until: $daysUntil")
        }
    }

    private fun checkExpirationReminders(subscription: Subscription, today: Date, reminderDays: Set<Int>) {
        val endDate = subscription.endDate ?: return

        if (endDate.before(today)) return

        val daysUntil = getDaysDifference(today, endDate)

        if (daysUntil in reminderDays) {
            notificationHelper.showExpirationReminderNotification(subscription, daysUntil)
            Log.d(TAG, "Expiration reminder sent for ${subscription.name}, days until: $daysUntil")
        }
    }

    private fun calculateNextPaymentDate(subscription: Subscription, today: Date): Date? {
        val startDate = subscription.startDate
        val endDate = subscription.endDate

        if (startDate.after(today)) {
            return startDate
        }

        val calendar = Calendar.getInstance()
        calendar.time = startDate

        when (subscription.billingFrequency) {
            BillingFrequency.MONTHLY -> {
                while (calendar.time.before(today) || calendar.time == today) {
                    calendar.add(Calendar.MONTH, 1)
                }
            }
            BillingFrequency.YEARLY -> {
                while (calendar.time.before(today) || calendar.time == today) {
                    calendar.add(Calendar.YEAR, 1)
                }
            }
        }

        val nextPaymentDate = calendar.time

        if (endDate != null && nextPaymentDate.after(endDate)) {
            return null
        }

        return nextPaymentDate
    }

    private fun getDaysDifference(from: Date, to: Date): Int {
        val diffMillis = to.time - from.time
        return TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS).toInt()
    }
}