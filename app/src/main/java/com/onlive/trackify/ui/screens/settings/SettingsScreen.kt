package com.onlive.trackify.ui.screens.settings

import android.content.Intent
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import com.onlive.trackify.utils.LocalLocalizedContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.onlive.trackify.R
import com.onlive.trackify.ui.components.SettingsSection
import com.onlive.trackify.utils.NotificationScheduler
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.utils.ThemeManager
import com.onlive.trackify.utils.stringResource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
    val context = LocalLocalizedContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val preferenceManager = remember { PreferenceManager(context) }
    val notificationScheduler = remember { NotificationScheduler(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val checkNotificationPermission = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    var notificationsEnabled by remember {
        mutableStateOf(preferenceManager.areNotificationsEnabled() && checkNotificationPermission())
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val hasNotificationPermission = checkNotificationPermission()

                if (preferenceManager.areNotificationsEnabled() && !hasNotificationPermission) {
                    notificationsEnabled = false
                    preferenceManager.setNotificationsEnabled(false)
                    notificationScheduler.cancelNotifications()
                } else if (preferenceManager.areNotificationsEnabled() && hasNotificationPermission) {
                    notificationsEnabled = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var selectedThemeMode by remember {
        mutableIntStateOf(themeManager.getThemeMode())
    }

    var dynamicColorEnabled by remember {
        mutableStateOf(themeManager.isDynamicColorEnabled())
    }

    val waitingForPermissionState = remember { mutableStateOf(false) }

    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        if (waitingForPermissionState.value) {
            waitingForPermissionState.value = false

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
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            SettingsSection(title = stringResource(R.string.theme_settings)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                ) {
                    ToggleButton(
                        checked = selectedThemeMode == ThemeManager.MODE_LIGHT,
                        onCheckedChange = {
                            selectedThemeMode = ThemeManager.MODE_LIGHT
                            themeManager.setThemeMode(ThemeManager.MODE_LIGHT)
                        },
                        shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LightMode,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(ToggleButtonDefaults.IconSpacing))
                        Text(
                            text = stringResource(R.string.light_theme),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    ToggleButton(
                        checked = selectedThemeMode == ThemeManager.MODE_DARK,
                        onCheckedChange = {
                            selectedThemeMode = ThemeManager.MODE_DARK
                            themeManager.setThemeMode(ThemeManager.MODE_DARK)
                        },
                        shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DarkMode,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(ToggleButtonDefaults.IconSpacing))
                        Text(
                            text = stringResource(R.string.dark_theme),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    SettingsToggleRow(
                        icon = Icons.Outlined.Palette,
                        title = stringResource(R.string.dynamic_colors),
                        description = stringResource(R.string.dynamic_colors_description),
                        checked = dynamicColorEnabled,
                        onToggle = {
                            dynamicColorEnabled = it
                            themeManager.setDynamicColorEnabled(it)
                        }
                    )
                }
            }

            SettingsSection {
                SettingsToggleRow(
                    icon = Icons.Outlined.Notifications,
                    title = stringResource(R.string.notifications),
                    checked = notificationsEnabled,
                    onToggle = { enabled ->
                        if (enabled) {
                            if (checkNotificationPermission()) {
                                notificationsEnabled = true
                                preferenceManager.setNotificationsEnabled(true)
                                notificationScheduler.scheduleNotifications()
                            } else {
                                waitingForPermissionState.value = true
                                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                }
                                settingsLauncher.launch(intent)
                            }
                        } else {
                            notificationsEnabled = false
                            preferenceManager.setNotificationsEnabled(false)
                            notificationScheduler.cancelNotifications()
                        }
                    }
                )

                if (notificationsEnabled) {
                    SettingsRow(
                        icon = Icons.Outlined.Notifications,
                        title = stringResource(R.string.notification_settings),
                        onClick = onNavigateToNotificationSettings
                    )
                }
            }

            SettingsSection(title = stringResource(R.string.general_settings)) {
                SettingsRow(
                    icon = Icons.Outlined.Category,
                    title = stringResource(R.string.manage_categories),
                    onClick = onNavigateToCategoryManagement
                )
                SettingsRow(
                    icon = Icons.Outlined.Payments,
                    title = stringResource(R.string.currency_settings),
                    onClick = onNavigateToCurrencySettings
                )
                SettingsRow(
                    icon = Icons.Outlined.Language,
                    title = stringResource(R.string.language_settings),
                    onClick = onNavigateToLanguageSettings
                )
                SettingsRow(
                    icon = Icons.Outlined.DataObject,
                    title = stringResource(R.string.data_management),
                    onClick = onNavigateToDataManagement
                )
                SettingsRow(
                    icon = Icons.Outlined.Info,
                    title = stringResource(R.string.about_app),
                    onClick = onNavigateToAboutApp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    description: String? = null
) {
    val haptic = LocalHapticFeedback.current
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = description?.let { { Text(it) } },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggle(it)
                }
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        }
    )
}