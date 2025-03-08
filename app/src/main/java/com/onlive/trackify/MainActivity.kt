package com.onlive.trackify

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.onlive.trackify.ui.theme.TrackifyTheme
import com.onlive.trackify.utils.LocaleHelper
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.utils.ThemeManager

class MainActivity : ComponentActivity(), PreferenceManager.OnPreferenceChangedListener {

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var themeManager: ThemeManager

    override fun attachBaseContext(newBase: Context) {
        preferenceManager = PreferenceManager(newBase)
        val context = LocaleHelper.getLocalizedContext(newBase, preferenceManager)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        themeManager = ThemeManager(this)
        preferenceManager.addOnPreferenceChangedListener(this)

        setContent {
            TrackifyTheme(themeManager = themeManager) {
                TrackifyApp(themeManager = themeManager)
            }
        }
    }

    override fun onPreferenceChanged(key: String, value: Any?) {
        if (key == PreferenceManager.KEY_LANGUAGE_CODE) {
            recreate()
        }
    }

    override fun onDestroy() {
        preferenceManager.removeOnPreferenceChangedListener(this)
        super.onDestroy()
    }
}