package com.onlive.trackify.utils

import android.content.Context
import android.content.SharedPreferences
import com.onlive.trackify.data.model.Currency

class PreferenceManager(context: Context) {
    companion object {
        private const val PREFS_NAME = "trackify_preferences"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_REMINDER_DAYS = "reminder_days"
        private const val KEY_NOTIFICATION_TIME = "notification_time_hour"
        private const val KEY_NOTIFICATION_MINUTE = "notification_time_minute"
        private const val KEY_NOTIFICATION_FREQUENCY = "notification_frequency"
        private const val KEY_CURRENCY_CODE = "currency_code"
        private const val KEY_LANGUAGE_CODE = "language_code"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val listeners = mutableListOf<OnPreferenceChangedListener>()

    interface OnPreferenceChangedListener {
        fun onPreferenceChanged(key: String, value: Any?)
    }

    fun addOnPreferenceChangedListener(listener: OnPreferenceChangedListener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    fun removeOnPreferenceChangedListener(listener: OnPreferenceChangedListener) {
        listeners.remove(listener)
    }

    private fun notifyListeners(key: String, value: Any?) {
        listeners.forEach { it.onPreferenceChanged(key, value) }
    }

    fun areNotificationsEnabled(): Boolean {
        return prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled).apply()
        notifyListeners(KEY_NOTIFICATIONS_ENABLED, enabled)
    }

    fun getReminderDays(): Set<Int> {
        val defaultSet = setOf(0, 1, 3, 7)
        val stringSet = prefs.getStringSet(KEY_REMINDER_DAYS, defaultSet.map { it.toString() }.toSet()) ?: emptySet()
        return stringSet.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun setReminderDays(days: Set<Int>) {
        prefs.edit().putStringSet(KEY_REMINDER_DAYS, days.map { it.toString() }.toSet()).apply()
        notifyListeners(KEY_REMINDER_DAYS, days)
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
        notifyListeners(KEY_NOTIFICATION_FREQUENCY, frequency)
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
        notifyListeners(KEY_NOTIFICATION_TIME, Pair(hour, minute))
    }

    fun getCurrencyCode(): String {
        return prefs.getString(KEY_CURRENCY_CODE, "RUB") ?: "RUB"
    }

    fun setCurrencyCode(currencyCode: String) {
        prefs.edit().putString(KEY_CURRENCY_CODE, currencyCode).apply()
        notifyListeners(KEY_CURRENCY_CODE, currencyCode)
    }

    fun getCurrentCurrency(): Currency {
        return Currency.getCurrencyByCode(getCurrencyCode())
    }

    fun getLanguageCode(): String {
        return prefs.getString(KEY_LANGUAGE_CODE, "") ?: ""
    }

    fun setLanguageCode(languageCode: String) {
        prefs.edit().putString(KEY_LANGUAGE_CODE, languageCode).apply()
        notifyListeners(KEY_LANGUAGE_CODE, languageCode)
    }
}

enum class NotificationFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    CUSTOM
}