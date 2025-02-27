package com.onlive.trackify.utils

import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.onlive.trackify.databinding.DialogAdvancedDatePickerBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DatePickerHelper(private val context: Context) {

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))

    fun showAdvancedDatePickerForSubscription(
        initialDate: Date = Date(),
        allowNullDate: Boolean = true,
        onDateSelected: (Date?) -> Unit
    ) {
        val binding = DialogAdvancedDatePickerBinding.inflate(LayoutInflater.from(context))

        calendar.time = initialDate
        updateCalendarDisplay(binding.textViewSelectedDate)

        setupQuickDateButtons(binding)

        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .setTitle("Выберите дату")
            .setPositiveButton("Готово") { _, _ ->
                onDateSelected(calendar.time)
            }
            .create()

        if (allowNullDate) {
            dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Бессрочно") { _, _ ->
                onDateSelected(null)
            }
        }

        binding.buttonSelectDate.setOnClickListener {
            showStandardDatePicker { year, month, day ->
                calendar.set(year, month, day)
                updateCalendarDisplay(binding.textViewSelectedDate)
            }
        }

        dialog.show()
    }

    private fun setupQuickDateButtons(binding: DialogAdvancedDatePickerBinding) {
        binding.buttonToday.setOnClickListener {
            calendar.time = Date()
            updateCalendarDisplay(binding.textViewSelectedDate)
        }

        binding.buttonTomorrow.setOnClickListener {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            updateCalendarDisplay(binding.textViewSelectedDate)
        }

        binding.buttonOneWeek.setOnClickListener {
            calendar.time = Date()
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            updateCalendarDisplay(binding.textViewSelectedDate)
        }

        binding.buttonOneMonth.setOnClickListener {
            calendar.time = Date()
            calendar.add(Calendar.MONTH, 1)
            updateCalendarDisplay(binding.textViewSelectedDate)
        }

        binding.buttonOneYear.setOnClickListener {
            calendar.time = Date()
            calendar.add(Calendar.YEAR, 1)
            updateCalendarDisplay(binding.textViewSelectedDate)
        }
    }

    private fun updateCalendarDisplay(textView: Button) {
        textView.text = dateFormat.format(calendar.time)
    }

    private fun showStandardDatePicker(onDateSet: (Int, Int, Int) -> Unit) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                onDateSet(year, month, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}