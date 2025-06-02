package com.onlive.trackify.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.ui.components.SubscriptionGridItem
import com.onlive.trackify.ui.components.SubscriptionListItem
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.viewmodel.SubscriptionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddSubscription: () -> Unit,
    onSubscriptionClick: (Long) -> Unit,
    viewModel: SubscriptionViewModel = viewModel()
) {
    val allActiveSubscriptions by viewModel.allActiveSubscriptions.observeAsState(emptyList())
    val loading by viewModel.isLoading.observeAsState(false)

    var query by remember { mutableStateOf("") }
    var isGridMode by remember { mutableStateOf(false) }

    val filteredSubscriptions = remember(query, allActiveSubscriptions) {
        if (query.isEmpty()) {
            allActiveSubscriptions
        } else {
            allActiveSubscriptions.filter { subscription ->
                subscription.name.contains(query, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TrackifyTopAppBar(
                title = stringResource(R.string.title_subscriptions),
                actions = {
                    IconButton(onClick = { isGridMode = !isGridMode }) {
                        Icon(
                            imageVector = if (isGridMode) Icons.AutoMirrored.Filled.ViewList else Icons.Filled.GridView,
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
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
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
            } else if (allActiveSubscriptions.isEmpty()) {
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
                if (isGridMode) {
                    SubscriptionsGrid(
                        subscriptions = filteredSubscriptions,
                        onSubscriptionClick = onSubscriptionClick
                    )
                } else {
                    SubscriptionsList(
                        subscriptions = filteredSubscriptions,
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