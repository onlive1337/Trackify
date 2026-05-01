package com.onlive.trackify.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.onlive.trackify.data.database.AppDatabase
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.utils.AlarmScheduler
import com.onlive.trackify.utils.NotificationHelper
import com.onlive.trackify.utils.PreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.util.concurrent.TimeUnit

class NotificationReceiver : BroadcastReceiver() {

    private val tag = "NotificationReceiver"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(tag, "Alarm received")

        val preferenceManager = PreferenceManager(context)
        if (!preferenceManager.areNotificationsEnabled()) {
            Log.d(tag, "Notifications disabled, skipping check")
            return
        }

        val alarmScheduler = AlarmScheduler(context)
        alarmScheduler.scheduleNextAlarm()

        val pendingResult = goAsync()

        scope.launch {
            try {
                checkNotifications(context, preferenceManager)
            } catch (e: Exception) {
                Log.e(tag, "Error checking notifications", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun checkNotifications(context: Context, preferenceManager: PreferenceManager) {
        val database = AppDatabase.getDatabase(context)
        val notificationHelper = NotificationHelper(context)
        
        preferenceManager.cleanupOldNotificationRecords()

        val subscriptions = database.subscriptionDao().getAllSubscriptionsForWorker()
        val reminderDays = preferenceManager.getReminderDays()
        val today = Calendar.getInstance().time

        Log.d(tag, "Checking ${subscriptions.size} subscriptions")

        for (subscription in subscriptions) {
            checkPaymentReminders(subscription, today, reminderDays, notificationHelper, preferenceManager)
            checkExpirationReminders(subscription, today, reminderDays, notificationHelper, preferenceManager)
        }
    }

    private fun checkPaymentReminders(
        subscription: Subscription, 
        today: Date, 
        reminderDays: Set<Int>,
        notificationHelper: NotificationHelper,
        preferenceManager: PreferenceManager,
    ) {
        val nextPaymentDate = calculateNextPaymentDate(subscription, today) ?: return
        val daysUntil = getDaysDifference(today, nextPaymentDate)

        if (daysUntil in reminderDays) {
            if (!preferenceManager.wasNotificationSentToday(subscription.subscriptionId, daysUntil)) {
                notificationHelper.showPaymentReminderNotification(subscription, daysUntil)
                preferenceManager.markNotificationSent(subscription.subscriptionId, daysUntil)
                Log.d(tag, "Payment reminder sent for ${subscription.name}")
            }
        }
    }

    private fun checkExpirationReminders(
        subscription: Subscription, 
        today: Date, 
        reminderDays: Set<Int>,
        notificationHelper: NotificationHelper,
        preferenceManager: PreferenceManager,
    ) {
        val endDate = subscription.endDate ?: return
        if (endDate.before(today)) return

        val daysUntil = getDaysDifference(today, endDate)

        if (daysUntil in reminderDays) {
            val expirationKey = -1000 - daysUntil
            if (!preferenceManager.wasNotificationSentToday(subscription.subscriptionId, expirationKey)) {
                notificationHelper.showExpirationReminderNotification(subscription, daysUntil)
                preferenceManager.markNotificationSent(subscription.subscriptionId, expirationKey)
                Log.d(tag, "Expiration reminder sent for ${subscription.name}")
            }
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
        if (endDate != null && (nextPaymentDate.after(normalizeToStartOfDay(endDate)))) {
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