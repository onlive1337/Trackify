package com.onlive.trackify.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Subscription
import com.onlive.trackify.ui.components.EmptyState
import com.onlive.trackify.ui.components.SubscriptionListItem
import com.onlive.trackify.ui.components.TrackifyFab
import com.onlive.trackify.utils.stringResource
import com.onlive.trackify.viewmodel.SubscriptionViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomeScreen(
    onAddSubscription: () -> Unit,
    onSubscriptionClick: (Long) -> Unit,
    viewModel: SubscriptionViewModel = viewModel()
) {
    val allSubscriptions by viewModel.allSubscriptions.observeAsState(initial = null)

    var query by remember { mutableStateOf("") }

    val filteredSubscriptions = remember(query, allSubscriptions) {
        val list = allSubscriptions ?: return@remember null
        if (query.isEmpty()) list
        else list.filter { it.name.contains(query, ignoreCase = true) }
    }

    val listState = rememberLazyListState()
    val fabVisible = listState.isScrollingUp()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = query,
                        onQueryChange = { query = it },
                        onSearch = { },
                        expanded = false,
                        onExpandedChange = { },
                        placeholder = { Text(stringResource(R.string.search_subscriptions)) },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null)
                        }
                    )
                },
                expanded = false,
                onExpandedChange = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = SearchBarDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                )
            ) {}

            when {
                allSubscriptions == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                allSubscriptions!!.isEmpty() -> EmptySubscriptionsView()

                filteredSubscriptions.isNullOrEmpty() -> {
                    EmptyState(
                        icon = Icons.Rounded.SearchOff,
                        title = stringResource(R.string.subscriptions_not_found)
                    )
                }

                else -> {
                    SubscriptionsList(
                        subscriptions = filteredSubscriptions,
                        onSubscriptionClick = onSubscriptionClick,
                        listState = listState
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = fabVisible,
            enter = scaleIn() + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
        ) {
            TrackifyFab(
                onClick = onAddSubscription,
                contentDescription = stringResource(R.string.add_subscription)
            )
        }
    }
}

@Composable
private fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

@Composable
fun EmptySubscriptionsView() {
    EmptyState(
        icon = Icons.Rounded.Subscriptions,
        title = stringResource(R.string.no_subscriptions)
    )
}

@Composable
fun SubscriptionsList(
    subscriptions: List<Subscription>,
    onSubscriptionClick: (Long) -> Unit,
    listState: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 4.dp,
            bottom = 96.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(subscriptions) { subscription ->
            SubscriptionListItem(
                subscription = subscription,
                onClick = { onSubscriptionClick(subscription.subscriptionId) }
            )
        }
    }
}