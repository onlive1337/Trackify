package com.onlive.trackify.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.onlive.trackify.R
import com.onlive.trackify.utils.DateUtils
import java.util.*

data class QuickPickOption(
    val label: String,
    val icon: ImageVector,
    val dateProvider: () -> Date,
    val isSpecial: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackifyDatePicker(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit,
    allowNull: Boolean = false,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val isLargeScreen = screenHeight > 600.dp
    val isWideScreen = screenWidth > 400.dp

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.time
    )

    var showCalendar by remember { mutableStateOf(true) }

    val todayLabel = stringResource(R.string.today)
    val tomorrowLabel = stringResource(R.string.tomorrow)
    val weekLabel = stringResource(R.string.in_a_week)
    val monthLabel = stringResource(R.string.in_a_month)
    val yearLabel = stringResource(R.string.one_year_later)
    val indefinitelyLabel = stringResource(R.string.indefinitely)

    val quickPickOptions = remember(allowNull, todayLabel, tomorrowLabel, weekLabel, monthLabel, yearLabel, indefinitelyLabel) {
        listOf(
            QuickPickOption(
                label = todayLabel,
                icon = Icons.Default.Today,
                dateProvider = { Date() }
            ),
            QuickPickOption(
                label = tomorrowLabel,
                icon = Icons.AutoMirrored.Filled.NavigateNext,
                dateProvider = { DateUtils.getDateAfterDays(1) }
            ),
            QuickPickOption(
                label = weekLabel,
                icon = Icons.Default.DateRange,
                dateProvider = { DateUtils.getDateAfterDays(7) }
            ),
            QuickPickOption(
                label = monthLabel,
                icon = Icons.Default.CalendarMonth,
                dateProvider = { DateUtils.getDateAfterMonths(1) }
            ),
            QuickPickOption(
                label = yearLabel,
                icon = Icons.Default.Event,
                dateProvider = { DateUtils.getDateAfterYears(1) }
            )
        ).let { baseOptions ->
            if (allowNull) {
                baseOptions + QuickPickOption(
                    label = indefinitelyLabel,
                    icon = Icons.Default.AllInclusive,
                    dateProvider = { Date(Long.MAX_VALUE) },
                    isSpecial = true
                )
            } else {
                baseOptions
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(if (isWideScreen) 0.92f else 0.98f)
                .wrapContentHeight()
                .padding(horizontal = if (isWideScreen) 16.dp else 8.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(if (isWideScreen) 24.dp else 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.select_date),
                        style = if (isWideScreen) MaterialTheme.typography.headlineSmall
                        else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            onClick = { showCalendar = true },
                            label = {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Календарь",
                                    modifier = Modifier.size(if (isWideScreen) 18.dp else 16.dp)
                                )
                            },
                            selected = showCalendar,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )

                        FilterChip(
                            onClick = { showCalendar = false },
                            label = {
                                Icon(
                                    imageVector = Icons.Default.Apps,
                                    contentDescription = "Быстрый выбор",
                                    modifier = Modifier.size(if (isWideScreen) 18.dp else 16.dp)
                                )
                            },
                            selected = !showCalendar,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (showCalendar) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        DatePicker(
                            state = datePickerState,
                            modifier = Modifier
                                .widthIn(min = 320.dp)
                                .wrapContentWidth(),
                            colors = DatePickerDefaults.colors(
                                containerColor = Color.Transparent,
                                titleContentColor = MaterialTheme.colorScheme.onSurface,
                                headlineContentColor = MaterialTheme.colorScheme.onSurface,
                                weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                yearContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                currentYearContentColor = MaterialTheme.colorScheme.primary,
                                selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                                selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                                dayContentColor = MaterialTheme.colorScheme.onSurface,
                                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                                todayContentColor = MaterialTheme.colorScheme.primary,
                                todayDateBorderColor = MaterialTheme.colorScheme.primary,
                                dayInSelectionRangeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                dayInSelectionRangeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                disabledDayContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                navigationContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                dateTextFieldColors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        )
                    }
                } else {
                    QuickPickSection(
                        options = quickPickOptions,
                        onOptionSelected = { option ->
                            onDateSelected(option.dateProvider())
                            onDismiss()
                        },
                        isLargeScreen = isLargeScreen,
                        isWideScreen = isWideScreen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = if (isLargeScreen) 400.dp else 350.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (showCalendar) {
                                datePickerState.selectedDateMillis?.let {
                                    onDateSelected(Date(it))
                                }
                            }
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.choose),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickPickSection(
    options: List<QuickPickOption>,
    onOptionSelected: (QuickPickOption) -> Unit,
    isLargeScreen: Boolean,
    isWideScreen: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .then(
                if (!isLargeScreen) {
                    Modifier.verticalScroll(rememberScrollState())
                } else {
                    Modifier
                }
            )
    ) {
        Text(
            text = stringResource(R.string.quick_pick),
            style = if (isWideScreen) MaterialTheme.typography.titleMedium
            else MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val columns = if (isLargeScreen && isWideScreen) 2 else 1
        val chunkedOptions = options.chunked(columns)

        Column(
            verticalArrangement = Arrangement.spacedBy(if (isWideScreen) 12.dp else 8.dp)
        ) {
            chunkedOptions.forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(if (isWideScreen) 12.dp else 8.dp)
                ) {
                    rowOptions.forEach { option ->
                        QuickPickCard(
                            option = option,
                            onClick = { onOptionSelected(option) },
                            modifier = Modifier.weight(1f),
                            isLargeScreen = isLargeScreen,
                            isWideScreen = isWideScreen
                        )
                    }

                    repeat(columns - rowOptions.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickPickCard(
    option: QuickPickOption,
    onClick: () -> Unit,
    isLargeScreen: Boolean,
    isWideScreen: Boolean,
    modifier: Modifier = Modifier
) {
    val cardHeight = when {
        isLargeScreen && isWideScreen -> 80.dp
        isWideScreen -> 64.dp
        else -> 56.dp
    }

    val iconSize = when {
        isLargeScreen && isWideScreen -> 24.dp
        isWideScreen -> 20.dp
        else -> 18.dp
    }

    val cardColors = if (option.isSpecial) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

    ElevatedCard(
        onClick = onClick,
        modifier = modifier.height(cardHeight),
        shape = RoundedCornerShape(12.dp),
        colors = cardColors,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = if (isWideScreen) 16.dp else 12.dp,
                    vertical = if (isWideScreen) 12.dp else 8.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (isWideScreen) 12.dp else 8.dp)
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
                tint = if (option.isSpecial) {
                    MaterialTheme.colorScheme.onTertiaryContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
            )

            Text(
                text = option.label,
                style = when {
                    isLargeScreen && isWideScreen -> MaterialTheme.typography.bodyMedium
                    isWideScreen -> MaterialTheme.typography.bodySmall
                    else -> MaterialTheme.typography.labelLarge
                },
                fontWeight = FontWeight.Medium,
                color = if (option.isSpecial) {
                    MaterialTheme.colorScheme.onTertiaryContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                },
                modifier = Modifier.weight(1f),
                maxLines = 1
            )
        }
    }
}