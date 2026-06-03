package com.onlive.trackify.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.onlive.trackify.utils.stringResource
import androidx.compose.ui.unit.dp
import com.onlive.trackify.R
import com.onlive.trackify.ui.components.SettingsSection
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.utils.BatteryOptimizationHelper
import com.onlive.trackify.utils.NotificationScheduler
import com.onlive.trackify.utils.PreferenceManager
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val locale = configuration.locales[0]
    val preferenceManager = remember { PreferenceManager(context) }
    val notificationScheduler = remember { NotificationScheduler(context) }

    var reminderDays by remember {
        mutableStateOf(preferenceManager.getReminderDays())
    }

    val (hour, minute) = preferenceManager.getNotificationTime()
    val notificationHourState = remember { mutableIntStateOf(hour) }
    val notificationMinuteState = remember { mutableIntStateOf(minute) }
    var notificationTime by remember(locale) {
        mutableStateOf(formatTime(notificationHourState.intValue, notificationMinuteState.intValue, locale))
    }

    val showTimePickerDialogState = remember { mutableStateOf(false) }

    LaunchedEffect(reminderDays, notificationHourState.intValue, notificationMinuteState.intValue) {
        notificationScheduler.scheduleNotifications()
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TrackifyTopAppBar(
                title = stringResource(R.string.notification_settings),
                showBackButton = true,
                onBackClick = onNavigateBack,
                scrollBehavior = scrollBehavior
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

            SettingsSection(
                title = stringResource(R.string.notification_time)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = stringResource(R.string.notification_time_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = { showTimePickerDialogState.value = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(text = notificationTime)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection(
                title = stringResource(R.string.reminder_days)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = stringResource(R.string.reminder_days_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

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

            Spacer(modifier = Modifier.height(16.dp))

            BatteryOptimizationSection()

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showTimePickerDialogState.value) {
        TimePickerDialog(
            initialHour = notificationHourState.intValue,
            initialMinute = notificationMinuteState.intValue,
            onDismiss = { showTimePickerDialogState.value = false },
            onConfirm = { selectedHour, selectedMinute ->
                notificationHourState.intValue = selectedHour
                notificationMinuteState.intValue = selectedMinute
                notificationTime = formatTime(selectedHour, selectedMinute, locale)
                preferenceManager.setNotificationTime(selectedHour, selectedMinute)
                notificationScheduler.scheduleNotifications()
                showTimePickerDialogState.value = false
            }
        )
    }
}

@Composable
private fun BatteryOptimizationSection() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var isIgnoring by remember {
        mutableStateOf(BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context))
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isIgnoring = BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    SettingsSection(
        title = stringResource(R.string.battery_optimization_title)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                text = stringResource(R.string.battery_optimization_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (isIgnoring) {
                Text(
                    text = stringResource(R.string.battery_optimization_enabled),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Button(
                    onClick = { BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(text = stringResource(R.string.battery_optimization_button))
                }
            }

            if (BatteryOptimizationHelper.isXiaomiDevice()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.battery_optimization_xiaomi_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = stringResource(R.string.battery_optimization_xiaomi_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = { BatteryOptimizationHelper.openXiaomiAutostartSettings(context) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text(text = stringResource(R.string.battery_optimization_xiaomi_button))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_time)) },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = state)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(state.hour, state.minute) }) {
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
            onCheckedChange = { onClick() },
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTime(hour: Int, minute: Int, locale: Locale): String {
    return String.format(locale, "%02d:%02d", hour, minute)
}