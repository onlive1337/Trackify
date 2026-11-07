package com.onlive.trackify.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.onlive.trackify.data.model.Currency
import com.onlive.trackify.data.model.SubscriptionSortOption
import java.time.LocalDate

class PreferenceManager(context: Context) {
    companion object {
        private const val PREFS_NAME = "trackify_preferences"
        private const val LEGACY_THEME_PREFS_NAME = "theme_prefs"
        const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val KEY_REMINDER_DAYS = "reminder_days"
        const val KEY_NOTIFICATION_TIME_HOUR = "notification_time_hour"
        const val KEY_NOTIFICATION_TIME_MINUTE = "notification_time_minute"
        const val KEY_CURRENCY_CODE = "currency_code"
        const val KEY_LANGUAGE_CODE = "language_code"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        const val KEY_SUBSCRIPTION_SORT = "subscription_sort"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_DYNAMIC_COLOR = "dynamic_color"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        migrateLegacyThemePrefs(context)
    }

    private fun migrateLegacyThemePrefs(context: Context) {
        if (prefs.contains(KEY_THEME_MODE) || prefs.contains(KEY_DYNAMIC_COLOR)) return
        val legacyPrefs = context.getSharedPreferences(LEGACY_THEME_PREFS_NAME, Context.MODE_PRIVATE)
        if (legacyPrefs.all.isNotEmpty()) {
            prefs.edit {
                if (legacyPrefs.contains(KEY_THEME_MODE)) {
                    putInt(KEY_THEME_MODE, legacyPrefs.getInt(KEY_THEME_MODE, ThemeManager.MODE_DARK))
                }
                if (legacyPrefs.contains(KEY_DYNAMIC_COLOR)) {
                    putBoolean(KEY_DYNAMIC_COLOR, legacyPrefs.getBoolean(KEY_DYNAMIC_COLOR, true))
                }
            }
            context.deleteSharedPreferences(LEGACY_THEME_PREFS_NAME)
        }
    }

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
        val saved = prefs.getString(KEY_LANGUAGE_CODE, null)
        return if (!saved.isNullOrBlank()) {
            saved
        } else {
            LocaleHelper.getSupportedSystemLanguage()
        }
    }

    fun setLanguageCode(languageCode: String) {
        prefs.edit { putString(KEY_LANGUAGE_CODE, languageCode) }
    }

    fun getSubscriptionSortOption(): SubscriptionSortOption {
        return SubscriptionSortOption.fromName(prefs.getString(KEY_SUBSCRIPTION_SORT, null))
    }

    fun setSubscriptionSortOption(option: SubscriptionSortOption) {
        prefs.edit { putString(KEY_SUBSCRIPTION_SORT, option.name) }
    }

    fun getThemeMode(): Int {
        return prefs.getInt(KEY_THEME_MODE, ThemeManager.MODE_DARK)
    }

    fun setThemeMode(mode: Int) {
        prefs.edit { putInt(KEY_THEME_MODE, mode) }
    }

    fun isDynamicColorEnabled(): Boolean {
        return prefs.getBoolean(KEY_DYNAMIC_COLOR, true)
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_DYNAMIC_COLOR, enabled) }
    }

    fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    fun wasNotificationSentForDate(
        subscriptionId: Long,
        type: String,
        dateKey: String,
        thresholdDay: Int
    ): Boolean {
        val key = getNotificationKey(subscriptionId, type, dateKey, thresholdDay)
        return prefs.contains(key)
    }

    fun markNotificationSentForDate(
        subscriptionId: Long,
        type: String,
        dateKey: String,
        thresholdDay: Int
    ) {
        val key = getNotificationKey(subscriptionId, type, dateKey, thresholdDay)
        prefs.edit { putString(key, dateKey) }
    }

    private fun getNotificationKey(
        subscriptionId: Long,
        type: String,
        dateKey: String,
        thresholdDay: Int
    ): String {
        return "notified_${type}_${subscriptionId}_${dateKey}_$thresholdDay"
    }

    fun formatDateKey(date: LocalDate): String {
        return date.toString()
    }

    fun cleanupOldNotificationRecords() {
        val today = formatDateKey(LocalDate.now())
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
