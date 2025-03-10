package com.onlive.trackify.ui.screens.settings

import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.onlive.trackify.R
import com.onlive.trackify.ui.components.TrackifyCard
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.utils.ThemeManager

@Composable
fun SettingsScreen(
    onNavigateToCategoryManagement: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToCurrencySettings: () -> Unit,
    onNavigateToLanguageSettings: () -> Unit,
    onNavigateToDataManagement: () -> Unit,
    themeManager: ThemeManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var notificationsEnabled by remember { mutableStateOf(true) }
    var selectedThemeMode by remember { mutableStateOf(themeManager.getThemeMode()) }

    Scaffold(
        topBar = {
            TrackifyTopAppBar(
                title = stringResource(R.string.title_settings)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            TrackifyCard(
                title = stringResource(R.string.theme_settings),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Column {
                    ThemeOption(
                        title = stringResource(R.string.system_theme),
                        selected = selectedThemeMode == ThemeManager.MODE_SYSTEM,
                        onClick = {
                            selectedThemeMode = ThemeManager.MODE_SYSTEM
                            themeManager.setThemeMode(ThemeManager.MODE_SYSTEM)
                        }
                    )

                    ThemeOption(
                        title = stringResource(R.string.light_theme),
                        selected = selectedThemeMode == ThemeManager.MODE_LIGHT,
                        onClick = {
                            selectedThemeMode = ThemeManager.MODE_LIGHT
                            themeManager.setThemeMode(ThemeManager.MODE_LIGHT)
                        }
                    )

                    ThemeOption(
                        title = stringResource(R.string.dark_theme),
                        selected = selectedThemeMode == ThemeManager.MODE_DARK,
                        onClick = {
                            selectedThemeMode = ThemeManager.MODE_DARK
                            themeManager.setThemeMode(ThemeManager.MODE_DARK)
                        }
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Text(
                            text = stringResource(R.string.dynamic_colors_enabled),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.dynamic_colors_unavailable),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TrackifyCard(
                title = null,
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = stringResource(R.string.notifications),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )

                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                    }

                    if (notificationsEnabled) {
                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        SettingsItem(
                            icon = Icons.Outlined.Notifications,
                            title = stringResource(R.string.notification_settings),
                            onClick = onNavigateToNotificationSettings
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TrackifyCard(
                title = stringResource(R.string.general_settings),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Outlined.Category,
                        title = stringResource(R.string.manage_categories),
                        onClick = onNavigateToCategoryManagement
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    SettingsItem(
                        icon = Icons.Outlined.Payments,
                        title = stringResource(R.string.currency_settings),
                        onClick = onNavigateToCurrencySettings
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    SettingsItem(
                        icon = Icons.Outlined.Language,
                        title = stringResource(R.string.language_settings),
                        onClick = onNavigateToLanguageSettings
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TrackifyCard(
                title = null,
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                SettingsItem(
                    icon = Icons.Outlined.DataObject,
                    title = stringResource(R.string.data_management),
                    onClick = onNavigateToDataManagement
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TrackifyCard(
                title = stringResource(R.string.about_app),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.app_version),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.app_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ThemeOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Palette,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        RadioButton(
            selected = selected,
            onClick = onClick
        )
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}