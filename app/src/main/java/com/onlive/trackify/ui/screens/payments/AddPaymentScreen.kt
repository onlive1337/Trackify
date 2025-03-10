package com.onlive.trackify.ui.screens.payments

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.PaymentStatus
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.utils.DateUtils
import com.onlive.trackify.viewmodel.PaymentViewModel
import com.onlive.trackify.viewmodel.SubscriptionViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPaymentScreen(
    subscriptionId: Long = -1L,
    onNavigateBack: () -> Unit,
    paymentViewModel: PaymentViewModel = viewModel(),
    subscriptionViewModel: SubscriptionViewModel = viewModel()
) {
    val subscriptions by subscriptionViewModel.allActiveSubscriptions.observeAsState(emptyList())

    var selectedSubscriptionId by remember { mutableStateOf(subscriptionId) }
    var amount by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var paymentDate by remember { mutableStateOf(Date()) }

    var isSubscriptionDropdownExpanded by remember { mutableStateOf(false) }
    var isSubscriptionError by remember { mutableStateOf(false) }
    var isAmountError by remember { mutableStateOf(false) }

    LaunchedEffect(subscriptionId, subscriptions) {
        if (subscriptionId != -1L && subscriptions.any { it.subscriptionId == subscriptionId }) {
            selectedSubscriptionId = subscriptionId
        }
    }

    Scaffold(
        topBar = {
            TrackifyTopAppBar(
                title = stringResource(R.string.add_payment),
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

            ExposedDropdownMenuBox(
                expanded = isSubscriptionDropdownExpanded,
                onExpandedChange = { isSubscriptionDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = subscriptions.find { it.subscriptionId == selectedSubscriptionId }?.name
                        ?: "",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSubscriptionDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    isError = isSubscriptionError,
                    supportingText = if (isSubscriptionError) {
                        { Text(stringResource(R.string.select_subscription)) }
                    } else null
                )

                ExposedDropdownMenu(
                    expanded = isSubscriptionDropdownExpanded,
                    onDismissRequest = { isSubscriptionDropdownExpanded = false }
                ) {
                    subscriptions.forEach { subscription ->
                        DropdownMenuItem(
                            text = { Text(subscription.name) },
                            onClick = {
                                selectedSubscriptionId = subscription.subscriptionId
                                isSubscriptionDropdownExpanded = false
                                isSubscriptionError = false
                            }
                        )
                    }
                }
            }

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
                onClick = {
                    // TODO: Показать диалог выбора даты
                },
                modifier = Modifier.fillMaxWidth()
            ) {
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

                    val payment = Payment(
                        subscriptionId = selectedSubscriptionId,
                        amount = amountValue,
                        date = paymentDate,
                        notes = notes.takeIf { it.isNotEmpty() },
                        status = PaymentStatus.MANUAL
                    )

                    paymentViewModel.insert(payment)

                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}