package com.onlive.trackify

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.onlive.trackify.utils.ErrorHandler
import com.onlive.trackify.utils.NotificationHelper
import com.onlive.trackify.utils.NotificationScheduler
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.utils.ThemeManager
import com.onlive.trackify.workers.DatabaseCleanupWorker
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class TrackifyApplication : Application(), Configuration.Provider {

    lateinit var errorHandler: ErrorHandler
        private set

    private lateinit var themeManager: ThemeManager
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var notificationScheduler: NotificationScheduler

    override fun onCreate() {
        super.onCreate()

        setupGlobalExceptionHandler()
        initializeFirebase()

        try {
            errorHandler = ErrorHandler.getInstance(this)
            initializeComponents()
        } catch (e: Exception) {
            handleFatalError(e)
        }
    }

    private fun initializeFirebase() {
        try {
            FirebaseApp.initializeApp(this)

            FirebaseCrashlytics.getInstance().apply {
                isCrashlyticsCollectionEnabled = true

                setUserId("user_${System.currentTimeMillis()}")
                setCustomKey("app_version", BuildConfig.VERSION_NAME)
                setCustomKey("version_code", BuildConfig.VERSION_CODE)
                setCustomKey("debug_build", BuildConfig.DEBUG)

            }

            Log.i("TrackifyApp", "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e("TrackifyApp", "Firebase initialization failed", e)
        }
    }

    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, exception ->
            try {
                FirebaseCrashlytics.getInstance().apply {
                    setCustomKey("crash_thread", thread.name)
                    setCustomKey("crash_timestamp", System.currentTimeMillis())
                    recordException(exception)
                }

                Log.e("TrackifyApp", "Uncaught exception", exception)
            } catch (e: Exception) {
                Log.e("TrackifyApp", "Error in exception handler", e)
            } finally {
                defaultHandler?.uncaughtException(thread, exception)
            }
        }
    }

    private fun initializeComponents() {
        themeManager = ThemeManager(this)
        preferenceManager = PreferenceManager(this)

        notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()

        notificationScheduler = NotificationScheduler(this)
        if (preferenceManager.areNotificationsEnabled()) {
            notificationScheduler.scheduleNotifications()
        }

        setupDatabaseCleanup()

        FirebaseCrashlytics.getInstance().log("Application initialized successfully")
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
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    private fun handleFatalError(e: Exception) {
        Log.e("TrackifyApp", "Fatal error during initialization", e)
        FirebaseCrashlytics.getInstance().recordException(e)
        e.printStackTrace()
        exitProcess(1)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()
}