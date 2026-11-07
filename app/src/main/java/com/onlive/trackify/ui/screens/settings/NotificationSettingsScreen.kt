package com.onlive.trackify.ui.screens.settings

import android.content.Context
import android.text.format.DateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.onlive.trackify.R
import com.onlive.trackify.ui.components.SettingsSection
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.ui.components.segmentedListItemShapes
import com.onlive.trackify.utils.BatteryOptimizationHelper
import com.onlive.trackify.utils.NotificationScheduler
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.utils.stringResource
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NotificationSettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val is24Hour = DateFormat.is24HourFormat(context)
    val preferenceManager = remember { PreferenceManager(context) }
    val notificationScheduler = remember { NotificationScheduler(context) }

    var reminderDays by remember {
        mutableStateOf(preferenceManager.getReminderDays())
    }

    val (hour, minute) = preferenceManager.getNotificationTime()
    val notificationHourState = remember { mutableIntStateOf(hour) }
    val notificationMinuteState = remember { mutableIntStateOf(minute) }
    var notificationTime by remember(configuration, is24Hour) {
        mutableStateOf(formatTime(context, notificationHourState.intValue, notificationMinuteState.intValue))
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
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = stringResource(R.string.notification_time_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = { showTimePickerDialogState.value = true },
                        shapes = ButtonDefaults.shapes(),
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

            Spacer(modifier = Modifier.height(20.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.reminder_days),
                    style = MaterialTheme.typography.labelLargeEmphasized,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Text(
                    text = stringResource(R.string.reminder_days_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )

                val reminderOptions = listOf(
                    Triple(0, stringResource(R.string.on_payment_day), stringResource(R.string.notify_on_payment_day)),
                    Triple(1, stringResource(R.string.one_day_before), stringResource(R.string.notify_one_day_before)),
                    Triple(3, stringResource(R.string.three_days_before), stringResource(R.string.notify_three_days_before)),
                    Triple(7, stringResource(R.string.week_before), stringResource(R.string.notify_week_before))
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                ) {
                    reminderOptions.forEachIndexed { index, (dayOffset, title, description) ->
                        ReminderDayOption(
                            title = title,
                            description = description,
                            checked = reminderDays.contains(dayOffset),
                            index = index,
                            count = reminderOptions.size,
                            onCheckedChange = { shouldSelect ->
                                reminderDays = if (shouldSelect) {
                                    reminderDays + dayOffset
                                } else {
                                    reminderDays - dayOffset
                                }
                                preferenceManager.setReminderDays(reminderDays)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            BatteryOptimizationSection()

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showTimePickerDialogState.value) {
        TimePickerDialog(
            initialHour = notificationHourState.intValue,
            initialMinute = notificationMinuteState.intValue,
            is24Hour = is24Hour,
            onDismiss = { showTimePickerDialogState.value = false },
            onConfirm = { selectedHour, selectedMinute ->
                notificationHourState.intValue = selectedHour
                notificationMinuteState.intValue = selectedMinute
                notificationTime = formatTime(context, selectedHour, selectedMinute)
                preferenceManager.setNotificationTime(selectedHour, selectedMinute)
                notificationScheduler.scheduleNotifications()
                showTimePickerDialogState.value = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
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
                    shapes = ButtonDefaults.shapes(),
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
                    shapes = ButtonDefaults.shapes(),
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    is24Hour: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = is24Hour
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
            Button(
                onClick = { onConfirm(state.hour, state.minute) },
                shapes = ButtonDefaults.shapes()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shapes = ButtonDefaults.shapes()
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ReminderDayOption(
    title: String,
    description: String,
    checked: Boolean,
    index: Int,
    count: Int,
    onCheckedChange: (Boolean) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    SegmentedListItem(
        checked = checked,
        onCheckedChange = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onCheckedChange(it)
        },
        shapes = segmentedListItemShapes(index = index, count = count),
        colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        leadingContent = {
            Checkbox(
                checked = checked,
                onCheckedChange = null
            )
        },
        supportingContent = {
            Text(text = description)
        }
    ) {
        Text(
            text = title,
            style = if (checked) MaterialTheme.typography.bodyLargeEmphasized else MaterialTheme.typography.bodyLarge
        )
    }
}

private fun formatTime(context: Context, hour: Int, minute: Int): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return DateFormat.getTimeFormat(context).format(calendar.time)
}