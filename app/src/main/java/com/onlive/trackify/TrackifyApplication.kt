package com.onlive.trackify

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.onlive.trackify.utils.ErrorHandler
import com.onlive.trackify.utils.NotificationHelper
import com.onlive.trackify.utils.NotificationScheduler
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.workers.DatabaseCleanupWorker
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class TrackifyApplication : Application(), Configuration.Provider {

    lateinit var errorHandler: ErrorHandler
        private set

    private lateinit var notificationHelper: NotificationHelper
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var notificationScheduler: NotificationScheduler

    override fun onCreate() {
        super.onCreate()

        try {
            errorHandler = ErrorHandler.getInstance(this)
            initializeComponents()
        } catch (e: Exception) {
            handleFatalError(e)
        }
    }

    private fun initializeComponents() {
        preferenceManager = PreferenceManager(this)

        notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()

        notificationScheduler = NotificationScheduler(this)
        if (preferenceManager.areNotificationsEnabled()) {
            notificationScheduler.scheduleNotifications()
            notificationScheduler.triggerImmediateCheck()
        }

        setupDatabaseCleanup()
    }

    private fun setupDatabaseCleanup() {
        try {
            val cleanupWorkRequest = PeriodicWorkRequestBuilder<DatabaseCleanupWorker>(
                7, TimeUnit.DAYS
            ).build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "database_cleanup",
                ExistingPeriodicWorkPolicy.UPDATE,
                cleanupWorkRequest
            )
        } catch (e: Exception) {
            Log.e("TrackifyApp", "Failed to setup database cleanup", e)
        }
    }

    private fun handleFatalError(e: Exception) {
        Log.e("TrackifyApp", "Fatal error during initialization", e)
        e.printStackTrace()
        exitProcess(1)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
}