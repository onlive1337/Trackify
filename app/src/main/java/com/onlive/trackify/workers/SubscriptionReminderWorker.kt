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
import com.onlive.trackify.data.model.Subscription
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
            Log.d(TAG, "SubscriptionReminderWorker запущен")

            if (!preferenceManager.areNotificationsEnabled()) {
                Log.d(TAG, "Уведомления отключены в настройках")
                return@withContext Result.success()
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

                if (!hasPermission) {
                    Log.d(TAG, "Нет разрешения на отправку уведомлений")
                    return@withContext Result.success()
                }
            }

            val today = Calendar.getInstance()

            if (preferenceManager.getNotificationFrequency() == NotificationFrequency.WEEKLY) {
                if (today.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                    Log.d(TAG, "Сегодня не понедельник, пропускаем для еженедельной частоты")
                    return@withContext Result.success()
                }
            }

            if (preferenceManager.getNotificationFrequency() == NotificationFrequency.MONTHLY) {
                if (today.get(Calendar.DAY_OF_MONTH) != 1) {
                    Log.d(TAG, "Сегодня не первый день месяца, пропускаем для ежемесячной частоты")
                    return@withContext Result.success()
                }
            }

            val database = AppDatabase.getDatabase(applicationContext)
            val subscriptions = database.subscriptionDao().getActiveSubscriptionsSync()

            if (subscriptions.isEmpty()) {
                Log.d(TAG, "Активные подписки не найдены")
                return@withContext Result.success()
            }

            var notificationCount = 0
            val reminderDays = preferenceManager.getReminderDays()
            val currentDate = today.time

            for (subscription in subscriptions) {
                subscription.endDate?.let { endDate ->
                    if (endDate.after(currentDate)) {
                        val daysUntilExpiration = getDaysDifference(currentDate, endDate)

                        if (daysUntilExpiration in reminderDays) {
                            notificationHelper.showSubscriptionExpirationNotification(subscription, daysUntilExpiration)
                            notificationCount++
                            Log.d(TAG, "Отправлено уведомление об окончании подписки ${subscription.name} через $daysUntilExpiration дней")
                        }
                    }
                }

                val nextPaymentDate = calculateNextPaymentDate(subscription)
                if (nextPaymentDate != null) {
                    val daysUntilPayment = getDaysDifference(currentDate, nextPaymentDate)

                    if (daysUntilPayment in reminderDays) {
                        notificationHelper.showUpcomingSubscriptionPaymentNotification(subscription, daysUntilPayment)
                        notificationCount++
                        Log.d(TAG, "Отправлено уведомление о платеже по подписке ${subscription.name} через $daysUntilPayment дней")
                    }
                }
            }

            Log.d(TAG, "SubscriptionReminderWorker завершен успешно, отправлено $notificationCount уведомлений")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка в SubscriptionReminderWorker: ${e.message}", e)
            Result.failure()
        }
    }

    private fun calculateNextPaymentDate(subscription: Subscription): Date? {
        val calendar = Calendar.getInstance()
        val today = calendar.time
        val startDate = subscription.startDate

        if (startDate.after(today)) {
            return startDate
        }

        val paymentCalendar = Calendar.getInstance()
        paymentCalendar.time = startDate

        when (subscription.billingFrequency) {
            BillingFrequency.MONTHLY -> {
                while (paymentCalendar.time.before(today)) {
                    paymentCalendar.add(Calendar.MONTH, 1)
                }
            }
            BillingFrequency.YEARLY -> {
                while (paymentCalendar.time.before(today)) {
                    paymentCalendar.add(Calendar.YEAR, 1)
                }
            }
        }

        subscription.endDate?.let { endDate ->
            if (endDate.before(paymentCalendar.time)) {
                return null
            }
        }

        return paymentCalendar.time
    }

    private fun getDaysDifference(date1: Date, date2: Date): Int {
        val diffMillis = date2.time - date1.time
        return TimeUnit.DAYS.convert(diffMillis, TimeUnit.MILLISECONDS).toInt()
    }
}