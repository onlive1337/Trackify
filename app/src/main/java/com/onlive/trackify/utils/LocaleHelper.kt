package com.onlive.trackify.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

object LocaleHelper {

    fun updateLocale(context: Context, languageCode: String?): Context {
        if (languageCode.isNullOrEmpty()) {
            return context
        }

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale)
            return context.createConfigurationContext(configuration)
        } else {
            @Suppress("DEPRECATION")
            configuration.locale = locale
            context.resources.updateConfiguration(configuration, context.resources.displayMetrics)
            return context
        }
    }

    fun getLocalizedContext(context: Context, preferenceManager: PreferenceManager): Context {
        val languageCode = preferenceManager.getLanguageCode()
        return updateLocale(context, languageCode)
    }

    fun getAvailableLanguages(): List<Language> {
        return listOf(
            Language("", "Системный язык"),
            Language("ru", "Русский"),
            Language("en", "English")
        )
    }

    fun getLanguageByCode(code: String?): Language {
        return getAvailableLanguages().find { it.code == code } ?: getAvailableLanguages()[0]
    }

    data class Language(
        val code: String,
        val name: String
    )
}