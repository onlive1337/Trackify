package com.onlive.trackify.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.core.content.edit

class ThemeManager(context: Context) {
    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_THEME_MODE = "theme_mode"

        const val MODE_SYSTEM = 0
        const val MODE_LIGHT = 1
        const val MODE_DARK = 2
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _themeMode = mutableIntStateOf(getThemeMode())
    val themeMode: State<Int> = _themeMode

    fun getThemeMode(): Int {
        return prefs.getInt(KEY_THEME_MODE, MODE_SYSTEM)
    }

    fun setThemeMode(mode: Int) {
        prefs.edit { putInt(KEY_THEME_MODE, mode) }
        _themeMode.intValue = mode
    }

    fun isDarkTheme(systemIsDark: Boolean): Boolean {
        return when (_themeMode.intValue) {
            MODE_LIGHT -> false
            MODE_DARK -> true
            else -> systemIsDark
        }
    }

    fun supportsDynamicColors(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }
}