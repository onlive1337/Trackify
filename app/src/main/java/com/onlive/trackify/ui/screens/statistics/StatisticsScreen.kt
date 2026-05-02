package com.onlive.trackify.ui.screens.statistics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import com.onlive.trackify.utils.CurrencyFormatter
import com.onlive.trackify.utils.LocalLocalizedContext
import com.onlive.trackify.viewmodel.StatisticsViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = viewModel()) {
    val context = LocalLocalizedContext.current
    val isLoading by viewModel.isLoading.observeAsState(true)
    val totalMonthlySpending by viewModel.totalMonthlySpending.observeAsState(0.0)
    val totalYearlySpending by viewModel.totalYearlySpending.observeAsState(0.0)
    val spendingByCategory by viewModel.spendingByCategory.observeAsState(emptyList())

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        AnimatedVisibility(
            visible = !isLoading,
            enter = fadeIn(animationSpec = tween(400)) +
                    slideInVertically(
                        animationSpec = tween(400),
                        initialOffsetY = { it / 12 }
                    ),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(0.dp))

                SpendingHeroCard(
                    monthlyAmount = totalMonthlySpending,
                    yearlyAmount = totalYearlySpending,
                    formatAmount = { CurrencyFormatter.formatAmount(context, it) }
                )

                if (spendingByCategory.isNotEmpty()) {
                    CategoryBreakdownCard(
                        categories = spendingByCategory,
                        totalAmount = totalMonthlySpending,
                        formatAmount = { CurrencyFormatter.formatAmount(context, it) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}