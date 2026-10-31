package com.onlive.trackify.utils

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

object LocaleHelper {

    private fun updateLocale(context: Context, languageCode: String?): Context {
        if (languageCode.isNullOrEmpty()) {
            return context
        }

        val locale = Locale.Builder().setLanguage(languageCode).build()
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }

    fun getLocalizedContext(context: Context, preferenceManager: PreferenceManager): Context {
        val languageCode = preferenceManager.getLanguageCode()
        return updateLocale(context, languageCode)
    }

    fun getSupportedSystemLanguage(): String {
        val sysLang = runCatching {
            Resources.getSystem().configuration.locales[0]?.language
        }.getOrNull().takeUnless { it.isNullOrBlank() }
            ?: Locale.getDefault().language

        val supportedCodes = getAvailableLanguages().map { it.code }.toSet()
        return if (sysLang in supportedCodes) sysLang else "en"
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