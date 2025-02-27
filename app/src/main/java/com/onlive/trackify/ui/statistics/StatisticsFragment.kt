package com.onlive.trackify.ui.statistics

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.onlive.trackify.R
import com.onlive.trackify.databinding.FragmentStatisticsBinding
import com.onlive.trackify.viewmodel.StatisticsViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    private val statisticsViewModel: StatisticsViewModel by viewModels()

    private lateinit var categoryLegendAdapter: CategoryLegendAdapter
    private lateinit var typeLegendAdapter: TypeLegendAdapter
    private lateinit var donutChartCategory: DonutChartView
    private lateinit var donutChartType: DonutChartView
    private lateinit var barChart: BarChart

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

        setupCategoryChart()
        setupMonthlyBarChart()
        setupTypeChart()
        setupAdapters()

        showLoadingState(true)
        observeStatistics()
    }

    private fun setupAdapters() {
        categoryLegendAdapter = CategoryLegendAdapter()
        binding.recyclerViewCategoryLegend.apply {
            adapter = categoryLegendAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }

        typeLegendAdapter = TypeLegendAdapter()
        binding.recyclerViewTypeLegend.apply {
            adapter = typeLegendAdapter
            layoutManager = LinearLayoutManager(requireContext())
            isNestedScrollingEnabled = false
        }
    }

    private fun setupCategoryChart() {
        donutChartCategory = DonutChartView(requireContext())
        binding.categoryChartContainer.addView(donutChartCategory)

        val layoutParams = donutChartCategory.layoutParams as ViewGroup.LayoutParams
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        donutChartCategory.layoutParams = layoutParams
    }

    private fun setupTypeChart() {
        donutChartType = DonutChartView(requireContext())
        binding.typeChartContainer.addView(donutChartType)

        val layoutParams = donutChartType.layoutParams as ViewGroup.LayoutParams
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        donutChartType.layoutParams = layoutParams
    }

    private fun setupMonthlyBarChart() {
        barChart = BarChart(requireContext())
        binding.monthlyChartContainer.addView(barChart)

        styleBarChart()
    }

    private fun styleBarChart() {
        val isNightMode = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES

        val textColor = if (isNightMode) Color.WHITE else Color.BLACK
        val outlineColor = ContextCompat.getColor(requireContext(), if (isNightMode)
            R.color.md_theme_dark_outline else R.color.md_theme_light_outline)
        val primaryColor = ContextCompat.getColor(requireContext(), if (isNightMode)
            R.color.md_theme_dark_primary else R.color.md_theme_light_primary)

        barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            setPinchZoom(false)
            isDoubleTapToZoomEnabled = false
            setScaleEnabled(false)
            setExtraOffsets(10f, 20f, 10f, 10f)

            isHighlightPerTapEnabled = false
            setDrawMarkers(false)

            axisLeft.apply {
                setDrawGridLines(true)
                gridColor = outlineColor
                setDrawAxisLine(false)
                granularity = 1f
                axisMinimum = 0f
                setTextColor(textColor)
                textSize = 10f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return formatCurrency(value.toDouble())
                    }
                }
                setLabelCount(5, true)
            }

            axisRight.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                setDrawAxisLine(true)
                axisLineColor = outlineColor
                setTextColor(textColor)
                textSize = 10f
            }

            legend.isEnabled = false

            setNoDataTextColor(textColor)
            setNoDataText("Нет данных для отображения")
            animateY(800)
        }
    }

    private fun showLoadingState(isLoading: Boolean) {
        binding.loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.statisticsContent.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun observeStatistics() {
        statisticsViewModel.totalMonthlySpending.observe(viewLifecycleOwner) { monthlySpending ->
            binding.textViewMonthlySpending.text = formatCurrency(monthlySpending)
        }

        statisticsViewModel.totalYearlySpending.observe(viewLifecycleOwner) { yearlySpending ->
            binding.textViewYearlySpending.text = formatCurrency(yearlySpending)
        }

        statisticsViewModel.spendingByCategory.observe(viewLifecycleOwner) { categorySpending ->
            lifecycleScope.launch {
                updateCategoryChart(categorySpending)
            }
        }

        statisticsViewModel.monthlySpendingHistory.observe(viewLifecycleOwner) { monthlyHistory ->
            lifecycleScope.launch {
                updateBarChart(monthlyHistory)
            }
        }

        statisticsViewModel.subscriptionTypeSpending.observe(viewLifecycleOwner) { typeSpending ->
            lifecycleScope.launch {
                updateTypeChart(typeSpending)
            }
        }

        showLoadingState(false)
    }

    private fun updateCategoryChart(categorySpending: List<StatisticsViewModel.CategorySpending>) {
        if (categorySpending.isEmpty()) {
            binding.cardCategorySpending.visibility = View.GONE
            return
        }

        binding.cardCategorySpending.visibility = View.VISIBLE
        val totalAmount = categorySpending.sumOf { it.amount }

        val chartItems = categorySpending.map {
            DonutChartView.ChartItem(it.amount, it.colorCode)
        }

        donutChartCategory.setData(
            items = chartItems,
            centerText = formatCurrency(totalAmount),
            centerSubText = "всего"
        )

        categoryLegendAdapter.submitData(categorySpending, totalAmount)
    }

    private fun updateTypeChart(typeSpending: List<StatisticsViewModel.SubscriptionTypeSpending>) {
        if (typeSpending.isEmpty()) {
            binding.cardSubscriptionTypes.visibility = View.GONE
            return
        }

        binding.cardSubscriptionTypes.visibility = View.VISIBLE
        val totalAmount = typeSpending.sumOf { it.amount }

        val chartItems = typeSpending.map {
            DonutChartView.ChartItem(it.amount, it.colorCode)
        }

        donutChartType.setData(
            items = chartItems,
            centerText = formatCurrency(totalAmount),
            centerSubText = "в месяц"
        )

        typeLegendAdapter.submitData(typeSpending, totalAmount)
    }

    private fun updateBarChart(monthlyData: List<StatisticsViewModel.MonthlySpending>) {
        if (monthlyData.isEmpty()) {
            binding.cardMonthlyHistory.visibility = View.GONE
            return
        }

        binding.cardMonthlyHistory.visibility = View.VISIBLE

        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        for (i in monthlyData.indices) {
            entries.add(BarEntry(i.toFloat(), monthlyData[i].amount.toFloat()))
            labels.add(monthlyData[i].month)
        }

        val isNightMode = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
        val primaryColor = ContextCompat.getColor(requireContext(), if (isNightMode)
            R.color.md_theme_dark_primary else R.color.md_theme_light_primary)
        val textColor = if (isNightMode) Color.WHITE else Color.BLACK

        val dataSet = BarDataSet(entries, "Расходы по месяцам").apply {
            color = primaryColor
            valueTextColor = textColor
            valueTextSize = 12f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return formatCurrency(value.toDouble(), false)
                }
            }
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.7f
        }

        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.data = barData
        barChart.invalidate()
    }

    private fun formatCurrency(amount: Double, includeSymbol: Boolean = true): String {
        val format = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
        format.maximumFractionDigits = 0
        val formatted = format.format(amount)

        return if (includeSymbol) {
            formatted
        } else {
            formatted.replace("₽", "").trim()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}