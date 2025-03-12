package com.onlive.trackify.ui.components.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun BarChart(
    data: List<BarChartData>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    axisColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
    showLabels: Boolean = true
) {
    val maxValue = data.maxOfOrNull { it.value } ?: 0.0
    val bottomPadding = if (showLabels) 40.dp else 0.dp

    Box(
        modifier = modifier.height(300.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val barWidth = canvasWidth / data.size * 0.6f
            val spacing = canvasWidth / data.size * 0.4f / 2
            val heightRatio = canvasHeight / maxValue.toFloat()

            drawLine(
                color = axisColor,
                start = Offset(0f, canvasHeight),
                end = Offset(canvasWidth, canvasHeight),
                strokeWidth = 1.5f
            )

            data.forEachIndexed { index, item ->
                val barHeight = (item.value * heightRatio).toFloat()
                val left = index * (barWidth + spacing * 2) + spacing

                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(left, canvasHeight - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
            }
        }

        if (showLabels) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                data.forEach { item ->
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(32.dp)
                    )
                }
            }
        }
    }
}

data class BarChartData(
    val label: String,
    val value: Double
)