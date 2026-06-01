package com.onlive.trackify.utils

import android.content.Context
import android.content.SharedPreferences
import com.onlive.trackify.data.model.Currency
import androidx.core.content.edit

class PreferenceManager(context: Context) {
    companion object {
        private const val PREFS_NAME = "trackify_preferences"
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val KEY_REMINDER_DAYS = "reminder_days"
        const val KEY_NOTIFICATION_TIME_HOUR = "notification_time_hour"
        const val KEY_NOTIFICATION_TIME_MINUTE = "notification_time_minute"
        const val KEY_CURRENCY_CODE = "currency_code"
        const val KEY_LANGUAGE_CODE = "language_code"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit { putBoolean(KEY_ONBOARDING_COMPLETED, completed) }
    }

    fun areNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, false)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled) }
    }

    fun getReminderDays(): Set<Int> {
        val defaultSet = setOf(0, 1, 3)
        val stringSet = prefs.getStringSet(KEY_REMINDER_DAYS, defaultSet.map { it.toString() }.toSet()) ?: emptySet()
        return stringSet.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun setReminderDays(days: Set<Int>) {
        prefs.edit { putStringSet(KEY_REMINDER_DAYS, days.map { it.toString() }.toSet()) }
    }

    fun getNotificationTime(): Pair<Int, Int> {
        val hour = prefs.getInt(KEY_NOTIFICATION_TIME_HOUR, 9)
        val minute = prefs.getInt(KEY_NOTIFICATION_TIME_MINUTE, 0)
        return Pair(hour, minute)
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        prefs.edit {
            putInt(KEY_NOTIFICATION_TIME_HOUR, hour)
                .putInt(KEY_NOTIFICATION_TIME_MINUTE, minute)
        }
    }

    fun getCurrencyCode(): String {
        return prefs.getString(KEY_CURRENCY_CODE, "USD") ?: "USD"
    }

    fun setCurrencyCode(currencyCode: String) {
        prefs.edit { putString(KEY_CURRENCY_CODE, currencyCode) }
    }

    fun getCurrentCurrency(): Currency {
        return Currency.getCurrencyByCode(getCurrencyCode())
    }

    fun getLanguageCode(): String {
        return prefs.getString(KEY_LANGUAGE_CODE, "en") ?: "en"
    }

    fun setLanguageCode(languageCode: String) {
        prefs.edit { putString(KEY_LANGUAGE_CODE, languageCode) }
    }

    fun wasNotificationSentForDate(subscriptionId: Long, type: String, dateKey: String): Boolean {
        val key = getNotificationKey(subscriptionId, type, dateKey)
        return prefs.contains(key)
    }

    fun markNotificationSentForDate(subscriptionId: Long, type: String, dateKey: String) {
        val key = getNotificationKey(subscriptionId, type, dateKey)
        prefs.edit { putString(key, dateKey) }
    }

    private fun getNotificationKey(subscriptionId: Long, type: String, dateKey: String): String {
        return "notified_${type}_${subscriptionId}_$dateKey"
    }

    fun formatDateKey(date: java.util.Date): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(date)
    }

    fun cleanupOldNotificationRecords() {
        val today = formatDateKey(java.util.Date())
        prefs.all.keys.toList().forEach { key ->
            when {
                key.startsWith("notified_") -> {
                    val savedDate = prefs.getString(key, null)
                    if (savedDate != null && savedDate < today) {
                        prefs.edit { remove(key) }
                    }
                }
                // Legacy keys from the old daysUntil-based dedup: drop them all.
                key.startsWith("notification_sent_") -> {
                    prefs.edit { remove(key) }
                }
            }
        }
    }
}