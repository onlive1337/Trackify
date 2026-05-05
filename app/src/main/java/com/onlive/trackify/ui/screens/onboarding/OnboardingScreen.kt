package com.onlive.trackify.ui.screens.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Currency
import com.onlive.trackify.utils.LocaleHelper
import com.onlive.trackify.utils.LocaleManager
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.utils.stringResource
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    localeManager: LocaleManager
) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }

    var currentStep by remember { mutableIntStateOf(0) }
    var selectedLanguage by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("USD") }

    LaunchedEffect(currentStep) {
        if (currentStep == 1 && selectedLanguage.isBlank()) {
            val deviceLang = Locale.getDefault().language.takeIf { it.isNotBlank() }
            val defaultLang = deviceLang ?: "en"
            selectedLanguage = defaultLang
            localeManager.setLocale(defaultLang)
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        finishOnboarding(preferenceManager, selectedLanguage, selectedCurrency, onComplete)
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
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(bottom = if (showBottomContinue) 80.dp + 24.dp else 0.dp)
            ) {
                OnboardingProgressIndicator(
                    currentStep = currentStep,
                    onStepClick = { step -> currentStep = step },
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
                )

                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        (fadeIn(
                            animationSpec = tween(400)
                        ) + androidx.compose.animation.scaleIn(
                            initialScale = 0.95f,
                            animationSpec = tween(400)
                        )) togetherWith androidx.compose.animation.fadeOut(
                            animationSpec = tween(400)
                        ) using androidx.compose.animation.SizeTransform(clip = false)
                    },
                    label = "onboarding_step",
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
                                localeManager.setLocale(languageCode)
                            }
                        )

                        2 -> CurrencySelectionStep(
                            selectedCurrency = selectedCurrency,
                            onCurrencySelected = { selectedCurrency = it }
                        )

                        3 -> NotificationPermissionStep(
                            onGrantPermission = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.POST_NOTIFICATIONS
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        finishOnboarding(preferenceManager, selectedLanguage, selectedCurrency, onComplete)
                                    } else {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                } else {
                                    finishOnboarding(preferenceManager, selectedLanguage, selectedCurrency, onComplete)
                                }
                            },
                            onSkip = {
                                finishOnboarding(preferenceManager, selectedLanguage, selectedCurrency, onComplete)
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
                        .padding(bottom = 32.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OnboardingButton(
                        text = stringResource(R.string.continue_text),
                        onClick = onContinueClick,
                        enabled = continueEnabled,
                        icon = Icons.AutoMirrored.Filled.ArrowForward
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingProgressIndicator(
    currentStep: Int,
    onStepClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSteps = 4

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(totalSteps) { index ->
            val isActive = index <= currentStep
            val animatedWeight by animateFloatAsState(
                targetValue = if (isActive) 1.5f else 1f,
                animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
                label = "progress_weight"
            )

            Box(
                modifier = Modifier
                    .weight(animatedWeight)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable(
                        enabled = index < currentStep,
                        onClick = { onStepClick(index) }
                    )
            )
        }
    }
}

@Composable
private fun WelcomeStep(
    onNext: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(800)) + androidx.compose.animation.slideInVertically(
            animationSpec = spring(stiffness = Spring.StiffnessLow),
            initialOffsetY = { fullHeight -> fullHeight / 3 }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val scale by animateFloatAsState(
                targetValue = if (isVisible) 1f else 0.7f,
                animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
                label = "logo_scale"
            )

            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale)
                    .clip(MaterialTheme.shapes.large)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                    contentDescription = "Trackify Logo",
                    modifier = Modifier.size(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = stringResource(R.string.welcome_to_trackify),
                style = MaterialTheme.typography.headlineLargeEmphasized,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.welcome_description),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )

            Spacer(modifier = Modifier.height(56.dp))

            OnboardingButton(
                text = stringResource(R.string.get_started),
                onClick = onNext,
                icon = Icons.AutoMirrored.Filled.ArrowForward
            )
        }
    }
}

