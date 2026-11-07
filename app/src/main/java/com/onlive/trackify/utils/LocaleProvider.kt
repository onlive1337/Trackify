package com.onlive.trackify.utils

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

class LocaleManager(private val context: Context) {
    private var _localizedContext by mutableStateOf(context)
    val localizedContext: Context get() = _localizedContext

    var currentLocale: Locale by mutableStateOf(
        LocaleHelper.toSafeLocale(context.resources.configuration.locales[0]?.language)
    )
        private set

    fun setLocale(languageCode: String) {
        val locale = LocaleHelper.toSafeLocale(languageCode)
        Locale.setDefault(locale)
        currentLocale = locale

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        _localizedContext = context.createConfigurationContext(config)

        LocaleHelper.syncAppLanguageToSystemIfNeeded(context, locale.language)
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
    val baseConfiguration = LocalConfiguration.current
    val localizedConfiguration = remember(baseConfiguration, localeManager.currentLocale) {
        Configuration(baseConfiguration).apply {
            setLocale(localeManager.currentLocale)
        }
    }

    CompositionLocalProvider(
        LocalLocaleManager provides localeManager,
        LocalLocalizedContext provides localeManager.localizedContext,
        LocalConfiguration provides localizedConfiguration,
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

@Composable
fun pluralStringResource(id: Int, count: Int, vararg formatArgs: Any): String {
    val context = LocalLocalizedContext.current
    return context.resources.getQuantityString(id, count, *formatArgs)
}

