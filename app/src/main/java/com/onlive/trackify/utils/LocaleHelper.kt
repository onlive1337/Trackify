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
            Language("", getSystemLanguageName(context)),
            Language("ru", "Русский"),
            Language("en", "English")
        )
    }

    private fun getSystemLanguageName(context: Context): String {
        return try {
            context.getString(R.string.system_language)
        } catch (e: Exception) {
            "System language"
        }
    }

    fun getLocalizedString(context: Context, languageCode: String, resourceId: Int): String {
        return if (languageCode.isEmpty()) {
            context.getString(resourceId)
        } else {
            val locale = Locale(languageCode)
            val config = Configuration(context.resources.configuration)
            config.setLocale(locale)
            val localizedContext = context.createConfigurationContext(config)
            localizedContext.getString(resourceId)
        }
    }

    data class Language(
        val code: String,
        val name: String
    )
}