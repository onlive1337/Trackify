package com.onlive.trackify.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.onlive.trackify.R
import com.onlive.trackify.ui.components.TrackifyCard
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.utils.LocaleHelper
import com.onlive.trackify.utils.PreferenceManager

@Composable
fun LanguageSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }

    var selectedLanguageCode by remember {
        mutableStateOf(preferenceManager.getLanguageCode())
    }

    var showRestartDialog by remember { mutableStateOf(false) }

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
            Spacer(modifier = Modifier.height(16.dp))

            TrackifyCard {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = stringResource(R.string.language_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.choose_language),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(LocaleHelper.getAvailableLanguages()) { language ->
                    LanguageItem(
                        languageName = language.name,
                        selected = language.code == selectedLanguageCode,
                        onClick = {
                            if (language.code != selectedLanguageCode) {
                                selectedLanguageCode = language.code
                                preferenceManager.setLanguageCode(language.code)
                                showRestartDialog = true
                            }
                        }
                    )
                }
            }
        }
    }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text(stringResource(R.string.language_changed)) },
            text = {
                Text(stringResource(R.string.language_changed))
            },
            confirmButton = {
                Button(onClick = {
                    showRestartDialog = false
                    onNavigateBack()
                }) {
                    Text(stringResource(R.string.done))
                }
            }
        )
    }
}

@Composable
private fun LanguageItem(
    languageName: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = languageName,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            if (selected) {
                RadioButton(
                    selected = true,
                    onClick = null
                )
            }
        }
    }
}