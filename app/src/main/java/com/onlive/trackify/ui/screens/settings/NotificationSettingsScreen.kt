package com.onlive.trackify.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.onlive.trackify.utils.NotificationScheduler
import com.onlive.trackify.utils.PreferenceManager
import java.util.Locale

@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val notificationScheduler = remember { NotificationScheduler(context) }

    var notificationsEnabled by remember {
        mutableStateOf(preferenceManager.areNotificationsEnabled())
    }

    var reminderDays by remember {
        mutableStateOf(preferenceManager.getReminderDays())
    }

    val (hour, minute) = preferenceManager.getNotificationTime()
    var notificationHour by remember { mutableIntStateOf(hour) }
    var notificationMinute by remember { mutableIntStateOf(minute) }
    var notificationTime by remember {
        mutableStateOf(formatTime(hour, minute))
    }

    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            notificationScheduler.scheduleNotifications()
        } else {
            showPermissionDialog = true
        }
    }

    LaunchedEffect(notificationsEnabled, reminderDays, notificationHour, notificationMinute) {
        if (notificationsEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                notificationScheduler.scheduleNotifications()
            }
        } else {
            notificationScheduler.cancelNotifications()
        }
    }

    Scaffold(
        topBar = {
            TrackifyTopAppBar(
                title = stringResource(R.string.notification_settings),
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
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            TrackifyCard(
                title = stringResource(R.string.notifications)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.enable_notifications),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = stringResource(R.string.notification_description),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = {
                            notificationsEnabled = it
                            preferenceManager.setNotificationsEnabled(it)
                        }
                    )
                }
            }

            if (notificationsEnabled) {
                Spacer(modifier = Modifier.height(16.dp))

                TrackifyCard(
                    title = stringResource(R.string.notification_time)
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(
                            text = stringResource(R.string.notification_time_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showTimePickerDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = notificationTime)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TrackifyCard(
                    title = stringResource(R.string.reminder_days)
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(
                            text = stringResource(R.string.reminder_days_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        ReminderDayOption(
                            title = stringResource(R.string.on_payment_day),
                            description = stringResource(R.string.notify_on_payment_day),
                            selected = reminderDays.contains(0),
                            onClick = {
                                reminderDays = if (reminderDays.contains(0)) {
                                    reminderDays - 0
                                } else {
                                    reminderDays + 0
                                }
                                preferenceManager.setReminderDays(reminderDays)
                            }
                        )

                        ReminderDayOption(
                            title = stringResource(R.string.one_day_before),
                            description = stringResource(R.string.notify_one_day_before),
                            selected = reminderDays.contains(1),
                            onClick = {
                                reminderDays = if (reminderDays.contains(1)) {
                                    reminderDays - 1
                                } else {
                                    reminderDays + 1
                                }
                                preferenceManager.setReminderDays(reminderDays)
                            }
                        )

                        ReminderDayOption(
                            title = stringResource(R.string.three_days_before),
                            description = stringResource(R.string.notify_three_days_before),
                            selected = reminderDays.contains(3),
                            onClick = {
                                reminderDays = if (reminderDays.contains(3)) {
                                    reminderDays - 3
                                } else {
                                    reminderDays + 3
                                }
                                preferenceManager.setReminderDays(reminderDays)
                            }
                        )

                        ReminderDayOption(
                            title = stringResource(R.string.week_before),
                            description = stringResource(R.string.notify_week_before),
                            selected = reminderDays.contains(7),
                            onClick = {
                                reminderDays = if (reminderDays.contains(7)) {
                                    reminderDays - 7
                                } else {
                                    reminderDays + 7
                                }
                                preferenceManager.setReminderDays(reminderDays)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showTimePickerDialog) {
        TimePickerDialog(
            initialHour = notificationHour,
            initialMinute = notificationMinute,
            onDismiss = { showTimePickerDialog = false },
            onConfirm = { selectedHour, selectedMinute ->
                notificationHour = selectedHour
                notificationMinute = selectedMinute
                notificationTime = formatTime(selectedHour, selectedMinute)
                preferenceManager.setNotificationTime(selectedHour, selectedMinute)
                notificationScheduler.scheduleNotifications()
                showTimePickerDialog = false
            }
        )
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text(stringResource(R.string.permission_required)) },
            text = { Text(stringResource(R.string.notification_permission_required)) },
            confirmButton = {
                Button(onClick = {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    context.startActivity(intent)
                    showPermissionDialog = false
                }) {
                    Text(stringResource(R.string.open_settings))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var hour by remember { mutableIntStateOf(initialHour) }
    var minute by remember { mutableIntStateOf(initialMinute) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_time)) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NumberPicker(
                    value = hour,
                    range = 0..23,
                    onValueChange = { hour = it }
                )

                Text(
                    text = ":",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                NumberPicker(
                    value = minute,
                    range = 0..59,
                    onValueChange = { minute = it }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(hour, minute) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun NumberPicker(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                val newValue = if (value + 1 > range.last) range.first else value + 1
                onValueChange(newValue)
            },
            modifier = Modifier.size(40.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("▲")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = String.format(Locale.getDefault(), "%02d", value),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val newValue = if (value - 1 < range.first) range.last else value - 1
                onValueChange(newValue)
            },
            modifier = Modifier.size(40.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text("▼")
        }
    }
}

@Composable
private fun ReminderDayOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = selected,
            onCheckedChange = { onClick() }
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
}