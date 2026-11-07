package com.onlive.trackify.utils

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf

class ThemeManager(context: Context) {
    companion object {
        const val MODE_LIGHT = 1
        const val MODE_DARK = 2
    }

    private val preferenceManager = PreferenceManager(context)
    private val _themeMode = mutableIntStateOf(preferenceManager.getThemeMode())
    private val _dynamicColor = mutableStateOf(preferenceManager.isDynamicColorEnabled())

    fun getThemeMode(): Int {
        return preferenceManager.getThemeMode()
    }

    fun setThemeMode(mode: Int) {
        preferenceManager.setThemeMode(mode)
        _themeMode.intValue = mode
    }

    fun isDarkTheme(): Boolean {
        return _themeMode.intValue != MODE_LIGHT
    }

    fun isDynamicColorEnabled(): Boolean = _dynamicColor.value

    fun setDynamicColorEnabled(enabled: Boolean) {
        preferenceManager.setDynamicColorEnabled(enabled)
        _dynamicColor.value = enabled
    }

    fun reloadFromPreferences() {
        _themeMode.intValue = preferenceManager.getThemeMode()
        _dynamicColor.value = preferenceManager.isDynamicColorEnabled()
    }
}
