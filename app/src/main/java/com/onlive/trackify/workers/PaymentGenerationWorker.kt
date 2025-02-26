package com.onlive.trackify.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.onlive.trackify.data.database.AppDatabase
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.PaymentStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import kotlin.math.abs

class PaymentGenerationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val database = AppDatabase.getDatabase(appContext)
    private val subscriptionDao = database.subscriptionDao()
    private val paymentDao = database.paymentDao()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val activeSubscriptions = subscriptionDao.getActiveSubscriptionsSync()

            for (subscription in activeSubscriptions) {
                val lastPayment = paymentDao.getLastPaymentForSubscriptionSync(subscription.subscriptionId)

                val calendar = Calendar.getInstance()
                val today = calendar.time

                val lastPaymentDate = lastPayment?.date ?: subscription.startDate
                val nextPaymentDate = calculateNextPaymentDate(lastPaymentDate, subscription.billingFrequency)

                if (!isDateInFuture(nextPaymentDate, today) && !isPaymentAlreadyCreated(subscription.subscriptionId, nextPaymentDate)) {
                    val newPayment = Payment(
                        subscriptionId = subscription.subscriptionId,
                        amount = subscription.price,
                        date = nextPaymentDate,
                        status = PaymentStatus.PENDING,
                        autoGenerated = true,
                        notes = "Автоматически создан системой"
                    )

                    paymentDao.insert(newPayment)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun calculateNextPaymentDate(baseDate: Date, frequency: BillingFrequency): Date {
        val calendar = Calendar.getInstance()
        calendar.time = baseDate

        when (frequency) {
            BillingFrequency.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            BillingFrequency.YEARLY -> calendar.add(Calendar.YEAR, 1)
        }

        return calendar.time
    }

    private suspend fun isPaymentAlreadyCreated(subscriptionId: Long, date: Date): Boolean {
        val calendar = Calendar.getInstance()
        calendar.time = date

        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val startDate = calendar.time

        calendar.add(Calendar.DAY_OF_MONTH, 2)
        val endDate = calendar.time

        val paymentsInRange = paymentDao.getPaymentsForSubscriptionBetweenDatesSync(
            subscriptionId, startDate, endDate
        )

        return paymentsInRange.isNotEmpty()
    }

    private fun isDateInFuture(date: Date, referenceDate: Date): Boolean {
        return date.after(referenceDate)
    }
}