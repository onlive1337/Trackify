package com.onlive.trackify.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.onlive.trackify.ui.screens.home.HomeScreen
import com.onlive.trackify.ui.screens.payments.PaymentsScreen
import com.onlive.trackify.ui.screens.settings.SettingsScreen
import com.onlive.trackify.ui.screens.statistics.StatisticsScreen
import com.onlive.trackify.ui.screens.subscription.SubscriptionDetailScreen
import com.onlive.trackify.ui.screens.subscription.AddSubscriptionScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Payments : Screen("payments")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")

    // Детальные экраны
    object AddSubscription : Screen("add_subscription")
    object SubscriptionDetail : Screen("subscription_detail/{subscriptionId}") {
        fun createRoute(subscriptionId: Long) = "subscription_detail/$subscriptionId"
    }
    object AddPayment : Screen("add_payment?subscriptionId={subscriptionId}") {
        fun createRoute(subscriptionId: Long = -1L) =
            if (subscriptionId != -1L) "add_payment?subscriptionId=$subscriptionId"
            else "add_payment"
    }
    object CategoryManagement : Screen("category_management")
    object CategoryDetail : Screen("category_detail/{categoryId}") {
        fun createRoute(categoryId: Long) = "category_detail/$categoryId"
    }
    // Другие экраны...
}

class NavigationActions(navController: NavHostController) {
    val navigateToHome: () -> Unit = {
        navController.navigate(Screen.Home.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val navigateToPayments: () -> Unit = {
        navController.navigate(Screen.Payments.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val navigateToStatistics: () -> Unit = {
        navController.navigate(Screen.Statistics.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val navigateToSettings: () -> Unit = {
        navController.navigate(Screen.Settings.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val navigateToAddSubscription: () -> Unit = {
        navController.navigate(Screen.AddSubscription.route)
    }

    val navigateToSubscriptionDetail: (Long) -> Unit = { subscriptionId ->
        navController.navigate(Screen.SubscriptionDetail.createRoute(subscriptionId))
    }

    val navigateToAddPayment: (Long) -> Unit = { subscriptionId ->
        navController.navigate(Screen.AddPayment.createRoute(subscriptionId))
    }

    val navigateToCategoryManagement: () -> Unit = {
        navController.navigate(Screen.CategoryManagement.route)
    }

    val navigateToCategoryDetail: (Long) -> Unit = { categoryId ->
        navController.navigate(Screen.CategoryDetail.createRoute(categoryId))
    }

    val navigateBack: () -> Unit = {
        navController.popBackStack()
    }
}

@Composable
fun TrackifyNavGraph(
    navController: NavHostController
) {
    val navigationActions = remember(navController) {
        NavigationActions(navController)
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onAddSubscription = navigationActions.navigateToAddSubscription,
                onSubscriptionClick = navigationActions.navigateToSubscriptionDetail
            )
        }

        composable(Screen.Payments.route) {
            PaymentsScreen(
                onAddPayment = navigationActions.navigateToAddPayment,
                onNavigateBack = navigationActions.navigateBack
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToCategoryManagement = navigationActions.navigateToCategoryManagement,
                onNavigateBack = navigationActions.navigateBack
            )
        }

        composable(Screen.AddSubscription.route) {
            AddSubscriptionScreen(
                onNavigateBack = navigationActions.navigateBack
            )
        }

        composable(
            route = Screen.SubscriptionDetail.route,
            arguments = listOf(navArgument("subscriptionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val subscriptionId = backStackEntry.arguments?.getLong("subscriptionId") ?: -1L
            SubscriptionDetailScreen(
                subscriptionId = subscriptionId,
                onNavigateBack = navigationActions.navigateBack,
                onAddPayment = { navigationActions.navigateToAddPayment(subscriptionId) }
            )
        }

        // Добавляем другие экраны...
    }
}