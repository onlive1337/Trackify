package com.onlive.trackify.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Currency
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.ui.components.segmentedListItemShapes
import com.onlive.trackify.utils.LocalCurrency
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.utils.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CurrencySettingsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val preferenceManager = remember { PreferenceManager(context) }
    val selectedCurrencyCode = LocalCurrency.current.code

    val currencies = Currency.POPULAR_CURRENCIES
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TrackifyTopAppBar(
                title = stringResource(R.string.currency_settings),
                showBackButton = true,
                onBackClick = onNavigateBack,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.currency_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.choose_currency),
                style = MaterialTheme.typography.titleMediumEmphasized,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                itemsIndexed(
                    items = currencies,
                    key = { _, currency -> currency.code }
                ) { index, currency ->
                    CurrencyItem(
                        currency = currency,
                        selected = currency.code == selectedCurrencyCode,
                        index = index,
                        count = currencies.size,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            preferenceManager.setCurrencyCode(currency.code)
                        },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CurrencyItem(
    currency: Currency,
    selected: Boolean,
    index: Int,
    count: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SegmentedListItem(
        selected = selected,
        onClick = onClick,
        shapes = segmentedListItemShapes(index = index, count = count),
        modifier = modifier,
        colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        leadingContent = {
            Text(
                text = currency.symbol,
                style = MaterialTheme.typography.titleLargeEmphasized
            )
        },
        supportingContent = {
            Text(
                text = currency.code,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        trailingContent = {
            RadioButton(selected = selected, onClick = null)
        }
    ) {
        Text(
            text = stringResource(currency.nameRes),
            style = if (selected) MaterialTheme.typography.titleMediumEmphasized else MaterialTheme.typography.titleMedium
        )
    }
}