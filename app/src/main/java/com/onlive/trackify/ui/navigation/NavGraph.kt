package com.onlive.trackify.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
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
import com.onlive.trackify.ui.screens.payments.AddPaymentScreen
import com.onlive.trackify.ui.screens.payments.BulkPaymentActionsScreen
import com.onlive.trackify.ui.screens.payments.PaymentsScreen
import com.onlive.trackify.ui.screens.payments.PendingPaymentsScreen
import com.onlive.trackify.ui.screens.settings.CurrencySettingsScreen
import com.onlive.trackify.ui.screens.settings.DataManagementScreen
import com.onlive.trackify.ui.screens.settings.LanguageSettingsScreen
import com.onlive.trackify.ui.screens.settings.NotificationSettingsScreen
import com.onlive.trackify.ui.screens.settings.SettingsScreen
import com.onlive.trackify.ui.screens.statistics.StatisticsScreen
import com.onlive.trackify.ui.screens.subscription.SubscriptionDetailScreen
import com.onlive.trackify.utils.ThemeManager

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Payments : Screen("payments")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")

    object SubscriptionDetail : Screen("subscription_detail/{subscriptionId}") {
        fun createRoute(subscriptionId: Long) = "subscription_detail/$subscriptionId"
    }

    object AddSubscription : Screen("add_subscription")

    object AddPayment : Screen("add_payment?subscriptionId={subscriptionId}") {
        fun createRoute(subscriptionId: Long = -1L) =
            if (subscriptionId != -1L) "add_payment?subscriptionId=$subscriptionId"
            else "add_payment"
    }

    object CategoryManagement : Screen("category_management")

    object CategoryDetail : Screen("category_detail/{categoryId}") {
        fun createRoute(categoryId: Long) = "category_detail/$categoryId"
    }

    object CategoryGroupDetail : Screen("category_group_detail/{groupId}") {
        fun createRoute(groupId: Long) = "category_group_detail/$groupId"
    }

    object CurrencySettings : Screen("currency_settings")
    object LanguageSettings : Screen("language_settings")
    object NotificationSettings : Screen("notification_settings")
    object DataManagement : Screen("data_management")
    object PendingPayments : Screen("pending_payments")
    object BulkPaymentActions : Screen("bulk_payment_actions")
    object AboutApp : Screen("about_app")
}

class NavigationActions(navController: NavHostController) {
    val navigateToHome: () -> Unit = {
        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Home.route) {
                inclusive = false
            }
        }
    }

    val navigateToPayments: () -> Unit = {
        navController.navigate(Screen.Payments.route) {
            popUpTo(Screen.Home.route) {
                inclusive = false
            }
        }
    }

    val navigateToStatistics: () -> Unit = {
        navController.navigate(Screen.Statistics.route) {
            popUpTo(Screen.Home.route) {
                inclusive = false
            }
        }
    }

    val navigateToSettings: () -> Unit = {
        navController.navigate(Screen.Settings.route) {
            popUpTo(Screen.Home.route) {
                inclusive = false
            }
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

    val navigateToCategoryGroupDetail: (Long) -> Unit = { groupId ->
        navController.navigate(Screen.CategoryGroupDetail.createRoute(groupId))
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

    val navigateToPendingPayments: () -> Unit = {
        navController.navigate(Screen.PendingPayments.route)
    }

    val navigateToBulkPaymentActions: () -> Unit = {
        navController.navigate(Screen.BulkPaymentActions.route)
    }

    val navigateToAboutApp: () -> Unit = {
        navController.navigate(Screen.AboutApp.route)
    }

    val navigateBack: () -> Unit = {
        navController.popBackStack()
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun TrackifyNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Home.route,
    themeManager: ThemeManager? = null
) {
    val navigationActions = remember(navController) {
        NavigationActions(navController)
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
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
                onNavigateToBulkActions = navigationActions.navigateToBulkPaymentActions,
                onNavigateToPendingPayments = navigationActions.navigateToPendingPayments
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
                onNavigateBack = navigationActions.navigateBack,
                onAddPayment = { navigationActions.navigateToAddPayment(subscriptionId) }
            )
        }

        composable(Screen.AddSubscription.route) {
            SubscriptionDetailScreen(
                subscriptionId = -1L,
                onNavigateBack = navigationActions.navigateBack,
                onAddPayment = { /* Не используется при создании */ }
            )
        }

        composable(
            route = Screen.AddPayment.route,
            arguments = listOf(
                navArgument("subscriptionId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { entry ->
            val subscriptionId = entry.arguments?.getLong("subscriptionId") ?: -1L
            AddPaymentScreen(
                subscriptionId = subscriptionId,
                onNavigateBack = navigationActions.navigateBack
            )
        }

        composable(Screen.CategoryManagement.route) {
            CategoryManagementScreen(
                onNavigateBack = navigationActions.navigateBack,
                onAddCategory = { navigationActions.navigateToCategoryDetail(-1L) },
                onEditCategory = navigationActions.navigateToCategoryDetail,
                onAddCategoryGroup = { navigationActions.navigateToCategoryGroupDetail(-1L) },
                onEditCategoryGroup = navigationActions.navigateToCategoryGroupDetail
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

        composable(
            route = Screen.CategoryGroupDetail.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.LongType }
            )
        ) { entry ->
            val groupId = entry.arguments?.getLong("groupId") ?: -1L
            CategoryDetailScreen(
                categoryId = groupId,
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

        composable(Screen.PendingPayments.route) {
            PendingPaymentsScreen(
                onNavigateBack = navigationActions.navigateBack,
                onAddPayment = navigationActions.navigateToAddPayment
            )
        }

        composable(Screen.BulkPaymentActions.route) {
            BulkPaymentActionsScreen(
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