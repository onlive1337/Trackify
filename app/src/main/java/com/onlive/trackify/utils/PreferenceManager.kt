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
        prefs.edit { putBoolean(KEY_NOTIFICATIONS_ENABLED, enabled) }
        notifyListeners(KEY_NOTIFICATIONS_ENABLED, enabled)
    }

    fun getReminderDays(): Set<Int> {
        val defaultSet = setOf(0, 1, 3)
        val stringSet = prefs.getStringSet(KEY_REMINDER_DAYS, defaultSet.map { it.toString() }.toSet()) ?: emptySet()
        return stringSet.mapNotNull { it.toIntOrNull() }.toSet()
    }

    fun setReminderDays(days: Set<Int>) {
        prefs.edit { putStringSet(KEY_REMINDER_DAYS, days.map { it.toString() }.toSet()) }
        notifyListeners(KEY_REMINDER_DAYS, days)
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
        notifyListeners(KEY_NOTIFICATION_TIME_HOUR, Pair(hour, minute))
    }

    fun getCurrencyCode(): String {
        return prefs.getString(KEY_CURRENCY_CODE, "USD") ?: "USD"
    }

    fun setCurrencyCode(currencyCode: String) {
        prefs.edit { putString(KEY_CURRENCY_CODE, currencyCode) }
        notifyListeners(KEY_CURRENCY_CODE, currencyCode)
    }

    fun getCurrentCurrency(): Currency {
        return Currency.getCurrencyByCode(getCurrencyCode())
    }

    fun getLanguageCode(): String {
        return prefs.getString(KEY_LANGUAGE_CODE, "") ?: ""
    }

    fun setLanguageCode(languageCode: String) {
        prefs.edit { putString(KEY_LANGUAGE_CODE, languageCode) }
        notifyListeners(KEY_LANGUAGE_CODE, languageCode)
    }
}