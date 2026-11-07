package com.onlive.trackify.ui.screens.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.onlive.trackify.R
import com.onlive.trackify.utils.LocaleManager
import com.onlive.trackify.utils.NotificationHelper
import com.onlive.trackify.utils.NotificationScheduler
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.utils.stringResource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

private val OnboardingPredictiveBackEasing = CubicBezierEasing(0.1f, 0.1f, 0.0f, 1.0f)
private const val ONBOARDING_PREDICTIVE_DURATION_MS = 300

private val OnboardingPredictiveFadeOutEasing = Easing { t ->
    if (t <= 0.35f) {
        0f
    } else {
        LinearOutSlowInEasing.transform(((t - 0.35f) / 0.65f).coerceIn(0f, 1f))
    }
}

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    localeManager: LocaleManager
) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val notificationScheduler = remember { NotificationScheduler(context) }

    val enableNotifications = {
        preferenceManager.setNotificationsEnabled(true)
        notificationScheduler.scheduleNotifications()
    }

    var currentStep by rememberSaveable { mutableIntStateOf(0) }
    var selectedLanguage by rememberSaveable { mutableStateOf(preferenceManager.getLanguageCode()) }
    var selectedCurrency by rememberSaveable { mutableStateOf(preferenceManager.getCurrencyCode()) }

    val transitionState = remember { SeekableTransitionState(currentStep) }
    var isInPredictiveBack by remember { mutableStateOf(false) }
    var swipeEdge by remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }

    LaunchedEffect(currentStep) {
        if (!isInPredictiveBack && (transitionState.targetState != currentStep || transitionState.currentState != currentStep)) {
            transitionState.animateTo(currentStep)
        }
    }

    PredictiveBackHandler(enabled = currentStep > 0) { progress ->
        val targetStep = (currentStep - 1).coerceAtLeast(0)
        isInPredictiveBack = true
        try {
            progress.collect { backEvent ->
                swipeEdge = backEvent.swipeEdge
                transitionState.seekTo(
                    fraction = backEvent.progress,
                    targetState = targetStep
                )
            }
            isInPredictiveBack = false
            currentStep = targetStep
            transitionState.animateTo(targetStep)
        } catch (_: CancellationException) {
            isInPredictiveBack = false
            withContext(NonCancellable) {
                transitionState.animateTo(currentStep)
            }
        }
    }

    val completeOnboarding = {
        finishOnboarding(preferenceManager, selectedLanguage, selectedCurrency, onComplete)
        NotificationHelper(context).createNotificationChannel()
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            enableNotifications()
        }
        completeOnboarding()
    }

    val showBottomContinue = currentStep == 1 || currentStep == 2
    val continueEnabled = when (currentStep) {
        1 -> selectedLanguage.isNotBlank()
        2 -> selectedCurrency.isNotBlank()
        else -> false
    }
    val onContinueClick: (() -> Unit)? = when (currentStep) {
        1 -> ({ currentStep = 2 })
        2 -> ({ currentStep = 3 })
        else -> null
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (showBottomContinue) 80.dp + 24.dp else 0.dp)
            ) {
                OnboardingProgressIndicator(
                    currentStep = currentStep,
                    onStepClick = { step -> currentStep = step },
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 3.dp)
                )

                val stepEffectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
                val stepSpatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
                val stepTransition = rememberTransition(transitionState, label = "onboarding_step")

                stepTransition.AnimatedContent(
                    transitionSpec = {
                        if (targetState < initialState && isInPredictiveBack) {
                            val origin = when (swipeEdge) {
                                BackEventCompat.EDGE_LEFT -> TransformOrigin(0.9f, 0.5f)
                                BackEventCompat.EDGE_RIGHT -> TransformOrigin(0.1f, 0.5f)
                                else -> TransformOrigin.Center
                            }
                            ContentTransform(
                                targetContentEnter = fadeIn(
                                    animationSpec = tween(
                                        durationMillis = ONBOARDING_PREDICTIVE_DURATION_MS,
                                        easing = OnboardingPredictiveBackEasing
                                    )
                                ),
                                initialContentExit = scaleOut(
                                    targetScale = 0.90f,
                                    transformOrigin = origin,
                                    animationSpec = tween(
                                        durationMillis = ONBOARDING_PREDICTIVE_DURATION_MS,
                                        easing = OnboardingPredictiveBackEasing
                                    )
                                ) + fadeOut(
                                    animationSpec = tween(
                                        durationMillis = ONBOARDING_PREDICTIVE_DURATION_MS,
                                        easing = OnboardingPredictiveFadeOutEasing
                                    )
                                ),
                                targetContentZIndex = -1f,
                                sizeTransform = SizeTransform(clip = false)
                            )
                        } else {
                            (fadeIn(
                                animationSpec = stepEffectsSpec
                            ) + scaleIn(
                                initialScale = 0.95f,
                                animationSpec = stepSpatialSpec
                            )) togetherWith fadeOut(
                                animationSpec = stepEffectsSpec
                            ) using SizeTransform(clip = false)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .clipToBounds()
                ) { step ->
                    when (step) {
                        0 -> WelcomeStep(
                            onNext = { currentStep = 1 }
                        )

                        1 -> LanguageSelectionStep(
                            selectedLanguage = selectedLanguage,
                            onLanguageSelected = { languageCode ->
                                selectedLanguage = languageCode
                                preferenceManager.setLanguageCode(languageCode)
                                localeManager.setLocale(languageCode)
                            }
                        )

                        2 -> CurrencySelectionStep(
                            selectedCurrency = selectedCurrency,
                            onCurrencySelected = { currencyCode ->
                                selectedCurrency = currencyCode
                                preferenceManager.setCurrencyCode(currencyCode)
                            }
                        )

                        3 -> NotificationPermissionStep(
                            onGrantPermission = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        enableNotifications()
                                        completeOnboarding()
                                    } else {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                } else {
                                    // Pre-Android 13: no runtime permission needed,
                                    // the user agreed, so enable reminders directly.
                                    enableNotifications()
                                    completeOnboarding()
                                }
                            },
                            onSkip = {
                                completeOnboarding()
                            }
                        )
                    }
                }
            }

            if (showBottomContinue && onContinueClick != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OnboardingButton(
                        text = stringResource(R.string.continue_text),
                        onClick = onContinueClick,
                        enabled = continueEnabled,
                        icon = Icons.AutoMirrored.Rounded.ArrowForward
                    )
                }
            }
        }
    }
}

private fun finishOnboarding(
    preferenceManager: PreferenceManager,
    selectedLanguage: String,
    selectedCurrency: String,
    onComplete: () -> Unit
) {
    if (selectedLanguage.isNotEmpty()) {
        preferenceManager.setLanguageCode(selectedLanguage)
    }
    preferenceManager.setCurrencyCode(selectedCurrency)
    preferenceManager.setOnboardingCompleted(true)
    onComplete()
}
