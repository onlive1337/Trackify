package com.example.trackify

import android.app.Application
import com.example.trackify.utils.ThemeManager
import com.google.android.material.color.DynamicColors

class TrackifyApplication : Application() {
    private lateinit var themeManager: ThemeManager

    override fun onCreate() {
        super.onCreate()

        themeManager = ThemeManager(this)

        themeManager.applyTheme()

        if (themeManager.supportsDynamicColors()) {
            DynamicColors.applyToActivitiesIfAvailable(this)
        }
    }
}