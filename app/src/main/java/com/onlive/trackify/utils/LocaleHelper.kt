package com.onlive.trackify.utils

import android.content.Context
import android.content.res.Configuration
import com.onlive.trackify.R
import java.util.Locale

object LocaleHelper {

    private fun updateLocale(context: Context, languageCode: String?): Context {
        if (languageCode.isNullOrEmpty()) {
            return context
        }

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }

    fun getLocalizedContext(context: Context, preferenceManager: PreferenceManager): Context {
        val languageCode = preferenceManager.getLanguageCode()
        return updateLocale(context, languageCode)
    }

    fun getAvailableLanguages(context: Context): List<Language> {
        return listOf(
            Language("", context.getString(R.string.system_language)),
            Language("ru", "Русский"),
            Language("en", "English")
        )
    }

    data class Language(
        val code: String,
        val name: String
    )
}