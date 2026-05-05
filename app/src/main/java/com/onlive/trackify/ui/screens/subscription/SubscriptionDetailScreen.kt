package com.onlive.trackify.ui.screens.subscription

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.ui.components.AutoSizeText
import com.onlive.trackify.ui.components.TrackifyDatePicker
import com.onlive.trackify.ui.components.TrackifyOutlinedCard
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.utils.CurrencyFormatter
import com.onlive.trackify.utils.DateUtils
import com.onlive.trackify.utils.stringResource
import com.onlive.trackify.viewmodel.CategoryViewModel
import com.onlive.trackify.viewmodel.PaymentViewModel
import com.onlive.trackify.viewmodel.SubscriptionViewModel
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SubscriptionDetailScreen(
    subscriptionId: Long,
    onNavigateBack: () -> Unit,
    subscriptionViewModel: SubscriptionViewModel = viewModel(),
    categoryViewModel: CategoryViewModel = viewModel(),
    paymentViewModel: PaymentViewModel = viewModel()
) {
    val allCategories by categoryViewModel.allCategories.observeAsState(emptyList())
    val subscriptionState = subscriptionViewModel.getSubscriptionById(subscriptionId).observeAsState()
    val allPayments by paymentViewModel.getPaymentsBySubscription(subscriptionId).observeAsState(emptyList())

    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var billingFrequency by remember { mutableStateOf(BillingFrequency.MONTHLY) }
    var startDate by remember { mutableStateOf(Date()) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var categoryId by remember { mutableStateOf<Long?>(null) }

    var hasLoadedExisting by remember { mutableStateOf(false) }

    LaunchedEffect(subscriptionState.value) {
        if (subscriptionId != -1L && subscriptionState.value != null && !hasLoadedExisting) {
            val sub = subscriptionState.value!!
            name = sub.name
            price = sub.price.toString()
            description = sub.description ?: ""
            billingFrequency = sub.billingFrequency
            startDate = sub.startDate
            endDate = sub.endDate
            categoryId = sub.categoryId
            hasLoadedExisting = true
        }
    }

    val showStartDatePicker = remember { mutableStateOf(false) }
    val showEndDatePicker = remember { mutableStateOf(false) }
    val showDeleteDialog = remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf(false) }
    var priceError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TrackifyTopAppBar(
                title = if (subscriptionId == -1L) stringResource(R.string.add_subscription) else name,
                showBackButton = true,
                onBackClick = onNavigateBack,
                actions = {
                    if (subscriptionId != -1L) {
                        IconButton(onClick = { showDeleteDialog.value = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            TrackifyOutlinedCard(title = stringResource(R.string.subscription_name)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (nameError && it.isNotBlank()) nameError = false
                    },
                    placeholder = { Text(stringResource(R.string.enter_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text("Required field") }
                    } else null,
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(androidx.compose.foundation.layout.IntrinsicSize.Min)
            ) {
                TrackifyOutlinedCard(
                    title = stringResource(R.string.subscription_price),
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = {
                            if (it.isEmpty() || it.toDoubleOrNull() != null) {
                                price = it
                                if (priceError && it.toDoubleOrNull()?.let { v -> v > 0 } == true) priceError = false
                            }
                        },
                        placeholder = { Text("0.00") },
                        modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                        singleLine = true,
                        isError = priceError,
                        supportingText = if (priceError) {
                            { Text("Invalid price") }
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

                Spacer(modifier = Modifier.width(16.dp))

                TrackifyOutlinedCard(
                    title = stringResource(R.string.subscription_billing_frequency),
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth().fillMaxHeight()
                    ) {
                        SegmentedButton(
                            selected = billingFrequency == BillingFrequency.MONTHLY,
                            onClick = { billingFrequency = BillingFrequency.MONTHLY },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            icon = {}
                        ) {
                            AutoSizeText(
                                text = stringResource(R.string.subscription_monthly),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        SegmentedButton(
                            selected = billingFrequency == BillingFrequency.YEARLY,
                            onClick = { billingFrequency = BillingFrequency.YEARLY },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            icon = {}
                        ) {
                            AutoSizeText(
                                text = stringResource(R.string.subscription_yearly),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TrackifyOutlinedCard(title = stringResource(R.string.subscription_category)) {
                CategorySelector(
                    categories = allCategories,
                    selectedCategoryId = categoryId,
                    onCategorySelected = { categoryId = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                TrackifyOutlinedCard(
                    title = stringResource(R.string.subscription_start_date),
                    modifier = Modifier.weight(1f),
                    onClick = { showStartDatePicker.value = true }
                ) {
                    Text(
                        text = DateUtils.formatDate(startDate),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                TrackifyOutlinedCard(
                    title = stringResource(R.string.subscription_end_date),
                    modifier = Modifier.weight(1f),
                    onClick = { showEndDatePicker.value = true }
                ) {
                    Text(
                        text = endDate?.let { DateUtils.formatDate(it) } ?: stringResource(R.string.indefinitely),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (endDate == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TrackifyOutlinedCard(title = stringResource(R.string.subscription_description)) {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
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
                    val priceValue = price.toDoubleOrNull() ?: 0.0
                    val isNameValid = name.isNotBlank()
                    val isPriceValid = priceValue > 0

                    nameError = !isNameValid
                    priceError = !isPriceValid

                    if (isNameValid && isPriceValid) {
                        val sub = Subscription(
                            subscriptionId = if (subscriptionId == -1L) 0 else subscriptionId,
                            name = name,
                            description = description.ifBlank { null },
                            price = priceValue,
                            billingFrequency = billingFrequency,
                            startDate = startDate,
                            endDate = endDate,
                            categoryId = categoryId
                        )
                        if (subscriptionId == -1L) {
                            subscriptionViewModel.insert(sub)
                        } else {
                            subscriptionViewModel.update(sub)
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
            onDateSelected = { startDate = it },
            onDismiss = { showStartDatePicker.value = false }
        )
    }

    if (showEndDatePicker.value) {
        TrackifyDatePicker(
            selectedDate = endDate ?: Date(),
            onDateSelected = { endDate = if (it.time == Long.MAX_VALUE) null else it },
            onDismiss = { showEndDatePicker.value = false },
            allowNull = true
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
                        subscriptionState.value?.let { subscriptionViewModel.delete(it) }
                        showDeleteDialog.value = false
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
                TextButton(onClick = { showDeleteDialog.value = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun CategorySelector(
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCategory = categories.find { it.categoryId == selectedCategoryId }

    Box(modifier = Modifier.fillMaxWidth()) {
        Surface(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            selectedCategory?.colorCode?.let {
                                try { Color(it.toColorInt()) } catch (_: Exception) { Color.Gray }
                            } ?: MaterialTheme.colorScheme.outline
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = selectedCategory?.name ?: stringResource(R.string.without_category),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f).background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.without_category)) },
                onClick = {
                    onCategorySelected(null)
                    expanded = false
                }
            )
            categories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(
                                        try { Color(category.colorCode.toColorInt()) } catch (_: Exception) { Color.Gray }
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(category.name)
                        }
                    },
                    onClick = {
                        onCategorySelected(category.categoryId)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun PaymentHistorySection(payments: List<Payment>) {
    Text(
        text = stringResource(R.string.payment_history),
        style = MaterialTheme.typography.titleMediumEmphasized,
        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        payments.sortedByDescending { it.date }.take(5).forEach { payment ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = DateUtils.formatDate(payment.date),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = CurrencyFormatter.formatAmount(LocalContext.current, payment.amount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun NoPaymentsSection() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.no_payments_for_subscription),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
