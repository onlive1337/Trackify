package com.example.trackify.ui.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.trackify.databinding.FragmentStatisticsBinding
import com.example.trackify.viewmodel.StatisticsViewModel

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private val statisticsViewModel: StatisticsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Загрузка данных статистики
        statisticsViewModel.calculateMonthlySpending()
        statisticsViewModel.calculateYearlySpending()

        // Наблюдение за данными
        observeStatistics()
    }

    private fun observeStatistics() {
        statisticsViewModel.totalMonthlySpending.observe(viewLifecycleOwner) { monthlySpending ->
            binding.textViewMonthlySpending.text = String.format("₽%.2f", monthlySpending)
        }

        statisticsViewModel.totalYearlySpending.observe(viewLifecycleOwner) { yearlySpending ->
            binding.textViewYearlySpending.text = String.format("₽%.2f", yearlySpending)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}