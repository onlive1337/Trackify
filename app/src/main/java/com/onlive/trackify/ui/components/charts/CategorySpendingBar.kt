package com.onlive.trackify.ui.components.charts

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.onlive.trackify.ui.components.AutoSizeText
import com.onlive.trackify.viewmodel.StatisticsViewModel
import androidx.core.graphics.toColorInt

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
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 20.dp)
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
        Color(category.colorCode.toColorInt())
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val percentage = if (totalAmount > 0) (category.amount / totalAmount) else 0.0
    var startAnimation by remember { mutableStateOf(false) }
    val animatedPercentage by animateFloatAsState(
        targetValue = if (startAnimation) percentage.toFloat() else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bar-animation"
    )

    LaunchedEffect(Unit) {
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

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = category.categoryName,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            AutoSizeText(
                text = formatAmount(category.amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = categoryColor,
                modifier = Modifier.weight(1f, fill = false)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedPercentage)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(categoryColor)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${(percentage * 100).toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = categoryColor,
            modifier = Modifier.align(Alignment.End)
        )
    }
}
