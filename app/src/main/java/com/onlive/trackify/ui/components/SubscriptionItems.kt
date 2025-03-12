package com.onlive.trackify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.utils.CurrencyFormatter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.onlive.trackify.R

@Composable
fun SubscriptionListItem(
    subscription: Subscription,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val categoryColor = remember {
                subscription.categoryColor?.let { colorCode ->
                    try {
                        Color(android.graphics.Color.parseColor(colorCode))
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            if (categoryColor != null) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(48.dp)
                        .background(categoryColor)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.titleMedium,
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
                    color = MaterialTheme.colorScheme.primary
                )
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

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val categoryColor = remember {
                subscription.categoryColor?.let { colorCode ->
                    try {
                        Color(android.graphics.Color.parseColor(colorCode))
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            if (categoryColor != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(categoryColor)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = subscription.name,
                style = MaterialTheme.typography.titleMedium,
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
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subscription.categoryName ?: stringResource(R.string.without_category),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}