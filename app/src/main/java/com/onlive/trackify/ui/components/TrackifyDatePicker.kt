package com.onlive.trackify.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.onlive.trackify.R
import com.onlive.trackify.utils.DateUtils
import com.onlive.trackify.utils.stringResource
import java.util.Date

data class QuickPickOption(
    val label: String,
    val icon: ImageVector,
    val dateProvider: () -> Date,
    val isSpecial: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TrackifyDatePicker(
    modifier: Modifier = Modifier,
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit,
    allowNull: Boolean = false,
) {
    val screenHeight = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding() +
            WindowInsets.systemBars.asPaddingValues().calculateTopPadding() +
            600.dp
    val isLargeScreen = screenHeight > 600.dp

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
                .fillMaxWidth(0.97f)
                .wrapContentHeight()
                .padding(horizontal = 8.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(vertical = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.select_date),
                        style = MaterialTheme.typography.headlineSmallEmphasized,
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
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            selected = showCalendar,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = CircleShape
                        )

                        FilterChip(
                            onClick = { showCalendar = false },
                            label = {
                                Icon(
                                    imageVector = Icons.Default.Apps,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            selected = !showCalendar,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = CircleShape
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                }

                if (showCalendar) {
                    DatePicker(
                        state = datePickerState,
                        modifier = Modifier.fillMaxWidth(),
                        title = null,
                        headline = null,
                        showModeToggle = false,
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
                            navigationContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                } else {
                    QuickPickSection(
                        options = quickPickOptions,
                        onOptionSelected = { option ->
                            onDateSelected(option.dateProvider())
                            onDismiss()
                        },
                        isLargeScreen = isLargeScreen,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .heightIn(max = if (isLargeScreen) 400.dp else 350.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.cancel),
                            style = MaterialTheme.typography.bodyLargeEmphasized
                        )
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
                        shape = MaterialTheme.shapes.medium,
                        enabled = if (showCalendar) datePickerState.selectedDateMillis != null else true
                    ) {
                        Text(
                            text = stringResource(R.string.choose),
                            style = MaterialTheme.typography.bodyLargeEmphasized
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
            style = MaterialTheme.typography.titleMediumEmphasized,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        val columns = if (isLargeScreen && options.size > 3) 2 else 1
        val chunkedOptions = options.chunked(columns)

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            chunkedOptions.forEach { rowOptions ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowOptions.forEach { option ->
                        QuickPickCard(
                            option = option,
                            onClick = { onOptionSelected(option) },
                            modifier = Modifier.weight(1f),
                            isLargeScreen = isLargeScreen
                        )
                    }
                    if (rowOptions.size < columns) {
                        repeat(columns - rowOptions.size) {
                            Spacer(modifier = Modifier.weight(1f).height(1.dp))
                        }
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
    modifier: Modifier = Modifier
) {
    val cardHeight = if (isLargeScreen) 80.dp else 64.dp
    val iconSize = if (isLargeScreen) 24.dp else 22.dp

    val cardColors = if (option.isSpecial) {
        CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    } else {
        CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }

    ElevatedCard(
        onClick = onClick,
        modifier = modifier.height(cardHeight),
        shape = MaterialTheme.shapes.medium,
        colors = cardColors,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 8.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (isLargeScreen) 16.dp else 12.dp)
        ) {
            Icon(
                imageVector = option.icon,
                contentDescription = option.label,
                modifier = Modifier.size(iconSize),
                tint = if (option.isSpecial) {
                    MaterialTheme.colorScheme.onTertiaryContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                }
            )

            Text(
                text = option.label,
                style = if (isLargeScreen) {
                    MaterialTheme.typography.bodyLargeEmphasized
                } else {
                    MaterialTheme.typography.bodyMediumEmphasized
                },
                color = if (option.isSpecial) {
                    MaterialTheme.colorScheme.onTertiaryContainer
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
