package com.onlive.trackify.utils

import android.content.Context
import android.view.LayoutInflater
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.onlive.trackify.R
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
        updateDateDisplay(binding)

        setupQuickDateButtons(binding)

        val dialogBuilder = MaterialAlertDialogBuilder(context)
            .setView(binding.root)
            .setPositiveButton(context.getString(R.string.done)) { _, _ ->
                onDateSelected(calendar.time)
            }

        if (allowNullDate) {
            dialogBuilder.setNeutralButton(context.getString(R.string.indefinitely)) { _, _ ->
                onDateSelected(null)
            }
        }

        val dialog = dialogBuilder.create()

        binding.buttonSelectDate.setOnClickListener {
            showMaterialDatePicker(calendar.time) { selectedDate ->
                if (selectedDate != null) {
                    calendar.time = selectedDate
                    updateDateDisplay(binding)
                }
            }
        }

        dialog.show()
    }

    private fun showMaterialDatePicker(initialDate: Date, onDateSelected: (Date?) -> Unit) {
        val calendar = Calendar.getInstance()
        calendar.time = initialDate

        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(context.getString(R.string.select_date))
            .setSelection(calendar.timeInMillis)
            .setTheme(R.style.ThemeOverlay_Material3_MaterialCalendar)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.timeInMillis = selection
            onDateSelected(selectedCalendar.time)
        }

        datePicker.addOnCancelListener {
            onDateSelected(null)
        }

        datePicker.addOnNegativeButtonClickListener {
            onDateSelected(null)
        }

        datePicker.show((context as androidx.fragment.app.FragmentActivity).supportFragmentManager, "MATERIAL_DATE_PICKER")
    }

    private fun setupQuickDateButtons(binding: DialogAdvancedDatePickerBinding) {
        binding.chipToday.setOnClickListener {
            calendar.time = Date()
            updateDateDisplay(binding)
        }

        binding.chipTomorrow.setOnClickListener {
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            updateDateDisplay(binding)
        }

        binding.chipOneWeek.setOnClickListener {
            calendar.time = Date()
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            updateDateDisplay(binding)
        }

        binding.chipOneMonth.setOnClickListener {
            calendar.time = Date()
            calendar.add(Calendar.MONTH, 1)
            updateDateDisplay(binding)
        }

        binding.chipOneYear.setOnClickListener {
            calendar.time = Date()
            calendar.add(Calendar.YEAR, 1)
            updateDateDisplay(binding)
        }
    }

    private fun updateDateDisplay(binding: DialogAdvancedDatePickerBinding) {
        binding.textViewSelectedDate.text = dateFormat.format(calendar.time)
    }
}