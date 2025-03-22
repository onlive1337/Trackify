package com.onlive.trackify.ui.screens.statistics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.ui.screens.statistics.SubscriptionTypeCard
import com.onlive.trackify.utils.CurrencyFormatter
import com.onlive.trackify.viewmodel.StatisticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = viewModel()
) {
    val context = LocalContext.current
    val totalMonthlySpending by viewModel.totalMonthlySpending.observeAsState(0.0)
    val totalYearlySpending by viewModel.totalYearlySpending.observeAsState(0.0)
    val spendingByCategory by viewModel.spendingByCategory.observeAsState(emptyList())
    val monthlySpendingHistory by viewModel.monthlySpendingHistory.observeAsState(emptyList())
    val subscriptionTypeSpending by viewModel.subscriptionTypeSpending.observeAsState(emptyList())

    val formattedMonthlySpending = CurrencyFormatter.formatAmount(context, totalMonthlySpending)
    val formattedYearlySpending = CurrencyFormatter.formatAmount(context, totalYearlySpending)

    LaunchedEffect(Unit) {
        viewModel.calculateMonthlySpending()
        viewModel.calculateYearlySpending()
        viewModel.calculateSpendingByCategory()
        viewModel.calculateMonthlySpendingHistory()
        viewModel.calculateSpendingBySubscriptionType()
    }

    Scaffold(
        topBar = {
            TrackifyTopAppBar(
                title = stringResource(R.string.title_statistics)
            )
        }
    ) { paddingValues ->
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
                formattedMonthlySpending = formattedMonthlySpending,
                formattedYearlySpending = formattedYearlySpending
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (spendingByCategory.isNotEmpty()) {
                CategorySpendingCard(
                    categories = spendingByCategory,
                    totalAmount = totalMonthlySpending,
                    formattedTotalAmount = formattedMonthlySpending,
                    perMonthText = stringResource(R.string.per_month),
                    formatAmount = { amount -> CurrencyFormatter.formatAmount(context, amount) }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (monthlySpendingHistory.isNotEmpty()) {
                MonthlySpendingCard(monthlySpending = monthlySpendingHistory)

                Spacer(modifier = Modifier.height(16.dp))
            }

            if (subscriptionTypeSpending.isNotEmpty()) {
                SubscriptionTypeCard(
                    subscriptionTypes = subscriptionTypeSpending,
                    formatAmount = { amount -> CurrencyFormatter.formatAmount(context, amount) }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}