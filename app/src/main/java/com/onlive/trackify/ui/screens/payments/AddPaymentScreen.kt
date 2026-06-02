package com.onlive.trackify.ui.screens.payments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.ui.components.FormPickerField
import com.onlive.trackify.ui.components.SubscriptionSelector
import com.onlive.trackify.ui.components.TrackifyDatePicker
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.ui.components.formFieldColors
import com.onlive.trackify.utils.DateUtils
import com.onlive.trackify.utils.stringResource
import com.onlive.trackify.viewmodel.PaymentViewModel
import com.onlive.trackify.viewmodel.SubscriptionViewModel
import java.util.*

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentScreen(
    subscriptionId: Long,
    paymentId: Long,
    onNavigateBack: () -> Unit,
    paymentViewModel: PaymentViewModel = viewModel(),
    subscriptionViewModel: SubscriptionViewModel = viewModel()
) {
    val allSubscriptions by subscriptionViewModel.allSubscriptions.observeAsState(emptyList())

    var selectedSubscriptionId by rememberSaveable { mutableLongStateOf(subscriptionId) }
    var amount by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf(Date()) }
    var notes by rememberSaveable { mutableStateOf("") }

    val existingPayment by paymentViewModel.allPayments.observeAsState(emptyList())
    var hasLoadedExisting by rememberSaveable { mutableStateOf(false) }

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

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TrackifyTopAppBar(
                title = if (paymentId == -1L) stringResource(R.string.add_payment) else stringResource(R.string.edit_payment),
                showBackButton = true,
                onBackClick = onNavigateBack,
                scrollBehavior = scrollBehavior
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
            Spacer(modifier = Modifier.height(8.dp))

            SubscriptionSelector(
                subscriptions = allSubscriptions,
                selectedSubscriptionId = selectedSubscriptionId,
                onSubscriptionSelected = {
                    selectedSubscriptionId = it
                    if (subError && selectedSubscriptionId != -1L) subError = false
                },
                isError = subError,
                errorMessage = requiredFieldStr
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = amount,
                onValueChange = {
                    if (it.isEmpty() || it.toDoubleOrNull() != null) {
                        amount = it
                        if (amountError && it.toDoubleOrNull()?.let { v -> v > 0 } == true) amountError = false
                    }
                },
                label = { Text(stringResource(R.string.payment_amount)) },
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
                colors = formFieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))

            FormPickerField(
                label = stringResource(R.string.payment_date),
                value = DateUtils.formatDate(date),
                onClick = { showDatePicker.value = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.payment_notes)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = MaterialTheme.shapes.medium,
                colors = formFieldColors()
            )

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
                    .height(64.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Text(
                    text = stringResource(R.string.save),
                    style = MaterialTheme.typography.titleLargeEmphasized
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