package com.onlive.trackify

import android.app.Application
import android.os.Build
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.onlive.trackify.data.cache.CacheService
import com.onlive.trackify.data.database.DatabaseInitializer
import com.onlive.trackify.utils.ErrorHandler
import com.onlive.trackify.utils.MemoryUtils
import com.onlive.trackify.utils.NotificationHelper
import com.onlive.trackify.utils.PaymentScheduler
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.utils.ThemeManager
import com.onlive.trackify.workers.DatabaseCleanupWorker
import com.onlive.trackify.workers.SubscriptionReminderWorker
import com.google.android.material.color.DynamicColors
import java.util.concurrent.TimeUnit

class TrackifyApplication : Application(), Configuration.Provider {

    lateinit var errorHandler: ErrorHandler
        private set

    private lateinit var themeManager: ThemeManager
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var databaseInitializer: DatabaseInitializer
    private lateinit var paymentScheduler: PaymentScheduler
    private lateinit var cacheService: CacheService

    override fun onCreate() {
        super.onCreate()

        try {
            // Инициализация обработчика ошибок
            errorHandler = ErrorHandler.getInstance(this)

            // Инициализация сервиса кэширования
            cacheService = CacheService.getInstance()

            // Инициализация всех компонентов
            initializeComponents()

            // Применение динамических цветов для Android S+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                DynamicColors.applyToActivitiesIfAvailable(this)
            }
        } catch (e: Exception) {
            handleFatalError(e)
        }
    }

    private fun initializeComponents() {
        // Инициализация менеджера тем
        themeManager = ThemeManager(this)
        themeManager.applyTheme()

        // Инициализация менеджера предпочтений
        preferenceManager = PreferenceManager(this)

        // Инициализация базы данных
        databaseInitializer = DatabaseInitializer(this)
        databaseInitializer.initializeCategories()

        // Инициализация уведомлений
        notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()

        // Настройка заданий WorkManager
        setupSubscriptionReminders()
        setupDatabaseCleanup()

        // Инициализация планировщика платежей
        paymentScheduler = PaymentScheduler(this)
        paymentScheduler.schedulePaymentGeneration()
    }

    private fun setupSubscriptionReminders() {
        // Настраиваем периодическое задание для напоминаний о подписках
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
        // Настраиваем периодическое задание для очистки базы данных
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
        // Очищаем кэш при завершении
        cacheService.clearCache()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        // Обрабатываем ситуацию с нехваткой памяти
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

    // Конфигурация WorkManager
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}