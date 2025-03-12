package com.onlive.trackify.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.onlive.trackify.ui.components.TrackifyCard
import com.onlive.trackify.ui.components.charts.BarChart
import com.onlive.trackify.ui.components.charts.BarChartData
import com.onlive.trackify.ui.components.charts.PieChart
import com.onlive.trackify.ui.components.charts.PieChartData
import com.onlive.trackify.viewmodel.StatisticsViewModel

@Composable
fun TotalSpendingCard(
    monthlySpending: Double,
    yearlySpending: Double,
    formattedMonthlySpending: String,
    formattedYearlySpending: String,
    modifier: Modifier = Modifier
) {
    TrackifyCard(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formattedMonthlySpending,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "в месяц",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formattedYearlySpending,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "в год",
                    style = MaterialTheme.typography.bodyMedium,
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
    val pieChartData = categories.map { category ->
        PieChartData(
            value = category.amount,
            color = try {
                Color(android.graphics.Color.parseColor(category.colorCode))
            } catch (e: Exception) {
                Color.Gray
            },
            label = category.categoryName
        )
    }

    TrackifyCard(
        title = "Расходы по категориям",
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .aspectRatio(1f)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                PieChart(
                    data = pieChartData,
                    centerText = "$formattedTotalAmount\n$perMonthText"
                )
            }

            Column(
                modifier = Modifier
                    .weight(0.6f)
                    .padding(start = 8.dp)
            ) {
                categories.take(5).forEach { category ->
                    CategorySpendingItem(
                        categoryName = category.categoryName,
                        amount = category.amount,
                        percentage = (category.amount / totalAmount * 100).toInt(),
                        colorCode = category.colorCode,
                        formattedAmount = formatAmount(category.amount)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (categories.size > 5) {
                    Text(
                        text = "... и ещё ${categories.size - 5}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun CategorySpendingItem(
    categoryName: String,
    amount: Double,
    percentage: Int,
    colorCode: String,
    formattedAmount: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(
                    try {
                        Color(android.graphics.Color.parseColor(colorCode))
                    } catch (e: Exception) {
                        Color.Gray
                    }
                )
        )

        Spacer(modifier = Modifier.size(8.dp))

        Text(
            text = categoryName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.size(8.dp))

        Text(
            text = formattedAmount,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
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
        title = "Динамика расходов",
        modifier = modifier
    ) {
        BarChart(
            data = barChartData,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(top = 8.dp, bottom = 16.dp)
        )
    }
}