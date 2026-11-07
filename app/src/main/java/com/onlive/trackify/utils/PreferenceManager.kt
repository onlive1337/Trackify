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
        const val KEY_SYSTEM_LOCALE_OVERRIDDEN = "system_locale_overridden"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        const val KEY_SUBSCRIPTION_SORT = "subscription_sort"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_DYNAMIC_COLOR = "dynamic_color"
        val VALID_REMINDER_DAYS: Set<Int> = setOf(0, 1, 3, 7)
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
        return stringSet.mapNotNull { it.toIntOrNull() }.filter { it in VALID_REMINDER_DAYS }.toSet()
    }

    fun setReminderDays(days: Set<Int>) {
        val sanitized = days.filter { it in VALID_REMINDER_DAYS }.map { it.toString() }.toSet()
        prefs.edit { putStringSet(KEY_REMINDER_DAYS, sanitized) }
    }

    fun getNotificationTime(): Pair<Int, Int> {
        val hour = prefs.getInt(KEY_NOTIFICATION_TIME_HOUR, 9).coerceIn(0, 23)
        val minute = prefs.getInt(KEY_NOTIFICATION_TIME_MINUTE, 0).coerceIn(0, 59)
        return Pair(hour, minute)
    }

    fun setNotificationTime(hour: Int, minute: Int) {
        prefs.edit {
            putInt(KEY_NOTIFICATION_TIME_HOUR, hour.coerceIn(0, 23))
                .putInt(KEY_NOTIFICATION_TIME_MINUTE, minute.coerceIn(0, 59))
        }
    }

    fun getCurrencyCode(): String {
        val code = prefs.getString(KEY_CURRENCY_CODE, "USD") ?: "USD"
        return if (Currency.isSupportedCurrencyCode(code)) code else "USD"
    }

    fun setCurrencyCode(currencyCode: String) {
        val validCode = if (Currency.isSupportedCurrencyCode(currencyCode)) currencyCode else "USD"
        prefs.edit { putString(KEY_CURRENCY_CODE, validCode) }
    }

    fun getCurrentCurrency(): Currency {
        return Currency.getCurrencyByCode(getCurrencyCode())
    }

    fun getSavedLanguageCodeOrNull(): String? {
        return LocaleHelper.normalizeLanguageCode(prefs.getString(KEY_LANGUAGE_CODE, null))
    }

    fun getLanguageCode(): String {
        return getSavedLanguageCodeOrNull() ?: LocaleHelper.getSupportedSystemLanguage()
    }

    fun setLanguageCode(languageCode: String) {
        val normalized = LocaleHelper.normalizeLanguageCode(languageCode) ?: return
        prefs.edit { putString(KEY_LANGUAGE_CODE, normalized) }
    }

    fun clearSavedLanguageCode() {
        prefs.edit { remove(KEY_LANGUAGE_CODE) }
    }

    fun isSystemLocaleOverridden(): Boolean {
        return prefs.getBoolean(KEY_SYSTEM_LOCALE_OVERRIDDEN, false)
    }

    fun setSystemLocaleOverridden(overridden: Boolean) {
        prefs.edit { putBoolean(KEY_SYSTEM_LOCALE_OVERRIDDEN, overridden) }
    }

    fun getSubscriptionSortOption(): SubscriptionSortOption {
        return SubscriptionSortOption.fromName(prefs.getString(KEY_SUBSCRIPTION_SORT, null))
    }

    fun setSubscriptionSortOption(option: SubscriptionSortOption) {
        prefs.edit { putString(KEY_SUBSCRIPTION_SORT, option.name) }
    }

    fun getThemeMode(): Int {
        val saved = prefs.getInt(KEY_THEME_MODE, ThemeManager.MODE_DARK)
        return if (saved == ThemeManager.MODE_LIGHT || saved == ThemeManager.MODE_DARK) {
            saved
        } else {
            ThemeManager.MODE_DARK
        }
    }

    fun setThemeMode(mode: Int) {
        val validMode = if (mode == ThemeManager.MODE_LIGHT || mode == ThemeManager.MODE_DARK) {
            mode
        } else {
            ThemeManager.MODE_DARK
        }
        prefs.edit { putInt(KEY_THEME_MODE, validMode) }
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

    fun clearNotificationRecordsForSubscription(subscriptionId: Long) {
        val token = "_${subscriptionId}_"
        val matchingKeys = prefs.all.keys.filter { key ->
            key.startsWith("notified_") && key.contains(token)
        }
        if (matchingKeys.isNotEmpty()) {
            prefs.edit {
                matchingKeys.forEach { remove(it) }
            }
        }
    }

    fun clearAllNotificationRecords() {
        val matchingKeys = prefs.all.keys.filter { key ->
            key.startsWith("notified_") || key.startsWith("notification_sent_")
        }
        if (matchingKeys.isNotEmpty()) {
            prefs.edit {
                matchingKeys.forEach { remove(it) }
            }
        }
    }

    fun cleanupOldNotificationRecords() {
        val today = formatDateKey(LocalDate.now())
        val keysToRemove = prefs.all.keys.filter { key ->
            when {
                key.startsWith("notified_") -> {
                    val savedDate = prefs.getString(key, null)
                    savedDate != null && savedDate < today
                }
                // Legacy keys from the old daysUntil-based dedup: drop them all.
                key.startsWith("notification_sent_") -> true
                else -> false
            }
        }
        if (keysToRemove.isNotEmpty()) {
            prefs.edit {
                keysToRemove.forEach { remove(it) }
            }
        }
    }
}
