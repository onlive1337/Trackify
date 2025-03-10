package com.onlive.trackify

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.onlive.trackify.ui.navigation.Screen
import com.onlive.trackify.ui.navigation.TrackifyNavGraph
import com.onlive.trackify.utils.ThemeManager

@Composable
fun TrackifyApp(themeManager: ThemeManager) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = when {
        currentDestination?.route == Screen.Home.route -> true
        currentDestination?.route == Screen.Payments.route -> true
        currentDestination?.route == Screen.Statistics.route -> true
        currentDestination?.route == Screen.Settings.route -> true
        else -> false
    }

    val bottomNavItems = listOf(
        BottomNavItem(
            title = stringResource(R.string.title_subscriptions),
            icon = Icons.Default.Home,
            route = Screen.Home.route
        ),
        BottomNavItem(
            title = stringResource(R.string.title_payments),
            icon = Icons.Default.AttachMoney,
            route = Screen.Payments.route
        ),
        BottomNavItem(
            title = stringResource(R.string.title_statistics),
            icon = Icons.Default.ShowChart,
            route = Screen.Statistics.route
        ),
        BottomNavItem(
            title = stringResource(R.string.title_settings),
            icon = Icons.Default.AccountCircle,
            route = Screen.Settings.route
        )
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.title) },
                            label = {
                                Text(
                                    text = item.title,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
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

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)