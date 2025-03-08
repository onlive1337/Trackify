package com.onlive.trackify

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.onlive.trackify.ui.components.TrackifyBottomBar
import com.onlive.trackify.ui.navigation.Screen
import com.onlive.trackify.ui.navigation.TrackifyNavGraph
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
            TrackifyApp(themeManager)
        }
    }

    override fun onPreferenceChanged(key: String, value: Any?) {
        if (key == "language_code") {
            recreate()
        }
    }

    override fun onDestroy() {
        preferenceManager.removeOnPreferenceChangedListener(this)
        super.onDestroy()
    }
}

@Composable
fun TrackifyApp(themeManager: ThemeManager) {
    TrackifyTheme(themeManager = themeManager) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val showBottomBar = when {
            currentRoute == Screen.Home.route -> true
            currentRoute == Screen.Payments.route -> true
            currentRoute == Screen.Statistics.route -> true
            currentRoute == Screen.Settings.route -> true
            else -> false
        }

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    TrackifyBottomBar(
                        navController = navController,
                        currentRoute = currentRoute.orEmpty()
                    )
                }
            }
        ) { paddingValues ->
            TrackifyNavGraph(
                navController = navController,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}