package com.onlive.trackify.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.onlive.trackify.R
import com.onlive.trackify.ui.components.TrackifyCard
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.utils.NotificationScheduler
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.utils.ThemeManager

@Composable
fun ThemeOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector
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

        RadioButton(
            selected = selected,
            onClick = onClick
        )
    }
}

@Composable
fun SettingsScreen(
    onNavigateToCategoryManagement: () -> Unit,
    onNavigateToNotificationSettings: () -> Unit,
    onNavigateToCurrencySettings: () -> Unit,
    onNavigateToLanguageSettings: () -> Unit,
    onNavigateToDataManagement: () -> Unit,
    onNavigateToAboutApp: () -> Unit,
    themeManager: ThemeManager
) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }

    var notificationsEnabled by remember {
        mutableStateOf(preferenceManager.areNotificationsEnabled())
    }
    var selectedThemeMode by remember {
        mutableIntStateOf(themeManager.getThemeMode())
    }

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
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            TrackifyCard(
                title = stringResource(R.string.theme_settings)
            ) {
                Column {
                    ThemeOption(
                        title = stringResource(R.string.system_theme),
                        selected = selectedThemeMode == ThemeManager.MODE_SYSTEM,
                        onClick = {
                            selectedThemeMode = ThemeManager.MODE_SYSTEM
                            themeManager.setThemeMode(ThemeManager.MODE_SYSTEM)
                        },
                        icon = Icons.Rounded.PhoneAndroid
                    )

                    ThemeOption(
                        title = stringResource(R.string.light_theme),
                        selected = selectedThemeMode == ThemeManager.MODE_LIGHT,
                        onClick = {
                            selectedThemeMode = ThemeManager.MODE_LIGHT
                            themeManager.setThemeMode(ThemeManager.MODE_LIGHT)
                        },
                        icon = Icons.Rounded.LightMode
                    )

                    ThemeOption(
                        title = stringResource(R.string.dark_theme),
                        selected = selectedThemeMode == ThemeManager.MODE_DARK,
                        onClick = {
                            selectedThemeMode = ThemeManager.MODE_DARK
                            themeManager.setThemeMode(ThemeManager.MODE_DARK)
                        },
                        icon = Icons.Rounded.DarkMode
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TrackifyCard {
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

                        val notificationScheduler = remember { NotificationScheduler(context) }
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = {
                                notificationsEnabled = it
                                preferenceManager.setNotificationsEnabled(it)
                                if (it) {
                                    notificationScheduler.rescheduleNotifications()

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        if (ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.POST_NOTIFICATIONS
                                            ) != PackageManager.PERMISSION_GRANTED) {

                                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                            }
                                            context.startActivity(intent)
                                        }
                                    }
                                } else {
                                    notificationScheduler.cancelAllReminders()
                                }
                            }
                        )
                    }

                    if (notificationsEnabled) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

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
                title = stringResource(R.string.general_settings)
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Outlined.Category,
                        title = stringResource(R.string.manage_categories),
                        onClick = onNavigateToCategoryManagement
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    SettingsItem(
                        icon = Icons.Outlined.Payments,
                        title = stringResource(R.string.currency_settings),
                        onClick = onNavigateToCurrencySettings
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    SettingsItem(
                        icon = Icons.Outlined.Language,
                        title = stringResource(R.string.language_settings),
                        onClick = onNavigateToLanguageSettings
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TrackifyCard {
                SettingsItem(
                    icon = Icons.Outlined.DataObject,
                    title = stringResource(R.string.data_management),
                    onClick = onNavigateToDataManagement
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TrackifyCard {
                SettingsItem(
                    icon = Icons.Outlined.Info,
                    title = stringResource(R.string.about_app),
                    onClick = onNavigateToAboutApp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
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
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}