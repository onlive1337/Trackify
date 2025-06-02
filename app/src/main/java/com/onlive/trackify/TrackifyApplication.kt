package com.onlive.trackify

import android.app.Application
import android.content.ComponentCallbacks2
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.onlive.trackify.data.cache.CacheService
import com.onlive.trackify.data.database.DatabaseInitializer
import com.onlive.trackify.utils.ErrorHandler
import com.onlive.trackify.utils.MemoryUtils
import com.onlive.trackify.utils.NotificationHelper
import com.onlive.trackify.utils.NotificationScheduler
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.utils.ThemeManager
import com.onlive.trackify.workers.DatabaseCleanupWorker
import com.onlive.trackify.workers.SubscriptionReminderWorker
import java.util.concurrent.TimeUnit

class TrackifyApplication : Application(), Configuration.Provider {

    lateinit var errorHandler: ErrorHandler
        private set

    private lateinit var themeManager: ThemeManager
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var databaseInitializer: DatabaseInitializer
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
        databaseInitializer.initializeCategories()

        notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()

        val notificationScheduler = NotificationScheduler(this)
        if (preferenceManager.areNotificationsEnabled()) {
            notificationScheduler.rescheduleNotifications()
        }

        setupSubscriptionReminders()
        setupDatabaseCleanup()
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
        System.exit(1)
    }

    override fun onTerminate() {
        super.onTerminate()
        cacheService.clearCache()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        MemoryUtils.handleLowMemory(this)
        cacheService.clearCache()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            MemoryUtils.handleLowMemory(this)
            cacheService.trimCache()
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}