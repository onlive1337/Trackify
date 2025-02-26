package com.onlive.trackify

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.onlive.trackify.data.cache.CacheService
import com.onlive.trackify.data.database.DatabaseInitializer
import com.onlive.trackify.utils.ErrorHandler
import com.onlive.trackify.utils.NotificationHelper
import com.onlive.trackify.utils.PaymentScheduler
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.utils.ThemeManager
import com.onlive.trackify.workers.DatabaseCleanupWorker
import com.onlive.trackify.workers.SubscriptionReminderWorker
import com.google.android.material.color.DynamicColors
import kotlin.system.exitProcess
import java.util.concurrent.TimeUnit

class TrackifyApplication : Application() {
    private lateinit var themeManager: ThemeManager
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var databaseInitializer: DatabaseInitializer
    private lateinit var paymentScheduler: PaymentScheduler

    lateinit var errorHandler: ErrorHandler
        private set

    private lateinit var cacheService: CacheService

    override fun onCreate() {
        super.onCreate()

        try {
            errorHandler = ErrorHandler.getInstance(this)

            cacheService = CacheService.getInstance()

            initializeComponents()

        } catch (e: Exception) {
            handleFatalError(e)
        }
    }

    private fun initializeComponents() {
        themeManager = ThemeManager(this)
        preferenceManager = PreferenceManager(this)
        databaseInitializer = DatabaseInitializer(this)
        paymentScheduler = PaymentScheduler(this)

        themeManager.applyTheme()

        if (themeManager.supportsDynamicColors()) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }

        databaseInitializer.initializeCategories()

        notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()

        setupSubscriptionReminders()

        setupDatabaseCleanup()

        paymentScheduler.schedulePaymentGeneration()
    }

    private fun setupSubscriptionReminders() {
        val reminderWorkRequest = PeriodicWorkRequestBuilder<SubscriptionReminderWorker>(
            1, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "subscription_reminders",
            ExistingPeriodicWorkPolicy.UPDATE,
            reminderWorkRequest
        )
    }

    private fun setupDatabaseCleanup() {
        val cleanupWorkRequest = PeriodicWorkRequestBuilder<DatabaseCleanupWorker>(
            7, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "database_cleanup",
            ExistingPeriodicWorkPolicy.UPDATE,
            cleanupWorkRequest
        )
    }

    private fun handleFatalError(e: Exception) {
        e.printStackTrace()

        android.util.Log.e("TrackifyApplication", "Fatal error during initialization", e)

        exitProcess(1)
    }

    override fun onTerminate() {
        super.onTerminate()

        cacheService.clearCache()
    }
}