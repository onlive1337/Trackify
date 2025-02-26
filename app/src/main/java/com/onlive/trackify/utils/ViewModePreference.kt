package com.onlive.trackify.utils

import android.content.Context
import android.content.SharedPreferences

class ViewModePreference(context: Context) {

    companion object {
        private const val PREFS_NAME = "view_mode_preferences"
        private const val KEY_GRID_MODE = "grid_mode_enabled"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isGridModeEnabled(): Boolean {
        return prefs.getBoolean(KEY_GRID_MODE, false)
    }

    fun setGridModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_GRID_MODE, enabled).apply()
    }
}