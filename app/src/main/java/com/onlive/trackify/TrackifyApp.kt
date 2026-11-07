package com.onlive.trackify

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.onlive.trackify.ui.navigation.Screen
import com.onlive.trackify.ui.navigation.TrackifyNavGraph
import com.onlive.trackify.utils.LocalLocalizedContext
import com.onlive.trackify.utils.LocaleManager
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.utils.ThemeManager
import com.onlive.trackify.utils.UiErrorMessage

@Composable
fun TrackifyApp(
    themeManager: ThemeManager,
    localeManager: LocaleManager,
    initialSubscriptionId: Long = -1L
) {
    val context = LocalContext.current
    val currentLocalizedContext by rememberUpdatedState(LocalLocalizedContext.current)
    val preferenceManager = remember { PreferenceManager(context) }
    val errorHandler = remember(context) {
        (context.applicationContext as? TrackifyApplication)?.errorHandler
    }
    val snackbarHostState = remember { SnackbarHostState() }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isMainScreen = currentDestination?.hasRoute<Screen.Home>() == true ||
        currentDestination?.hasRoute<Screen.Payments>() == true ||
        currentDestination?.hasRoute<Screen.Statistics>() == true ||
        currentDestination?.hasRoute<Screen.Settings>() == true

    val startDestination: Screen = remember {
        if (preferenceManager.isOnboardingCompleted()) {
            Screen.Home
        } else {
            Screen.Onboarding
        }
    }

    LaunchedEffect(errorHandler) {
        errorHandler?.errors?.collect { uiError ->
            val message = when (uiError) {
                is UiErrorMessage.Resource -> currentLocalizedContext.getString(uiError.resId)
                is UiErrorMessage.Dynamic -> uiError.text
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(initialSubscriptionId) {
        if (initialSubscriptionId != -1L && preferenceManager.isOnboardingCompleted()) {
            navController.navigate(Screen.SubscriptionDetail(initialSubscriptionId))
        }
    }

    val backdropColor = if (themeManager.isDarkTheme()) {
        MaterialTheme.colorScheme.surfaceContainerLowest
    } else {
        MaterialTheme.colorScheme.surfaceDim
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = backdropColor
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            TrackifyNavGraph(
                modifier = Modifier.fillMaxSize(),
                navController = navController,
                startDestination = startDestination,
                themeManager = themeManager,
                localeManager = localeManager
            )

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(bottom = if (isMainScreen) 84.dp else 0.dp)
            )
        }
    }
}
