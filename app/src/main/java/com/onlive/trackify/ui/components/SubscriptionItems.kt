package com.onlive.trackify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.utils.CurrencyFormatter
import androidx.compose.ui.platform.LocalContext
import com.onlive.trackify.utils.stringResource
import com.onlive.trackify.R
import androidx.core.graphics.toColorInt

private fun getCategoryColor(colorCode: String?, defaultColor: Color): Color {
    return try {
        colorCode?.let { Color(it.toColorInt()) } ?: defaultColor
    } catch (e: Exception) {
        defaultColor
    }
}

@Composable
fun SubscriptionListItem(
    subscription: Subscription,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val defaultColor = MaterialTheme.colorScheme.surfaceVariant

    val categoryColor = getCategoryColor(subscription.categoryColor, defaultColor)

    val backgroundColor = categoryColor.copy(alpha = 0.15f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(48.dp)
                        .background(
                            color = categoryColor,
                            shape = RoundedCornerShape(8.dp)
                        )
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = subscription.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = subscription.categoryName ?: stringResource(R.string.without_category),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    val formattedPrice = when (subscription.billingFrequency) {
                        BillingFrequency.MONTHLY ->
                            "${CurrencyFormatter.formatAmount(context, subscription.price)}/${stringResource(R.string.month)}"
                        BillingFrequency.YEARLY ->
                            "${CurrencyFormatter.formatAmount(context, subscription.price)}/${stringResource(R.string.year)}"
                    }

                    Text(
                        text = formattedPrice,
                        style = MaterialTheme.typography.titleMedium,
                        color = categoryColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SubscriptionGridItem(
    subscription: Subscription,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val defaultColor = MaterialTheme.colorScheme.surfaceVariant

    val categoryColor = getCategoryColor(subscription.categoryColor, defaultColor)

    val backgroundColor = categoryColor.copy(alpha = 0.15f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        border = null,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(
                            color = categoryColor,
                            shape = RoundedCornerShape(4.dp)
                        )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                val formattedPrice = when (subscription.billingFrequency) {
                    BillingFrequency.MONTHLY ->
                        "${CurrencyFormatter.formatAmount(context, subscription.price)}/${stringResource(R.string.month)}"
                    BillingFrequency.YEARLY ->
                        "${CurrencyFormatter.formatAmount(context, subscription.price)}/${stringResource(R.string.year)}"
                }

                Text(
                    text = formattedPrice,
                    style = MaterialTheme.typography.titleMedium,
                    color = categoryColor,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(categoryColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = subscription.categoryName ?: stringResource(R.string.without_category),
                        style = MaterialTheme.typography.bodySmall,
                        color = categoryColor
                    )
                }
            }
        }
    }
}