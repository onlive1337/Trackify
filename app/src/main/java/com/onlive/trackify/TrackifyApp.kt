package com.onlive.trackify

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.onlive.trackify.ui.components.TrackifyBottomBar
import com.onlive.trackify.ui.navigation.Screen
import com.onlive.trackify.ui.navigation.TrackifyNavGraph
import com.onlive.trackify.utils.LocaleManager
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.utils.ThemeManager
import com.onlive.trackify.utils.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
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

    val titleRes = when (currentDestination?.route) {
        Screen.Home.route -> R.string.title_subscriptions
        Screen.Payments.route -> R.string.title_payments
        Screen.Statistics.route -> R.string.title_statistics
        Screen.Settings.route -> R.string.title_settings
        else -> null
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

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
        topBar = {
            if (isMainScreen && titleRes != null) {
                MediumFlexibleTopAppBar(
                    title = {
                        Text(
                            text = stringResource(titleRes),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        },
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
                .then(
                    if (isMainScreen) Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                    else Modifier
                )
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