@Composable
private fun LanguageSelectionStep(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    val availableLanguages = remember { LocaleHelper.getAvailableLanguages() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp)
    ) {
        item {
            OnboardingStepHeader(
                icon = Icons.Default.Language,
                title = stringResource(R.string.choose_your_language),
                description = stringResource(R.string.language_onboarding_description)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        items(availableLanguages) { language ->
            LanguageOption(
                language = language,
                selected = language.code == selectedLanguage,
                onClick = { onLanguageSelected(language.code) }
            )
        }
    }
}

@Composable
private fun CurrencySelectionStep(
    selectedCurrency: String,
    onCurrencySelected: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp)
    ) {
        item {
            OnboardingStepHeader(
                icon = Icons.Default.AttachMoney,
                title = stringResource(R.string.choose_your_currency),
                description = stringResource(R.string.currency_onboarding_description)
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        items(Currency.POPULAR_CURRENCIES) { currency ->
            CurrencyOption(
                currency = currency,
                selected = currency.code == selectedCurrency,
                onClick = { onCurrencySelected(currency.code) }
            )
        }
    }
}

@Composable
private fun NotificationPermissionStep(
    onGrantPermission: () -> Unit,
    onSkip: () -> Unit
) {
    val context = LocalContext.current
    val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        OnboardingStepHeader(
            icon = Icons.Default.Notifications,
            title = stringResource(R.string.stay_notified),
            description = stringResource(R.string.notification_onboarding_description)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            NotificationBenefit(
                icon = Icons.Default.Schedule,
                title = stringResource(R.string.timely_reminders),
                description = stringResource(R.string.timely_reminders_description)
            )

            NotificationBenefit(
                icon = Icons.Default.MonetizationOn,
                title = stringResource(R.string.never_miss_payment),
                description = stringResource(R.string.never_miss_payment_description)
            )

            NotificationBenefit(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                title = stringResource(R.string.expense_tracking),
                description = stringResource(R.string.expense_tracking_description)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(bottom = 24.dp, top = 32.dp)
        ) {
            if (hasNotificationPermission) {
                OnboardingButton(
                    text = stringResource(R.string.notifications_already_enabled),
                    onClick = onGrantPermission,
                    icon = Icons.Default.Check,
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            } else {
                OnboardingButton(
                    text = stringResource(R.string.enable_notifications),
                    onClick = onGrantPermission,
                    icon = Icons.Default.Notifications
                )

                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.skip_for_now),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingStepHeader(
    icon: ImageVector,
    title: String,
    description: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMediumEmphasized,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
        )
    }
}

@Composable
private fun LanguageOption(
    language: LocaleHelper.Language,
    selected: Boolean,
    onClick: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (selected) 1.03f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "language_scale"
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 8.dp else 1.dp
        ),
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.name,
                style = if (selected) MaterialTheme.typography.titleMediumEmphasized else MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
                color = if (selected)
                    MaterialTheme.colorScheme.onPrimaryContainer
                else
                    MaterialTheme.colorScheme.onSurface
            )

            if (selected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun CurrencyOption(
    currency: Currency,
    selected: Boolean,
    onClick: () -> Unit
) {
    val animatedScale by animateFloatAsState(
        targetValue = if (selected) 1.03f else 1f,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "currency_scale"
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (selected) 8.dp else 1.dp
        ),
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currency.symbol,
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currency.name,
                    style = if (selected) MaterialTheme.typography.titleMediumEmphasized else MaterialTheme.typography.titleMedium,
                    color = if (selected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = currency.code,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (selected)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (selected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun NotificationBenefit(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
        }
    }
}

@Composable
private fun OnboardingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor
        ),
        shape = MaterialTheme.shapes.large,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 12.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMediumEmphasized
            )

            if (icon != null) {
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
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
