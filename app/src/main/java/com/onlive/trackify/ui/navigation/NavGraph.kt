package com.onlive.trackify.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.onlive.trackify.ui.screens.category.CategoryDetailScreen
import com.onlive.trackify.ui.screens.category.CategoryManagementScreen
import com.onlive.trackify.ui.screens.home.HomeScreen
import com.onlive.trackify.ui.screens.payments.AddPaymentScreen
import com.onlive.trackify.ui.screens.payments.PaymentsScreen
import com.onlive.trackify.ui.screens.settings.SettingsScreen
import com.onlive.trackify.ui.screens.statistics.StatisticsScreen
import com.onlive.trackify.ui.screens.subscription.SubscriptionDetailScreen
import com.onlive.trackify.utils.ThemeManager

// Определение всех экранов приложения
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Payments : Screen("payments")
    object Statistics : Screen("statistics")
    object Settings : Screen("settings")

    // Детальные экраны
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
}

// Класс для действий навигации
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

    val navigateBack: () -> Unit = {
        navController.popBackStack()
    }
}

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
        // Основные экраны нижней навигации
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
                themeManager = themeManager ?: ThemeManager(null)
            )
        }

        // Экран деталей подписки
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

        // Экран добавления подписки
        composable(Screen.AddSubscription.route) {
            SubscriptionDetailScreen(
                subscriptionId = -1L,
                onNavigateBack = navigationActions.navigateBack,
                onAddPayment = { /* Не используется при создании */ }
            )
        }

        // Экран добавления платежа
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

        // Управление категориями
        composable(Screen.CategoryManagement.route) {
            CategoryManagementScreen(
                onNavigateBack = navigationActions.navigateBack,
                onAddCategory = { navigationActions.navigateToCategoryDetail(-1L) },
                onEditCategory = navigationActions.navigateToCategoryDetail,
                onAddCategoryGroup = { navigationActions.navigateToCategoryGroupDetail(-1L) },
                onEditCategoryGroup = navigationActions.navigateToCategoryGroupDetail
            )
        }

        // Детали категории
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

        // Детали группы категорий
        composable(
            route = Screen.CategoryGroupDetail.route,
            arguments = listOf(
                navArgument("groupId") { type = NavType.LongType }
            )
        ) { entry ->
            val groupId = entry.arguments?.getLong("groupId") ?: -1L
            // Временно используем обычный экран категории, пока не реализуем экран группы
            CategoryDetailScreen(
                categoryId = groupId,
                onNavigateBack = navigationActions.navigateBack
            )
        }

        // Настройки валюты
        composable(Screen.CurrencySettings.route) {
            // TODO: Реализовать экран настроек валюты
            SettingsScreen(
                onNavigateToCategoryManagement = navigationActions.navigateToCategoryManagement,
                onNavigateToNotificationSettings = navigationActions.navigateToNotificationSettings,
                onNavigateToCurrencySettings = navigationActions.navigateToCurrencySettings,
                onNavigateToLanguageSettings = navigationActions.navigateToLanguageSettings,
                onNavigateToDataManagement = navigationActions.navigateToDataManagement,
                themeManager = themeManager ?: ThemeManager(null)
            )
        }

        // Настройки языка
        composable(Screen.LanguageSettings.route) {
            // TODO: Реализовать экран настроек языка
            SettingsScreen(
                onNavigateToCategoryManagement = navigationActions.navigateToCategoryManagement,
                onNavigateToNotificationSettings = navigationActions.navigateToNotificationSettings,
                onNavigateToCurrencySettings = navigationActions.navigateToCurrencySettings,
                onNavigateToLanguageSettings = navigationActions.navigateToLanguageSettings,
                onNavigateToDataManagement = navigationActions.navigateToDataManagement,
                themeManager = themeManager ?: ThemeManager(null)
            )
        }

        // Настройки уведомлений
        composable(Screen.NotificationSettings.route) {
            // TODO: Реализовать экран настроек уведомлений
            SettingsScreen(
                onNavigateToCategoryManagement = navigationActions.navigateToCategoryManagement,
                onNavigateToNotificationSettings = navigationActions.navigateToNotificationSettings,
                onNavigateToCurrencySettings = navigationActions.navigateToCurrencySettings,
                onNavigateToLanguageSettings = navigationActions.navigateToLanguageSettings,
                onNavigateToDataManagement = navigationActions.navigateToDataManagement,
                themeManager = themeManager ?: ThemeManager(null)
            )
        }

        // Управление данными
        composable(Screen.DataManagement.route) {
            // TODO: Реализовать экран управления данными
            SettingsScreen(
                onNavigateToCategoryManagement = navigationActions.navigateToCategoryManagement,
                onNavigateToNotificationSettings = navigationActions.navigateToNotificationSettings,
                onNavigateToCurrencySettings = navigationActions.navigateToCurrencySettings,
                onNavigateToLanguageSettings = navigationActions.navigateToLanguageSettings,
                onNavigateToDataManagement = navigationActions.navigateToDataManagement,
                themeManager = themeManager ?: ThemeManager(null)
            )
        }

        // Ожидающие платежи
        composable(Screen.PendingPayments.route) {
            // TODO: Реализовать экран ожидающих платежей
            PaymentsScreen(
                onAddPayment = navigationActions.navigateToAddPayment,
                onNavigateToBulkActions = navigationActions.navigateToBulkPaymentActions,
                onNavigateToPendingPayments = navigationActions.navigateToPendingPayments
            )
        }

        // Групповые операции с платежами
        composable(Screen.BulkPaymentActions.route) {
            // TODO: Реализовать экран групповых операций с платежами
            PaymentsScreen(
                onAddPayment = navigationActions.navigateToAddPayment,
                onNavigateToBulkActions = navigationActions.navigateToBulkPaymentActions,
                onNavigateToPendingPayments = navigationActions.navigateToPendingPayments
            )
        }
    }
}