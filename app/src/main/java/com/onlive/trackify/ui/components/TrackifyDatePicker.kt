package com.onlive.trackify.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.onlive.trackify.R
import com.onlive.trackify.utils.DateUtils
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackifyDatePicker(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    onDismiss: () -> Unit,
    allowNull: Boolean = false
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.time
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.select_date),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                DatePicker(
                    state = datePickerState
                )

                Text(
                    text = stringResource(R.string.quick_pick),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuggestionChip(
                        onClick = {
                            onDateSelected(Date())
                            onDismiss()
                        },
                        label = { Text(stringResource(R.string.today)) }
                    )

                    SuggestionChip(
                        onClick = {
                            onDateSelected(DateUtils.getDateAfterDays(1))
                            onDismiss()
                        },
                        label = { Text(stringResource(R.string.tomorrow)) }
                    )

                    SuggestionChip(
                        onClick = {
                            onDateSelected(DateUtils.getDateAfterDays(7))
                            onDismiss()
                        },
                        label = { Text(stringResource(R.string.in_a_week)) }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuggestionChip(
                        onClick = {
                            onDateSelected(DateUtils.getDateAfterMonths(1))
                            onDismiss()
                        },
                        label = { Text(stringResource(R.string.in_a_month)) }
                    )

                    SuggestionChip(
                        onClick = {
                            onDateSelected(DateUtils.getDateAfterYears(1))
                            onDismiss()
                        },
                        label = { Text(stringResource(R.string.one_year_later)) }
                    )

                    if (allowNull) {
                        SuggestionChip(
                            onClick = {
                                onDateSelected(Date(Long.MAX_VALUE))
                                onDismiss()
                            },
                            label = { Text(stringResource(R.string.indefinitely)) }
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(stringResource(R.string.cancel))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            datePickerState.selectedDateMillis?.let {
                                onDateSelected(Date(it))
                            }
                            onDismiss()
                        }
                    ) {
                        Text(stringResource(R.string.choose))
                    }
                }
            }
        }
    }
}