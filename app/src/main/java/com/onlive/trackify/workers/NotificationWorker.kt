package com.onlive.trackify.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.onlive.trackify.data.database.AppDatabase
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.utils.DateUtils
import com.onlive.trackify.utils.NotificationHelper
import com.onlive.trackify.utils.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

class NotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val notificationHelper = NotificationHelper(appContext)
    private val preferenceManager = PreferenceManager(appContext)
    private val database = AppDatabase.getDatabase(appContext)

    companion object {
        private const val TAG = "NotificationWorker"
        private const val MAX_FUTURE_YEARS = 10
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            Log.d(TAG, "Starting notification check")

            if (!preferenceManager.areNotificationsEnabled()) {
                Log.d(TAG, "Notifications disabled")
                return@withContext Result.success()
            }

            val subscriptions = database.subscriptionDao().getActiveSubscriptionsSync()
            val reminderDays = preferenceManager.getReminderDays()
            val today = Date()

            Log.d(TAG, "Checking ${subscriptions.size} subscriptions with reminder days: $reminderDays")

            for (subscription in subscriptions) {
                try {
                    checkPaymentReminders(subscription, today, reminderDays)
                    checkExpirationReminders(subscription, today, reminderDays)
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing subscription ${subscription.subscriptionId}", e)
                }
            }

            Log.d(TAG, "Notification check completed")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in notification worker", e)
            Result.failure()
        }
    }

    private fun checkPaymentReminders(subscription: Subscription, today: Date, reminderDays: Set<Int>) {
        try {
            val nextPaymentDate = calculateNextPaymentDate(subscription, today) ?: return
            val daysUntil = DateUtils.getDaysDifference(today, nextPaymentDate)

            if (daysUntil in reminderDays && daysUntil >= 0) {
                notificationHelper.showPaymentReminderNotification(subscription, daysUntil)
                Log.d(TAG, "Payment reminder sent for ${subscription.name}, days until: $daysUntil")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking payment reminders for ${subscription.subscriptionId}", e)
        }
    }

    private fun checkExpirationReminders(subscription: Subscription, today: Date, reminderDays: Set<Int>) {
        try {
            val endDate = subscription.endDate ?: return
            val validatedEndDate = DateUtils.validateDate(endDate)

            if (validatedEndDate.before(today)) return

            val daysUntil = DateUtils.getDaysDifference(today, validatedEndDate)

            if (daysUntil in reminderDays && daysUntil >= 0) {
                notificationHelper.showExpirationReminderNotification(subscription, daysUntil)
                Log.d(TAG, "Expiration reminder sent for ${subscription.name}, days until: $daysUntil")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking expiration reminders for ${subscription.subscriptionId}", e)
        }
    }

    private fun calculateNextPaymentDate(subscription: Subscription, today: Date): Date? {
        return try {
            val startDate = DateUtils.validateDate(subscription.startDate)
            val endDate = subscription.endDate?.let { DateUtils.validateDate(it) }

            if (startDate.after(today)) {
                return startDate
            }

            val calendar = Calendar.getInstance()
            calendar.time = startDate

            var iterations = 0
            val maxIterations = when (subscription.billingFrequency) {
                BillingFrequency.MONTHLY -> 12 * MAX_FUTURE_YEARS
                BillingFrequency.YEARLY -> MAX_FUTURE_YEARS
            }

            when (subscription.billingFrequency) {
                BillingFrequency.MONTHLY -> {
                    while (calendar.time.before(today) || calendar.time == today) {
                        if (iterations++ > maxIterations) {
                            Log.w(TAG, "Too many iterations for subscription ${subscription.subscriptionId}")
                            return null
                        }

                        calendar.time = DateUtils.addMonthsSafely(calendar.time, 1)
                    }
                }
                BillingFrequency.YEARLY -> {
                    while (calendar.time.before(today) || calendar.time == today) {
                        if (iterations++ > maxIterations) {
                            Log.w(TAG, "Too many iterations for subscription ${subscription.subscriptionId}")
                            return null
                        }

                        calendar.time = DateUtils.addYearsSafely(calendar.time, 1)
                    }
                }
            }

            val nextPaymentDate = calendar.time

            val futureLimit = DateUtils.getDateAfterYears(MAX_FUTURE_YEARS)
            if (nextPaymentDate.after(futureLimit)) {
                Log.w(TAG, "Next payment date too far in future for subscription ${subscription.subscriptionId}")
                return null
            }

            if (endDate != null && nextPaymentDate.after(endDate)) {
                return null
            }

            nextPaymentDate
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating next payment date for subscription ${subscription.subscriptionId}", e)
            null
        }
    }
}