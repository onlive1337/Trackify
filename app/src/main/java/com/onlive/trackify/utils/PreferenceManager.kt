package com.onlive.trackify.utils

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    companion object {
        private const val PREFS_NAME = "trackify_preferences"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_REMINDER_DAYS = "reminder_days"
        private const val KEY_NOTIFICATION_TIME = "notification_time_hour"
        private const val KEY_NOTIFICATION_MINUTE = "notification_time_minute"
        private const val KEY_NOTIFICATION_FREQUENCY = "notification_frequency"
        private const val KEY_CURRENCY_CODE = "currency_code"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun areNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
    }

    fun getReminderDays(): Set<Int> {
        val defaultSet = setOf(0, 1, 3, 7)
        val stringSet = prefs.getStringSet(KEY_REMINDER_DAYS, defaultSet.map { it.toString() }.toSet()) ?: emptySet()
        return stringSet.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun setReminderDays(days: Set<Int>) {
        prefs.edit().putStringSet(KEY_REMINDER_DAYS, days.map { it.toString() }.toSet()).apply()
    }

    fun getNotificationFrequency(): NotificationFrequency {
        val value = prefs.getString(KEY_NOTIFICATION_FREQUENCY, NotificationFrequency.DAILY.name)
        return try {
            NotificationFrequency.valueOf(value ?: NotificationFrequency.DAILY.name)
        } catch (e: IllegalArgumentException) {
            NotificationFrequency.DAILY
        }
    }

    fun setNotificationFrequency(frequency: NotificationFrequency) {
        prefs.edit().putString(KEY_NOTIFICATION_FREQUENCY, frequency.name).apply()
    }

    fun getNotificationTime(): Pair<Int, Int> {
        val hour = prefs.getInt(KEY_NOTIFICATION_TIME, 9)
        val minute = prefs.getInt(KEY_NOTIFICATION_MINUTE, 0)
        return Pair(hour, minute)
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_NOTIFICATION_TIME, hour)
            .putInt(KEY_NOTIFICATION_MINUTE, minute)
            .apply()
    }

    fun getCurrencyCode(): String {
        return prefs.getString(KEY_CURRENCY_CODE, "RUB") ?: "RUB"
    }

    fun setCurrencyCode(currencyCode: String) {
        prefs.edit().putString(KEY_CURRENCY_CODE, currencyCode).apply()
    }

    fun getCurrentCurrency(): Currency {
        return Currency.getCurrencyByCode(getCurrencyCode())
    }
}

enum class NotificationFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    CUSTOM
}