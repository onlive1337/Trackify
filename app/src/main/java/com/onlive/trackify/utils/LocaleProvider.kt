package com.onlive.trackify.utils

import android.content.Context
import androidx.compose.runtime.*
import java.util.Locale

class LocaleManager(private val context: Context) {
    private var _currentLocale by mutableStateOf(getSystemLocale())
    val currentLocale: Locale get() = _currentLocale

    private var _localizedContext by mutableStateOf(context)
    val localizedContext: Context get() = _localizedContext

    private fun getSystemLocale(): Locale {
        return context.resources.configuration.locales[0]
    }

    fun setLocale(languageCode: String) {
        val locale = if (languageCode.isEmpty()) {
            Locale.getDefault()
        } else {
            Locale(languageCode)
        }

        _currentLocale = locale
        Locale.setDefault(locale)

        val config = android.content.res.Configuration(context.resources.configuration)
        config.setLocale(locale)
        _localizedContext = context.createConfigurationContext(config)
    }
}

val LocalLocaleManager = compositionLocalOf<LocaleManager> {
    error("LocaleManager not provided")
}

val LocalLocalizedContext = compositionLocalOf<Context> {
    error("LocalizedContext not provided")
}

@Composable
fun ProvideLocaleManager(
    localeManager: LocaleManager,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalLocaleManager provides localeManager,
        LocalLocalizedContext provides localeManager.localizedContext,
        content = content
    )
}

@Composable
fun stringResource(id: Int): String {
    val context = LocalLocalizedContext.current
    return context.getString(id)
}

@Composable
fun stringResource(id: Int, vararg formatArgs: Any): String {
    val context = LocalLocalizedContext.current
    return context.getString(id, *formatArgs)
}