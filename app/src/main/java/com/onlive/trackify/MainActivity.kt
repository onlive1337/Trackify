package com.onlive.trackify

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import com.onlive.trackify.ui.theme.TrackifyTheme
import com.onlive.trackify.utils.LocaleHelper
import com.onlive.trackify.utils.LocaleManager
import com.onlive.trackify.utils.NotificationHelper
import com.onlive.trackify.utils.NotificationScheduler
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.utils.ProvideCurrency
import com.onlive.trackify.utils.ProvideLocaleManager
import com.onlive.trackify.utils.ThemeManager

class MainActivity : ComponentActivity() {

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var themeManager: ThemeManager
    private lateinit var localeManager: LocaleManager
    private var pendingSubscriptionId by mutableLongStateOf(-1L)

    override fun attachBaseContext(newBase: Context) {
        preferenceManager = PreferenceManager(newBase)
        val context = LocaleHelper.getLocalizedContext(newBase, preferenceManager)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        themeManager = ThemeManager(this)
        localeManager = LocaleManager(this)

        val savedLanguage = preferenceManager.getLanguageCode()
        localeManager.setLocale(savedLanguage)

        if (savedInstanceState == null) {
            pendingSubscriptionId = consumeSubscriptionIdFromIntent(intent)
        }

        setContent {
            ProvideLocaleManager(localeManager = localeManager) {
                ProvideCurrency(context = this) {
                    TrackifyTheme(themeManager = themeManager) {
                        TrackifyApp(
                            themeManager = themeManager,
                            localeManager = localeManager,
                            initialSubscriptionId = pendingSubscriptionId,
                            onInitialSubscriptionConsumed = { pendingSubscriptionId = -1L }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val targetId = consumeSubscriptionIdFromIntent(intent)
        if (targetId != -1L) {
            pendingSubscriptionId = targetId
        }
    }

    private fun consumeSubscriptionIdFromIntent(source: Intent?): Long {
        if (source == null) return -1L
        val id = source.getLongExtra(NotificationHelper.EXTRA_SUBSCRIPTION_ID, -1L)
        if (id != -1L) {
            source.removeExtra(NotificationHelper.EXTRA_SUBSCRIPTION_ID)
        }
        return id
    }

    override fun onStart() {
        super.onStart()
        if (preferenceManager.areNotificationsEnabled()) {
            NotificationScheduler(this).scheduleNotifications()
        }
    }
}