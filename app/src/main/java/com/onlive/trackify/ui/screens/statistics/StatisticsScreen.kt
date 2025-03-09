package com.onlive.trackify.ui.screens.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.ui.components.TrackifyCard
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
                    yearlySpending = totalYearlySpending
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (spendingByCategory.isNotEmpty()) {
                    CategorySpendingCard(categories = spendingByCategory)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (subscriptionTypeSpending.isNotEmpty()) {
                    SubscriptionTypeSpendingCard(subscriptionTypes = subscriptionTypeSpending)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (monthlySpendingHistory.isNotEmpty()) {
                    MonthlySpendingCard(monthlySpending = monthlySpendingHistory)
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun TotalSpendingCard(
    monthlySpending: Double,
    yearlySpending: Double
) {
    TrackifyCard(
        title = stringResource(R.string.total),
        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        contentColor = MaterialTheme.colorScheme.onSurface
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
                    text = stringResource(R.string.monthly_spending),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatCurrency(monthlySpending),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.yearly_spending),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatCurrency(yearlySpending),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CategorySpendingCard(
    categories: List<StatisticsViewModel.CategorySpending>
) {
    val totalSpending = categories.sumOf { it.amount }

    TrackifyCard(
        title = stringResource(R.string.spending_by_category),
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
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
                CategoryPieChart(
                    categories = categories,
                    totalAmount = totalSpending
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
                        percentage = (category.amount / totalSpending * 100).toInt(),
                        colorCode = category.colorCode
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
fun CategoryPieChart(
    categories: List<StatisticsViewModel.CategorySpending>,
    totalAmount: Double
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val radius = minOf(canvasWidth, canvasHeight) / 2 * 0.8f
            val center = Offset(canvasWidth / 2, canvasHeight / 2)

            var startAngle = 0f

            categories.forEach { category ->
                val sweepAngle = (category.amount / totalAmount * 360f).toFloat()
                val categoryColor = try {
                    Color(android.graphics.Color.parseColor(category.colorCode))
                } catch (e: Exception) {
                    Color.Gray
                }

                drawArc(
                    color = categoryColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )

                startAngle += sweepAngle
            }

            drawCircle(
                color = MaterialTheme.colorScheme.surface,
                radius = radius * 0.6f,
                center = center
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatCurrency(totalAmount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.per_month),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CategorySpendingItem(
    categoryName: String,
    amount: Double,
    percentage: Int,
    colorCode: String
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
            text = formatCurrency(amount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SubscriptionTypeSpendingCard(
    subscriptionTypes: List<StatisticsViewModel.SubscriptionTypeSpending>
) {
    val totalAmount = subscriptionTypes.sumOf { it.amount }

    TrackifyCard(
        title = stringResource(R.string.subscription_types),
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            subscriptionTypes.forEach { type ->
                SubscriptionTypeItem(
                    typeName = type.type,
                    amount = type.amount,
                    percentage = (type.amount / totalAmount * 100).toInt(),
                    colorCode = type.colorCode
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun SubscriptionTypeItem(
    typeName: String,
    amount: Double,
    percentage: Int,
    colorCode: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
                text = typeName,
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
                text = formatCurrency(amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage / 100f)
                    .height(4.dp)
                    .background(
                        try {
                            Color(android.graphics.Color.parseColor(colorCode))
                        } catch (e: Exception) {
                            Color.Gray
                        }
                    )
            )
        }
    }
}

@Composable
fun MonthlySpendingCard(
    monthlySpending: List<StatisticsViewModel.MonthlySpending>
) {
    val maxAmount = monthlySpending.maxOfOrNull { it.amount } ?: 0.0

    TrackifyCard(
        title = stringResource(R.string.spending_over_time),
        backgroundColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            MonthlyBarChart(
                data = monthlySpending,
                maxAmount = maxAmount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .padding(top = 16.dp, bottom = 32.dp)
            )
        }
    }
}

@Composable
fun MonthlyBarChart(
    data: List<StatisticsViewModel.MonthlySpending>,
    maxAmount: Double,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barWidth = canvasWidth / data.size * 0.6f
        val spacing = canvasWidth / data.size * 0.4f / 2
        val bottomPadding = 30f
        val heightRatio = (canvasHeight - bottomPadding) / (maxAmount * 1.1f).toFloat()

        drawLine(
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            start = Offset(0f, canvasHeight - bottomPadding),
            end = Offset(canvasWidth, canvasHeight - bottomPadding),
            strokeWidth = 1.5f
        )

        data.forEachIndexed { index, item ->
            val barHeight = (item.amount * heightRatio).toFloat()
            val left = index * (barWidth + spacing * 2) + spacing

            drawRoundRect(
                color = MaterialTheme.colorScheme.primary,
                topLeft = Offset(left, canvasHeight - bottomPadding - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
        }
    }
}

@Composable
private fun formatCurrency(amount: Double, includeSymbol: Boolean = true): String {
    return CurrencyFormatter.formatAmount(LocalContext.current, amount, includeSymbol)
}