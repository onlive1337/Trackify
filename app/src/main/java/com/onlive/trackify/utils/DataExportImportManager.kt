package com.onlive.trackify.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.onlive.trackify.data.database.AppDatabase
import com.onlive.trackify.data.model.Category
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
    private val tag = "DataExportImportManager"
    private val database = AppDatabase.getDatabase(context)
    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .serializeNulls()
        .create()

    private val preferenceManager = PreferenceManager(context)
    private val themeManager = ThemeManager(context)
    private val viewModePreference = ViewModePreference(context)

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
        val userPreferences: UserPreferences = UserPreferences(),
        val exportDate: String = ""
    )

    suspend fun exportData(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(tag, "Starting data export using Scoped Storage")

            val subscriptions = database.subscriptionDao().getAllSubscriptionsSync()
            val payments = database.paymentDao().getAllPaymentsSync()
            val categories = database.categoryDao().getAllCategoriesSync()

            Log.d(tag, "Fetched data: ${subscriptions.size} subscriptions, ${payments.size} payments, ${categories.size} categories")

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

            val exportData = ExportData(
                subscriptions = subscriptions,
                payments = payments,
                categories = categories,
                userPreferences = userPreferences,
                exportDate = currentDateStr
            )

            val jsonData = gson.toJson(exportData)
            Log.d(tag, "JSON data generated, length: ${jsonData.length}")

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                BufferedWriter(OutputStreamWriter(outputStream, "UTF-8")).use { writer ->
                    writer.write(jsonData)
                    writer.flush()
                }
            } ?: return@withContext false

            Log.d(tag, "Data exported successfully to ${uri.path}")
            return@withContext true
        } catch (e: Exception) {
            Log.e(tag, "Error exporting data", e)
            return@withContext false
        }
    }

    suspend fun importData(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(tag, "Starting data import from ${uri.path} using Scoped Storage")

            val jsonData = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext false

            Log.d(tag, "Read JSON data, length: ${jsonData.length}")

            val importedData = try {
                gson.fromJson(jsonData, ExportData::class.java)
            } catch (e: Exception) {
                Log.e(tag, "Error parsing JSON data", e)
                return@withContext false
            }

            val subscriptions = importedData.subscriptions
            val payments = importedData.payments
            val categories = importedData.categories
            val userPreferences = importedData.userPreferences

            Log.d(tag, "Parsed data: ${subscriptions.size} subscriptions, ${payments.size} payments, ${categories.size} categories")

            try {
                database.runInTransaction {
                    Log.d(tag, "Clearing database before import")
                    try {
                        database.paymentDao().deleteAllSync()
                        database.subscriptionDao().deleteAllSync()
                        database.categoryDao().deleteAllSync()
                    } catch (e: Exception) {
                        Log.e(tag, "Error clearing database", e)
                    }

                    Log.d(tag, "Importing categories")
                    for (category in categories) {
                        try {
                            database.categoryDao().insertSync(category)
                        } catch (e: Exception) {
                            Log.e(tag, "Error inserting category: ${category.categoryId}", e)
                        }
                    }

                    Log.d(tag, "Importing subscriptions")
                    for (subscription in subscriptions) {
                        try {
                            database.subscriptionDao().insertSync(subscription)
                        } catch (e: Exception) {
                            Log.e(tag, "Error inserting subscription: ${subscription.subscriptionId}", e)
                        }
                    }

                    Log.d(tag, "Importing payments")
                    for (payment in payments) {
                        try {
                            database.paymentDao().insertSync(payment)
                        } catch (e: Exception) {
                            Log.e(tag, "Error inserting payment: ${payment.paymentId}", e)
                        }
                    }
                }
                Log.d(tag, "Database transaction completed successfully")
            } catch (e: Exception) {
                Log.e(tag, "Error during database transaction", e)
                return@withContext false
            }

            try {
                Log.d(tag, "Restoring user preferences")
                preferenceManager.setNotificationsEnabled(userPreferences.notificationsEnabled)
                preferenceManager.setReminderDays(userPreferences.reminderDays)
                preferenceManager.setNotificationTime(userPreferences.notificationHour, userPreferences.notificationMinute)

                try {
                    val frequency = NotificationFrequency.valueOf(userPreferences.notificationFrequency)
                    preferenceManager.setNotificationFrequency(frequency)
                } catch (e: Exception) {
                    Log.e(tag, "Error setting notification frequency", e)
                    preferenceManager.setNotificationFrequency(NotificationFrequency.DAILY)
                }

                preferenceManager.setCurrencyCode(userPreferences.currencyCode)
                preferenceManager.setLanguageCode(userPreferences.languageCode)

                themeManager.setThemeMode(userPreferences.themeMode)
                viewModePreference.setGridModeEnabled(userPreferences.gridModeEnabled)
            } catch (e: Exception) {
                Log.e(tag, "Error restoring user preferences", e)
            }

            Log.d(tag, "Data import completed successfully")
            return@withContext true
        } catch (e: Exception) {
            Log.e(tag, "Error importing data", e)
            return@withContext false
        }
    }
}