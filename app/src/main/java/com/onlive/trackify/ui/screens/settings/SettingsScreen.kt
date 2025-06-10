package com.onlive.trackify.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.onlive.trackify.R
import com.onlive.trackify.ui.components.TrackifyOutlinedCard
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.utils.NotificationScheduler
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.utils.ThemeManager
import com.onlive.trackify.utils.stringResource
import kotlinx.coroutines.launch

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
    val notificationScheduler = remember { NotificationScheduler(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val checkNotificationPermission = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    var notificationsEnabled by remember {
        mutableStateOf(preferenceManager.areNotificationsEnabled() && checkNotificationPermission())
    }

    var selectedThemeMode by remember {
        mutableIntStateOf(themeManager.getThemeMode())
    }

    var waitingForPermission by remember { mutableStateOf(false) }

    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (waitingForPermission) {
            waitingForPermission = false

            val permissionGranted = checkNotificationPermission()

            if (permissionGranted) {
                notificationsEnabled = true
                preferenceManager.setNotificationsEnabled(true)
                notificationScheduler.scheduleNotifications()
            } else {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message = context.getString(R.string.notification_permission_denied),
                        duration = SnackbarDuration.Long
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TrackifyTopAppBar(
                title = stringResource(R.string.title_settings)
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
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

            TrackifyOutlinedCard(
                title = stringResource(R.string.theme_settings)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
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

            TrackifyOutlinedCard {
                Column {
                    NotificationToggleRow(
                        enabled = notificationsEnabled,
                        onToggle = { enabled ->
                            if (enabled) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    val permissionGranted = checkNotificationPermission()

                                    if (permissionGranted) {
                                        notificationsEnabled = true
                                        preferenceManager.setNotificationsEnabled(true)
                                        notificationScheduler.scheduleNotifications()
                                    } else {
                                        waitingForPermission = true
                                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                        }
                                        settingsLauncher.launch(intent)
                                    }
                                } else {
                                    notificationsEnabled = true
                                    preferenceManager.setNotificationsEnabled(true)
                                    notificationScheduler.scheduleNotifications()
                                }
                            } else {
                                notificationsEnabled = false
                                preferenceManager.setNotificationsEnabled(false)
                                notificationScheduler.cancelNotifications()
                            }
                        }
                    )

                    if (notificationsEnabled) {
                        SettingsDivider()

                        SettingsItem(
                            icon = Icons.Outlined.Notifications,
                            title = stringResource(R.string.notification_settings),
                            onClick = onNavigateToNotificationSettings
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TrackifyOutlinedCard(
                title = stringResource(R.string.general_settings)
            ) {
                Column {
                    SettingsItem(
                        icon = Icons.Outlined.Category,
                        title = stringResource(R.string.manage_categories),
                        onClick = onNavigateToCategoryManagement
                    )

                    SettingsDivider()

                    SettingsItem(
                        icon = Icons.Outlined.Payments,
                        title = stringResource(R.string.currency_settings),
                        onClick = onNavigateToCurrencySettings
                    )

                    SettingsDivider()

                    SettingsItem(
                        icon = Icons.Outlined.Language,
                        title = stringResource(R.string.language_settings),
                        onClick = onNavigateToLanguageSettings
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TrackifyOutlinedCard {
                SettingsItem(
                    icon = Icons.Outlined.DataObject,
                    title = stringResource(R.string.data_management),
                    onClick = onNavigateToDataManagement
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TrackifyOutlinedCard {
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
private fun ThemeOption(
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
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
private fun NotificationToggleRow(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.notifications),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun SettingsItem(
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
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    )
}