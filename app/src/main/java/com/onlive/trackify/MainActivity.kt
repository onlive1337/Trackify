package com.onlive.trackify

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import com.onlive.trackify.ui.theme.TrackifyTheme
import com.onlive.trackify.utils.LocaleHelper
import com.onlive.trackify.utils.LocaleManager
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.utils.ProvideLocaleManager
import com.onlive.trackify.utils.ThemeManager

class MainActivity : ComponentActivity(), PreferenceManager.OnPreferenceChangedListener {

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var themeManager: ThemeManager
    private lateinit var localeManager: LocaleManager

    override fun attachBaseContext(newBase: Context) {
        preferenceManager = PreferenceManager(newBase)
        val context = LocaleHelper.getLocalizedContext(newBase, preferenceManager)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        themeManager = ThemeManager(this)
        localeManager = LocaleManager(this)
        preferenceManager.addOnPreferenceChangedListener(this)

        val savedLanguage = preferenceManager.getLanguageCode()
        localeManager.setLocale(savedLanguage)

        setContent {
            ProvideLocaleManager(localeManager = localeManager) {
                TrackifyTheme(themeManager = themeManager) {
                    TrackifyApp(
                        themeManager = themeManager,
                        localeManager = localeManager
                    )
                }
            }
        }
    }

    override fun onPreferenceChanged(key: String, value: Any?) {
        if (key == PreferenceManager.KEY_LANGUAGE_CODE) {
            val newLanguage = value as? String ?: ""
            localeManager.setLocale(newLanguage)
        }
    }

    override fun onDestroy() {
        preferenceManager.removeOnPreferenceChangedListener(this)
        super.onDestroy()
    }
}