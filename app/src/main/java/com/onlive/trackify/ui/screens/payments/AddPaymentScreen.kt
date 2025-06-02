package com.onlive.trackify.ui.screens.payments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.PaymentStatus
import com.onlive.trackify.ui.components.SubscriptionSelector
import com.onlive.trackify.ui.components.TrackifyDatePicker
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.utils.DateUtils
import com.onlive.trackify.viewmodel.PaymentViewModel
import com.onlive.trackify.viewmodel.SubscriptionViewModel
import java.util.*

@Composable
fun AddPaymentScreen(
    subscriptionId: Long = -1L,
    paymentId: Long = -1L,
    onNavigateBack: () -> Unit,
    paymentViewModel: PaymentViewModel = viewModel(),
    subscriptionViewModel: SubscriptionViewModel = viewModel()
) {
    val subscriptions by subscriptionViewModel.allActiveSubscriptions.observeAsState(emptyList())
    val allPayments by paymentViewModel.allPayments.observeAsState(emptyList())

    val existingPayment = remember(allPayments, paymentId) {
        allPayments.find { it.paymentId == paymentId }
    }

    val isEditing = paymentId != -1L && existingPayment != null

    var selectedSubscriptionId by remember { mutableStateOf(subscriptionId) }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var paymentDate by remember { mutableStateOf(Date()) }

    var isSubscriptionError by remember { mutableStateOf(false) }
    var isAmountError by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(existingPayment) {
        existingPayment?.let {
            selectedSubscriptionId = it.subscriptionId
            amount = it.amount.toString()
            notes = it.notes ?: ""
            paymentDate = it.date
        }
    }

    LaunchedEffect(subscriptionId, subscriptions) {
        if (subscriptionId != -1L && !isEditing && subscriptions.any { it.subscriptionId == subscriptionId }) {
            selectedSubscriptionId = subscriptionId
        }
    }

    Scaffold(
        topBar = {
            TrackifyTopAppBar(
                title = stringResource(if (isEditing) R.string.edit_payment else R.string.add_payment),
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.payment_subscription),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            SubscriptionSelector(
                subscriptions = subscriptions,
                selectedSubscriptionId = selectedSubscriptionId,
                onSubscriptionSelected = {
                    selectedSubscriptionId = it
                    isSubscriptionError = false
                },
                isError = isSubscriptionError
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.payment_amount),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = {
                    amount = it.replace(",", ".")
                    isAmountError = false
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = isAmountError,
                supportingText = if (isAmountError) {
                    { Text(stringResource(R.string.enter_correct_amount)) }
                } else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.payment_date),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(text = DateUtils.formatDate(paymentDate))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.payment_notes),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (selectedSubscriptionId == -1L) {
                        isSubscriptionError = true
                        return@Button
                    }

                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue == null || amountValue <= 0) {
                        isAmountError = true
                        return@Button
                    }

                    if (isEditing && existingPayment != null) {
                        val updatedPayment = existingPayment.copy(
                            subscriptionId = selectedSubscriptionId,
                            amount = amountValue,
                            date = paymentDate,
                            notes = notes.takeIf { it.isNotEmpty() }
                        )
                        paymentViewModel.update(updatedPayment)
                    } else {
                        val payment = Payment(
                            subscriptionId = selectedSubscriptionId,
                            amount = amountValue,
                            date = paymentDate,
                            notes = notes.takeIf { it.isNotEmpty() },
                            status = PaymentStatus.MANUAL
                        )
                        paymentViewModel.insert(payment)
                    }
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }

    if (showDatePicker) {
        TrackifyDatePicker(
            selectedDate = paymentDate,
            onDateSelected = {
                paymentDate = it
            },
            onDismiss = { showDatePicker = false }
        )
    }
}