package com.onlive.trackify.utils

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

class DateUtility {
    companion object {
        private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))

        fun formatDate(date: Date): String {
            return dateFormat.format(date)
        }

        fun getDateAfterDays(days: Int): Date {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, days)
            return calendar.time
        }

        fun getDateAfterMonths(months: Int): Date {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.MONTH, months)
            return calendar.time
        }

        fun getDateAfterYears(years: Int): Date {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.YEAR, years)
            return calendar.time
        }
    }
}

@Composable
fun DatePicker(
    initialDate: Date = Date(),
    onDateSelected: (Date?) -> Unit,
    allowNullDate: Boolean = true,
    modifier: Modifier = Modifier
) {
    var selectedDate by remember { mutableStateOf(initialDate) }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = "Выберите дату",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Выбрано: ${DateUtility.formatDate(selectedDate)}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = "Быстрый выбор:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    selectedDate = Date()
                    onDateSelected(selectedDate)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Сегодня")
            }

            OutlinedButton(
                onClick = {
                    selectedDate = DateUtility.getDateAfterDays(1)
                    onDateSelected(selectedDate)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Завтра")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    selectedDate = DateUtility.getDateAfterDays(7)
                    onDateSelected(selectedDate)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Через неделю")
            }

            OutlinedButton(
                onClick = {
                    selectedDate = DateUtility.getDateAfterMonths(1)
                    onDateSelected(selectedDate)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Через месяц")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    selectedDate = DateUtility.getDateAfterYears(1)
                    onDateSelected(selectedDate)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Через год")
            }

            if (allowNullDate) {
                OutlinedButton(
                    onClick = { onDateSelected(null) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Бессрочно")
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(
                onClick = { onDateSelected(selectedDate) }
            ) {
                Text("Выбрать")
            }
        }
    }
}