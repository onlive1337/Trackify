package com.onlive.trackify.ui.screens.statistics

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.utils.LocalCurrency
import com.onlive.trackify.viewmodel.StatisticsViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel = viewModel(factory = StatisticsViewModel.Factory)
) {
    val currency = LocalCurrency.current
    val formatAmount: (Double) -> String = remember(currency) {
        { amount -> currency.format(amount) }
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = uiState.isLoading,
            enter = fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()),
            exit = fadeOut(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec())
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }

        AnimatedVisibility(
            visible = !uiState.isLoading,
            enter = fadeIn(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec()) +
                    slideInVertically(
                        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                        initialOffsetY = { it / 12 }
                    ),
            exit = fadeOut(animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec())
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
                    monthlyAmount = uiState.totalMonthlySpending,
                    yearlyAmount = uiState.totalYearlySpending,
                    formatAmount = formatAmount
                )

                if (uiState.spendingByCategory.isNotEmpty()) {
                    CategoryBreakdownCard(
                        categories = uiState.spendingByCategory,
                        totalAmount = uiState.totalMonthlySpending,
                        formatAmount = formatAmount
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}