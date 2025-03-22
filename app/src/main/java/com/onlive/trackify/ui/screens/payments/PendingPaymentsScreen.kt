package com.onlive.trackify.ui.screens.payments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.PaymentStatus
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.utils.CurrencyFormatter
import com.onlive.trackify.viewmodel.PaymentViewModel
import com.onlive.trackify.viewmodel.SubscriptionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingPaymentsScreen(
    onNavigateBack: () -> Unit,
    onAddPayment: (Long) -> Unit,
    paymentViewModel: PaymentViewModel = viewModel(),
    subscriptionViewModel: SubscriptionViewModel = viewModel()
) {
    val pendingPayments by paymentViewModel.pendingPayments.observeAsState(emptyList())
    val subscriptions by subscriptionViewModel.allActiveSubscriptions.observeAsState(emptyList())
    val context = LocalContext.current

    var paymentToDelete by remember { mutableStateOf<Payment?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    var showConfirmAllDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TrackifyTopAppBar(
                title = stringResource(R.string.pending_payments),
                showBackButton = true,
                onBackClick = onNavigateBack,
                actions = {
                    if (pendingPayments.isNotEmpty()) {
                        TextButton(
                            onClick = { showConfirmAllDialog = true }
                        ) {
                            Text(
                                text = stringResource(R.string.confirm_selected),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
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
            if (pendingPayments.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_pending_payments),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pendingPayments) { payment ->
                        val subscription = subscriptions.find { it.subscriptionId == payment.subscriptionId }
                        PaymentItem(
                            payment = payment,
                            subscriptionName = subscription?.name ?: stringResource(R.string.unknown),
                            formatAmount = { amount -> CurrencyFormatter.formatAmount(context, amount) },
                            onPaymentClick = {
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

    if (showConfirmAllDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmAllDialog = false },
            title = { Text(stringResource(R.string.confirm_all_payments_title)) },
            text = { Text(stringResource(R.string.confirm_all_payments_message, pendingPayments.size)) },
            confirmButton = {
                Button(
                    onClick = {
                        pendingPayments.forEach { payment ->
                            paymentViewModel.confirmPayment(payment)
                        }
                        showConfirmAllDialog = false
                    }
                ) {
                    Text(stringResource(R.string.confirm_selected))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmAllDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}