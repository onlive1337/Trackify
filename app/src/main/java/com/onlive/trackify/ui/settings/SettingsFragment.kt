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
import com.onlive.trackify.utils.NotificationTester
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.utils.ThemeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var themeManager: ThemeManager
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var notificationTester: NotificationTester

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            binding.switchNotifications.isChecked = true
            preferenceManager.setNotificationsEnabled(true)
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
        notificationTester = NotificationTester(requireContext())

        setupThemeOptions()
        setupNotificationOptions()
        setupTestButtons()

        binding.buttonManageCategories.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_settings_to_categoryListFragment)
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
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.notifications_enabled),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                preferenceManager.setNotificationsEnabled(false)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.notifications_disabled),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        setupDynamicColorsInfo()
    }

    private fun setupTestButtons() {
        binding.buttonTestNotification.setOnClickListener {
            if (hasNotificationPermission()) {
                CoroutineScope(Dispatchers.IO).launch {
                    notificationTester.sendTestNotification()
                }
                Toast.makeText(
                    requireContext(),
                    "Отправляю тестовые уведомления...",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                requestNotificationPermission()
            }
        }

        binding.buttonCreateTestSubscription.setOnClickListener {
            if (hasNotificationPermission()) {
                notificationTester.createTestSubscriptionAndRunCheck()
                Toast.makeText(
                    requireContext(),
                    "Создаю тестовую подписку и проверяю...",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                requestNotificationPermission()
            }
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Toast.makeText(
                requireContext(),
                "Для отправки уведомлений нужно разрешение",
                Toast.LENGTH_SHORT
            ).show()
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun setupDynamicColorsInfo() {
        if (themeManager.supportsDynamicColors()) {
            binding.textViewDynamicColorsStatus.text = getString(R.string.dynamic_colors_enabled)
            binding.imageViewDynamicColorsStatus.setImageResource(R.drawable.ic_check_circle)
        } else {
            binding.textViewDynamicColorsStatus.text = getString(R.string.dynamic_colors_unavailable)
            binding.imageViewDynamicColorsStatus.setImageResource(R.drawable.ic_info)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}