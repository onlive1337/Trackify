package com.onlive.trackify

import android.app.Application
import android.util.Log
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
import kotlin.system.exitProcess

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

        setupGlobalExceptionHandler()

        try {
            errorHandler = ErrorHandler.getInstance(this)

            cacheService = CacheService.getInstance()

            initializeComponents()
        } catch (e: Exception) {
            handleFatalError(e)
        }
    }

    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            try {
                Log.e("TrackifyApplication", "Uncaught exception in thread ${thread.name}", exception)

                exception.printStackTrace()

                try {
                    cacheService.clearCache()
                } catch (e: Exception) {
                    Log.e("TrackifyApplication", "Error clearing cache during crash", e)
                }

            } catch (e: Exception) {
                Log.e("TrackifyApplication", "Error in exception handler", e)
            } finally {
                defaultHandler?.uncaughtException(thread, exception)
            }
        }
    }

    private fun initializeComponents() {
        themeManager = ThemeManager(this)

        preferenceManager = PreferenceManager(this)

        databaseInitializer = DatabaseInitializer(this)

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
        Log.e("TrackifyApplication", "Fatal error during initialization", e)
        e.printStackTrace()
        exitProcess(1)
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
        if (level >= TRIM_MEMORY_RUNNING_LOW) {
            MemoryUtils.handleLowMemory(this)
            cacheService.trimCache()
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
}