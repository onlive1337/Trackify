package com.onlive.trackify.ui.navigation

import androidx.activity.BackEventCompat
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleOut
import androidx.compose.animation.unveilIn
import androidx.compose.animation.veilOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.onlive.trackify.R
import com.onlive.trackify.ui.components.TrackifyBottomBar
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
import com.onlive.trackify.utils.stringResource
import kotlinx.serialization.Serializable

/** Material 3 MotionTokens.EasingEmphasizedDecelerateCubicBezier. */
private val EmphasizedDecelerateEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)

/**
 * Official Android / Material 3 Predictive Back gesture progress curve
 * (`androidx.compose.material3.internal.PredictiveBack` & Android Predictive Back Design Guide).
 */
private val PredictiveBackEasing = CubicBezierEasing(0.1f, 0.1f, 0.0f, 1.0f)

/** Material 3 MotionTokens.DurationMedium2 (300ms) for standard spatial navigation. */
private const val NAVIGATION_DURATION_MS = 300

/** Seekable predictive back transition duration; all predictive specs share this duration for 1:1 progress mapping. */
private const val PREDICTIVE_BACK_DURATION_MS = 300

/**
 * Android 14-17 Cross-Activity / Material 3 Shared X-Axis spatial offset (96dp).
 */
private val SharedAxisSlideDistance = 96.dp

/**
 * Full-screen surface corner radius during predictive back preview (28dp Material 3 ExtraLarge / Pixel display radius).
 */
private val PredictiveBackCornerRadius = 28.dp

/**
 * Subtle Material 3 elevation cast by the shrinking outgoing surface over the underlying screen.
 */
private val PredictiveBackElevation = 6.dp

/**
 * Underlying screen scrim color unveiled during predictive back and pop transitions.
 */
private val PredictiveBackScrimColor = Color.Black.copy(alpha = 0.22f)

/**
 * Official Android Predictive Back Design Guide 35% fade-through progress threshold:
 * - 0%..35% progress: outgoing full-screen surface stays at 100% opacity so user can inspect the preview.
 * - 35%..100% progress: outgoing surface smoothly fades to 0% as the transition completes on commit.
 */
private val PredictiveBackFadeOutEasing = Easing { t ->
    if (t <= 0.35f) {
        0f
    } else {
        LinearOutSlowInEasing.transform(((t - 0.35f) / 0.65f).coerceIn(0f, 1f))
    }
}

/**
 * Edge-aware TransformOrigin per Google's Full-Screen Predictive Back Design Guide:
 * - Left edge swipe: pivots near right edge (0.9f) so card insets away from the left edge while keeping ~8dp right margin.
 * - Right edge swipe: pivots near left edge (0.1f) so card insets away from the right edge while keeping ~8dp left margin.
 * - EDGE_NONE (Android 16/17 3-button navigation predictive back): scales symmetrically toward center (0.5f, 0.5f).
 */
private fun predictiveBackTransformOrigin(swipeEdge: Int): TransformOrigin =
    when (swipeEdge) {
        BackEventCompat.EDGE_LEFT -> TransformOrigin(pivotFractionX = 0.9f, pivotFractionY = 0.5f)
        BackEventCompat.EDGE_RIGHT -> TransformOrigin(pivotFractionX = 0.1f, pivotFractionY = 0.5f)
        else -> TransformOrigin.Center
    }

/**
 * Official Google Compose Navigation + Android 17 Full-Screen Predictive Back exit transition:
 * Shrinks to ~90% scale across the 0..35% preview window (targetScale = 0.88f at 100%) and fades through past 35%.
 */
private fun predictiveBackExitTransition(swipeEdge: Int): ExitTransition =
    scaleOut(
        targetScale = 0.88f,
        transformOrigin = predictiveBackTransformOrigin(swipeEdge),
        animationSpec = tween(durationMillis = PREDICTIVE_BACK_DURATION_MS, easing = PredictiveBackEasing)
    ) + fadeOut(
        animationSpec = tween(durationMillis = PREDICTIVE_BACK_DURATION_MS, easing = PredictiveBackFadeOutEasing)
    )

