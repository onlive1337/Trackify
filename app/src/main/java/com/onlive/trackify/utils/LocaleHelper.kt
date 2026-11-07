package com.onlive.trackify.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {

    val SUPPORTED_LANGUAGE_CODES: Set<String> = setOf("en", "ru")

    fun normalizeLanguageCode(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val primaryTag = raw.trim()
            .split('-', '_')
            .firstOrNull()
            ?.lowercase(Locale.ROOT)
            ?.takeIf { it.isNotEmpty() }
            ?: return null
        return primaryTag.takeIf { it in SUPPORTED_LANGUAGE_CODES }
    }

    fun toSafeLocale(languageCode: String?): Locale {
        val resolvedCode = normalizeLanguageCode(languageCode) ?: getSupportedSystemLanguage()
        return Locale.forLanguageTag(resolvedCode)
    }

    sealed interface SystemPerAppLocaleState {
        data object Unavailable : SystemPerAppLocaleState
        data object SystemDefault : SystemPerAppLocaleState
        data class Overridden(val languageCode: String) : SystemPerAppLocaleState
    }

    fun querySystemPerAppLocaleState(context: Context): SystemPerAppLocaleState {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return SystemPerAppLocaleState.Unavailable
        }
        return runCatching {
            val systemLocaleManager = context.getSystemService(android.app.LocaleManager::class.java)
                ?: return@runCatching SystemPerAppLocaleState.Unavailable
            val appLocales = systemLocaleManager.applicationLocales
            if (appLocales.isEmpty) {
                SystemPerAppLocaleState.SystemDefault
            } else {
                val normalized = normalizeLanguageCode(appLocales[0]?.language)
                if (normalized != null) {
                    SystemPerAppLocaleState.Overridden(normalized)
                } else {
                    SystemPerAppLocaleState.Unavailable
                }
            }
        }.getOrDefault(SystemPerAppLocaleState.Unavailable)
    }

    fun syncAppLanguageToSystemIfNeeded(context: Context, languageCode: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val normalized = normalizeLanguageCode(languageCode) ?: return
        runCatching {
            val systemLocaleManager = context.getSystemService(android.app.LocaleManager::class.java)
                ?: return
            val appLocales = systemLocaleManager.applicationLocales
            if (!appLocales.isEmpty && normalizeLanguageCode(appLocales[0]?.language) != normalized) {
                systemLocaleManager.applicationLocales = LocaleList.forLanguageTags(normalized)
            }
        }
    }

    private fun updateLocale(context: Context, languageCode: String?): Context {
        val normalized = normalizeLanguageCode(languageCode) ?: return context
        val locale = Locale.forLanguageTag(normalized)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }

    fun getLocalizedContext(context: Context, preferenceManager: PreferenceManager): Context {
        val languageCode = when (val state = querySystemPerAppLocaleState(context)) {
            is SystemPerAppLocaleState.Overridden -> {
                if (!preferenceManager.isSystemLocaleOverridden()) {
                    preferenceManager.setSystemLocaleOverridden(true)
                }
                if (state.languageCode != preferenceManager.getSavedLanguageCodeOrNull()) {
                    preferenceManager.setLanguageCode(state.languageCode)
                }
                state.languageCode
            }
            SystemPerAppLocaleState.SystemDefault -> {
                if (preferenceManager.isSystemLocaleOverridden()) {
                    preferenceManager.setSystemLocaleOverridden(false)
                    preferenceManager.clearSavedLanguageCode()
                }
                preferenceManager.getLanguageCode()
            }
            SystemPerAppLocaleState.Unavailable -> preferenceManager.getLanguageCode()
        }
        return updateLocale(context, languageCode)
    }

    fun getSupportedSystemLanguage(): String {
        val sysLang = runCatching {
            Resources.getSystem().configuration.locales[0]?.language
        }.getOrNull().takeUnless { it.isNullOrBlank() }
            ?: Locale.getDefault().language

        return normalizeLanguageCode(sysLang) ?: "en"
    }

    fun getAvailableLanguages(): List<Language> {
        return listOf(
            Language("en", "English"),
            Language("ru", "Русский")
        )
    }

    data class Language(
        val code: String,
        val name: String
    )
}