package com.onlive.trackify.utils

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
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

    private val database = AppDatabase.getDatabase(context)
    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()

    data class ExportData(
        val subscriptions: List<Subscription>,
        val payments: List<Payment>,
        val categories: List<Category>,
        val exportDate: String,
        val version: Int = 1
    )

    suspend fun exportData(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val subscriptions = database.subscriptionDao().getAllSubscriptionsSync()
            val payments = database.paymentDao().getAllPaymentsSync()
            val categories = database.categoryDao().getAllCategoriesSync()

            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            val currentDateStr = dateFormat.format(Date())

            val exportData = ExportData(
                subscriptions = subscriptions,
                payments = payments,
                categories = categories,
                exportDate = currentDateStr
            )

            val jsonData = gson.toJson(exportData)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                    writer.write(jsonData as String)
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun importData(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val jsonData = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            } ?: return@withContext false

            val typeToken = object : TypeToken<ExportData>() {}.type
            val importedData = gson.fromJson<ExportData>(jsonData, typeToken)

            if (importedData.version > 1) {
                return@withContext false
            }

            val categories = importedData.categories
            val subscriptions = importedData.subscriptions
            val payments = importedData.payments

            database.runInTransaction {
                database.categoryDao().deleteAllSync()
                database.subscriptionDao().deleteAllSync()
                database.paymentDao().deleteAllSync()

                for (category in categories) {
                    database.categoryDao().insert(category)
                }

                for (subscription in subscriptions) {
                    database.subscriptionDao().insert(subscription)
                }

                for (payment in payments) {
                    database.paymentDao().insert(payment)
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}