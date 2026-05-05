package com.onlive.trackify.ui.screens.statistics

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.onlive.trackify.R
import com.onlive.trackify.ui.components.AutoSizeText
import com.onlive.trackify.utils.stringResource
import com.onlive.trackify.viewmodel.StatisticsViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SpendingHeroCard(
    monthlyAmount: Double,
    yearlyAmount: Double,
    formatAmount: (Double) -> String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Text(
                text = stringResource(R.string.monthly_spending),
                style = MaterialTheme.typography.labelLargeEmphasized,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            AutoSizeText(
                text = formatAmount(monthlyAmount),
                style = MaterialTheme.typography.headlineLargeEmphasized,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                thickness = 1.dp
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.per_year),
                    style = MaterialTheme.typography.labelMediumEmphasized,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.55f)
                )
                Text(
                    text = formatAmount(yearlyAmount),
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CategoryBreakdownCard(
    categories: List<StatisticsViewModel.CategorySpending>,
    totalAmount: Double,
    formatAmount: (Double) -> String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = stringResource(R.string.spending_by_category),
                style = MaterialTheme.typography.labelLargeEmphasized,
                color = MaterialTheme.colorScheme.primary
            )

            categories.take(6).forEach { category ->
                CategoryItem(
                    category = category,
                    totalAmount = totalAmount,
                    formatAmount = formatAmount
                )
            }

            if (categories.size > 6) {
                Text(
                    text = "+${categories.size - 6}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CategoryItem(
    category: StatisticsViewModel.CategorySpending,
    totalAmount: Double,
    formatAmount: (Double) -> String
) {
    val categoryColor = remember(category.colorCode) {
        runCatching { Color(category.colorCode.toColorInt()) }.getOrNull()
    }
    val accentColor = categoryColor ?: Color(0xFF808080)

    val percentage = if (totalAmount > 0.0) (category.amount / totalAmount).toFloat() else 0f

    val startAnimation = remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = if (startAnimation.value) percentage else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bar_${category.categoryId}"
    )

    LaunchedEffect(Unit) { startAnimation.value = true }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )

            Text(
                text = category.categoryName,
                style = MaterialTheme.typography.bodyMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = formatAmount(category.amount),
                style = MaterialTheme.typography.bodyMediumEmphasized,
                color = accentColor,
                fontWeight = FontWeight.Black,
                maxLines = 1
            )
        }

        LinearWavyProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
            color = accentColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            stopSize = WavyProgressIndicatorDefaults.LinearTrackStopIndicatorSize,
            amplitude = { 1f },
            waveSpeed = 0.dp
        )

        Text(
            text = "${(percentage * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmallEmphasized,
            color = accentColor.copy(alpha = 0.8f),
            modifier = Modifier.align(Alignment.End)
        )
    }
}