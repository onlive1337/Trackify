package com.onlive.trackify.ui.components.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.onlive.trackify.viewmodel.StatisticsViewModel

@Composable
fun CategorySpendingBar(
    categories: List<StatisticsViewModel.CategorySpending>,
    totalAmount: Double,
    formatAmount: (Double) -> String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        categories.take(5).forEach { category ->
            CategoryBarItem(
                category = category,
                totalAmount = totalAmount,
                formatAmount = formatAmount
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (categories.size > 5) {
            Text(
                text = "... и ещё ${categories.size - 5}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun CategoryBarItem(
    category: StatisticsViewModel.CategorySpending,
    totalAmount: Double,
    formatAmount: (Double) -> String
) {
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(category.colorCode))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val percentage = (category.amount / totalAmount)
    var startAnimation by remember { mutableStateOf(false) }
    val animatedPercentage by animateFloatAsState(
        targetValue = if (startAnimation) percentage.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "bar-animation"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(categoryColor)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = category.categoryName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = formatAmount(category.amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = categoryColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedPercentage)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(categoryColor)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "${(percentage * 100).toInt()}%",
            style = MaterialTheme.typography.bodySmall,
            color = categoryColor,
            modifier = Modifier.align(Alignment.End)
        )
    }
}