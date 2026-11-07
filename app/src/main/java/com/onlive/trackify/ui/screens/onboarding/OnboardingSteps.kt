@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.onlive.trackify.ui.screens.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.MonetizationOn
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Currency
import com.onlive.trackify.utils.LocaleHelper
import com.onlive.trackify.utils.stringResource
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

@Composable
internal fun WelcomeStep(
    onNext: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300.milliseconds)
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = MaterialTheme.motionScheme.slowEffectsSpec()) + slideInVertically(
            animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
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
                icon = Icons.AutoMirrored.Rounded.ArrowForward
            )
        }
    }
}

@Composable
internal fun LanguageSelectionStep(
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
                icon = Icons.Rounded.Language,
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
internal fun CurrencySelectionStep(
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
                icon = Icons.Rounded.AttachMoney,
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
internal fun NotificationPermissionStep(
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
            icon = Icons.Rounded.Notifications,
            title = stringResource(R.string.stay_notified),
            description = stringResource(R.string.notification_onboarding_description)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            NotificationBenefit(
                icon = Icons.Rounded.Schedule,
                title = stringResource(R.string.timely_reminders),
                description = stringResource(R.string.timely_reminders_description)
            )

            NotificationBenefit(
                icon = Icons.Rounded.MonetizationOn,
                title = stringResource(R.string.never_miss_payment),
                description = stringResource(R.string.never_miss_payment_description)
            )

            NotificationBenefit(
                icon = Icons.AutoMirrored.Rounded.TrendingUp,
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
                    icon = Icons.Rounded.Check,
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            } else {
                OnboardingButton(
                    text = stringResource(R.string.enable_notifications),
                    onClick = onGrantPermission,
                    icon = Icons.Rounded.Notifications
                )

                TextButton(
                    onClick = onSkip,
                    shapes = ButtonDefaults.shapes(),
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
