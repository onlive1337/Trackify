package com.onlive.trackify.ui.screens.statistics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.utils.CurrencyFormatter
import com.onlive.trackify.utils.LocalLocalizedContext
import com.onlive.trackify.utils.stringResource
import com.onlive.trackify.viewmodel.StatisticsViewModel

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = viewModel()
) {
    val context = LocalLocalizedContext.current
    val isLoading by viewModel.isLoading.observeAsState(true)
    val totalMonthlySpending by viewModel.totalMonthlySpending.observeAsState(0.0)
    val totalYearlySpending by viewModel.totalYearlySpending.observeAsState(0.0)
    val spendingByCategory by viewModel.spendingByCategory.observeAsState(emptyList())
    val monthlySpendingHistory by viewModel.monthlySpendingHistory.observeAsState(emptyList())
    val subscriptionTypeSpending by viewModel.subscriptionTypeSpending.observeAsState(emptyList())

    val formattedMonthlySpending = CurrencyFormatter.formatAmount(context, totalMonthlySpending)
    val formattedYearlySpending = CurrencyFormatter.formatAmount(context, totalYearlySpending)

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.loading),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = !isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                TotalSpendingCard(
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
}