package com.onlive.trackify.ui.screens.subscription

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.ui.components.TrackifyDatePicker
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.utils.CurrencyFormatter
import com.onlive.trackify.utils.DateUtils
import com.onlive.trackify.viewmodel.CategoryViewModel
import com.onlive.trackify.viewmodel.PaymentViewModel
import com.onlive.trackify.viewmodel.SubscriptionViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionDetailScreen(
    subscriptionId: Long,
    onNavigateBack: () -> Unit,
    onAddPayment: () -> Unit,
    subscriptionViewModel: SubscriptionViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel(),
    paymentViewModel: PaymentViewModel = viewModel()
) {
    LocalContext.current
    val isNewSubscription = subscriptionId == -1L

    val existingSubscription by if (!isNewSubscription) {
        subscriptionViewModel.getSubscriptionById(subscriptionId).observeAsState()
    } else {
        remember { mutableStateOf<Subscription?>(null) }
    }

    val categories by categoryViewModel.allCategories.observeAsState(emptyList())
    val payments by if (!isNewSubscription) {
        paymentViewModel.getPaymentsBySubscription(subscriptionId).observeAsState(emptyList())
    } else {
        remember { mutableStateOf<List<Payment>>(emptyList()) }
    }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var billingFrequency by remember { mutableStateOf(BillingFrequency.MONTHLY) }
    var startDate by remember { mutableStateOf(Date()) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var isActive by remember { mutableStateOf(true) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var isNameError by remember { mutableStateOf(false) }
    var isPriceError by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var expandCategoryDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(existingSubscription) {
        existingSubscription?.let {
            name = it.name
            description = it.description ?: ""
            price = it.price.toString()
            billingFrequency = it.billingFrequency
            startDate = it.startDate
            endDate = it.endDate
            isActive = it.active
            selectedCategoryId = it.categoryId
        }
    }

    Scaffold(
        topBar = {
            TrackifyTopAppBar(
                title = if (isNewSubscription) stringResource(R.string.add_subscription) else name,
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            if (!isNewSubscription) {
                FloatingActionButton(
                    onClick = onAddPayment,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_payment)
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    isNameError = false
                },
                label = { Text(stringResource(R.string.subscription_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = isNameError,
                supportingText = if (isNameError) {
                    { Text(stringResource(R.string.enter_name)) }
                } else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.subscription_description)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = price,
                onValueChange = {
                    price = it.replace(",", ".")
                    isPriceError = false
                },
                label = { Text(stringResource(R.string.subscription_price)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = isPriceError,
                supportingText = if (isPriceError) {
                    { Text(stringResource(R.string.enter_correct_price)) }
                } else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.subscription_category),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = expandCategoryDropdown,
                onExpandedChange = { expandCategoryDropdown = it }
            ) {
                OutlinedTextField(
                    value = categories.find { it.categoryId == selectedCategoryId }?.name
                        ?: stringResource(R.string.without_category),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandCategoryDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )

                ExposedDropdownMenu(
                    expanded = expandCategoryDropdown,
                    onDismissRequest = { expandCategoryDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.without_category)) },
                        onClick = {
                            selectedCategoryId = null
                            expandCategoryDropdown = false
                        }
                    )

                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                selectedCategoryId = category.categoryId
                                expandCategoryDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.subscription_billing_frequency),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = billingFrequency == BillingFrequency.MONTHLY,
                    onClick = { billingFrequency = BillingFrequency.MONTHLY }
                )

                Text(
                    text = stringResource(R.string.subscription_monthly),
                    modifier = Modifier.clickable { billingFrequency = BillingFrequency.MONTHLY }
                )

                Spacer(modifier = Modifier.width(16.dp))

                RadioButton(
                    selected = billingFrequency == BillingFrequency.YEARLY,
                    onClick = { billingFrequency = BillingFrequency.YEARLY }
                )

                Text(
                    text = stringResource(R.string.subscription_yearly),
                    modifier = Modifier.clickable { billingFrequency = BillingFrequency.YEARLY }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column {
                Text(
                    text = stringResource(R.string.subscription_start_date),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = DateUtils.formatDate(startDate))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.subscription_end_date),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { showEndDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(text = endDate?.let { DateUtils.formatDate(it) }
                        ?: stringResource(R.string.indefinitely))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.subscription_active),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )

                Switch(
                    checked = isActive,
                    onCheckedChange = { isActive = it }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (!isNewSubscription) {
                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.delete))
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                }

                Button(
                    onClick = {
                        if (name.isEmpty()) {
                            isNameError = true
                            return@Button
                        }

                        val priceValue = price.toDoubleOrNull()
                        if (priceValue == null || priceValue <= 0) {
                            isPriceError = true
                            return@Button
                        }

                        if (isNewSubscription) {
                            val newSubscription = Subscription(
                                name = name,
                                description = description.ifEmpty { null },
                                price = priceValue,
                                billingFrequency = billingFrequency,
                                startDate = startDate,
                                endDate = endDate,
                                categoryId = selectedCategoryId,
                                active = isActive
                            )
                            subscriptionViewModel.insert(newSubscription)
                        } else {
                            existingSubscription?.let {
                                val updatedSubscription = it.copy(
                                    name = name,
                                    description = description.ifEmpty { null },
                                    price = priceValue,
                                    billingFrequency = billingFrequency,
                                    startDate = startDate,
                                    endDate = endDate,
                                    categoryId = selectedCategoryId,
                                    active = isActive
                                )
                                subscriptionViewModel.update(updatedSubscription)
                            }
                        }
                        onNavigateBack()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.save))
                }
            }

            if (!isNewSubscription && payments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                PaymentHistorySection(payments = payments)
            } else if (!isNewSubscription) {
                Spacer(modifier = Modifier.height(32.dp))
                NoPaymentsSection()
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showStartDatePicker) {
        TrackifyDatePicker(
            selectedDate = startDate,
            onDateSelected = {
                startDate = it
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        TrackifyDatePicker(
            selectedDate = endDate ?: Date(),
            onDateSelected = { date ->
                endDate = if (date.time == Long.MAX_VALUE) null else date
            },
            onDismiss = { showEndDatePicker = false },
            allowNull = true
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_subscription_confirmation)) },
            text = { Text(stringResource(R.string.delete_subscription_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        existingSubscription?.let {
                            subscriptionViewModel.delete(it)
                        }
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun PaymentHistorySection(payments: List<Payment>) {
    val context = LocalContext.current

    Column {
        Text(
            text = stringResource(R.string.payment_history),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        payments.take(5).forEach { payment ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = DateUtils.formatDate(payment.date),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = CurrencyFormatter.formatAmount(context, payment.amount),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (!payment.notes.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = payment.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        if (payments.size > 5) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.and_more_count, payments.size - 5),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun NoPaymentsSection() {
    Column {
        Text(
            text = stringResource(R.string.payment_history),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.no_payments_for_subscription),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}