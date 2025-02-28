package com.onlive.trackify.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Currency
import com.onlive.trackify.databinding.FragmentCurrencySettingsBinding
import com.onlive.trackify.utils.PreferenceManager

class CurrencySettingsFragment : Fragment() {

    private var _binding: FragmentCurrencySettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var preferenceManager: PreferenceManager
    private lateinit var currencyAdapter: CurrencyAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCurrencySettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferenceManager = PreferenceManager(requireContext())
        setupCurrencyList()
    }

    private fun setupCurrencyList() {
        val currentCurrencyCode = preferenceManager.getCurrencyCode()

        currencyAdapter = CurrencyAdapter(
            Currency.POPULAR_CURRENCIES,
            currentCurrencyCode
        ) { currency ->
            preferenceManager.setCurrencyCode(currency.code)
            Toast.makeText(
                requireContext(),
                getString(R.string.currency_saved, currency.name),
                Toast.LENGTH_SHORT
            ).show()
            findNavController().popBackStack()
        }

        binding.recyclerViewCurrencies.apply {
            adapter = currencyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}