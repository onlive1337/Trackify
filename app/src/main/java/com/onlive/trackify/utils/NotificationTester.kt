package com.onlive.trackify.utils

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.onlive.trackify.data.database.AppDatabase
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.workers.SubscriptionReminderWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class NotificationTester(private val context: Context) {

    private val notificationHelper = NotificationHelper(context)
    private val db = AppDatabase.getDatabase(context)

    fun runWorkerNow() {
        val workRequest = OneTimeWorkRequestBuilder<SubscriptionReminderWorker>()
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "test_subscription_reminders",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }

    fun sendTestNotification() {
        val testSubscription = Subscription(
            subscriptionId = 9999,
            name = "Тестовая подписка",
            description = "Тестирование уведомлений",
            price = 299.0,
            billingFrequency = BillingFrequency.MONTHLY,
            startDate = Date(),
            endDate = null,
            categoryId = null,
            active = true
        )

        notificationHelper.showUpcomingPaymentNotification(testSubscription, 0)

        Thread.sleep(2000)

        notificationHelper.showUpcomingPaymentNotification(testSubscription, 1)

        Thread.sleep(2000)

        notificationHelper.showUpcomingPaymentNotification(testSubscription, 7)
    }

    fun createTestSubscriptionAndRunCheck() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.MONTH, -1)

                val testSubscription = Subscription(
                    subscriptionId = 0,
                    name = "Тестовая подписка (сегодня платеж)",
                    description = "Автоматически создана для тестирования",
                    price = 399.0,
                    billingFrequency = BillingFrequency.MONTHLY,
                    startDate = calendar.time,
                    endDate = null,
                    categoryId = null,
                    active = true
                )

                db.subscriptionDao().insert(testSubscription)

                runWorkerNow()
            } catch (e: Exception) {
            }
        }
    }
}