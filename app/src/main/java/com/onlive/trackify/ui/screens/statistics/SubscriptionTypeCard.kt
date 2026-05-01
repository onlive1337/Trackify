package com.onlive.trackify.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.onlive.trackify.R
import com.onlive.trackify.ui.components.AutoSizeText
import com.onlive.trackify.ui.components.TrackifyCard
import com.onlive.trackify.utils.stringResource
import com.onlive.trackify.viewmodel.StatisticsViewModel

@Composable
fun SubscriptionTypeCard(
    subscriptionTypes: List<StatisticsViewModel.SubscriptionTypeSpending>,
    formatAmount: (Double) -> String,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary
    )

    TrackifyCard(
        title = stringResource(R.string.subscription_types_spending),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(top = 8.dp)
        ) {
            subscriptionTypes.forEachIndexed { index, typeSpending ->
                val accentColor = colors.getOrElse(index) { MaterialTheme.colorScheme.primary }

                val localizedType = when (typeSpending.type) {
                    "monthly_spending" -> stringResource(R.string.monthly_spending)
                    "yearly_type" -> stringResource(R.string.yearly_type)
                    else -> typeSpending.type
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = accentColor.copy(alpha = 0.1f)
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(accentColor)
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = localizedType,
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f)
                        )

                        AutoSizeText(
                            text = formatAmount(typeSpending.amount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = accentColor,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
            }
        }
    }
}
