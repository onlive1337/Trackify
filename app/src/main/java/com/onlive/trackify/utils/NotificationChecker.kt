package com.onlive.trackify.utils

import android.content.Context
import android.util.Log
import com.onlive.trackify.data.database.AppDatabase
import com.onlive.trackify.data.model.Subscription
import java.util.Date

class NotificationChecker(private val context: Context) {

    private val tag = "NotificationChecker"

    suspend fun checkNotifications() {
        val preferenceManager = PreferenceManager(context)
        if (!preferenceManager.areNotificationsEnabled()) {
            Log.d(tag, "Notifications disabled, skipping check")
            return
        }

        val database = AppDatabase.getDatabase(context)
        val notificationHelper = NotificationHelper(context)

        preferenceManager.cleanupOldNotificationRecords()

        val reminderDays = preferenceManager.getReminderDays()
        val maxReminderDay = reminderDays.maxOrNull()
        if (maxReminderDay == null) {
            Log.d(tag, "No reminder days configured, nothing to do")
            return
        }

        val subscriptions = database.subscriptionDao().getAllSubscriptionsForWorker()
        val today = Date()

        Log.d(tag, "Checking ${subscriptions.size} subscriptions (window=$maxReminderDay days)")

        for (subscription in subscriptions) {
            checkPaymentReminder(subscription, today, maxReminderDay, notificationHelper, preferenceManager)
            checkExpirationReminder(subscription, today, maxReminderDay, notificationHelper, preferenceManager)
        }
    }

    private fun checkPaymentReminder(
        subscription: Subscription,
        today: Date,
        maxReminderDay: Int,
        notificationHelper: NotificationHelper,
        preferenceManager: PreferenceManager,
    ) {
        val nextPaymentDate = PaymentScheduleCalculator.calculateNextPaymentDate(
            subscription.startDate,
            subscription.billingFrequency,
            subscription.endDate,
            today
        ) ?: return

        val daysUntil = PaymentScheduleCalculator.getDaysDifference(today, nextPaymentDate)
        if (daysUntil < 0 || daysUntil > maxReminderDay) return

        val dateKey = preferenceManager.formatDateKey(nextPaymentDate)
        if (preferenceManager.wasNotificationSentForDate(subscription.subscriptionId, TYPE_PAYMENT, dateKey)) return

        notificationHelper.showPaymentReminderNotification(subscription, daysUntil)
        preferenceManager.markNotificationSentForDate(subscription.subscriptionId, TYPE_PAYMENT, dateKey)
        Log.d(tag, "Payment reminder sent for ${subscription.name} (in $daysUntil days, cycle $dateKey)")
    }

    private fun checkExpirationReminder(
        subscription: Subscription,
        today: Date,
        maxReminderDay: Int,
        notificationHelper: NotificationHelper,
        preferenceManager: PreferenceManager,
    ) {
        val endDate = subscription.endDate ?: return
        val todayStart = PaymentScheduleCalculator.normalizeToStartOfDay(today)
        val endDateStart = PaymentScheduleCalculator.normalizeToStartOfDay(endDate)
        if (endDateStart.before(todayStart)) return

        val daysUntil = PaymentScheduleCalculator.getDaysDifference(today, endDate)
        if (daysUntil < 0 || daysUntil > maxReminderDay) return

        val dateKey = preferenceManager.formatDateKey(endDate)
        if (preferenceManager.wasNotificationSentForDate(subscription.subscriptionId, TYPE_EXPIRATION, dateKey)) return

        notificationHelper.showExpirationReminderNotification(subscription, daysUntil)
        preferenceManager.markNotificationSentForDate(subscription.subscriptionId, TYPE_EXPIRATION, dateKey)
        Log.d(tag, "Expiration reminder sent for ${subscription.name} (in $daysUntil days, cycle $dateKey)")
    }

    companion object {
        private const val TYPE_PAYMENT = "payment"
        private const val TYPE_EXPIRATION = "expiration"
    }
}
