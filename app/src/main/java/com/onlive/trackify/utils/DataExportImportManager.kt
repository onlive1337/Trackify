package com.onlive.trackify.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.onlive.trackify.data.database.AppDatabase
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.model.Currency
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.data.model.SubscriptionSortOption
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.Instant
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class DataExportImportManager(private val context: Context) {
    private val tag = "DataExportImportManager"
    private val database = AppDatabase.getDatabase(context)
    private val preferenceManager = PreferenceManager(context)

    @Serializable
    data class UserPreferencesBackupDto(
        val notificationsEnabled: Boolean = false,
        val reminderDays: Set<Int> = setOf(0, 1, 3),
        val notificationHour: Int = 9,
        val notificationMinute: Int = 0,
        val currencyCode: String = "USD",
        val languageCode: String = "",
        val themeMode: Int = ThemeManager.MODE_DARK,
        val dynamicColorEnabled: Boolean = true,
        val subscriptionSort: String? = null,
    )

    @Serializable
    data class CategoryBackupDto(
        val categoryId: Long,
        val name: String,
        val description: String? = null,
        val colorCode: String,
    )

    @Serializable
    data class SubscriptionBackupDto(
        val subscriptionId: Long,
        val name: String,
        val description: String? = null,
        val price: Double,
        val billingFrequency: String,
        val startDate: String,
        val endDate: String? = null,
        val categoryId: Long? = null,
        val image: String? = null,
    )

    @Serializable
    data class PaymentBackupDto(
        val paymentId: Long,
        val subscriptionId: Long,
        val amount: Double,
        val date: String,
        val notes: String? = null,
    )

    @Serializable
    data class BackupPayload(
        val backupVersion: Int = CURRENT_BACKUP_VERSION,
        val exportDate: String = "",
        val categories: List<CategoryBackupDto>? = null,
        val subscriptions: List<SubscriptionBackupDto>? = null,
        val payments: List<PaymentBackupDto>? = null,
        val userPreferences: UserPreferencesBackupDto? = null,
    )

    data class ValidatedBackup(
        val categories: List<Category>,
        val subscriptions: List<Subscription>,
        val payments: List<Payment>,
        val userPreferences: UserPreferencesBackupDto,
    )

    suspend fun exportData(uri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val subscriptions = database.subscriptionDao().getAllSnapshot()
            val payments = database.paymentDao().getAllSnapshot()
            val categories = database.categoryDao().getAllSnapshot()

            val userPreferences = UserPreferencesBackupDto(
                notificationsEnabled = preferenceManager.areNotificationsEnabled(),
                reminderDays = preferenceManager.getReminderDays(),
                notificationHour = preferenceManager.getNotificationTime().first,
                notificationMinute = preferenceManager.getNotificationTime().second,
                currencyCode = preferenceManager.getCurrencyCode(),
                languageCode = preferenceManager.getLanguageCode(),
                themeMode = preferenceManager.getThemeMode(),
                dynamicColorEnabled = preferenceManager.isDynamicColorEnabled(),
                subscriptionSort = preferenceManager.getSubscriptionSortOption().name,
            )

            val jsonData = encodeBackup(
                categories = categories,
                subscriptions = subscriptions,
                payments = payments,
                userPreferences = userPreferences,
                exportedAt = Instant.now()
            )

            val outputStream = runCatching {
                context.contentResolver.openOutputStream(uri, "wt")
            }.getOrNull() ?: context.contentResolver.openOutputStream(uri)

            outputStream?.use { stream ->
                BufferedWriter(OutputStreamWriter(stream, Charsets.UTF_8)).use { writer ->
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
            val jsonData = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                readBoundedUtf8Text(inputStream, MAX_BACKUP_CHARS)
            } ?: return@withContext false

            val validated = decodeAndValidateBackup(jsonData) ?: run {
                Log.e(tag, "Invalid backup payload")
                return@withContext false
            }

            try {
                database.runInTransaction {
                    database.paymentDao().deleteAll()
                    database.subscriptionDao().deleteAll()
                    database.categoryDao().deleteAll()

                    database.categoryDao().insertAll(validated.categories)
                    database.subscriptionDao().insertAll(validated.subscriptions)
                    database.paymentDao().insertAll(validated.payments)
                }
            } catch (e: Exception) {
                Log.e(tag, "Error during database transaction, rolling back", e)
                return@withContext false
            }

            try {
                val prefs = validated.userPreferences
                val hasNotificationPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                val effectiveNotificationsEnabled = prefs.notificationsEnabled && hasNotificationPermission

                preferenceManager.clearAllNotificationRecords()
                NotificationHelper(context).cancelAllNotifications()

                preferenceManager.setNotificationsEnabled(effectiveNotificationsEnabled)
                preferenceManager.setReminderDays(prefs.reminderDays)
                preferenceManager.setNotificationTime(
                    prefs.notificationHour.coerceIn(0, 23),
                    prefs.notificationMinute.coerceIn(0, 59)
                )
                preferenceManager.setCurrencyCode(prefs.currencyCode)
                if (prefs.languageCode.isNotBlank()) {
                    preferenceManager.setLanguageCode(prefs.languageCode)
                }
                prefs.subscriptionSort?.let {
                    preferenceManager.setSubscriptionSortOption(SubscriptionSortOption.fromName(it))
                }

                preferenceManager.setThemeMode(prefs.themeMode)
                preferenceManager.setDynamicColorEnabled(prefs.dynamicColorEnabled)

                val scheduler = NotificationScheduler(context)
                if (effectiveNotificationsEnabled) {
                    scheduler.scheduleNotifications()
                    scheduler.triggerImmediateCheck()
                } else {
                    scheduler.cancelNotifications()
                }
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

    companion object {
        const val CURRENT_BACKUP_VERSION = 1
        private const val MAX_BACKUP_CHARS = 5_000_000
        private const val MIN_BACKUP_YEAR = 1970
        private const val MAX_BACKUP_YEAR = 2100

        private val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            encodeDefaults = true
            explicitNulls = false
        }

        internal fun readBoundedUtf8Text(inputStream: InputStream, maxChars: Int = MAX_BACKUP_CHARS): String? {
            BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                val builder = StringBuilder()
                val buffer = CharArray(8192)
                var totalRead = 0
                while (true) {
                    val read = reader.read(buffer)
                    if (read == -1) break
                    totalRead += read
                    if (totalRead > maxChars) return null
                    builder.appendRange(buffer, 0, read)
                }
                return builder.toString()
            }
        }

        fun encodeBackup(
            categories: List<Category>,
            subscriptions: List<Subscription>,
            payments: List<Payment>,
            userPreferences: UserPreferencesBackupDto,
            exportedAt: Instant = Instant.now(),
        ): String {
            val payload = BackupPayload(
                backupVersion = CURRENT_BACKUP_VERSION,
                exportDate = exportedAt.toString(),
                categories = categories.map { category ->
                    CategoryBackupDto(
                        categoryId = category.categoryId,
                        name = category.name,
                        description = category.description,
                        colorCode = category.colorCode,
                    )
                },
                subscriptions = subscriptions.map { subscription ->
                    SubscriptionBackupDto(
                        subscriptionId = subscription.subscriptionId,
                        name = subscription.name,
                        description = subscription.description,
                        price = subscription.price,
                        billingFrequency = subscription.billingFrequency.name,
                        startDate = subscription.startDate.toString(),
                        endDate = subscription.endDate?.toString(),
                        categoryId = subscription.categoryId,
                        image = subscription.image,
                    )
                },
                payments = payments.map { payment ->
                    PaymentBackupDto(
                        paymentId = payment.paymentId,
                        subscriptionId = payment.subscriptionId,
                        amount = payment.amount,
                        date = payment.date.toString(),
                        notes = payment.notes,
                    )
                },
                userPreferences = userPreferences,
            )
            return json.encodeToString(BackupPayload.serializer(), payload)
        }

        fun decodeAndValidateBackup(jsonData: String): ValidatedBackup? {
            val payload = runCatching {
                json.decodeFromString(BackupPayload.serializer(), jsonData)
            }.getOrNull() ?: return null

            if (payload.backupVersion !in 1..CURRENT_BACKUP_VERSION) return null

            val categoryDtos = payload.categories ?: return null
            val subscriptionDtos = payload.subscriptions ?: return null
            val paymentDtos = payload.payments ?: return null

            val categories = categoryDtos.map { dto ->
                if (dto.categoryId <= 0L) return null
                if (dto.name.isBlank() || dto.colorCode.isBlank()) return null
                Category(
                    categoryId = dto.categoryId,
                    name = dto.name.trim(),
                    description = dto.description,
                    colorCode = dto.colorCode.trim(),
                )
            }
            val categoryIds = categories.map { it.categoryId }.toSet()
            if (categoryIds.size != categories.size) return null

            val subscriptions = subscriptionDtos.map { dto ->
                if (dto.subscriptionId <= 0L) return null
                if (dto.name.isBlank() || !CurrencyFormatter.isValidPositiveAmount(dto.price)) return null
                val frequency = runCatching {
                    BillingFrequency.valueOf(dto.billingFrequency)
                }.getOrNull() ?: return null
                val startDate = parseBackupDate(dto.startDate) ?: return null
                val endDate = dto.endDate?.let { parseBackupDate(it) ?: return null }
                if (endDate != null && endDate.isBefore(startDate)) return null
                if (dto.categoryId != null && dto.categoryId !in categoryIds) return null

                Subscription(
                    subscriptionId = dto.subscriptionId,
                    name = dto.name.trim(),
                    description = dto.description,
                    price = dto.price,
                    billingFrequency = frequency,
                    startDate = startDate,
                    endDate = endDate,
                    categoryId = dto.categoryId,
                    image = dto.image,
                )
            }
            val subscriptionIds = subscriptions.map { it.subscriptionId }.toSet()
            if (subscriptionIds.size != subscriptions.size) return null

            val payments = paymentDtos.map { dto ->
                if (dto.paymentId <= 0L) return null
                if (!CurrencyFormatter.isValidPositiveAmount(dto.amount)) return null
                if (dto.subscriptionId !in subscriptionIds) return null
                val paymentDate = parseBackupDate(dto.date) ?: return null

                Payment(
                    paymentId = dto.paymentId,
                    subscriptionId = dto.subscriptionId,
                    amount = dto.amount,
                    date = paymentDate,
                    notes = dto.notes,
                )
            }
            val paymentIds = payments.map { it.paymentId }.toSet()
            if (paymentIds.size != payments.size) return null

            val rawPrefs = payload.userPreferences ?: UserPreferencesBackupDto()
            val sanitizedPrefs = rawPrefs.copy(
                reminderDays = rawPrefs.reminderDays.filter { it in PreferenceManager.VALID_REMINDER_DAYS }.toSet(),
                notificationHour = rawPrefs.notificationHour.coerceIn(0, 23),
                notificationMinute = rawPrefs.notificationMinute.coerceIn(0, 59),
                currencyCode = if (Currency.isSupportedCurrencyCode(rawPrefs.currencyCode)) {
                    rawPrefs.currencyCode
                } else {
                    "USD"
                },
                languageCode = LocaleHelper.normalizeLanguageCode(rawPrefs.languageCode) ?: "",
                themeMode = if (
                    rawPrefs.themeMode == ThemeManager.MODE_LIGHT ||
                    rawPrefs.themeMode == ThemeManager.MODE_DARK
                ) {
                    rawPrefs.themeMode
                } else {
                    ThemeManager.MODE_DARK
                },
            )

            return ValidatedBackup(
                categories = categories,
                subscriptions = subscriptions,
                payments = payments,
                userPreferences = sanitizedPrefs,
            )
        }

        internal fun parseBackupDate(raw: String): LocalDate? {
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return null
            val parsed = runCatching { LocalDate.parse(trimmed) }.getOrNull()
                ?: runCatching { LocalDate.parse(trimmed.take(10)) }.getOrNull()
                ?: return null
            return parsed.takeIf { it.year in MIN_BACKUP_YEAR..MAX_BACKUP_YEAR }
        }
    }
}