/**
 * Official Google Compose Animation predictive back enter transition:
 * Keeps the underlying destination full-bleed edge-to-edge and smoothly unveils its dark scrim.
 */
@OptIn(ExperimentalAnimationApi::class)
private fun predictiveBackEnterTransition(): EnterTransition =
    unveilIn(
        animationSpec = tween(durationMillis = PREDICTIVE_BACK_DURATION_MS, easing = PredictiveBackEasing),
        initialColor = PredictiveBackScrimColor
    )

/**
 * Programmatic back exit transition (e.g. TopAppBar back button):
 * Material 3 Shared X-Axis / Android close transition (+96dp slide + subtle scale + fast fade-out).
 */
private fun AnimatedContentTransitionScope<NavBackStackEntry>.standardPopExitTransition(
    slideDistancePx: Int
): ExitTransition =
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.End,
        targetOffset = { slideDistancePx },
        animationSpec = tween(durationMillis = NAVIGATION_DURATION_MS, easing = EmphasizedDecelerateEasing)
    ) + scaleOut(
        targetScale = 0.92f,
        transformOrigin = TransformOrigin.Center,
        animationSpec = tween(durationMillis = NAVIGATION_DURATION_MS, easing = EmphasizedDecelerateEasing)
    ) + fadeOut(
        animationSpec = tween(durationMillis = NAVIGATION_DURATION_MS / 2, easing = FastOutLinearInEasing)
    )

/**
 * Programmatic back enter transition (e.g. TopAppBar back button):
 * Material 3 Shared X-Axis / Android close transition (-96dp slide + scrim unveil).
 */
@OptIn(ExperimentalAnimationApi::class)
private fun AnimatedContentTransitionScope<NavBackStackEntry>.standardPopEnterTransition(
    slideDistancePx: Int
): EnterTransition =
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.End,
        initialOffset = { slideDistancePx },
        animationSpec = tween(durationMillis = NAVIGATION_DURATION_MS, easing = EmphasizedDecelerateEasing)
    ) + unveilIn(
        animationSpec = tween(durationMillis = NAVIGATION_DURATION_MS, easing = EmphasizedDecelerateEasing),
        initialColor = PredictiveBackScrimColor
    )

/**
 * Applies rounded corners (28dp) and subtle depth shadow strictly when a destination is exiting on top
 * (during predictive back gesture or pop), keeping destinations flat edge-to-edge when idle or pushed underneath.
 */
@Composable
private fun PredictiveBackSurface(
    entry: NavBackStackEntry,
    currentBackStackEntry: NavBackStackEntry?,
    navController: NavHostController,
    transition: Transition<EnterExitState>,
    content: @Composable () -> Unit
) {
    val isPushedUnderneath = navController.previousBackStackEntry?.id == entry.id &&
        currentBackStackEntry?.id != entry.id
    val isTabCrossfade = isMainTabDestination(entry.destination) &&
        isMainTabDestination(currentBackStackEntry?.destination) &&
        currentBackStackEntry?.destination?.hasRoute<Screen.Home>() != true
    val shouldRoundOnExit = !isPushedUnderneath && !isTabCrossfade

    val cornerRadius by transition.animateDp(
        transitionSpec = { tween(durationMillis = 60, easing = PredictiveBackEasing) },
        label = "predictiveBackCornerRadius"
    ) { state ->
        if (state == EnterExitState.PostExit && shouldRoundOnExit) PredictiveBackCornerRadius else 0.dp
    }

    val elevation by transition.animateDp(
        transitionSpec = { tween(durationMillis = 120, easing = PredictiveBackEasing) },
        label = "predictiveBackElevation"
    ) { state ->
        if (state == EnterExitState.PostExit && shouldRoundOnExit) PredictiveBackElevation else 0.dp
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                val radius = cornerRadius
                val elev = elevation
                shape = RoundedCornerShape(radius)
                clip = radius > 0.dp
                shadowElevation = elev.toPx()
            }
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        content()
    }
}

