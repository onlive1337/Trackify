package com.onlive.trackify.ui.screens.settings

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.onlive.trackify.R
import com.onlive.trackify.ui.components.TrackifyCard
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.utils.NotificationFrequency
import com.onlive.trackify.utils.NotificationScheduler
import com.onlive.trackify.utils.PreferenceManager
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    val notificationScheduler = remember { NotificationScheduler(context) }

    var notificationFrequency by remember {
        mutableStateOf(preferenceManager.getNotificationFrequency())
    }

    var reminderDays by remember {
        mutableStateOf(preferenceManager.getReminderDays())
    }

    val (hour, minute) = preferenceManager.getNotificationTime()
    var notificationHour by remember { mutableStateOf(hour) }
    var notificationMinute by remember { mutableStateOf(minute) }
    var notificationTime by remember {
        mutableStateOf(formatTime(hour, minute))
    }

    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            notificationScheduler.rescheduleNotifications()
        } else {
            showPermissionDialog = true
        }
    }

    LaunchedEffect(notificationFrequency, reminderDays, notificationHour, notificationMinute) {
        if (preferenceManager.areNotificationsEnabled() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
                title = stringResource(R.string.notification_time_title)
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
                        Text(notificationTime)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TrackifyCard(
                title = stringResource(R.string.notification_frequency_title)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = stringResource(R.string.notification_frequency_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column {
                        FrequencyOption(
                            title = stringResource(R.string.daily),
                            selected = notificationFrequency == NotificationFrequency.DAILY,
                            onClick = {
                                notificationFrequency = NotificationFrequency.DAILY
                                preferenceManager.setNotificationFrequency(NotificationFrequency.DAILY)
                                notificationScheduler.rescheduleNotifications()
                            }
                        )

                        FrequencyOption(
                            title = stringResource(R.string.weekly),
                            selected = notificationFrequency == NotificationFrequency.WEEKLY,
                            onClick = {
                                notificationFrequency = NotificationFrequency.WEEKLY
                                preferenceManager.setNotificationFrequency(NotificationFrequency.WEEKLY)
                                notificationScheduler.rescheduleNotifications()
                            }
                        )

                        FrequencyOption(
                            title = stringResource(R.string.monthly),
                            selected = notificationFrequency == NotificationFrequency.MONTHLY,
                            onClick = {
                                notificationFrequency = NotificationFrequency.MONTHLY
                                preferenceManager.setNotificationFrequency(NotificationFrequency.MONTHLY)
                                notificationScheduler.rescheduleNotifications()
                            }
                        )

                        FrequencyOption(
                            title = stringResource(R.string.custom),
                            selected = notificationFrequency == NotificationFrequency.CUSTOM,
                            onClick = {
                                notificationFrequency = NotificationFrequency.CUSTOM
                                preferenceManager.setNotificationFrequency(NotificationFrequency.CUSTOM)
                                notificationScheduler.rescheduleNotifications()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TrackifyCard(
                title = stringResource(R.string.reminder_days_title)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = stringResource(R.string.reminder_days_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column {
                        ReminderDayOption(
                            title = stringResource(R.string.on_payment_day),
                            selected = reminderDays.contains(0),
                            onClick = {
                                reminderDays = if (reminderDays.contains(0)) {
                                    reminderDays - 0
                                } else {
                                    reminderDays + 0
                                }
                                preferenceManager.setReminderDays(reminderDays)
                                notificationScheduler.rescheduleNotifications()
                            }
                        )

                        ReminderDayOption(
                            title = stringResource(R.string.one_day_before),
                            selected = reminderDays.contains(1),
                            onClick = {
                                reminderDays = if (reminderDays.contains(1)) {
                                    reminderDays - 1
                                } else {
                                    reminderDays + 1
                                }
                                preferenceManager.setReminderDays(reminderDays)
                                notificationScheduler.rescheduleNotifications()
                            }
                        )

                        ReminderDayOption(
                            title = stringResource(R.string.three_days_before),
                            selected = reminderDays.contains(3),
                            onClick = {
                                reminderDays = if (reminderDays.contains(3)) {
                                    reminderDays - 3
                                } else {
                                    reminderDays + 3
                                }
                                preferenceManager.setReminderDays(reminderDays)
                                notificationScheduler.rescheduleNotifications()
                            }
                        )

                        ReminderDayOption(
                            title = stringResource(R.string.week_before),
                            selected = reminderDays.contains(7),
                            onClick = {
                                reminderDays = if (reminderDays.contains(7)) {
                                    reminderDays - 7
                                } else {
                                    reminderDays + 7
                                }
                                preferenceManager.setReminderDays(reminderDays)
                                notificationScheduler.rescheduleNotifications()
                            }
                        )

                        ReminderDayOption(
                            title = stringResource(R.string.two_weeks_before),
                            selected = reminderDays.contains(14),
                            onClick = {
                                reminderDays = if (reminderDays.contains(14)) {
                                    reminderDays - 14
                                } else {
                                    reminderDays + 14
                                }
                                preferenceManager.setReminderDays(reminderDays)
                                notificationScheduler.rescheduleNotifications()
                            }
                        )

                        ReminderDayOption(
                            title = stringResource(R.string.month_before),
                            selected = reminderDays.contains(30),
                            onClick = {
                                reminderDays = if (reminderDays.contains(30)) {
                                    reminderDays - 30
                                } else {
                                    reminderDays + 30
                                }
                                preferenceManager.setReminderDays(reminderDays)
                                notificationScheduler.rescheduleNotifications()
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
                notificationScheduler.rescheduleNotifications()
                showTimePickerDialog = false
            }
        )
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text(stringResource(R.string.notifications_permission_denied)) },
            text = { Text(stringResource(R.string.notifications_permission_rationale)) },
            confirmButton = {
                Button(onClick = {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    context.startActivity(intent)
                    showPermissionDialog = false
                }) {
                    Text(stringResource(R.string.yes))
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
    var hour by remember { mutableStateOf(initialHour) }
    var minute by remember { mutableStateOf(initialMinute) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.notification_time),
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NumberSelector(
                        value = hour,
                        range = 0..23,
                        onValueChange = { hour = it }
                    )

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    NumberSelector(
                        value = minute,
                        range = 0..59,
                        onValueChange = { minute = it }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(onClick = { onConfirm(hour, minute) }) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }
}

@Composable
fun NumberSelector(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = {
            val newValue = if (value + 1 > range.last) range.first else value + 1
            onValueChange(newValue)
        }) {
            Text("▲", style = MaterialTheme.typography.titleLarge)
        }

        Text(
            text = value.toString().padStart(2, '0'),
            style = MaterialTheme.typography.headlineMedium
        )

        IconButton(onClick = {
            val newValue = if (value - 1 < range.first) range.last else value - 1
            onValueChange(newValue)
        }) {
            Text("▼", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun FrequencyOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .padding(start = 8.dp)
                .clickable(onClick = onClick)
        )
    }
}

@Composable
private fun ReminderDayOption(
    title: String,
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

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .padding(start = 8.dp)
                .clickable(onClick = onClick)
        )
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    return String.format("%02d:%02d", hour, minute)
}