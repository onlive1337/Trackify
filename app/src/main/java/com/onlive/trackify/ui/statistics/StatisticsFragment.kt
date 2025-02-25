package com.onlive.trackify.ui.statistics

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.onlive.trackify.R
import com.onlive.trackify.databinding.FragmentStatisticsBinding
import com.onlive.trackify.viewmodel.StatisticsViewModel
import java.text.NumberFormat
import java.util.Locale

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

        statisticsViewModel.calculateMonthlySpending()
        statisticsViewModel.calculateYearlySpending()
        statisticsViewModel.calculateSpendingByCategory()
        statisticsViewModel.calculateMonthlySpendingHistory()
        statisticsViewModel.calculateSpendingBySubscriptionType()

        observeStatistics()

        setupPieChart()
        setupBarChart()
        setupTypeChart()
    }

    private fun isUsingNightMode(): Boolean {
        return resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }

    private fun observeStatistics() {
        statisticsViewModel.totalMonthlySpending.observe(viewLifecycleOwner) { monthlySpending ->
            binding.textViewMonthlySpending.text = formatCurrency(monthlySpending)
        }

        statisticsViewModel.totalYearlySpending.observe(viewLifecycleOwner) { yearlySpending ->
            binding.textViewYearlySpending.text = formatCurrency(yearlySpending)
        }

        statisticsViewModel.spendingByCategory.observe(viewLifecycleOwner) { categorySpending ->
            updatePieChart(categorySpending)
        }

        statisticsViewModel.monthlySpendingHistory.observe(viewLifecycleOwner) { monthlyHistory ->
            updateBarChart(monthlyHistory)
        }

        statisticsViewModel.subscriptionTypeSpending.observe(viewLifecycleOwner) { typeSpending ->
            updateTypeChart(typeSpending)
        }
    }

    private fun setupPieChart() {
        val isNightMode = isUsingNightMode()
        val textColor = if (isNightMode) Color.WHITE else Color.BLACK

        binding.pieChartCategories.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 60f
            transparentCircleRadius = 65f
            setDrawEntryLabels(false)
            setDrawCenterText(true)
            centerText = "Расходы\nпо категориям"
            setCenterTextSize(14f)
            setCenterTextColor(textColor)
            setExtraOffsets(20f, 20f, 20f, 20f)

            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.CENTER
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(false)
                yOffset = 0f
                xOffset = 10f
                yEntrySpace = 10f
                isWordWrapEnabled = true
                textSize = 12f
                setTextColor(textColor)
            }

            animateY(1400, Easing.EaseInOutQuad)

            setNoDataTextColor(textColor)
            setNoDataText("Нет данных для отображения")
        }
    }

    private fun setupBarChart() {
        val isNightMode = isUsingNightMode()
        val textColor = if (isNightMode) Color.WHITE else Color.BLACK

        binding.barChartMonthly.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setPinchZoom(false)
            isDoubleTapToZoomEnabled = false
            setScaleEnabled(false)
            setExtraOffsets(10f, 10f, 10f, 10f)

            axisLeft.apply {
                setDrawGridLines(false)
                setDrawAxisLine(false)
                granularity = 1f
                axisMinimum = 0f
                setTextColor(textColor)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return formatCurrency(value.toDouble())
                    }
                }
            }

            axisRight.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                setDrawAxisLine(true)
                setTextColor(textColor)
            }

            animateY(1400)

            legend.apply {
                isEnabled = false
                setTextColor(textColor)
            }

            setNoDataTextColor(textColor)
            setNoDataText("Нет данных для отображения")
        }
    }

    private fun setupTypeChart() {
        val isNightMode = isUsingNightMode()
        val textColor = if (isNightMode) Color.WHITE else Color.BLACK

        binding.pieChartTypes.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 60f
            transparentCircleRadius = 65f
            setDrawEntryLabels(false)
            setDrawCenterText(true)
            centerText = "Расходы\nпо типам"
            setCenterTextSize(14f)
            setCenterTextColor(textColor)
            setExtraOffsets(20f, 20f, 20f, 20f)

            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.CENTER
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                setDrawInside(false)
                yOffset = 0f
                xOffset = 10f
                yEntrySpace = 10f
                isWordWrapEnabled = true
                setTextColor(textColor)
            }

            animateY(1400, Easing.EaseInOutQuad)

            setNoDataTextColor(textColor)
            setNoDataText("Нет данных для отображения")
        }
    }

    private fun updatePieChart(categorySpending: List<StatisticsViewModel.CategorySpending>) {
        val textColor = if (isUsingNightMode()) Color.WHITE else Color.BLACK

        if (categorySpending.isEmpty()) {
            binding.pieChartCategories.setNoDataText("Нет данных для отображения")
            binding.pieChartCategories.setNoDataTextColor(textColor)
            binding.pieChartCategories.invalidate()
            return
        }

        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        for (category in categorySpending) {
            entries.add(PieEntry(category.amount.toFloat(), category.categoryName))
            try {
                colors.add(Color.parseColor(category.colorCode))
            } catch (e: Exception) {
                colors.add(ColorTemplate.PASTEL_COLORS[categorySpending.indexOf(category) % ColorTemplate.PASTEL_COLORS.size])
            }
        }

        val dataSet = PieDataSet(entries, "Категории").apply {
            setDrawIcons(false)
            sliceSpace = 3f
            selectionShift = 5f
            setColors(colors)
        }

        val data = PieData(dataSet).apply {
            setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return formatCurrency(value.toDouble())
                }
            })
            setValueTextSize(12f)
            setValueTextColor(Color.WHITE)
        }

        binding.pieChartCategories.data = data
        binding.pieChartCategories.highlightValues(null)
        binding.pieChartCategories.invalidate()
    }

    private fun updateBarChart(monthlyData: List<StatisticsViewModel.MonthlySpending>) {
        val textColor = if (isUsingNightMode()) Color.WHITE else Color.BLACK

        if (monthlyData.isEmpty()) {
            binding.barChartMonthly.setNoDataText("Нет данных для отображения")
            binding.barChartMonthly.setNoDataTextColor(textColor)
            binding.barChartMonthly.invalidate()
            return
        }

        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        for (i in monthlyData.indices) {
            entries.add(BarEntry(i.toFloat(), monthlyData[i].amount.toFloat()))
            labels.add(monthlyData[i].month)
        }

        val dataSet = BarDataSet(entries, "Расходы по месяцам").apply {
            color = ContextCompat.getColor(requireContext(), R.color.md_theme_light_primary)
            valueTextColor = textColor
            valueTextSize = 12f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return formatCurrency(value.toDouble())
                }
            }
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.5f
        }

        binding.barChartMonthly.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        binding.barChartMonthly.data = barData
        binding.barChartMonthly.invalidate()
    }

    private fun updateTypeChart(typeData: List<StatisticsViewModel.SubscriptionTypeSpending>) {
        val textColor = if (isUsingNightMode()) Color.WHITE else Color.BLACK

        if (typeData.isEmpty()) {
            binding.pieChartTypes.setNoDataText("Нет данных для отображения")
            binding.pieChartTypes.setNoDataTextColor(textColor)
            binding.pieChartTypes.invalidate()
            return
        }

        val entries = mutableListOf<PieEntry>()
        val colors = mutableListOf<Int>()

        for (type in typeData) {
            if (type.amount > 0) {
                entries.add(PieEntry(type.amount.toFloat(), type.type))
                try {
                    colors.add(Color.parseColor(type.colorCode))
                } catch (e: Exception) {
                    colors.add(ColorTemplate.PASTEL_COLORS[typeData.indexOf(type) % ColorTemplate.PASTEL_COLORS.size])
                }
            }
        }

        val dataSet = PieDataSet(entries, "Типы подписок").apply {
            setDrawIcons(false)
            sliceSpace = 3f
            selectionShift = 5f
            setColors(colors)
        }

        val data = PieData(dataSet).apply {
            setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return formatCurrency(value.toDouble())
                }
            })
            setValueTextSize(12f)
            setValueTextColor(Color.WHITE)
        }

        binding.pieChartTypes.data = data
        binding.pieChartTypes.highlightValues(null)
        binding.pieChartTypes.invalidate()
    }

    private fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
        format.maximumFractionDigits = 0
        return format.format(amount).replace(",00", "")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}