/** Destination wrapper integrating [PredictiveBackSurface] with [AnimatedContentScope.transition]. */
private inline fun <reified T : Any> NavGraphBuilder.predictiveBackComposable(
    navController: NavHostController,
    noinline currentEntryProvider: () -> NavBackStackEntry?,
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable<T> { entry ->
        PredictiveBackSurface(
            entry = entry,
            currentBackStackEntry = currentEntryProvider(),
            navController = navController,
            transition = transition
        ) {
            content(entry)
        }
    }
}

private val mainTabRoutes = listOf(
    Screen.Home::class,
    Screen.Payments::class,
    Screen.Statistics::class,
    Screen.Settings::class
)

private fun isMainTabDestination(destination: NavDestination?): Boolean {
    if (destination == null) return false
    return mainTabRoutes.any { routeClass -> destination.hasRoute(routeClass) }
}

@Serializable
sealed interface Screen {
    @Serializable
    data object Onboarding : Screen

    @Serializable
    data object Home : Screen

    @Serializable
    data object Payments : Screen

    @Serializable
    data object Statistics : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data class SubscriptionDetail(val subscriptionId: Long) : Screen

    @Serializable
    data object AddSubscription : Screen

    @Serializable
    data class AddPayment(
        val subscriptionId: Long = -1L,
        val paymentId: Long = -1L
    ) : Screen

    @Serializable
    data object CategoryManagement : Screen

    @Serializable
    data class CategoryDetail(val categoryId: Long) : Screen

    @Serializable
    data object CurrencySettings : Screen

    @Serializable
    data object LanguageSettings : Screen

    @Serializable
    data object NotificationSettings : Screen

    @Serializable
    data object DataManagement : Screen

    @Serializable
    data object AboutApp : Screen
}

