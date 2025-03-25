package com.onlive.trackify.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.onlive.trackify.data.database.AppDatabase
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.model.CategoryGroup
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.Subscription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DataExportImportManager(private val context: Context) {
    private val TAG = "DataExportImportManager"
    private val database = AppDatabase.getDatabase(context)
    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .serializeNulls()
        .create()

    private val preferenceManager = PreferenceManager(context)
    private val themeManager = ThemeManager(context)
    private val viewModePreference = ViewModePreference(context)

    private var googleDriveManager: GoogleDriveManager? = null

    data class UserPreferences(
        val notificationsEnabled: Boolean = true,
        val reminderDays: Set<Int> = setOf(0, 1, 3, 7),
        val notificationHour: Int = 9,
        val notificationMinute: Int = 0,
        val notificationFrequency: String = "DAILY",
        val currencyCode: String = "RUB",
        val languageCode: String = "",

        val themeMode: Int = 0,

        val gridModeEnabled: Boolean = false
    )

    data class ExportData(
        val subscriptions: List<Subscription> = emptyList(),
        val payments: List<Payment> = emptyList(),
        val categories: List<Category> = emptyList(),
        val categoryGroups: List<CategoryGroup> = emptyList(),
        val userPreferences: UserPreferences = UserPreferences(),
        val exportDate: String = ""
    )

    fun getGoogleDriveManager(): GoogleDriveManager {
        if (googleDriveManager == null) {
            googleDriveManager = GoogleDriveManager(context)
        }
        return googleDriveManager!!
    }

    suspend fun exportData(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting data export")

            val exportData = prepareExportData()
            val jsonData = gson.toJson(exportData)
            Log.d(TAG, "JSON data generated, length: ${jsonData.length}")

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                    writer.write(jsonData)
                }
            }

            Log.d(TAG, "Data exported successfully to ${uri.path}")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting data", e)
            return@withContext false
        }
    }

    suspend fun exportDataToGoogleDrive(): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting data export to Google Drive")

            val exportData = prepareExportData()
            val jsonData = gson.toJson(exportData)

            val driveManager = getGoogleDriveManager()
            return@withContext driveManager.backupToDrive(jsonData)
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting data to Google Drive", e)
            return@withContext Result.Error("Ошибка при экспорте данных в Google Drive", e)
        }
    }

    suspend fun importData(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting data import from ${uri.path}")

            val jsonData = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext false

            return@withContext importFromJson(jsonData)
        } catch (e: Exception) {
            Log.e(TAG, "Error importing data", e)
            return@withContext false
        }
    }

    suspend fun importDataFromGoogleDrive(fileId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting data import from Google Drive, file ID: $fileId")

            val driveManager = getGoogleDriveManager()
            val result = driveManager.restoreFromDrive(fileId)

            if (result.isSuccess) {
                val jsonData = result.getOrNull() ?: ""
                if (jsonData.isNotEmpty()) {
                    return@withContext importFromJson(jsonData)
                } else {
                    Log.e(TAG, "No data received from Google Drive")
                    return@withContext false
                }
            } else {
                val errorMsg = result.errorMessage() ?: "Unknown error"
                Log.e(TAG, "Failed to get backup file from Google Drive: $errorMsg")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error importing data from Google Drive", e)
            return@withContext false
        }
    }

    private suspend fun prepareExportData(): ExportData = withContext(Dispatchers.IO) {
        val subscriptions = database.subscriptionDao().getAllSubscriptionsSync()
        val payments = database.paymentDao().getAllPaymentsSync()
        val categories = database.categoryDao().getAllCategoriesSync()
        val categoryGroups = database.categoryGroupDao().getAllGroupsSync()

        Log.d(TAG, "Fetched data: ${subscriptions.size} subscriptions, ${payments.size} payments, ${categories.size} categories, ${categoryGroups.size} groups")

        val userPreferences = UserPreferences(
            notificationsEnabled = preferenceManager.areNotificationsEnabled(),
            reminderDays = preferenceManager.getReminderDays(),
            notificationHour = preferenceManager.getNotificationTime().first,
            notificationMinute = preferenceManager.getNotificationTime().second,
            notificationFrequency = preferenceManager.getNotificationFrequency().name,
            currencyCode = preferenceManager.getCurrencyCode(),
            languageCode = preferenceManager.getLanguageCode(),
            themeMode = themeManager.getThemeMode(),
            gridModeEnabled = viewModePreference.isGridModeEnabled()
        )

        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val currentDateStr = dateFormat.format(Date())

        return@withContext ExportData(
            subscriptions = subscriptions,
            payments = payments,
            categories = categories,
            categoryGroups = categoryGroups,
            userPreferences = userPreferences,
            exportDate = currentDateStr
        )
    }

    private suspend fun importFromJson(jsonData: String): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Parsing JSON data, length: ${jsonData.length}")

            val importedData = gson.fromJson(jsonData, ExportData::class.java)

            val subscriptions = importedData.subscriptions
            val payments = importedData.payments
            val categories = importedData.categories
            val categoryGroups = importedData.categoryGroups
            val userPreferences = importedData.userPreferences

            Log.d(TAG, "Parsed data: ${subscriptions.size} subscriptions, ${payments.size} payments, ${categories.size} categories, ${categoryGroups.size} groups")

            try {
                database.runInTransaction {
                    Log.d(TAG, "Clearing database before import")
                    try {
                        database.paymentDao().deleteAllSync()
                        database.subscriptionDao().deleteAllSync()
                        database.categoryDao().deleteAllSync()
                        database.categoryGroupDao().deleteAllSync()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error clearing database", e)
                    }

                    Log.d(TAG, "Importing category groups")
                    for (group in categoryGroups) {
                        try {
                            database.categoryGroupDao().insertSync(group)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error inserting category group: ${group.groupId}", e)
                        }
                    }

                    Log.d(TAG, "Importing categories")
                    for (category in categories) {
                        try {
                            database.categoryDao().insertSync(category)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error inserting category: ${category.categoryId}", e)
                        }
                    }

                    Log.d(TAG, "Importing subscriptions")
                    for (subscription in subscriptions) {
                        try {
                            database.subscriptionDao().insertSync(subscription)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error inserting subscription: ${subscription.subscriptionId}", e)
                        }
                    }

                    Log.d(TAG, "Importing payments")
                    for (payment in payments) {
                        try {
                            database.paymentDao().insertSync(payment)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error inserting payment: ${payment.paymentId}", e)
                        }
                    }
                }
                Log.d(TAG, "Database transaction completed successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error during database transaction", e)
                return@withContext false
            }

            try {
                Log.d(TAG, "Restoring user preferences")
                preferenceManager.setNotificationsEnabled(userPreferences.notificationsEnabled)
                preferenceManager.setReminderDays(userPreferences.reminderDays)
                preferenceManager.setNotificationTime(userPreferences.notificationHour, userPreferences.notificationMinute)

                try {
                    val frequency = NotificationFrequency.valueOf(userPreferences.notificationFrequency)
                    preferenceManager.setNotificationFrequency(frequency)
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting notification frequency", e)
                    preferenceManager.setNotificationFrequency(NotificationFrequency.DAILY)
                }

                preferenceManager.setCurrencyCode(userPreferences.currencyCode)
                preferenceManager.setLanguageCode(userPreferences.languageCode)

                themeManager.setThemeMode(userPreferences.themeMode)
                viewModePreference.setGridModeEnabled(userPreferences.gridModeEnabled)
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring user preferences", e)
            }

            Log.d(TAG, "Data import completed successfully")
            return@withContext true
        } catch (e: Exception) {
            Log.e(TAG, "Error during JSON import", e)
            return@withContext false
        }
    }
}