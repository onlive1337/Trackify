package com.onlive.trackify.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.onlive.trackify.utils.stringResource
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Subscription

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionSelector(
    subscriptions: List<Subscription>,
    selectedSubscriptionId: Long,
    onSubscriptionSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String = stringResource(R.string.select_subscription)
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = subscriptions.find { it.subscriptionId == selectedSubscriptionId }?.name
                    ?: "",
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                isError = isError,
                supportingText = if (isError) {
                    { Text(errorMessage) }
                } else null
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                subscriptions.forEach { subscription ->
                    DropdownMenuItem(
                        text = { Text(subscription.name) },
                        onClick = {
                            onSubscriptionSelected(subscription.subscriptionId)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}