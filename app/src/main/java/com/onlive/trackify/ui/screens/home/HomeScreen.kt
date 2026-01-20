package com.onlive.trackify.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.ui.components.SubscriptionListItem
import com.onlive.trackify.utils.stringResource
import com.onlive.trackify.viewmodel.SubscriptionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddSubscription: () -> Unit,
    onSubscriptionClick: (Long) -> Unit,
    viewModel: SubscriptionViewModel = viewModel()
) {
    val allSubscriptions by viewModel.allSubscriptions.observeAsState(emptyList())
    val loading by viewModel.isLoading.observeAsState(false)

    var query by remember { mutableStateOf("") }

    val filteredSubscriptions = remember(query, allSubscriptions) {
        if (query.isEmpty()) {
            allSubscriptions
        } else {
            allSubscriptions.filter { subscription ->
                subscription.name.contains(query, ignoreCase = true)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = query,
                        onQueryChange = { newQuery ->
                            query = newQuery
                        },
                        onSearch = { },
                        expanded = false,
                        onExpandedChange = { },
                        placeholder = { Text(stringResource(R.string.search_subscriptions)) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
                    )
                },
                expanded = false,
                onExpandedChange = { },
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
            } else if (allSubscriptions.isEmpty()) {
                EmptySubscriptionsView()
            } else if (filteredSubscriptions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.subscriptions_not_found),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            } else {
                SubscriptionsList(
                    subscriptions = filteredSubscriptions,
                    onSubscriptionClick = onSubscriptionClick
                )
            }
        }

        FloatingActionButton(
            onClick = onAddSubscription,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 130.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_subscription))
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
fun SubscriptionsList(
    subscriptions: List<Subscription>,
    onSubscriptionClick: (Long) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
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