class NavigationActions(navController: NavHostController) {
    val navigateToHome: () -> Unit = {
        navController.navigate(Screen.Home) {
            popUpTo<Screen.Onboarding> { inclusive = true }
            launchSingleTop = true
        }
    }
    val navigateToAddSubscription: () -> Unit = {
        navController.navigate(Screen.AddSubscription)
    }
    val navigateToSubscriptionDetail: (Long) -> Unit = { subscriptionId ->
        navController.navigate(Screen.SubscriptionDetail(subscriptionId))
    }
    val navigateToAddPayment: (Long, Long) -> Unit = { subscriptionId, paymentId ->
        navController.navigate(Screen.AddPayment(subscriptionId = subscriptionId, paymentId = paymentId))
    }
    val navigateToCategoryManagement: () -> Unit = {
        navController.navigate(Screen.CategoryManagement)
    }
    val navigateToCategoryDetail: (Long) -> Unit = { categoryId ->
        navController.navigate(Screen.CategoryDetail(categoryId))
    }
    val navigateToCurrencySettings: () -> Unit = {
        navController.navigate(Screen.CurrencySettings)
    }
    val navigateToLanguageSettings: () -> Unit = {
        navController.navigate(Screen.LanguageSettings)
    }
    val navigateToNotificationSettings: () -> Unit = {
        navController.navigate(Screen.NotificationSettings)
    }
    val navigateToDataManagement: () -> Unit = {
        navController.navigate(Screen.DataManagement)
    }
    val navigateToAboutApp: () -> Unit = {
        navController.navigate(Screen.AboutApp)
    }
    val navigateBack: () -> Unit = {
        if (navController.previousBackStackEntry != null) {
            navController.popBackStack()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MainTabScaffold(
    @StringRes titleRes: Int,
    currentTab: Screen,
    navController: NavHostController,
    content: @Composable () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
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
        },
        bottomBar = {
            TrackifyBottomBar(
                navController = navController,
                currentRoute = currentTab
            )
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
            content()
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TrackifyNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: Screen = Screen.Onboarding,
    themeManager: ThemeManager,
    localeManager: LocaleManager
) {
    val navigationActions = remember(navController) {
        NavigationActions(navController)
    }
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val density = LocalDensity.current
    val slideDistancePx = with(density) { SharedAxisSlideDistance.roundToPx() }
    val tabEffectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            val isTabSwitch = isMainTabDestination(initialState.destination) &&
                isMainTabDestination(targetState.destination)
            if (isTabSwitch) {
                fadeIn(animationSpec = tabEffectsSpec)
            } else {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    initialOffset = { slideDistancePx },
                    animationSpec = tween(durationMillis = NAVIGATION_DURATION_MS, easing = EmphasizedDecelerateEasing)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 200, easing = LinearOutSlowInEasing)
                )
            }
        },
        exitTransition = {
            val isTabSwitch = isMainTabDestination(initialState.destination) &&
                isMainTabDestination(targetState.destination)
            if (isTabSwitch) {
                fadeOut(animationSpec = tabEffectsSpec)
            } else {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Start,
                    targetOffset = { slideDistancePx },
                    animationSpec = tween(durationMillis = NAVIGATION_DURATION_MS, easing = EmphasizedDecelerateEasing)
                ) + veilOut(
                    animationSpec = tween(durationMillis = NAVIGATION_DURATION_MS, easing = EmphasizedDecelerateEasing),
                    targetColor = PredictiveBackScrimColor
                )
            }
        },
        popEnterTransition = {
            val isTabSwitch = isMainTabDestination(initialState.destination) &&
                isMainTabDestination(targetState.destination)
            if (isTabSwitch) {
                fadeIn(animationSpec = tabEffectsSpec)
            } else {
                standardPopEnterTransition(slideDistancePx)
            }
        },
        popExitTransition = {
            val isTabSwitch = isMainTabDestination(initialState.destination) &&
                isMainTabDestination(targetState.destination)
            if (isTabSwitch) {
                fadeOut(animationSpec = tabEffectsSpec)
            } else {
                standardPopExitTransition(slideDistancePx)
            }
        },
        predictivePopEnterTransition = {
            predictiveBackEnterTransition()
        },
        predictivePopExitTransition = { swipeEdge ->
            predictiveBackExitTransition(swipeEdge)
        }
    ) {
        composable<Screen.Onboarding> {
            OnboardingScreen(
                onComplete = navigationActions.navigateToHome,
                localeManager = localeManager
            )
        }
        composable<Screen.Home> {
            MainTabScaffold(
                titleRes = R.string.title_subscriptions,
                currentTab = Screen.Home,
                navController = navController
            ) {
                HomeScreen(
                    onAddSubscription = navigationActions.navigateToAddSubscription,
                    onSubscriptionClick = navigationActions.navigateToSubscriptionDetail
                )
            }
        }
        predictiveBackComposable<Screen.Payments>(
            navController = navController,
            currentEntryProvider = { currentBackStackEntry }
        ) {
            MainTabScaffold(
                titleRes = R.string.title_payments,
                currentTab = Screen.Payments,
                navController = navController
            ) {
                PaymentsScreen(onAddPayment = navigationActions.navigateToAddPayment)
            }
        }
        predictiveBackComposable<Screen.Statistics>(
            navController = navController,
            currentEntryProvider = { currentBackStackEntry }
        ) {
            MainTabScaffold(
                titleRes = R.string.title_statistics,
                currentTab = Screen.Statistics,
                navController = navController
            ) {
                StatisticsScreen()
            }
        }
        predictiveBackComposable<Screen.Settings>(
            navController = navController,
            currentEntryProvider = { currentBackStackEntry }
        ) {
            MainTabScaffold(
                titleRes = R.string.title_settings,
                currentTab = Screen.Settings,
                navController = navController
            ) {
                SettingsScreen(
                    onNavigateToCategoryManagement = navigationActions.navigateToCategoryManagement,
                    onNavigateToNotificationSettings = navigationActions.navigateToNotificationSettings,
                    onNavigateToCurrencySettings = navigationActions.navigateToCurrencySettings,
                    onNavigateToLanguageSettings = navigationActions.navigateToLanguageSettings,
                    onNavigateToDataManagement = navigationActions.navigateToDataManagement,
                    onNavigateToAboutApp = navigationActions.navigateToAboutApp,
                    themeManager = themeManager
                )
            }
        }
        predictiveBackComposable<Screen.SubscriptionDetail>(
            navController = navController,
            currentEntryProvider = { currentBackStackEntry }
        ) { entry ->
            val args = entry.toRoute<Screen.SubscriptionDetail>()
            SubscriptionDetailScreen(
                subscriptionId = args.subscriptionId,
                onNavigateBack = navigationActions.navigateBack
            )
        }
        predictiveBackComposable<Screen.AddSubscription>(
            navController = navController,
            currentEntryProvider = { currentBackStackEntry }
        ) {
            SubscriptionDetailScreen(
                subscriptionId = -1L,
                onNavigateBack = navigationActions.navigateBack
            )
        }
        predictiveBackComposable<Screen.AddPayment>(
            navController = navController,
            currentEntryProvider = { currentBackStackEntry }
        ) { entry ->
            val args = entry.toRoute<Screen.AddPayment>()
            AddPaymentScreen(
                subscriptionId = args.subscriptionId,
                paymentId = args.paymentId,
                onNavigateBack = navigationActions.navigateBack
            )
        }
        predictiveBackComposable<Screen.CategoryManagement>(
            navController = navController,
            currentEntryProvider = { currentBackStackEntry }
        ) {
            CategoryManagementScreen(
                onNavigateBack = navigationActions.navigateBack,
                onAddCategory = { navigationActions.navigateToCategoryDetail(-1L) },
                onEditCategory = navigationActions.navigateToCategoryDetail
            )
        }
        predictiveBackComposable<Screen.CategoryDetail>(
            navController = navController,
            currentEntryProvider = { currentBackStackEntry }
        ) { entry ->
            val args = entry.toRoute<Screen.CategoryDetail>()
            CategoryDetailScreen(
                categoryId = args.categoryId,
                onNavigateBack = navigationActions.navigateBack
            )
        }
        predictiveBackComposable<Screen.CurrencySettings>(
            navController = navController,
            currentEntryProvider = { currentBackStackEntry }
        ) {
            CurrencySettingsScreen(onNavigateBack = navigationActions.navigateBack)
        }
        predictiveBackComposable<Screen.LanguageSettings>(
            navController = navController,
            currentEntryProvider = { currentBackStackEntry }
        ) {
            LanguageSettingsScreen(onNavigateBack = navigationActions.navigateBack)
        }
        predictiveBackComposable<Screen.NotificationSettings>(
            navController = navController,
            currentEntryProvider = { currentBackStackEntry }
        ) {
            NotificationSettingsScreen(onNavigateBack = navigationActions.navigateBack)
        }
        predictiveBackComposable<Screen.DataManagement>(
            navController = navController,
            currentEntryProvider = { currentBackStackEntry }
        ) {
            DataManagementScreen(
                onNavigateBack = navigationActions.navigateBack,
                themeManager = themeManager
            )
        }
        predictiveBackComposable<Screen.AboutApp>(
            navController = navController,
            currentEntryProvider = { currentBackStackEntry }
        ) {
            AboutAppScreen(onNavigateBack = navigationActions.navigateBack)
        }
    }
}