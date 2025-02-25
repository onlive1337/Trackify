package com.onlive.trackify.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.onlive.trackify.R
import com.onlive.trackify.databinding.FragmentSettingsBinding
import com.onlive.trackify.utils.ThemeManager

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var themeManager: ThemeManager

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

        setupThemeOptions()

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(
                requireContext(),
                if (isChecked) "Уведомления включены" else "Уведомления отключены",
                Toast.LENGTH_SHORT
            ).show()
        }

        setupDynamicColorsInfo()

        binding.buttonManageCategories.setOnClickListener {
            Toast.makeText(requireContext(), "Функция в разработке", Toast.LENGTH_SHORT).show()
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