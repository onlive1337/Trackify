package com.onlive.trackify

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.onlive.trackify.ui.components.TrackifyBottomBar
import com.onlive.trackify.ui.navigation.Screen
import com.onlive.trackify.ui.navigation.TrackifyNavGraph
import com.onlive.trackify.utils.LocaleManager
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.utils.ThemeManager

@Composable
fun TrackifyApp(
    themeManager: ThemeManager,
    localeManager: LocaleManager,
    initialSubscriptionId: Long = -1L
) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val mainRoutes = setOf(
        Screen.Home.route,
        Screen.Payments.route,
        Screen.Statistics.route,
        Screen.Settings.route
    )

    val isMainScreen = currentDestination?.route in mainRoutes

    val startDestination = if (preferenceManager.isOnboardingCompleted()) {
        Screen.Home.route
    } else {
        Screen.Onboarding.route
    }

    LaunchedEffect(initialSubscriptionId) {
        if (initialSubscriptionId != -1L && preferenceManager.isOnboardingCompleted()) {
            navController.navigate(Screen.SubscriptionDetail.createRoute(initialSubscriptionId))
        }
    }

    Scaffold(
        bottomBar = {
            if (isMainScreen) {
                TrackifyBottomBar(
                    navController = navController,
                    currentRoute = currentDestination?.route ?: Screen.Home.route
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.systemBars
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
        ) {
            TrackifyNavGraph(
                navController = navController,
                startDestination = startDestination,
                themeManager = themeManager,
                localeManager = localeManager
            )
        }
    }
}