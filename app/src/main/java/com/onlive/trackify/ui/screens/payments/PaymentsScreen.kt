package com.onlive.trackify.ui.screens.payments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.PaymentStatus
import com.onlive.trackify.ui.components.TrackifyCard
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.utils.CurrencyFormatter
import com.onlive.trackify.utils.DateUtils
import com.onlive.trackify.viewmodel.PaymentViewModel
import com.onlive.trackify.viewmodel.SubscriptionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(
    onAddPayment: (Long) -> Unit,
    onNavigateToBulkActions: () -> Unit = {},
    onNavigateToPendingPayments: () -> Unit = {},
    modifier: Modifier = Modifier,
    paymentViewModel: PaymentViewModel = viewModel(),
    subscriptionViewModel: SubscriptionViewModel = viewModel()
) {
    val payments by paymentViewModel.allPayments.observeAsState(emptyList())
    val subscriptions by subscriptionViewModel.allActiveSubscriptions.observeAsState(emptyList())
    val pendingPaymentsCount by paymentViewModel.pendingPaymentsCount.observeAsState(0)

    val context = LocalContext.current

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
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(payments) { payment ->
                        val subscription = subscriptions.find { it.subscriptionId == payment.subscriptionId }
                        PaymentItem(
                            payment = payment,
                            subscriptionName = subscription?.name ?: stringResource(R.string.unknown),
                            formatAmount = { amount -> CurrencyFormatter.formatAmount(context, amount) },
                            onPaymentClick = {
                            },
                            onConfirmClick = {
                                paymentViewModel.confirmPayment(payment)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
fun PaymentItem(
    payment: Payment,
    subscriptionName: String,
    formatAmount: (Double) -> String,
    onPaymentClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    TrackifyCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPaymentClick)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateUtils.formatDate(payment.date),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = formatAmount(payment.amount),
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

            PaymentStatusIndicator(
                status = payment.status,
                onConfirmClick = if (payment.status == PaymentStatus.PENDING) onConfirmClick else null
            )

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
fun PaymentStatusIndicator(
    status: PaymentStatus,
    onConfirmClick: (() -> Unit)? = null
) {
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

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = backgroundColor,
            contentColor = contentColor,
            shape = MaterialTheme.shapes.small
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = when (status) {
                        PaymentStatus.PENDING -> stringResource(R.string.payment_status_pending)
                        PaymentStatus.CONFIRMED -> stringResource(R.string.payment_status_confirmed)
                        PaymentStatus.MANUAL -> stringResource(R.string.payment_status_manual)
                    },
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        if (onConfirmClick != null) {
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onConfirmClick) {
                Text(stringResource(R.string.confirm_payment))
            }
        }
    }
}