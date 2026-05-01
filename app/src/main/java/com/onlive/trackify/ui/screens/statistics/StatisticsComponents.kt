package com.onlive.trackify.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.onlive.trackify.R
import com.onlive.trackify.ui.components.AutoSizeText
import com.onlive.trackify.ui.components.TrackifyCard
import com.onlive.trackify.ui.components.charts.BarChart
import com.onlive.trackify.ui.components.charts.BarChartData
import com.onlive.trackify.ui.components.charts.CategorySpendingBar
import com.onlive.trackify.utils.stringResource
import com.onlive.trackify.viewmodel.StatisticsViewModel

@Composable
fun TotalSpendingCard(
    formattedMonthlySpending: String,
    formattedYearlySpending: String,
    modifier: Modifier = Modifier,
) {
    TrackifyCard(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                AutoSizeText(
                    text = formattedMonthlySpending,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.per_month),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                AutoSizeText(
                    text = formattedYearlySpending,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.per_year),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CategorySpendingCard(
    categories: List<StatisticsViewModel.CategorySpending>,
    totalAmount: Double,
    formattedTotalAmount: String,
    perMonthText: String,
    formatAmount: (Double) -> String,
    modifier: Modifier = Modifier
) {
    TrackifyCard(
        title = stringResource(R.string.spending_by_category),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.total_category),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))

                    AutoSizeText(
                        text = formattedTotalAmount,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }

                Text(
                    text = perMonthText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            CategorySpendingBar(
                categories = categories,
                totalAmount = totalAmount,
                formatAmount = formatAmount
            )
        }
    }
}

@Composable
fun MonthlySpendingCard(
    monthlySpending: List<StatisticsViewModel.MonthlySpending>,
    modifier: Modifier = Modifier
) {
    val barChartData = monthlySpending.map { monthly ->
        BarChartData(
            label = monthly.month,
            value = monthly.amount
        )
    }

    TrackifyCard(
        title = stringResource(R.string.spending_over_time),
        modifier = modifier
    ) {
        BarChart(
            data = barChartData,
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(top = 8.dp, bottom = 8.dp)
        )
    }
}
