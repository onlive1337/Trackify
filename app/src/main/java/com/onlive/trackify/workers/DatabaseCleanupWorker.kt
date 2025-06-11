package com.onlive.trackify.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.onlive.trackify.data.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class DatabaseCleanupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    private val database = AppDatabase.getDatabase(appContext)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            deleteOldPayments()
            deleteExpiredSubscriptions()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun deleteOldPayments() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -12)
        val thresholdDate = calendar.time

        val oldPayments = database.paymentDao().getAllPaymentsSync().filter {
            it.date.before(thresholdDate)
        }

        for (payment in oldPayments) {
            database.paymentDao().delete(payment)
        }
    }

    private suspend fun deleteExpiredSubscriptions() {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, -3)
        val thresholdDate = calendar.time

        val expiredSubscriptions = database.subscriptionDao().getAllSubscriptionsSync().filter {
            it.endDate != null && it.endDate.before(thresholdDate)
        }

        for (subscription in expiredSubscriptions) {
            database.subscriptionDao().delete(subscription)
        }
    }
}