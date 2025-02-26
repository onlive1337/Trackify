package com.onlive.trackify.ui.settings

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.onlive.trackify.databinding.FragmentNotificationSettingsBinding
import com.onlive.trackify.utils.NotificationFrequency
import com.onlive.trackify.utils.NotificationScheduler
import com.onlive.trackify.utils.PreferenceManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class NotificationSettingsFragment : Fragment() {

    private var _binding: FragmentNotificationSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var notificationScheduler: NotificationScheduler

    private val selectedDays = mutableSetOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager(requireContext())
        notificationScheduler = NotificationScheduler(requireContext())

        loadCurrentSettings()

        setupTimeSelection()

        setupFrequencySelection()

        setupDaysSelection()

        binding.buttonSave.setOnClickListener {
            saveSettings()
            Toast.makeText(requireContext(), "Настройки сохранены", Toast.LENGTH_SHORT).show()
            requireActivity().onBackPressed()
        }
    }

    private fun loadCurrentSettings() {
        val (hour, minute) = preferenceManager.getNotificationTime()
        updateTimeDisplay(hour, minute)

        val frequency = preferenceManager.getNotificationFrequency()
        when (frequency) {
            NotificationFrequency.DAILY -> binding.radioDaily.isChecked = true
            NotificationFrequency.WEEKLY -> binding.radioWeekly.isChecked = true
            NotificationFrequency.MONTHLY -> binding.radioMonthly.isChecked = true
            NotificationFrequency.CUSTOM -> binding.radioCustom.isChecked = true
        }

        updateDaysVisibility(frequency)

        selectedDays.clear()
        selectedDays.addAll(preferenceManager.getReminderDays())
        updateDaysSelection()
    }

    private fun setupTimeSelection() {
        binding.buttonSelectTime.setOnClickListener {
            val (currentHour, currentMinute) = preferenceManager.getNotificationTime()

            val timePickerDialog = TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    updateTimeDisplay(hour, minute)
                },
                currentHour,
                currentMinute,
                true
            )

            timePickerDialog.show()
        }
    }

    private fun updateTimeDisplay(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }

        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        binding.buttonSelectTime.text = timeFormat.format(calendar.time)
    }

    private fun setupFrequencySelection() {
        binding.radioGroupFrequency.setOnCheckedChangeListener { _, checkedId ->
            val frequency = when (checkedId) {
                binding.radioDaily.id -> NotificationFrequency.DAILY
                binding.radioWeekly.id -> NotificationFrequency.WEEKLY
                binding.radioMonthly.id -> NotificationFrequency.MONTHLY
                binding.radioCustom.id -> NotificationFrequency.CUSTOM
                else -> NotificationFrequency.DAILY
            }

            updateDaysVisibility(frequency)
        }
    }

    private fun updateDaysVisibility(frequency: NotificationFrequency) {
        binding.cardDaysSelection.visibility = when (frequency) {
            NotificationFrequency.CUSTOM -> View.VISIBLE
            else -> View.GONE
        }
    }

    private fun setupDaysSelection() {
        binding.chipDay0.setOnClickListener { toggleDay(0, binding.chipDay0) }

        binding.chipDay1.setOnClickListener { toggleDay(1, binding.chipDay1) }

        binding.chipDay3.setOnClickListener { toggleDay(3, binding.chipDay3) }

        binding.chipDay7.setOnClickListener { toggleDay(7, binding.chipDay7) }

        binding.chipDay14.setOnClickListener { toggleDay(14, binding.chipDay14) }

        binding.chipDay30.setOnClickListener { toggleDay(30, binding.chipDay30) }

        updateDaysSelection()
    }

    private fun toggleDay(day: Int, chip: Chip) {
        if (selectedDays.contains(day)) {
            selectedDays.remove(day)
            chip.isChecked = false
        } else {
            selectedDays.add(day)
            chip.isChecked = true
        }
    }

    private fun updateDaysSelection() {
        binding.chipDay0.isChecked = selectedDays.contains(0)
        binding.chipDay1.isChecked = selectedDays.contains(1)
        binding.chipDay3.isChecked = selectedDays.contains(3)
        binding.chipDay7.isChecked = selectedDays.contains(7)
        binding.chipDay14.isChecked = selectedDays.contains(14)
        binding.chipDay30.isChecked = selectedDays.contains(30)
    }

    private fun saveSettings() {
        val timeText = binding.buttonSelectTime.text.toString()
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val calendar = Calendar.getInstance()

        try {
            timeFormat.parse(timeText)?.let {
                calendar.time = it
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val minute = calendar.get(Calendar.MINUTE)
                preferenceManager.setNotificationTime(hour, minute)
            }
        } catch (e: Exception) {
            preferenceManager.setNotificationTime(9, 0)
        }

        val frequency = when {
            binding.radioDaily.isChecked -> NotificationFrequency.DAILY
            binding.radioWeekly.isChecked -> NotificationFrequency.WEEKLY
            binding.radioMonthly.isChecked -> NotificationFrequency.MONTHLY
            binding.radioCustom.isChecked -> NotificationFrequency.CUSTOM
            else -> NotificationFrequency.DAILY
        }
        preferenceManager.setNotificationFrequency(frequency)

        if (selectedDays.isEmpty() && frequency == NotificationFrequency.CUSTOM) {
            selectedDays.add(0)
        }
        preferenceManager.setReminderDays(selectedDays)

        notificationScheduler.rescheduleNotifications()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}