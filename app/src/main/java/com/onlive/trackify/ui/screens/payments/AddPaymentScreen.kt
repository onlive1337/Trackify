package com.onlive.trackify.ui.screens.payments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.ui.components.SubscriptionSelector
import com.onlive.trackify.ui.components.TrackifyDatePicker
import com.onlive.trackify.ui.components.TrackifyOutlinedCard
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.utils.DateUtils
import com.onlive.trackify.utils.stringResource
import com.onlive.trackify.viewmodel.PaymentViewModel
import com.onlive.trackify.viewmodel.SubscriptionViewModel
import java.util.*

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddPaymentScreen(
    subscriptionId: Long,
    paymentId: Long,
    onNavigateBack: () -> Unit,
    paymentViewModel: PaymentViewModel = viewModel(),
    subscriptionViewModel: SubscriptionViewModel = viewModel()
) {
    val allSubscriptions by subscriptionViewModel.allSubscriptions.observeAsState(emptyList())

    var selectedSubscriptionId by remember { mutableLongStateOf(subscriptionId) }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(Date()) }
    var notes by remember { mutableStateOf("") }

    val existingPayment by paymentViewModel.allPayments.observeAsState(emptyList())
    var hasLoadedExisting by remember { mutableStateOf(false) }

    LaunchedEffect(existingPayment) {
        if (paymentId != -1L && existingPayment.isNotEmpty() && !hasLoadedExisting) {
            val payment = existingPayment.find { it.paymentId == paymentId }
            payment?.let {
                selectedSubscriptionId = it.subscriptionId
                amount = it.amount.toString()
                date = it.date
                notes = it.notes ?: ""
                hasLoadedExisting = true
            }
        }
    }

    val showDatePicker = remember { mutableStateOf(false) }

    var subError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    val requiredFieldStr = stringResource(R.string.required_field)
    val invalidAmountStr = stringResource(R.string.invalid_amount)

    Scaffold(
        topBar = {
            TrackifyTopAppBar(
                title = if (paymentId == -1L) stringResource(R.string.add_payment) else stringResource(R.string.edit_payment),
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            TrackifyOutlinedCard(title = stringResource(R.string.payment_subscription)) {
                Column {
                    SubscriptionSelector(
                        subscriptions = allSubscriptions,
                        selectedSubscriptionId = selectedSubscriptionId,
                        onSubscriptionSelected = {
                            selectedSubscriptionId = it
                            if (subError && selectedSubscriptionId != -1L) subError = false
                        }
                    )
                    if (subError) {
                        Text(
                            text = requiredFieldStr,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TrackifyOutlinedCard(title = stringResource(R.string.payment_amount)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        if (it.isEmpty() || it.toDoubleOrNull() != null) {
                            amount = it
                            if (amountError && it.toDoubleOrNull()?.let { v -> v > 0 } == true) amountError = false
                        }
                    },
                    placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = amountError,
                    supportingText = if (amountError) {
                        { Text(invalidAmountStr) }
                    } else null,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    ),
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TrackifyOutlinedCard(
                title = stringResource(R.string.payment_date),
                onClick = { showDatePicker.value = true }
            ) {
                Text(
                    text = DateUtils.formatDate(date),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TrackifyOutlinedCard(title = stringResource(R.string.payment_notes)) {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    val isSubValid = selectedSubscriptionId != -1L
                    val isAmountValid = amountValue > 0

                    subError = !isSubValid
                    amountError = !isAmountValid

                    if (isSubValid && isAmountValid) {
                        val payment = Payment(
                            paymentId = if (paymentId == -1L) 0 else paymentId,
                            subscriptionId = selectedSubscriptionId,
                            amount = amountValue,
                            date = date,
                            notes = notes.ifBlank { null }
                        )
                        if (paymentId == -1L) {
                            paymentViewModel.insert(payment)
                        } else {
                            paymentViewModel.update(payment)
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text(
                    text = stringResource(R.string.save),
                    style = MaterialTheme.typography.titleMediumEmphasized
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showDatePicker.value) {
        TrackifyDatePicker(
            selectedDate = date,
            onDateSelected = { date = it },
            onDismiss = { showDatePicker.value = false }
        )
    }
}