package com.onlive.trackify

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.onlive.trackify.ui.components.TrackifyBottomBar
import com.onlive.trackify.ui.navigation.Screen
import com.onlive.trackify.ui.navigation.TrackifyNavGraph
import com.onlive.trackify.utils.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackifyApp(themeManager: ThemeManager) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = remember(currentDestination) {
        when (currentDestination?.route) {
            Screen.Home.route,
            Screen.Payments.route,
            Screen.Statistics.route,
            Screen.Settings.route -> true
            else -> false
        }
    }

    val items = listOf(
        Triple(
            Screen.Home.route,
            stringResource(R.string.title_subscriptions),
            Icons.Filled.Home
        ),
        Triple(
            Screen.Payments.route,
            stringResource(R.string.title_payments),
            Icons.Filled.AttachMoney
        ),
        Triple(
            Screen.Statistics.route,
            stringResource(R.string.title_statistics),
            Icons.Filled.ShowChart
        ),
        Triple(
            Screen.Settings.route,
            stringResource(R.string.title_settings),
            Icons.Filled.Settings
        )
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                TrackifyBottomBar(
                    navController = navController,
                    currentRoute = currentDestination?.route ?: Screen.Home.route
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            TrackifyNavGraph(
                navController = navController,
                themeManager = themeManager
            )
        }
    }
}