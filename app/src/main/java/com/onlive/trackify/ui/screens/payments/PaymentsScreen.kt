package com.onlive.trackify.ui.screens.payments

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.ui.components.CategoryAvatar
import com.onlive.trackify.ui.components.EmptyState
import com.onlive.trackify.ui.components.TrackifyFab
import com.onlive.trackify.ui.components.resolveCategoryColor
import com.onlive.trackify.utils.CurrencyFormatter
import com.onlive.trackify.utils.DateUtils
import com.onlive.trackify.utils.stringResource
import com.onlive.trackify.viewmodel.PaymentViewModel
import com.onlive.trackify.viewmodel.SubscriptionViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PaymentsScreen(
    onAddPayment: (Long, Long) -> Unit,
    paymentViewModel: PaymentViewModel = viewModel(),
    subscriptionViewModel: SubscriptionViewModel = viewModel()
) {
    val context = LocalContext.current
    val allPayments by paymentViewModel.allPayments.observeAsState(emptyList())
    val allSubscriptions by subscriptionViewModel.allSubscriptions.observeAsState(emptyList())

    val showDeleteDialogState = remember { mutableStateOf<Payment?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (allPayments.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.Payments,
                title = stringResource(R.string.no_payments)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(
                    items = allPayments.sortedByDescending { it.date },
                    key = { it.paymentId }
                ) { payment ->
                    val subscription = allSubscriptions.find { it.subscriptionId == payment.subscriptionId }
                    val subscriptionName = subscription?.name ?: stringResource(R.string.unknown)

                    PaymentItem(
                        payment = payment,
                        subscriptionName = subscriptionName,
                        categoryColor = subscription?.categoryColor,
                        formatAmount = { amount -> CurrencyFormatter.formatAmount(context, amount) },
                        onEdit = { onAddPayment(payment.subscriptionId, payment.paymentId) },
                        onDelete = { showDeleteDialogState.value = payment }
                    )
                }
            }
        }

        TrackifyFab(
            onClick = { onAddPayment(-1L, -1L) },
            contentDescription = stringResource(R.string.add_payment),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
        )
    }

    if (showDeleteDialogState.value != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialogState.value = null },
            title = { Text(stringResource(R.string.delete_payment_confirmation)) },
            text = { Text(stringResource(R.string.delete_payment_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialogState.value?.let { paymentViewModel.delete(it) }
                        showDeleteDialogState.value = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialogState.value = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PaymentItem(
    payment: Payment,
    subscriptionName: String,
    categoryColor: String?,
    formatAmount: (Double) -> String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val avatarColor = resolveCategoryColor(categoryColor, MaterialTheme.colorScheme.surfaceVariant)
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                CategoryAvatar(color = avatarColor)

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subscriptionName,
                        style = MaterialTheme.typography.titleMediumEmphasized
                    )
                    Text(
                        text = DateUtils.formatDate(payment.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = formatAmount(payment.amount),
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.edit_payment)) },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = stringResource(R.string.delete),
                                    color = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            if (!payment.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = payment.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}