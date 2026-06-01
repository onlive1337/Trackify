package com.onlive.trackify

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.onlive.trackify.data.database.AppDatabase
import com.onlive.trackify.data.repository.CategoryRepository
import com.onlive.trackify.data.repository.PaymentRepository
import com.onlive.trackify.data.repository.SubscriptionRepository
import com.onlive.trackify.utils.CrashLogger
import com.onlive.trackify.utils.ErrorHandler
import com.onlive.trackify.utils.NotificationHelper
import com.onlive.trackify.utils.NotificationScheduler
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.workers.DatabaseCleanupWorker
import com.onlive.trackify.workers.NotificationRefreshWorker
import java.util.concurrent.TimeUnit

class TrackifyApplication : Application(), Configuration.Provider {

    val errorHandler: ErrorHandler by lazy { ErrorHandler.getInstance(this) }

    val subscriptionRepository: SubscriptionRepository by lazy {
        val database = AppDatabase.getDatabase(this)
        SubscriptionRepository(database.subscriptionDao(), database.categoryDao(), applicationContext)
    }
    val paymentRepository: PaymentRepository by lazy {
        PaymentRepository(AppDatabase.getDatabase(this).paymentDao(), applicationContext)
    }
    val categoryRepository: CategoryRepository by lazy {
        CategoryRepository(AppDatabase.getDatabase(this).categoryDao())
    }

    override fun onCreate() {
        super.onCreate()

        CrashLogger.install(this)
        errorHandler

        try {
            initializeComponents()
        } catch (e: Exception) {
            handleInitError(e)
        }
    }

    private fun initializeComponents() {
        val preferenceManager = PreferenceManager(this)

        NotificationHelper(this).createNotificationChannel()

        val notificationScheduler = NotificationScheduler(this)
        if (preferenceManager.areNotificationsEnabled()) {
            notificationScheduler.scheduleNotifications()
            notificationScheduler.triggerImmediateCheck()
        }

        setupPeriodicWork()
    }

    private fun setupPeriodicWork() {
        try {
            val workManager = WorkManager.getInstance(this)

            val cleanupWorkRequest = PeriodicWorkRequestBuilder<DatabaseCleanupWorker>(
                7, TimeUnit.DAYS
            ).build()
            workManager.enqueueUniquePeriodicWork(
                "database_cleanup",
                ExistingPeriodicWorkPolicy.UPDATE,
                cleanupWorkRequest
            )

            val refreshWorkRequest = PeriodicWorkRequestBuilder<NotificationRefreshWorker>(
                12, TimeUnit.HOURS
            ).build()
            workManager.enqueueUniquePeriodicWork(
                "notification_refresh",
                ExistingPeriodicWorkPolicy.UPDATE,
                refreshWorkRequest
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set up periodic work", e)
            CrashLogger.writeLog(this, e, "Failed to set up periodic work")
        }
    }

    private fun handleInitError(e: Exception) {
        Log.e(TAG, "Error during initialization, continuing with degraded startup", e)
        CrashLogger.writeLog(this, e, "Error during Application initialization")
        runCatching { errorHandler.handleError(e, showToast = false) }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    companion object {
        private const val TAG = "TrackifyApp"
    }
}
