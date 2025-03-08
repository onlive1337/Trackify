package com.onlive.trackify.ui.screens.payments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Chip
import androidx.compose.material3.ChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.PaymentStatus
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.ui.screens.payments.components.formatDate
import com.onlive.trackify.ui.screens.payments.components.formatPaymentStatus
import com.onlive.trackify.viewmodel.PaymentViewModel
import com.onlive.trackify.viewmodel.SubscriptionViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun PaymentsScreen(
    onAddPayment: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToBulkActions: () -> Unit = {},
    onNavigateToPendingPayments: () -> Unit = {},
    paymentViewModel: PaymentViewModel = viewModel(),
    subscriptionViewModel: SubscriptionViewModel = viewModel()
) {
    val payments by paymentViewModel.allPayments.observeAsState(emptyList())
    val subscriptions by subscriptionViewModel.allActiveSubscriptions.observeAsState(emptyList())
    val pendingPaymentsCount by paymentViewModel.pendingPaymentsCount.observeAsState(0)

    Scaffold(
        topBar = {
            TrackifyTopAppBar(
                title = stringResource(R.string.title_payments)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddPayment(-1L) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.add_payment)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            if (pendingPaymentsCount > 0) {
                PendingPaymentsCard(
                    pendingCount = pendingPaymentsCount,
                    onClick = onNavigateToPendingPayments
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedButton(
                onClick = onNavigateToBulkActions,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.bulk_payment_actions))
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (payments.isEmpty()) {
                EmptyPaymentsView()
            } else {
                PaymentsList(
                    payments = payments,
                    subscriptions = subscriptions,
                    onPaymentClick = { /* TODO: Подробнее о платеже */ }
                )
            }
        }
    }
}

@Composable
fun PendingPaymentsCard(
    pendingCount: Int,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary
            )

            Text(
                text = stringResource(R.string.pending_payments_card_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            )

            Badge(
                containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text(
                    text = pendingCount.toString(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun EmptyPaymentsView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.no_payments),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun PaymentsList(
    payments: List<Payment>,
    subscriptions: List<Subscription>,
    onPaymentClick: (Payment) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(payments) { payment ->
            val subscription = subscriptions.find { it.subscriptionId == payment.subscriptionId }
            PaymentItem(
                payment = payment,
                subscriptionName = subscription?.name ?: stringResource(R.string.unknown),
                onClick = { onPaymentClick(payment) }
            )
        }
    }
}

@Composable
fun PaymentItem(
    payment: Payment,
    subscriptionName: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDate(payment.date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "₽%.0f".format(payment.amount),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subscriptionName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            PaymentStatusChip(status = payment.status)

            if (!payment.notes.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = payment.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun PaymentStatusChip(status: PaymentStatus) {
    val (backgroundColor, contentColor, icon) = when (status) {
        PaymentStatus.PENDING -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.error,
            Icons.Filled.Warning
        )
        PaymentStatus.CONFIRMED -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primary,
            Icons.Filled.CheckCircle
        )
        PaymentStatus.MANUAL -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.secondary,
            null
        )
    }

    Chip(
        onClick = { },
        colors = ChipDefaults.chipColors(
            containerColor = backgroundColor,
            labelColor = contentColor
        ),
        leadingIcon = {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor
                )
            }
        }
    ) {
        Text(
            text = formatPaymentStatus(status),
            style = MaterialTheme.typography.labelMedium
        )
    }
}