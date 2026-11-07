package com.onlive.trackify.ui.screens.payments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.ui.components.FormPickerField
import com.onlive.trackify.ui.components.SubscriptionSelector
import com.onlive.trackify.ui.components.TrackifyDatePicker
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.ui.components.formFieldColors
import com.onlive.trackify.utils.CurrencyFormatter
import com.onlive.trackify.utils.DateUtils
import com.onlive.trackify.utils.stringResource
import com.onlive.trackify.viewmodel.PaymentViewModel
import com.onlive.trackify.viewmodel.SubscriptionViewModel
import java.time.LocalDate

@Composable
fun AddPaymentScreen(
    subscriptionId: Long,
    paymentId: Long,
    onNavigateBack: () -> Unit,
    paymentViewModel: PaymentViewModel = viewModel(factory = PaymentViewModel.Factory),
    subscriptionViewModel: SubscriptionViewModel = viewModel(factory = SubscriptionViewModel.Factory)
) {
    val allSubscriptionsState by subscriptionViewModel.allSubscriptions.collectAsStateWithLifecycle()
    val allSubscriptions = remember(allSubscriptionsState) {
        allSubscriptionsState.orEmpty().map { it.subscription }
    }

    var selectedSubscriptionId by rememberSaveable(subscriptionId, paymentId) { mutableLongStateOf(subscriptionId) }
    var amount by rememberSaveable(subscriptionId, paymentId) { mutableStateOf("") }
    var date by rememberSaveable(subscriptionId, paymentId) { mutableStateOf(LocalDate.now()) }
    var notes by rememberSaveable(subscriptionId, paymentId) { mutableStateOf("") }

    val paymentFlow = remember(paymentId, paymentViewModel) {
        paymentViewModel.getPaymentById(paymentId)
    }
    var hasLoadedExisting by rememberSaveable(subscriptionId, paymentId) { mutableStateOf(false) }

    val showDatePicker = rememberSaveable(subscriptionId, paymentId) { mutableStateOf(false) }

    var subError by remember(subscriptionId, paymentId) { mutableStateOf(false) }
    var amountError by remember(subscriptionId, paymentId) { mutableStateOf(false) }
    var isSaving by remember(subscriptionId, paymentId) { mutableStateOf(false) }

    LaunchedEffect(paymentId, paymentFlow) {
        if (paymentId == -1L) return@LaunchedEffect
        paymentFlow.collect { payment ->
            if (payment == null) {
                if (!isSaving) {
                    onNavigateBack()
                }
            } else if (!hasLoadedExisting) {
                selectedSubscriptionId = payment.subscriptionId
                amount = CurrencyFormatter.formatPlainAmount(payment.amount)
                date = payment.date
                notes = payment.notes ?: ""
                hasLoadedExisting = true
            }
        }
    }

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
                .consumeWindowInsets(paddingValues)
                .imePadding()
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
                onValueChange = { input ->
                    val sanitized = CurrencyFormatter.sanitizeAmountInput(input)
                    if (sanitized != null) {
                        amount = sanitized
                        if (amountError && CurrencyFormatter.parseValidPositiveAmount(sanitized) != null) {
                            amountError = false
                        }
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
                    val amountValue = CurrencyFormatter.parseValidPositiveAmount(amount)
                    val isSubValid = selectedSubscriptionId != -1L
                    val isAmountValid = amountValue != null

                    subError = !isSubValid
                    amountError = !isAmountValid

                    if (isSubValid && amountValue != null && !isSaving) {
                        isSaving = true
                        val payment = Payment(
                            paymentId = if (paymentId == -1L) 0 else paymentId,
                            subscriptionId = selectedSubscriptionId,
                            amount = amountValue,
                            date = date,
                            notes = notes.ifBlank { null }
                        )
                        if (paymentId == -1L) {
                            paymentViewModel.insert(
                                payment = payment,
                                onSuccess = onNavigateBack,
                                onError = { isSaving = false }
                            )
                        } else {
                            paymentViewModel.update(
                                payment = payment,
                                onSuccess = onNavigateBack,
                                onError = { isSaving = false }
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shapes = ButtonDefaults.shapes(
                    shape = MaterialTheme.shapes.extraLarge,
                    pressedShape = ButtonDefaults.largePressedShape
                )
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