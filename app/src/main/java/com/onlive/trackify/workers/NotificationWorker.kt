package com.onlive.trackify.workers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.onlive.trackify.R
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

    private val tag = "NotificationWorker"
    private val foregroundChannelId = "trackify_worker_channel"
    private val foregroundNotificationId = 9999

    override suspend fun getForegroundInfo(): ForegroundInfo {
        createForegroundChannel()

        val notification = NotificationCompat.Builder(applicationContext, foregroundChannelId)
            .setContentTitle(applicationContext.getString(R.string.app_name))
            .setContentText(applicationContext.getString(R.string.checking_subscriptions))
            .setSmallIcon(R.drawable.ic_payments)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                foregroundNotificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(foregroundNotificationId, notification)
        }
    }

    private fun createForegroundChannel() {
        val channel = NotificationChannel(
            foregroundChannelId,
            "Background tasks",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Background subscription checking"
            setShowBadge(false)
        }

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(tag, "Starting notification check")

            if (!preferenceManager.areNotificationsEnabled()) {
                Log.d(tag, "Notifications disabled")
                return@withContext Result.success()
            }

            preferenceManager.cleanupOldNotificationRecords()

            val subscriptions = database.subscriptionDao().getAllSubscriptionsForWorker()
            val reminderDays = preferenceManager.getReminderDays()
            val today = Calendar.getInstance().time

            Log.d(tag, "Checking ${subscriptions.size} subscriptions with reminder days: $reminderDays")

            for (subscription in subscriptions) {
                checkPaymentReminders(subscription, today, reminderDays)
                checkExpirationReminders(subscription, today, reminderDays)
            }

            Log.d(tag, "Notification check completed")
            Result.success()
        } catch (e: Exception) {
            Log.e(tag, "Error in notification worker", e)
            Result.failure()
        }
    }

    private fun checkPaymentReminders(subscription: Subscription, today: Date, reminderDays: Set<Int>) {
        val nextPaymentDate = calculateNextPaymentDate(subscription, today)

        if (nextPaymentDate == null) {
            Log.d(tag, "${subscription.name}: No next payment date (subscription ended or null)")
            return
        }

        val daysUntil = getDaysDifference(today, nextPaymentDate)

        Log.d(tag, "${subscription.name}: next payment on $nextPaymentDate, days until: $daysUntil, reminder days: $reminderDays")

        if (daysUntil in reminderDays) {
            if (preferenceManager.wasNotificationSentToday(subscription.subscriptionId, daysUntil)) {
                Log.d(tag, "${subscription.name}: Notification already sent today, skipping")
                return
            }

            notificationHelper.showPaymentReminderNotification(subscription, daysUntil)
            preferenceManager.markNotificationSent(subscription.subscriptionId, daysUntil)
            Log.d(tag, "Payment reminder sent for ${subscription.name}, days until: $daysUntil")
        } else {
            Log.d(tag, "${subscription.name}: daysUntil ($daysUntil) not in reminderDays ($reminderDays)")
        }
    }

    private fun checkExpirationReminders(subscription: Subscription, today: Date, reminderDays: Set<Int>) {
        val endDate = subscription.endDate ?: return

        if (endDate.before(today)) return

        val daysUntil = getDaysDifference(today, endDate)

        if (daysUntil in reminderDays) {
            val expirationKey = -1000 - daysUntil
            if (preferenceManager.wasNotificationSentToday(subscription.subscriptionId, expirationKey)) {
                Log.d(tag, "${subscription.name}: Expiration notification already sent today, skipping")
                return
            }

            notificationHelper.showExpirationReminderNotification(subscription, daysUntil)
            preferenceManager.markNotificationSent(subscription.subscriptionId, expirationKey)
            Log.d(tag, "Expiration reminder sent for ${subscription.name}, days until: $daysUntil")
        }
    }

    private fun calculateNextPaymentDate(subscription: Subscription, today: Date): Date? {
        val startDate = subscription.startDate
        val endDate = subscription.endDate

        val todayStart = normalizeToStartOfDay(today)
        val startDateNormalized = normalizeToStartOfDay(startDate)

        if (startDateNormalized.after(todayStart)) {
            return startDateNormalized
        }

        val calendar = Calendar.getInstance()
        calendar.time = startDateNormalized

        when (subscription.billingFrequency) {
            BillingFrequency.MONTHLY -> {
                while (calendar.time.before(todayStart)) {
                    calendar.add(Calendar.MONTH, 1)
                }
            }
            BillingFrequency.YEARLY -> {
                while (calendar.time.before(todayStart)) {
                    calendar.add(Calendar.YEAR, 1)
                }
            }
        }

        val nextPaymentDate = calendar.time

        if (endDate != null && nextPaymentDate.after(normalizeToStartOfDay(endDate))) {
            return null
        }

        return nextPaymentDate
    }

    private fun normalizeToStartOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    private fun getDaysDifference(from: Date, to: Date): Int {
        val fromNormalized = normalizeToStartOfDay(from)
        val toNormalized = normalizeToStartOfDay(to)
        val diffMillis = toNormalized.time - fromNormalized.time
        return TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS).toInt()
    }
}