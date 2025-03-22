package com.onlive.trackify.ui.screens.payments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
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
import com.onlive.trackify.viewmodel.PaymentViewModel
import com.onlive.trackify.viewmodel.SubscriptionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BulkPaymentActionsScreen(
    onNavigateBack: () -> Unit,
    paymentViewModel: PaymentViewModel = viewModel(),
    subscriptionViewModel: SubscriptionViewModel = viewModel()
) {
    val allPayments by paymentViewModel.allPayments.observeAsState(emptyList())
    val subscriptions by subscriptionViewModel.allActiveSubscriptions.observeAsState(emptyList())
    val context = LocalContext.current

    var selectedPayments by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var filterTabIndex by remember { mutableStateOf(0) }

    // Состояние для диалогов
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showSuccessSnackbar by remember { mutableStateOf(false) }
    var snackbarMessage by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    val filteredPayments = when (filterTabIndex) {
        0 -> allPayments
        1 -> allPayments.filter { it.status == PaymentStatus.PENDING }
        2 -> allPayments.filter { it.status == PaymentStatus.CONFIRMED }
        3 -> allPayments.filter { it.status == PaymentStatus.MANUAL }
        else -> allPayments
    }

    // Эффект для снэкбара
    LaunchedEffect(showSuccessSnackbar) {
        if (showSuccessSnackbar) {
            snackbarHostState.showSnackbar(
                message = snackbarMessage,
                duration = SnackbarDuration.Short
            )
            showSuccessSnackbar = false
        }
    }

    Scaffold(
        topBar = {
            TrackifyTopAppBar(
                title = stringResource(R.string.bulk_payment_title),
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = filterTabIndex) {
                Tab(
                    selected = filterTabIndex == 0,
                    onClick = { filterTabIndex = 0 },
                    text = { Text(stringResource(R.string.all_payments)) }
                )
                Tab(
                    selected = filterTabIndex == 1,
                    onClick = { filterTabIndex = 1 },
                    text = { Text(stringResource(R.string.payments_pending_confirmation)) }
                )
                Tab(
                    selected = filterTabIndex == 2,
                    onClick = { filterTabIndex = 2 },
                    text = { Text(stringResource(R.string.confirmed_payments)) }
                )
                Tab(
                    selected = filterTabIndex == 3,
                    onClick = { filterTabIndex = 3 },
                    text = { Text(stringResource(R.string.manual_payments)) }
                )
            }

            Text(
                text = stringResource(R.string.bulk_payment_instruction),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { selectedPayments = filteredPayments.map { it.paymentId }.toSet() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.payments_select_all))
                }

                OutlinedButton(
                    onClick = { selectedPayments = emptySet() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (selectedPayments.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.payments_selected_count, selectedPayments.size),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showConfirmDialog = true },
                        modifier = Modifier.weight(1f),
                        enabled = selectedPayments.any { paymentId ->
                            allPayments.find { it.paymentId == paymentId }?.status ==
                                    PaymentStatus.PENDING
                        }
                    ) {
                        Text(stringResource(R.string.confirm_selected))
                    }

                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(stringResource(R.string.delete_selected))
                    }
                }
            }

            if (filteredPayments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_payments_for_bulk),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredPayments) { payment ->
                        val isSelected = selectedPayments.contains(payment.paymentId)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = isSelected,
                                    onClick = {
                                        selectedPayments = if (isSelected) {
                                            selectedPayments - payment.paymentId
                                        } else {
                                            selectedPayments + payment.paymentId
                                        }
                                    }
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        selectedPayments = if (checked) {
                                            selectedPayments + payment.paymentId
                                        } else {
                                            selectedPayments - payment.paymentId
                                        }
                                    }
                                )

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = subscriptions.find { it.subscriptionId == payment.subscriptionId }?.name
                                            ?: stringResource(R.string.unknown),
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Text(
                                        text = com.onlive.trackify.utils.DateUtils.formatDate(payment.date),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (!payment.notes.isNullOrEmpty()) {
                                        Text(
                                            text = payment.notes,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Text(
                                    text = com.onlive.trackify.utils.CurrencyFormatter.formatAmount(context, payment.amount),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Диалог подтверждения платежей
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(stringResource(R.string.confirm_selected)) },
            text = {
                val pendingCount = selectedPayments.count { paymentId ->
                    allPayments.find { it.paymentId == paymentId }?.status == PaymentStatus.PENDING
                }
                Text("Вы уверены, что хотите подтвердить $pendingCount платежей?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Подтверждаем выбранные платежи со статусом PENDING
                        val pendingPaymentIds = selectedPayments.filter { paymentId ->
                            allPayments.find { it.paymentId == paymentId }?.status == PaymentStatus.PENDING
                        }

                        pendingPaymentIds.forEach { paymentId ->
                            paymentViewModel.confirmPayment(paymentId)
                        }

                        // Показываем сообщение об успехе
                        snackbarMessage = "Подтверждено ${pendingPaymentIds.size} платежей"
                        showSuccessSnackbar = true

                        // Закрываем диалог
                        showConfirmDialog = false
                    }
                ) {
                    Text(stringResource(R.string.confirm_selected))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Диалог удаления платежей
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_selected)) },
            text = { Text("Вы уверены, что хотите удалить ${selectedPayments.size} платежей? Это действие нельзя отменить.") },
            confirmButton = {
                Button(
                    onClick = {
                        // Удаляем выбранные платежи
                        var deletedCount = 0
                        selectedPayments.forEach { paymentId ->
                            allPayments.find { it.paymentId == paymentId }?.let { payment ->
                                paymentViewModel.delete(payment)
                                deletedCount++
                            }
                        }

                        // Очищаем выбранные платежи
                        selectedPayments = emptySet()

                        // Показываем сообщение об успехе
                        snackbarMessage = "Удалено $deletedCount платежей"
                        showSuccessSnackbar = true

                        // Закрываем диалог
                        showDeleteDialog = false
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
                    onClick = { showDeleteDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}