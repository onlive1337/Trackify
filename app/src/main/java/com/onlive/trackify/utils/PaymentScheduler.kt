package com.onlive.trackify.utils

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.onlive.trackify.workers.PaymentGenerationWorker
import java.util.concurrent.TimeUnit

class PaymentScheduler(private val context: Context) {

    companion object {
        private const val PAYMENT_GENERATION_WORK_NAME = "payment_generation_work"
    }

    fun schedulePaymentGeneration() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiresDeviceIdle(false)
            .setRequiresCharging(false)
            .setRequiresStorageNotLow(false)
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<PaymentGenerationWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PAYMENT_GENERATION_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancelPaymentGeneration() {
        WorkManager.getInstance(context).cancelUniqueWork(PAYMENT_GENERATION_WORK_NAME)
    }

    fun runOneTimePaymentGeneration() {
        val workRequest = androidx.work.OneTimeWorkRequestBuilder<PaymentGenerationWorker>()
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}