package com.onlive.trackify.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.onlive.trackify.ui.screens.about.AboutAppScreen
import com.onlive.trackify.ui.screens.category.CategoryDetailScreen
import com.onlive.trackify.ui.screens.category.CategoryManagementScreen
import com.onlive.trackify.ui.screens.home.HomeScreen
import com.onlive.trackify.ui.screens.onboarding.OnboardingScreen
import com.onlive.trackify.ui.screens.payments.AddPaymentScreen
import com.onlive.trackify.ui.screens.payments.PaymentsScreen
import com.onlive.trackify.ui.screens.settings.CurrencySettingsScreen
import com.onlive.trackify.ui.screens.settings.DataManagementScreen
import com.onlive.trackify.ui.screens.settings.LanguageSettingsScreen
import com.onlive.trackify.ui.screens.settings.NotificationSettingsScreen
import com.onlive.trackify.ui.screens.settings.SettingsScreen
import com.onlive.trackify.ui.screens.statistics.StatisticsScreen
import com.onlive.trackify.ui.screens.subscription.SubscriptionDetailScreen
import com.onlive.trackify.utils.LocaleManager
import com.onlive.trackify.utils.ThemeManager

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Payments : Screen("payments")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")

    object SubscriptionDetail : Screen("subscription_detail/{subscriptionId}") {
        fun createRoute(subscriptionId: Long) = "subscription_detail/$subscriptionId"
    }

    object AddSubscription : Screen("add_subscription")

    object AddPayment : Screen("add_payment?subscriptionId={subscriptionId}&paymentId={paymentId}") {
        fun createRoute(subscriptionId: Long = -1L, paymentId: Long = -1L) =
            "add_payment?subscriptionId=$subscriptionId&paymentId=$paymentId"
    }

    object CategoryManagement : Screen("category_management")

    object CategoryDetail : Screen("category_detail/{categoryId}") {
        fun createRoute(categoryId: Long) = "category_detail/$categoryId"
    }


    object CurrencySettings : Screen("currency_settings")
    object LanguageSettings : Screen("language_settings")
    object NotificationSettings : Screen("notification_settings")
    object DataManagement : Screen("data_management")
    object AboutApp : Screen("about_app")
}

class NavigationActions(navController: NavHostController) {
    val navigateToHome: () -> Unit = {
        navController.navigate(Screen.Home.route) {
            popUpTo(0) { inclusive = true }
        }
    }

    val navigateToAddSubscription: () -> Unit = {
        navController.navigate(Screen.AddSubscription.route)
    }

    val navigateToSubscriptionDetail: (Long) -> Unit = { subscriptionId ->
        navController.navigate(Screen.SubscriptionDetail.createRoute(subscriptionId))
    }

    val navigateToAddPayment: (Long, Long) -> Unit = { subscriptionId, paymentId ->
        navController.navigate(Screen.AddPayment.createRoute(subscriptionId, paymentId))
    }

    val navigateToCategoryManagement: () -> Unit = {
        navController.navigate(Screen.CategoryManagement.route)
    }

    val navigateToCategoryDetail: (Long) -> Unit = { categoryId ->
        navController.navigate(Screen.CategoryDetail.createRoute(categoryId))
    }

    val navigateToCurrencySettings: () -> Unit = {
        navController.navigate(Screen.CurrencySettings.route)
    }

    val navigateToLanguageSettings: () -> Unit = {
        navController.navigate(Screen.LanguageSettings.route)
    }

    val navigateToNotificationSettings: () -> Unit = {
        navController.navigate(Screen.NotificationSettings.route)
    }

    val navigateToDataManagement: () -> Unit = {
        navController.navigate(Screen.DataManagement.route)
    }

    val navigateToAboutApp: () -> Unit = {
        navController.navigate(Screen.AboutApp.route)
    }

    val navigateBack: () -> Unit = {
        navController.popBackStack()
    }
}

@Composable
fun TrackifyNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Onboarding.route,
    themeManager: ThemeManager? = null,
    localeManager: LocaleManager
) {
    val navigationActions = remember(navController) {
        NavigationActions(navController)
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = navigationActions.navigateToHome,
                localeManager = localeManager
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onAddSubscription = navigationActions.navigateToAddSubscription,
                onSubscriptionClick = navigationActions.navigateToSubscriptionDetail
            )
        }

        composable(Screen.Payments.route) {
            PaymentsScreen(
                onAddPayment = navigationActions.navigateToAddPayment
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen()
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateToCategoryManagement = navigationActions.navigateToCategoryManagement,
                onNavigateToNotificationSettings = navigationActions.navigateToNotificationSettings,
                onNavigateToCurrencySettings = navigationActions.navigateToCurrencySettings,
                onNavigateToLanguageSettings = navigationActions.navigateToLanguageSettings,
                onNavigateToDataManagement = navigationActions.navigateToDataManagement,
                onNavigateToAboutApp = navigationActions.navigateToAboutApp,
                themeManager = themeManager ?: ThemeManager(LocalContext.current)
            )
        }

        composable(
            route = Screen.SubscriptionDetail.route,
            arguments = listOf(
                navArgument("subscriptionId") { type = NavType.LongType }
            )
        ) { entry ->
            val subscriptionId = entry.arguments?.getLong("subscriptionId") ?: -1L
            SubscriptionDetailScreen(
                subscriptionId = subscriptionId,
                onNavigateBack = navigationActions.navigateBack
            )
        }

        composable(Screen.AddSubscription.route) {
            SubscriptionDetailScreen(
                subscriptionId = -1L,
                onNavigateBack = navigationActions.navigateBack
            )
        }

        composable(
            route = Screen.AddPayment.route,
            arguments = listOf(
                navArgument("subscriptionId") {
                    type = NavType.LongType
                    defaultValue = -1L
                },
                navArgument("paymentId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { entry ->
            val subscriptionId = entry.arguments?.getLong("subscriptionId") ?: -1L
            val paymentId = entry.arguments?.getLong("paymentId") ?: -1L
            AddPaymentScreen(
                subscriptionId = subscriptionId,
                paymentId = paymentId,
                onNavigateBack = navigationActions.navigateBack
            )
        }

        composable(Screen.CategoryManagement.route) {
            CategoryManagementScreen(
                onNavigateBack = navigationActions.navigateBack,
                onAddCategory = { navigationActions.navigateToCategoryDetail(-1L) },
                onEditCategory = navigationActions.navigateToCategoryDetail
            )
        }

        composable(
            route = Screen.CategoryDetail.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.LongType }
            )
        ) { entry ->
            val categoryId = entry.arguments?.getLong("categoryId") ?: -1L
            CategoryDetailScreen(
                categoryId = categoryId,
                onNavigateBack = navigationActions.navigateBack
            )
        }

        composable(Screen.CurrencySettings.route) {
            CurrencySettingsScreen(
                onNavigateBack = navigationActions.navigateBack
            )
        }

        composable(Screen.LanguageSettings.route) {
            LanguageSettingsScreen(
                onNavigateBack = navigationActions.navigateBack
            )
        }

        composable(Screen.NotificationSettings.route) {
            NotificationSettingsScreen(
                onNavigateBack = navigationActions.navigateBack
            )
        }

        composable(Screen.DataManagement.route) {
            DataManagementScreen(
                onNavigateBack = navigationActions.navigateBack
            )
        }

        composable(Screen.AboutApp.route) {
            AboutAppScreen(
                onNavigateBack = navigationActions.navigateBack
            )
        }
    }
}