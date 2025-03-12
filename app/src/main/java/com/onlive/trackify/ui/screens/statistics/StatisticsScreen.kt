package com.onlive.trackify.ui.screens.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.utils.CurrencyFormatter
import com.onlive.trackify.viewmodel.StatisticsViewModel

@Composable
fun StatisticsScreen(
    statisticsViewModel: StatisticsViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val totalMonthlySpending by statisticsViewModel.totalMonthlySpending.observeAsState(0.0)
    val totalYearlySpending by statisticsViewModel.totalYearlySpending.observeAsState(0.0)
    val spendingByCategory by statisticsViewModel.spendingByCategory.observeAsState(emptyList())
    val monthlySpendingHistory by statisticsViewModel.monthlySpendingHistory.observeAsState(emptyList())
    val subscriptionTypeSpending by statisticsViewModel.subscriptionTypeSpending.observeAsState(emptyList())

    val isLoading = remember { false }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TrackifyTopAppBar(
                title = stringResource(R.string.title_statistics)
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                TotalSpendingCard(
                    monthlySpending = totalMonthlySpending,
                    yearlySpending = totalYearlySpending,
                    formattedMonthlySpending = CurrencyFormatter.formatAmount(context, totalMonthlySpending),
                    formattedYearlySpending = CurrencyFormatter.formatAmount(context, totalYearlySpending)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (spendingByCategory.isNotEmpty()) {
                    CategorySpendingCard(
                        categories = spendingByCategory,
                        totalAmount = spendingByCategory.sumOf { it.amount },
                        formattedTotalAmount = CurrencyFormatter.formatAmount(context, spendingByCategory.sumOf { it.amount }),
                        perMonthText = stringResource(R.string.per_month),
                        formatAmount = { amount -> CurrencyFormatter.formatAmount(context, amount) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (monthlySpendingHistory.isNotEmpty()) {
                    MonthlySpendingCard(
                        monthlySpending = monthlySpendingHistory
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}