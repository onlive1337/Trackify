package com.onlive.trackify.ui.screens.subscription

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.ui.components.FormFieldLabel
import com.onlive.trackify.ui.components.FormPickerField
import com.onlive.trackify.ui.components.TrackifyDatePicker
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.ui.components.connectedLeadingToggleShapes
import com.onlive.trackify.ui.components.connectedTrailingToggleShapes
import com.onlive.trackify.ui.components.formFieldColors
import com.onlive.trackify.utils.CurrencyFormatter
import com.onlive.trackify.utils.DateUtils
import com.onlive.trackify.utils.stringResource
import com.onlive.trackify.viewmodel.CategoryViewModel
import com.onlive.trackify.viewmodel.PaymentViewModel
import com.onlive.trackify.viewmodel.SubscriptionViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SubscriptionDetailScreen(
    subscriptionId: Long,
    onNavigateBack: () -> Unit,
    subscriptionViewModel: SubscriptionViewModel = viewModel(factory = SubscriptionViewModel.Factory),
    categoryViewModel: CategoryViewModel = viewModel(factory = CategoryViewModel.Factory),
    paymentViewModel: PaymentViewModel = viewModel(factory = PaymentViewModel.Factory)
) {
    val allCategories by categoryViewModel.allCategories.collectAsStateWithLifecycle()
    val subscriptionFlow = remember(subscriptionId, subscriptionViewModel) {
        subscriptionViewModel.getSubscriptionById(subscriptionId)
    }
    val paymentsFlow = remember(subscriptionId, paymentViewModel) {
        paymentViewModel.getPaymentsBySubscription(subscriptionId)
    }
    val allPayments by paymentsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    var name by rememberSaveable(subscriptionId) { mutableStateOf("") }
    var price by rememberSaveable(subscriptionId) { mutableStateOf("") }
    var description by rememberSaveable(subscriptionId) { mutableStateOf("") }
    var billingFrequency by rememberSaveable(subscriptionId) { mutableStateOf(BillingFrequency.MONTHLY) }
    var startDate by rememberSaveable(subscriptionId) { mutableStateOf(LocalDate.now()) }
    var endDate by rememberSaveable(subscriptionId) { mutableStateOf<LocalDate?>(null) }
    var categoryId by rememberSaveable(subscriptionId) { mutableStateOf<Long?>(null) }

    var hasLoadedExisting by rememberSaveable(subscriptionId) { mutableStateOf(false) }

    val showStartDatePicker = rememberSaveable(subscriptionId) { mutableStateOf(false) }
    val showEndDatePicker = rememberSaveable(subscriptionId) { mutableStateOf(false) }
    val showDeleteDialog = rememberSaveable(subscriptionId) { mutableStateOf(false) }

    var loadedSubscription by remember(subscriptionId) { mutableStateOf<Subscription?>(null) }
    var nameError by remember(subscriptionId) { mutableStateOf(false) }
    var priceError by remember(subscriptionId) { mutableStateOf(false) }
    var dateError by remember(subscriptionId) { mutableStateOf(false) }
    var isSaving by remember(subscriptionId) { mutableStateOf(false) }

    LaunchedEffect(subscriptionId, subscriptionFlow) {
        if (subscriptionId == -1L) return@LaunchedEffect
        subscriptionFlow.collect { item ->
            val loaded = item?.subscription
            loadedSubscription = loaded
            if (loaded == null) {
                if (!isSaving) {
                    onNavigateBack()
                }
            } else if (!hasLoadedExisting) {
                name = loaded.name
                price = CurrencyFormatter.formatPlainAmount(loaded.price)
                description = loaded.description ?: ""
                billingFrequency = loaded.billingFrequency
                startDate = loaded.startDate
                endDate = loaded.endDate
                categoryId = loaded.categoryId
                hasLoadedExisting = true
            }
        }
    }

    val requiredFieldStr = stringResource(R.string.required_field)
    val invalidPriceStr = stringResource(R.string.invalid_price)
    val invalidEndDateStr = stringResource(R.string.invalid_end_date)

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TrackifyTopAppBar(
                title = if (subscriptionId == -1L) stringResource(R.string.add_subscription) else name,
                showBackButton = true,
                onBackClick = onNavigateBack,
                scrollBehavior = scrollBehavior,
                actions = {
                    if (subscriptionId != -1L) {
                        IconButton(
                            onClick = { showDeleteDialog.value = true },
                            shapes = IconButtonDefaults.shapes()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
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

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (nameError && it.isNotBlank()) nameError = false
                },
                label = { Text(stringResource(R.string.subscription_name)) },
                placeholder = { Text(stringResource(R.string.enter_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nameError,
                supportingText = if (nameError) {
                    { Text(requiredFieldStr) }
                } else null,
                shape = MaterialTheme.shapes.medium,
                colors = formFieldColors()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = price,
                onValueChange = { input ->
                    val sanitized = CurrencyFormatter.sanitizeAmountInput(input)
                    if (sanitized != null) {
                        price = sanitized
                        if (priceError && CurrencyFormatter.parseValidPositiveAmount(sanitized) != null) {
                            priceError = false
                        }
                    }
                },
                label = { Text(stringResource(R.string.subscription_price)) },
                placeholder = { Text("0.00") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = priceError,
                supportingText = if (priceError) {
                    { Text(invalidPriceStr) }
                } else null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                shape = MaterialTheme.shapes.medium,
                colors = formFieldColors()
            )

            Spacer(modifier = Modifier.height(20.dp))

            FormFieldLabel(stringResource(R.string.subscription_billing_frequency))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
            ) {
                ToggleButton(
                    checked = billingFrequency == BillingFrequency.MONTHLY,
                    onCheckedChange = { billingFrequency = BillingFrequency.MONTHLY },
                    shapes = connectedLeadingToggleShapes(),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Text(
                        text = stringResource(R.string.subscription_monthly),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                ToggleButton(
                    checked = billingFrequency == BillingFrequency.YEARLY,
                    onCheckedChange = { billingFrequency = BillingFrequency.YEARLY },
                    shapes = connectedTrailingToggleShapes(),
                    modifier = Modifier.weight(1f).height(56.dp)
                ) {
                    Text(
                        text = stringResource(R.string.subscription_yearly),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            CategorySelector(
                categories = allCategories,
                selectedCategoryId = categoryId,
                onCategorySelected = { categoryId = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                FormPickerField(
                    label = stringResource(R.string.subscription_start_date),
                    value = DateUtils.formatDate(startDate),
                    onClick = { showStartDatePicker.value = true },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(16.dp))

                FormPickerField(
                    label = stringResource(R.string.subscription_end_date),
                    value = endDate?.let { DateUtils.formatDate(it) }
                        ?: stringResource(R.string.indefinitely),
                    onClick = { showEndDatePicker.value = true },
                    modifier = Modifier.weight(1f)
                )
            }

            if (dateError) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = invalidEndDateStr,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.subscription_description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = MaterialTheme.shapes.medium,
                colors = formFieldColors()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val priceValue = CurrencyFormatter.parseValidPositiveAmount(price)
                    val isNameValid = name.isNotBlank()
                    val isPriceValid = priceValue != null
                    val isEndDateValid = endDate?.let { !it.isBefore(startDate) } ?: true

                    nameError = !isNameValid
                    priceError = !isPriceValid
                    dateError = !isEndDateValid

                    if (isNameValid && priceValue != null && isEndDateValid && !isSaving) {
                        isSaving = true
                        val sub = Subscription(
                            subscriptionId = if (subscriptionId == -1L) 0 else subscriptionId,
                            name = name.trim(),
                            description = description.ifBlank { null },
                            price = priceValue,
                            billingFrequency = billingFrequency,
                            startDate = startDate,
                            endDate = endDate,
                            categoryId = categoryId,
                            image = loadedSubscription?.image
                        )
                        if (subscriptionId == -1L) {
                            subscriptionViewModel.insert(
                                subscription = sub,
                                onSuccess = onNavigateBack,
                                onError = { isSaving = false }
                            )
                        } else {
                            subscriptionViewModel.update(
                                subscription = sub,
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

            if (subscriptionId != -1L && allPayments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                PaymentHistorySection(payments = allPayments)
            } else if (subscriptionId != -1L) {
                Spacer(modifier = Modifier.height(32.dp))
                NoPaymentsSection()
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }

    if (showStartDatePicker.value) {
        TrackifyDatePicker(
            selectedDate = startDate,
            onDateSelected = { pickedDate ->
                startDate = pickedDate
                endDate?.let { currentEnd ->
                    dateError = currentEnd.isBefore(pickedDate)
                }
            },
            onDismiss = { showStartDatePicker.value = false }
        )
    }

    if (showEndDatePicker.value) {
        TrackifyDatePicker(
            selectedDate = endDate ?: LocalDate.now(),
            onDateSelected = { pickedDate ->
                endDate = pickedDate
                dateError = pickedDate.isBefore(startDate)
            },
            onIndefiniteSelected = {
                endDate = null
                dateError = false
            },
            onDismiss = { showEndDatePicker.value = false }
        )
    }

    if (showDeleteDialog.value) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog.value = false },
            title = { Text(stringResource(R.string.delete_subscription_confirmation)) },
            text = { Text(stringResource(R.string.delete_subscription_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        val current = loadedSubscription
                        showDeleteDialog.value = false
                        if (!isSaving && current != null) {
                            isSaving = true
                            subscriptionViewModel.delete(
                                subscription = current,
                                onSuccess = onNavigateBack,
                                onError = { isSaving = false }
                            )
                        }
                    },
                    shapes = ButtonDefaults.shapes(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog.value = false },
                    shapes = ButtonDefaults.shapes()
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
