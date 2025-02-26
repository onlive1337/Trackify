package com.onlive.trackify

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.onlive.trackify.data.database.DatabaseInitializer
import com.onlive.trackify.utils.NotificationHelper
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.utils.ThemeManager
import com.onlive.trackify.workers.SubscriptionReminderWorker
import com.google.android.material.color.DynamicColors
import java.util.concurrent.TimeUnit

class TrackifyApplication : Application() {
    private lateinit var themeManager: ThemeManager
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var databaseInitializer: DatabaseInitializer

    override fun onCreate() {
        super.onCreate()

        themeManager = ThemeManager(this)
        preferenceManager = PreferenceManager(this)
        databaseInitializer = DatabaseInitializer(this)

        themeManager.applyTheme()

        if (themeManager.supportsDynamicColors()) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }

        databaseInitializer.initializeCategories()

        notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()

        setupSubscriptionReminders()
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
}