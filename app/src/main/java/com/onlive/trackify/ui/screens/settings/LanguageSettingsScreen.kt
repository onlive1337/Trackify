package com.onlive.trackify.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.onlive.trackify.R
import com.onlive.trackify.ui.components.TrackifyOutlinedCard
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.utils.LocaleHelper
import com.onlive.trackify.utils.LocalLocaleManager
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.utils.stringResource

@Composable
fun LanguageSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val preferenceManager = remember { PreferenceManager(context) }
    val localeManager = LocalLocaleManager.current

    var selectedLanguageCode by remember {
        mutableStateOf(preferenceManager.getLanguageCode())
    }

    val availableLanguages = remember {
        LocaleHelper.getAvailableLanguages()
    }

    Scaffold(
        topBar = {
            TrackifyTopAppBar(
                title = stringResource(R.string.language_settings),
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {

            TrackifyOutlinedCard {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = stringResource(R.string.language_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.choose_language),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn {
                items(availableLanguages) { language ->
                    LanguageItem(
                        languageName = language.name,
                        selected = language.code == selectedLanguageCode,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            selectedLanguageCode = language.code
                            preferenceManager.setLanguageCode(language.code)
                            localeManager.setLocale(language.code)
                        },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageItem(
    languageName: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TrackifyOutlinedCard(
        modifier = modifier
            .padding(vertical = 4.dp),
        onClick = onClick,
        backgroundColor = if (selected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else
            MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = languageName,
                style = MaterialTheme.typography.titleMedium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            RadioButton(
                selected = selected,
                onClick = null // Handled by Card click
            )
        }
    }
}