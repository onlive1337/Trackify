package com.onlive.trackify.ui.screens.payments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.PaymentStatus
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

    var paymentToDelete by remember { mutableStateOf<Payment?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var selectedPayment by remember { mutableStateOf<Payment?>(null) }

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
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
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
                                selectedPayment = payment
                                onAddPayment(payment.subscriptionId)
                            },
                            onConfirmClick = {
                                paymentViewModel.confirmPayment(payment)
                            },
                            onDeleteClick = {
                                paymentToDelete = payment
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog && paymentToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                paymentToDelete = null
            },
            title = { Text(stringResource(R.string.delete_payment_confirmation)) },
            text = { Text(stringResource(R.string.delete_payment_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        paymentToDelete?.let { payment ->
                            paymentViewModel.delete(payment)
                        }
                        showDeleteDialog = false
                        paymentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        paymentToDelete = null
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
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

private data class StatusInfo(
    val backgroundColor: Color,
    val contentColor: Color,
    val text: String,
    val icon: ImageVector?
)

@Composable
fun PaymentItem(
    payment: Payment,
    subscriptionName: String,
    formatAmount: (Double) -> String,
    onPaymentClick: () -> Unit,
    onConfirmClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPaymentClick)
            .padding(vertical = 4.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E2630),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subscriptionName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = DateUtils.formatDate(payment.date),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = formatAmount(payment.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.more_actions),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.edit_payment)) },
                            onClick = {
                                showMenu = false
                                onPaymentClick()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete)) },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val statusInfo = when (payment.status) {
                    PaymentStatus.PENDING -> StatusInfo(
                        backgroundColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error,
                        text = stringResource(R.string.payment_status_pending),
                        icon = Icons.Filled.Warning
                    )
                    PaymentStatus.CONFIRMED -> StatusInfo(
                        backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.primary,
                        text = stringResource(R.string.payment_status_confirmed),
                        icon = Icons.Filled.CheckCircle
                    )
                    PaymentStatus.MANUAL -> StatusInfo(
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.secondary,
                        text = stringResource(R.string.payment_status_manual),
                        icon = null
                    )
                }

                Surface(
                    color = statusInfo.backgroundColor,
                    contentColor = statusInfo.contentColor,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        statusInfo.icon?.let {
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }

                        Text(
                            text = statusInfo.text,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                if (payment.status == PaymentStatus.PENDING) {
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = onConfirmClick,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(stringResource(R.string.confirm_payment))
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (!payment.notes.isNullOrEmpty()) {
                    Box {
                        var showNotes by remember { mutableStateOf(false) }

                        IconButton(onClick = { showNotes = !showNotes }) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = stringResource(R.string.notes),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (showNotes) {
                            Popup(
                                alignment = Alignment.CenterEnd,
                                onDismissRequest = { showNotes = false }
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 3.dp,
                                    shadowElevation = 3.dp
                                ) {
                                    Text(
                                        text = payment.notes,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(12.dp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}