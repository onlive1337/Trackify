package com.onlive.trackify.ui.screens.settings

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.onlive.trackify.R
import com.onlive.trackify.ui.components.SegmentedSettingsSection
import com.onlive.trackify.ui.components.connectedLeadingToggleShapes
import com.onlive.trackify.ui.components.connectedTrailingToggleShapes
import com.onlive.trackify.ui.components.segmentedListItemShapes
import com.onlive.trackify.utils.LocalLocalizedContext
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
    val context = LocalLocalizedContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val preferenceManager = remember { PreferenceManager(context) }
    val notificationScheduler = remember { NotificationScheduler(context) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val checkNotificationPermission = {
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
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
                notificationScheduler.triggerImmediateCheck()
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

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            notificationsEnabled = true
            preferenceManager.setNotificationsEnabled(true)
            notificationScheduler.scheduleNotifications()
            notificationScheduler.triggerImmediateCheck()
        } else {
            coroutineScope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = context.getString(R.string.notification_permission_denied),
                    actionLabel = context.getString(R.string.title_settings),
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    waitingForPermissionState.value = true
                    val notificationIntent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    try {
                        settingsLauncher.launch(notificationIntent)
                    } catch (_: ActivityNotFoundException) {
                        val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        runCatching { settingsLauncher.launch(fallbackIntent) }
                            .onFailure { waitingForPermissionState.value = false }
                    }
                }
            }
        }
    }

    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val themeSectionCount = if (supportsDynamicColor) 2 else 1
    val notificationSectionCount = if (notificationsEnabled) 2 else 1

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
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

            SegmentedSettingsSection(title = stringResource(R.string.theme_settings)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = ListItemDefaults.segmentedShapes(
                        index = 0,
                        count = themeSectionCount
                    ).shape,
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
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
                            shapes = connectedLeadingToggleShapes(),
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
                            shapes = connectedTrailingToggleShapes(),
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
                }

                if (supportsDynamicColor) {
                    SettingsToggleRow(
                        icon = Icons.Outlined.Palette,
                        title = stringResource(R.string.dynamic_colors),
                        description = stringResource(R.string.dynamic_colors_description),
                        checked = dynamicColorEnabled,
                        index = 1,
                        count = themeSectionCount,
                        onToggle = {
                            dynamicColorEnabled = it
                            themeManager.setDynamicColorEnabled(it)
                        }
                    )
                }
            }

            SegmentedSettingsSection {
                SettingsToggleRow(
                    icon = Icons.Outlined.Notifications,
                    title = stringResource(R.string.notifications),
                    checked = notificationsEnabled,
                    index = 0,
                    count = notificationSectionCount,
                    onToggle = { enabled ->
                        if (enabled) {
                            if (checkNotificationPermission()) {
                                notificationsEnabled = true
                                preferenceManager.setNotificationsEnabled(true)
                                notificationScheduler.scheduleNotifications()
                                notificationScheduler.triggerImmediateCheck()
                            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
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
                        icon = Icons.Outlined.Tune,
                        title = stringResource(R.string.notification_settings),
                        index = 1,
                        count = notificationSectionCount,
                        onClick = onNavigateToNotificationSettings
                    )
                }
            }

            SegmentedSettingsSection(title = stringResource(R.string.general_settings)) {
                SettingsRow(
                    icon = Icons.Outlined.Category,
                    title = stringResource(R.string.manage_categories),
                    index = 0,
                    count = 5,
                    onClick = onNavigateToCategoryManagement
                )
                SettingsRow(
                    icon = Icons.Outlined.Payments,
                    title = stringResource(R.string.currency_settings),
                    index = 1,
                    count = 5,
                    onClick = onNavigateToCurrencySettings
                )
                SettingsRow(
                    icon = Icons.Outlined.Language,
                    title = stringResource(R.string.language_settings),
                    index = 2,
                    count = 5,
                    onClick = onNavigateToLanguageSettings
                )
                SettingsRow(
                    icon = Icons.Outlined.DataObject,
                    title = stringResource(R.string.data_management),
                    index = 3,
                    count = 5,
                    onClick = onNavigateToDataManagement
                )
                SettingsRow(
                    icon = Icons.Outlined.Info,
                    title = stringResource(R.string.about_app),
                    index = 4,
                    count = 5,
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
    index: Int,
    count: Int,
    onToggle: (Boolean) -> Unit,
    description: String? = null
) {
    val haptic = LocalHapticFeedback.current
    SegmentedListItem(
        checked = checked,
        onCheckedChange = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onToggle(it)
        },
        shapes = segmentedListItemShapes(index = index, count = count),
        colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            leadingContentColor = MaterialTheme.colorScheme.primary,
            selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            selectedContentColor = MaterialTheme.colorScheme.onSurface,
            selectedLeadingContentColor = MaterialTheme.colorScheme.primary,
            selectedSupportingContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedTrailingContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        supportingContent = description?.let { { Text(it) } },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = null
            )
        }
    ) {
        Text(title)
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    index: Int,
    count: Int,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    SegmentedListItem(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        shapes = segmentedListItemShapes(index = index, count = count),
        colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            leadingContentColor = MaterialTheme.colorScheme.primary
        ),
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    ) {
        Text(title)
    }
}