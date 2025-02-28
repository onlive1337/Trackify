package com.onlive.trackify.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.onlive.trackify.R
import com.onlive.trackify.databinding.FragmentSettingsBinding
import com.onlive.trackify.utils.NotificationScheduler
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.utils.ThemeManager

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var themeManager: ThemeManager
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var notificationScheduler: NotificationScheduler

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            binding.switchNotifications.isChecked = true
            preferenceManager.setNotificationsEnabled(true)
            notificationScheduler.rescheduleNotifications()
            Toast.makeText(
                requireContext(),
                getString(R.string.notifications_enabled),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            binding.switchNotifications.isChecked = false
            preferenceManager.setNotificationsEnabled(false)
            Toast.makeText(
                requireContext(),
                getString(R.string.notifications_permission_denied),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        themeManager = ThemeManager(requireContext())
        preferenceManager = PreferenceManager(requireContext())
        notificationScheduler = NotificationScheduler(requireContext())

        setupThemeOptions()
        setupNotificationOptions()

        binding.buttonManageCategories.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_settings_to_categoryListFragment)
        }

        binding.buttonDataManagement.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_settings_to_dataManagementFragment)
        }

        binding.buttonNotificationSettings.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_settings_to_notificationSettingsFragment)
        }

        binding.buttonCurrencySettings.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_settings_to_currencySettingsFragment)
        }
    }

    private fun setupThemeOptions() {
        when (themeManager.getThemeMode()) {
            ThemeManager.MODE_LIGHT -> binding.radioButtonLightTheme.isChecked = true
            ThemeManager.MODE_DARK -> binding.radioButtonDarkTheme.isChecked = true
            else -> binding.radioButtonSystemTheme.isChecked = true
        }

        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.radioButtonLightTheme -> ThemeManager.MODE_LIGHT
                R.id.radioButtonDarkTheme -> ThemeManager.MODE_DARK
                else -> ThemeManager.MODE_SYSTEM
            }
            themeManager.setThemeMode(mode)
        }
    }

    private fun setupNotificationOptions() {
        binding.switchNotifications.isChecked = preferenceManager.areNotificationsEnabled()

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    when {
                        ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            preferenceManager.setNotificationsEnabled(true)
                            notificationScheduler.rescheduleNotifications()
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.notifications_enabled),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.notifications_permission_rationale),
                                Toast.LENGTH_LONG
                            ).show()
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        else -> {
                            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                } else {
                    preferenceManager.setNotificationsEnabled(true)
                    notificationScheduler.rescheduleNotifications()
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.notifications_enabled),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                preferenceManager.setNotificationsEnabled(false)
                notificationScheduler.rescheduleNotifications()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.notifications_disabled),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}