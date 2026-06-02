package com.onlive.trackify.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit

class ThemeManager(context: Context) {
    companion object {
        private const val PREFS_NAME = "theme_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"

        const val MODE_LIGHT = 1
        const val MODE_DARK = 2
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _themeMode = mutableIntStateOf(getThemeMode())
    private val _dynamicColor = mutableStateOf(prefs.getBoolean(KEY_DYNAMIC_COLOR, true))

    fun getThemeMode(): Int {
        return prefs.getInt(KEY_THEME_MODE, MODE_DARK)
    }

    fun setThemeMode(mode: Int) {
        prefs.edit { putInt(KEY_THEME_MODE, mode) }
        _themeMode.intValue = mode
    }

    fun isDarkTheme(): Boolean {
        return when (_themeMode.intValue) {
            MODE_LIGHT -> false
            else -> true
        }
    }

    fun isDynamicColorEnabled(): Boolean = _dynamicColor.value

    fun setDynamicColorEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_DYNAMIC_COLOR, enabled) }
        _dynamicColor.value = enabled
    }
}