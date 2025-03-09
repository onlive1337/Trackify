package com.onlive.trackify.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.data.model.BillingFrequency
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.viewmodel.SubscriptionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddSubscription: () -> Unit,
    onSubscriptionClick: (Long) -> Unit,
    viewModel: SubscriptionViewModel = viewModel()
) {
    val subscriptions by viewModel.allActiveSubscriptions.observeAsState(emptyList())
    val loading by viewModel.isLoading.observeAsState(false)

    var query by remember { mutableStateOf("") }
    var isGridMode by remember { mutableStateOf(false) }
    var errorState by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TrackifyTopAppBar(
                title = stringResource(R.string.title_subscriptions),
                actions = {
                    IconButton(onClick = { isGridMode = !isGridMode }) {
                        Icon(
                            imageVector = if (isGridMode) Icons.Filled.ViewList else Icons.Filled.GridView,
                            contentDescription = stringResource(R.string.toggle_view_mode)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddSubscription,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_subscription))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                query = query,
                onQueryChange = { newQuery ->
                    query = newQuery
                    if (newQuery.isEmpty()) {
                    } else {
                        viewModel.searchSubscriptions(newQuery)
                    }
                },
                onSearch = { },
                active = false,
                onActiveChange = { },
                placeholder = { Text(stringResource(R.string.search_subscriptions)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {}

            if (loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (subscriptions.isEmpty()) {
                EmptySubscriptionsView()
            } else {
                errorState?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                if (isGridMode) {
                    SubscriptionsGrid(
                        subscriptions = subscriptions,
                        onSubscriptionClick = onSubscriptionClick
                    )
                } else {
                    SubscriptionsList(
                        subscriptions = subscriptions,
                        onSubscriptionClick = onSubscriptionClick
                    )
                }
            }
        }
    }
}

@Composable
fun EmptySubscriptionsView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.no_subscriptions),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun SubscriptionsGrid(
    subscriptions: List<Subscription>,
    onSubscriptionClick: (Long) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(subscriptions) { subscription ->
            SubscriptionGridItem(
                subscription = subscription,
                onClick = { onSubscriptionClick(subscription.subscriptionId) }
            )
        }
    }
}

@Composable
fun SubscriptionsList(
    subscriptions: List<Subscription>,
    onSubscriptionClick: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(subscriptions) { subscription ->
            SubscriptionListItem(
                subscription = subscription,
                onClick = { onSubscriptionClick(subscription.subscriptionId) }
            )
        }
    }
}

@Composable
fun SubscriptionListItem(
    subscription: Subscription,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val categoryColor = remember {
                subscription.categoryColor?.let { colorCode ->
                    try {
                        Color(android.graphics.Color.parseColor(colorCode))
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            if (categoryColor != null) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(48.dp)
                        .background(categoryColor)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = subscription.categoryName ?: stringResource(R.string.without_category),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // TODO: Добавить отображение даты следующего платежа
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatPrice(subscription),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun SubscriptionGridItem(
    subscription: Subscription,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val categoryColor = remember {
                subscription.categoryColor?.let { colorCode ->
                    try {
                        Color(android.graphics.Color.parseColor(colorCode))
                    } catch (e: Exception) {
                        null
                    }
                }
            }

            if (categoryColor != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(categoryColor)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = subscription.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = formatPrice(subscription),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subscription.categoryName ?: stringResource(R.string.without_category),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // TODO: Добавить отображение даты следующего платежа
        }
    }
}

@Composable
private fun formatPrice(subscription: Subscription): String {
    return when (subscription.billingFrequency) {
        BillingFrequency.MONTHLY -> "${formatCurrency(subscription.price)}/${stringResource(R.string.month)}"
        BillingFrequency.YEARLY -> "${formatCurrency(subscription.price)}/${stringResource(R.string.year)}"
    }
}

@Composable
private fun formatCurrency(amount: Double): String {
    // TODO: Использовать CurrencyFormatter из приложения
    return "₽%.0f".format(amount)
}