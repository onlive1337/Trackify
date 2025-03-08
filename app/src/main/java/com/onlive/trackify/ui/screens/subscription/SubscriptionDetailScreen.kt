package com.onlive.trackify.ui.screens.subscription

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Payment
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.utils.CurrencyFormatter
import com.onlive.trackify.viewmodel.CategoryViewModel
import com.onlive.trackify.viewmodel.PaymentViewModel
import com.onlive.trackify.viewmodel.SubscriptionViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val context = LocalContext.current

    // Если ID равен -1, то это создание новой подписки
    val isNewSubscription = subscriptionId == -1L

    // Получаем существующую подписку, если это редактирование
    val existingSubscription by if (!isNewSubscription) {
        subscriptionViewModel.getSubscriptionById(subscriptionId).observeAsState()
    } else {
        remember { mutableStateOf<Subscription?>(null) }
    }

    // Состояния для полей формы
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var billingFrequency by remember { mutableStateOf(BillingFrequency.MONTHLY) }
    var startDate by remember { mutableStateOf(Date()) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var isActive by remember { mutableStateOf(true) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }

    // Состояние для диалога подтверждения удаления
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Состояние для выпадающего списка категорий
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

    // Получаем список категорий
    val categories by categoryViewModel.allCategories.observeAsState(emptyList())

    // Получаем платежи для данной подписки
    val payments by if (!isNewSubscription) {
        paymentViewModel.getPaymentsBySubscription(subscriptionId).observeAsState(emptyList())
    } else {
        remember { mutableStateOf<List<Payment>>(emptyList()) }
    }

    // Заполняем поля, если это редактирование
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

    // Форматтер дат
    val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

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
                        imageVector = Icons.Filled.Add,
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

            // Форма ввода/редактирования
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.subscription_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = name.isEmpty()
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
                onValueChange = { price = it.replace(",", ".") },
                label = { Text(stringResource(R.string.subscription_price)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = price.isEmpty() || price.toDoubleOrNull() == null || price.toDoubleOrNull()!! <= 0
            )

            if (price.isEmpty() || price.toDoubleOrNull() == null || price.toDoubleOrNull()!! <= 0) {
                Text(
                    text = stringResource(R.string.enter_correct_price),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Выпадающий список категорий
            Text(
                text = stringResource(R.string.subscription_category),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = isCategoryDropdownExpanded,
                onExpandedChange = { isCategoryDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = categories.find { it.categoryId == selectedCategoryId }?.name ?: stringResource(R.string.without_category),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCategoryDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = isCategoryDropdownExpanded,
                    onDismissRequest = { isCategoryDropdownExpanded = false }
                ) {
                    // Опция "Без категории"
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.without_category)) },
                        onClick = {
                            selectedCategoryId = null
                            isCategoryDropdownExpanded = false
                        }
                    )

                    // Все доступные категории
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                selectedCategoryId = category.categoryId
                                isCategoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Частота оплаты
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

            // Дата начала и окончания
            Column {
                Text(
                    text = stringResource(R.string.subscription_start_date),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dateFormatter.format(startDate),
                    onValueChange = { },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.subscription_end_date),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = endDate?.let { dateFormatter.format(it) } ?: stringResource(R.string.indefinitely),
                    onValueChange = { },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Переключатель активности
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

            // Кнопки действий
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Кнопка удаления (только для существующих подписок)
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

                // Кнопка сохранения
                Button(
                    onClick = {
                        if (name.isNotEmpty() && price.toDoubleOrNull() != null && price.toDoubleOrNull()!! > 0) {
                            if (isNewSubscription) {
                                // Создаем новую подписку
                                val newSubscription = Subscription(
                                    name = name,
                                    description = if (description.isEmpty()) null else description,
                                    price = price.toDouble(),
                                    billingFrequency = billingFrequency,
                                    startDate = startDate,
                                    endDate = endDate,
                                    categoryId = selectedCategoryId,
                                    active = isActive
                                )
                                subscriptionViewModel.insert(newSubscription)
                            } else {
                                // Обновляем существующую подписку
                                existingSubscription?.let {
                                    val updatedSubscription = it.copy(
                                        name = name,
                                        description = if (description.isEmpty()) null else description,
                                        price = price.toDouble(),
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
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.save))
                }
            }

            // Отображаем историю платежей только для существующей подписки
            if (!isNewSubscription && payments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = stringResource(R.string.payment_history),
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                PaymentHistoryList(payments = payments, subscriptionName = name)
            } else if (!isNewSubscription) {
                Spacer(modifier = Modifier.height(32.dp))

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

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Диалог подтверждения удаления
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
}

@Composable
fun PaymentHistoryList(
    payments: List<Payment>,
    subscriptionName: String
) {
    Column {
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
                        // Дата платежа
                        val dateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                        Text(
                            text = dateFormatter.format(payment.date),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Сумма платежа
                        Text(
                            text = CurrencyFormatter.formatAmount(LocalContext.current, payment.amount),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Примечания к платежу (если есть)
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

        // Если платежей больше 5, показываем "и еще..."
        if (payments.size > 5) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "...и ещё ${payments.size - 5}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}