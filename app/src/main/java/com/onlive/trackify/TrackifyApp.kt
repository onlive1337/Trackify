package com.onlive.trackify

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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

    val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                Box(
                    modifier = Modifier.padding(bottom = bottomPadding)
                ) {
                    TrackifyBottomBar(
                        navController = navController,
                        currentRoute = currentDestination?.route ?: Screen.Home.route
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TrackifyNavGraph(
                navController = navController,
                themeManager = themeManager
            )
        }
    }
}