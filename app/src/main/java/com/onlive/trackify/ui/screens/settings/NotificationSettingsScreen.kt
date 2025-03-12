package com.onlive.trackify.ui.screens.settings

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
import com.onlive.trackify.R
import com.onlive.trackify.ui.components.TrackifyCard
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.utils.NotificationFrequency
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

    var notificationFrequency by remember {
        mutableStateOf(preferenceManager.getNotificationFrequency())
    }

    var reminderDays by remember {
        mutableStateOf(preferenceManager.getReminderDays())
    }

    val (hour, minute) = preferenceManager.getNotificationTime()
    var notificationTime by remember {
        mutableStateOf(formatTime(hour, minute))
    }

    var showTimePickerDialog by remember { mutableStateOf(false) }

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
                            }
                        )

                        FrequencyOption(
                            title = stringResource(R.string.weekly),
                            selected = notificationFrequency == NotificationFrequency.WEEKLY,
                            onClick = {
                                notificationFrequency = NotificationFrequency.WEEKLY
                                preferenceManager.setNotificationFrequency(NotificationFrequency.WEEKLY)
                            }
                        )

                        FrequencyOption(
                            title = stringResource(R.string.monthly),
                            selected = notificationFrequency == NotificationFrequency.MONTHLY,
                            onClick = {
                                notificationFrequency = NotificationFrequency.MONTHLY
                                preferenceManager.setNotificationFrequency(NotificationFrequency.MONTHLY)
                            }
                        )

                        FrequencyOption(
                            title = stringResource(R.string.custom),
                            selected = notificationFrequency == NotificationFrequency.CUSTOM,
                            onClick = {
                                notificationFrequency = NotificationFrequency.CUSTOM
                                preferenceManager.setNotificationFrequency(NotificationFrequency.CUSTOM)
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
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showTimePickerDialog) {
        AlertDialog(
            onDismissRequest = { showTimePickerDialog = false },
            title = { Text(stringResource(R.string.notification_time)) },
            text = {
                Text("В этом диалоге должен быть выбор времени, " +
                        "но в демонстрационной версии это упрощено")
            },
            confirmButton = {
                Button(onClick = {
                    showTimePickerDialog = false
                }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePickerDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
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