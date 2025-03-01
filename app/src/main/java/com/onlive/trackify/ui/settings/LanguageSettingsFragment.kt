package com.onlive.trackify.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.onlive.trackify.R
import com.onlive.trackify.databinding.FragmentLanguageSettingsBinding
import com.onlive.trackify.utils.LocaleHelper
import com.onlive.trackify.utils.PreferenceManager

class LanguageSettingsFragment : Fragment() {

    private var _binding: FragmentLanguageSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferenceManager: PreferenceManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguageSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager(requireContext())
        setupLanguageOptions()

        binding.buttonSave.setOnClickListener {
            saveLanguageSettings()
        }
    }

    private fun setupLanguageOptions() {
        val currentLanguageCode = preferenceManager.getLanguageCode()
        val languages = LocaleHelper.getAvailableLanguages()

        binding.radioGroupLanguages.removeAllViews()

        languages.forEachIndexed { index, language ->
            val radioButton = RadioButton(requireContext()).apply {
                id = View.generateViewId()
                text = language.name
                isChecked = language.code == currentLanguageCode
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setPadding(16, 16, 16, 16)
                tag = language.code
            }
            binding.radioGroupLanguages.addView(radioButton)
        }
    }

    private fun saveLanguageSettings() {
        val selectedId = binding.radioGroupLanguages.checkedRadioButtonId
        if (selectedId != -1) {
            val radioButton = binding.root.findViewById<RadioButton>(selectedId)
            val languageCode = radioButton.tag as String

            preferenceManager.setLanguageCode(languageCode)

            Toast.makeText(
                requireContext(),
                getString(R.string.language_changed),
                Toast.LENGTH_SHORT
            ).show()

            requireActivity().recreate()

            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}