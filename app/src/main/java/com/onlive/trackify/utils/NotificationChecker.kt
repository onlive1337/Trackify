package com.onlive.trackify.utils

import android.content.Context
import android.util.Log
import com.onlive.trackify.data.database.AppDatabase
import com.onlive.trackify.data.model.Subscription
import java.time.LocalDate
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class NotificationChecker(private val context: Context) {

    private val tag = "NotificationChecker"

    suspend fun checkNotifications() = checkMutex.withLock {
        val preferenceManager = PreferenceManager(context)
        if (!preferenceManager.areNotificationsEnabled()) {
            Log.d(tag, "Notifications disabled, skipping check")
            return@withLock
        }

        val database = AppDatabase.getDatabase(context)
        val notificationHelper = NotificationHelper(context)

        preferenceManager.cleanupOldNotificationRecords()

        val reminderDays = preferenceManager.getReminderDays()
        if (reminderDays.isEmpty()) {
            Log.d(tag, "No reminder days configured, nothing to do")
            return@withLock
        }

        val subscriptions = database.subscriptionDao().getAllSubscriptionsForWorker()
        val today = LocalDate.now()

        Log.d(tag, "Checking ${subscriptions.size} subscriptions (days=$reminderDays)")

        for (subscription in subscriptions) {
            checkPaymentReminder(subscription, today, reminderDays, notificationHelper, preferenceManager)
            checkExpirationReminder(subscription, today, reminderDays, notificationHelper, preferenceManager)
        }
    }

    private fun checkPaymentReminder(
        subscription: Subscription,
        today: LocalDate,
        reminderDays: Set<Int>,
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
        val matchedThreshold = resolveReminderThreshold(daysUntil, reminderDays) ?: return

        val dateKey = preferenceManager.formatDateKey(nextPaymentDate)
        if (preferenceManager.wasNotificationSentForDate(subscription.subscriptionId, TYPE_PAYMENT, dateKey, matchedThreshold)) return

        if (notificationHelper.showPaymentReminderNotification(subscription, daysUntil)) {
            preferenceManager.markNotificationSentForDate(subscription.subscriptionId, TYPE_PAYMENT, dateKey, matchedThreshold)
            Log.d(tag, "Payment reminder sent for ${subscription.name} (in $daysUntil days, threshold=$matchedThreshold, cycle $dateKey)")
        }
    }

    private fun checkExpirationReminder(
        subscription: Subscription,
        today: LocalDate,
        reminderDays: Set<Int>,
        notificationHelper: NotificationHelper,
        preferenceManager: PreferenceManager,
    ) {
        val endDate = subscription.endDate ?: return
        if (endDate.isBefore(today)) return

        val daysUntil = PaymentScheduleCalculator.getDaysDifference(today, endDate)
        val matchedThreshold = resolveReminderThreshold(daysUntil, reminderDays) ?: return

        val dateKey = preferenceManager.formatDateKey(endDate)
        if (preferenceManager.wasNotificationSentForDate(subscription.subscriptionId, TYPE_EXPIRATION, dateKey, matchedThreshold)) return

        if (notificationHelper.showExpirationReminderNotification(subscription, daysUntil)) {
            preferenceManager.markNotificationSentForDate(subscription.subscriptionId, TYPE_EXPIRATION, dateKey, matchedThreshold)
            Log.d(tag, "Expiration reminder sent for ${subscription.name} (in $daysUntil days, threshold=$matchedThreshold, cycle $dateKey)")
        }
    }

    companion object {
        private const val TYPE_PAYMENT = "payment"
        private const val TYPE_EXPIRATION = "expiration"
        private val checkMutex = Mutex()

        internal fun resolveReminderThreshold(daysUntil: Int, reminderDays: Set<Int>): Int? {
            if (daysUntil < 0 || reminderDays.isEmpty()) return null
            return when {
                daysUntil in reminderDays -> daysUntil
                (daysUntil + 1) in reminderDays -> daysUntil + 1
                else -> null
            }
        }
    }
}
