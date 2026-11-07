package com.onlive.trackify.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.model.SubscriptionSortOption
import com.onlive.trackify.data.model.SubscriptionWithCategory
import com.onlive.trackify.ui.components.EmptyState
import com.onlive.trackify.ui.components.SubscriptionListItem
import com.onlive.trackify.ui.components.TrackifyFab
import com.onlive.trackify.ui.components.resolveCategoryColor
import com.onlive.trackify.utils.PaymentScheduleCalculator
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.utils.SubscriptionDueStatus
import com.onlive.trackify.utils.stringResource
import com.onlive.trackify.viewmodel.CategoryViewModel
import com.onlive.trackify.viewmodel.SubscriptionViewModel
import java.time.LocalDate
import kotlinx.coroutines.launch

private const val FILTER_ALL = -1L
private const val FILTER_UNCATEGORIZED = -2L

data class SubscriptionListEntry(
    val item: SubscriptionWithCategory,
    val dueStatus: SubscriptionDueStatus
)

@OptIn(
    ExperimentalLayoutApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)
@Composable
fun HomeScreen(
    onAddSubscription: () -> Unit,
    onSubscriptionClick: (Long) -> Unit,
    viewModel: SubscriptionViewModel = viewModel(factory = SubscriptionViewModel.Factory),
    categoryViewModel: CategoryViewModel = viewModel(factory = CategoryViewModel.Factory)
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val preferenceManager = remember { PreferenceManager(context) }
    val coroutineScope = rememberCoroutineScope()

    val allSubscriptions by viewModel.allSubscriptions.collectAsStateWithLifecycle()
    val allCategories by categoryViewModel.allCategories.collectAsStateWithLifecycle()

    val searchTextFieldState = rememberTextFieldState()
    val searchBarState = rememberSearchBarState()
    val query by remember { derivedStateOf { searchTextFieldState.text.toString() } }

    var sortOption by rememberSaveable { mutableStateOf(preferenceManager.getSubscriptionSortOption()) }
    var selectedCategoryFilter by rememberSaveable { mutableLongStateOf(FILTER_ALL) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    DisposableEffect(preferenceManager) {
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == PreferenceManager.KEY_SUBSCRIPTION_SORT || key == null) {
                sortOption = preferenceManager.getSubscriptionSortOption()
            }
        }
        preferenceManager.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            preferenceManager.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val isImeVisible = WindowInsets.isImeVisible
    var wasImeVisible by remember { mutableStateOf(false) }
    LaunchedEffect(isImeVisible) {
        if (isImeVisible) {
            wasImeVisible = true
        } else if (wasImeVisible) {
            wasImeVisible = false
            focusManager.clearFocus()
            searchBarState.animateToCollapsed()
        }
    }

    var todayEpochDay by remember {
        mutableLongStateOf(LocalDate.now().toEpochDay())
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                todayEpochDay = LocalDate.now().toEpochDay()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val today = remember(todayEpochDay) { LocalDate.ofEpochDay(todayEpochDay) }

    val activeCategoryFilter = remember(selectedCategoryFilter, allCategories, allSubscriptions) {
        when {
            selectedCategoryFilter == FILTER_ALL -> FILTER_ALL
            selectedCategoryFilter == FILTER_UNCATEGORIZED -> {
                if (allSubscriptions?.any { it.subscription.categoryId == null } == true) FILTER_UNCATEGORIZED else FILTER_ALL
            }
            allCategories.any { it.categoryId == selectedCategoryFilter } -> selectedCategoryFilter
            else -> FILTER_ALL
        }
    }

    val displayedEntries = remember(allSubscriptions, query, activeCategoryFilter, sortOption, today) {
        val list = allSubscriptions ?: return@remember null
        val byCategory = when (activeCategoryFilter) {
            FILTER_ALL -> list
            FILTER_UNCATEGORIZED -> list.filter { it.subscription.categoryId == null }
            else -> list.filter { it.subscription.categoryId == activeCategoryFilter }
        }
        val byQuery = if (query.isBlank()) {
            byCategory
        } else {
            val trimmed = query.trim()
            byCategory.filter {
                it.subscription.name.contains(trimmed, ignoreCase = true) ||
                    (it.categoryName?.contains(trimmed, ignoreCase = true) == true)
            }
        }
        val sorted = PaymentScheduleCalculator.sortSubscriptionsBy(
            items = byQuery,
            sortOption = sortOption,
            today = today,
            selector = { it.subscription }
        )
        sorted.map { item ->
            SubscriptionListEntry(
                item = item,
                dueStatus = PaymentScheduleCalculator.resolveDueStatus(item.subscription, today)
            )
        }
    }

    val listState = rememberLazyListState()
    val fabVisible = listState.isScrollingUp()
    val hasSubscriptions = !allSubscriptions.isNullOrEmpty()
    val showCategoryChips = hasSubscriptions && allCategories.isNotEmpty()
    val hasUncategorized = remember(allSubscriptions) {
        allSubscriptions?.any { it.subscription.categoryId == null } == true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = SearchBarDefaults.inputFieldShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = SearchBarDefaults.TonalElevation,
                shadowElevation = SearchBarDefaults.ShadowElevation
            ) {
                SearchBarDefaults.InputField(
                    textFieldState = searchTextFieldState,
                    searchBarState = searchBarState,
                    onSearch = {
                        focusManager.clearFocus()
                        coroutineScope.launch {
                            searchBarState.animateToCollapsed()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused && searchBarState.targetValue == SearchBarValue.Expanded) {
                                coroutineScope.launch {
                                    searchBarState.animateToCollapsed()
                                }
                            }
                        },
                    placeholder = { Text(stringResource(R.string.search_subscriptions)) },
                    leadingIcon = {
                        Icon(Icons.Rounded.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (query.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchTextFieldState.clearText() },
                                    shapes = IconButtonDefaults.shapes()
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = stringResource(R.string.clear_search)
                                    )
                                }
                            }

                            Box {
                                IconButton(
                                    onClick = { sortMenuExpanded = true },
                                    shapes = IconButtonDefaults.shapes()
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Sort,
                                        contentDescription = stringResource(R.string.sort_subscriptions),
                                        tint = if (sortOption != SubscriptionSortOption.NEAREST_PAYMENT) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }

                                DropdownMenu(
                                    expanded = sortMenuExpanded,
                                    onDismissRequest = { sortMenuExpanded = false }
                                ) {
                                    SubscriptionSortOption.entries.forEach { option ->
                                        val isSelected = option == sortOption
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = stringResource(option.labelRes),
                                                    color = if (isSelected) {
                                                        MaterialTheme.colorScheme.primary
                                                    } else {
                                                        MaterialTheme.colorScheme.onSurface
                                                    }
                                                )
                                            },
                                            leadingIcon = {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            },
                                            onClick = {
                                                sortMenuExpanded = false
                                                if (sortOption != option) {
                                                    sortOption = option
                                                    preferenceManager.setSubscriptionSortOption(option)
                                                    coroutineScope.launch {
                                                        listState.animateScrollToItem(0)
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            }

            if (showCategoryChips) {
                CategoryFilterChipsRow(
                    categories = allCategories,
                    selectedFilter = activeCategoryFilter,
                    showUncategorized = hasUncategorized,
                    onSelectFilter = { newFilter ->
                        selectedCategoryFilter = newFilter
                        coroutineScope.launch {
                            listState.animateScrollToItem(0)
                        }
                    }
                )
            }

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

                displayedEntries.isNullOrEmpty() -> {
                    EmptyState(
                        icon = Icons.Rounded.SearchOff,
                        title = stringResource(R.string.subscriptions_not_found)
                    )
                }

                else -> {
                    SubscriptionsList(
                        entries = displayedEntries,
                        today = today,
                        onSubscriptionClick = onSubscriptionClick,
                        listState = listState
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = fabVisible,
            enter = scaleIn(animationSpec = MaterialTheme.motionScheme.fastSpatialSpec()) +
                fadeIn(animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()),
            exit = scaleOut(animationSpec = MaterialTheme.motionScheme.fastSpatialSpec()) +
                fadeOut(animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()),
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
private fun CategoryFilterChipsRow(
    categories: List<Category>,
    selectedFilter: Long,
    showUncategorized: Boolean,
    onSelectFilter: (Long) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item(key = "filter_all") {
            FilterChip(
                selected = selectedFilter == FILTER_ALL,
                onClick = { onSelectFilter(FILTER_ALL) },
                label = { Text(stringResource(R.string.all_categories)) },
                shape = CircleShape
            )
        }

        items(categories, key = { it.categoryId }) { category ->
            val isSelected = selectedFilter == category.categoryId
            val dotColor = resolveCategoryColor(
                category.colorCode,
                MaterialTheme.colorScheme.primary
            )
            FilterChip(
                selected = isSelected,
                onClick = {
                    onSelectFilter(if (isSelected) FILTER_ALL else category.categoryId)
                },
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                },
                label = { Text(category.name) },
                shape = CircleShape
            )
        }

        if (showUncategorized) {
            item(key = "filter_uncategorized") {
                val isSelected = selectedFilter == FILTER_UNCATEGORIZED
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        onSelectFilter(if (isSelected) FILTER_ALL else FILTER_UNCATEGORIZED)
                    },
                    label = { Text(stringResource(R.string.without_category)) },
                    shape = CircleShape
                )
            }
        }
    }
}

private class ScrollPositionHolder(
    var index: Int,
    var scrollOffset: Int,
    var scrollingUp: Boolean = true
)

@Composable
private fun LazyListState.isScrollingUp(): Boolean {
    val holder = remember(this) {
        ScrollPositionHolder(
            index = firstVisibleItemIndex,
            scrollOffset = firstVisibleItemScrollOffset
        )
    }
    return remember(this) {
        derivedStateOf {
            val currentIndex = firstVisibleItemIndex
            val currentOffset = firstVisibleItemScrollOffset
            if (holder.index != currentIndex) {
                holder.scrollingUp = holder.index > currentIndex
                holder.index = currentIndex
                holder.scrollOffset = currentOffset
            } else if (holder.scrollOffset != currentOffset) {
                holder.scrollingUp = holder.scrollOffset > currentOffset
                holder.scrollOffset = currentOffset
            }
            holder.scrollingUp
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
    entries: List<SubscriptionListEntry>,
    today: LocalDate,
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
        items(
            items = entries,
            key = { it.item.subscription.subscriptionId }
        ) { entry ->
            SubscriptionListItem(
                item = entry.item,
                dueStatus = entry.dueStatus,
                today = today,
                onClick = { onSubscriptionClick(entry.item.subscription.subscriptionId) },
                modifier = Modifier.animateItem()
            )
        }
    }
}
