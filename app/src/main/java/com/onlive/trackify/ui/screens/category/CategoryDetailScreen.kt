package com.onlive.trackify.ui.screens.category

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.ui.components.CategoryAvatar
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.ui.components.resolveCategoryColor
import com.onlive.trackify.utils.CurrencyFormatter
import com.onlive.trackify.utils.stringResource
import com.onlive.trackify.viewmodel.CategoryViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    categoryId: Long,
    onNavigateBack: () -> Unit,
    categoryViewModel: CategoryViewModel = viewModel(factory = CategoryViewModel.Factory)
) {
    val categoryFlow = remember(categoryId, categoryViewModel) {
        categoryViewModel.getCategoryById(categoryId)
    }
    var loadedCategory by remember(categoryId) { mutableStateOf<Category?>(null) }
    var categoryName by rememberSaveable(categoryId) { mutableStateOf("") }
    var categoryColor by rememberSaveable(categoryId) { mutableStateOf("#FF5252") }
    var hasLoadedExisting by rememberSaveable(categoryId) { mutableStateOf(false) }
    var isSaving by remember(categoryId) { mutableStateOf(false) }

    LaunchedEffect(categoryId, categoryFlow) {
        if (categoryId == -1L) return@LaunchedEffect
        categoryFlow.collect { category ->
            loadedCategory = category
            if (category == null) {
                if (!isSaving) {
                    onNavigateBack()
                }
            } else if (!hasLoadedExisting) {
                categoryName = category.name
                categoryColor = category.colorCode
                hasLoadedExisting = true
            }
        }
    }

    CategoryDetailContent(
        categoryId = categoryId,
        title = if (categoryId == -1L) stringResource(R.string.add_category) else stringResource(R.string.edit_category),
        categoryName = categoryName,
        onCategoryNameChange = { categoryName = it },
        categoryColor = categoryColor,
        onCategoryColorChange = { categoryColor = it },
        isEditMode = categoryId != -1L,
        onNavigateBack = onNavigateBack,
        onSave = { name, color ->
            if (!isSaving) {
                isSaving = true
                if (categoryId == -1L) {
                    categoryViewModel.insert(
                        category = Category(name = name, colorCode = color),
                        onSuccess = onNavigateBack,
                        onError = { isSaving = false }
                    )
                } else {
                    val current = loadedCategory
                    if (current != null) {
                        categoryViewModel.update(
                            category = current.copy(name = name, colorCode = color),
                            onSuccess = onNavigateBack,
                            onError = { isSaving = false }
                        )
                    } else {
                        isSaving = false
                    }
                }
            }
        },
        onDelete = {
            val current = loadedCategory
            if (!isSaving && current != null) {
                isSaving = true
                categoryViewModel.delete(
                    category = current,
                    onSuccess = onNavigateBack,
                    onError = { isSaving = false }
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoryDetailContent(
    categoryId: Long,
    title: String,
    categoryName: String,
    onCategoryNameChange: (String) -> Unit,
    categoryColor: String,
    onCategoryColorChange: (String) -> Unit,
    isEditMode: Boolean,
    onNavigateBack: () -> Unit,
    onSave: (String, String) -> Unit,
    onDelete: () -> Unit
) {
    val predefinedColors = listOf(
        "#FF5252", "#FF4081", "#E040FB", "#7C4DFF", "#536DFE", "#448AFF", "#40C4FF", "#18FFFF",
        "#64FFDA", "#69F0AE", "#B2FF59", "#EEFF41", "#FFFF00", "#FFD740", "#FFAB40", "#FF6E40",
        "#8D6E63", "#BDBDBD", "#212121", "#455A64"
    )

    var hasAttemptedSave by rememberSaveable(categoryId) { mutableStateOf(false) }
    val isNameError = hasAttemptedSave && categoryName.trim().isEmpty()
    val showDeleteDialogState = rememberSaveable(categoryId) { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TrackifyTopAppBar(
                title = title,
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
                .consumeWindowInsets(paddingValues)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = stringResource(R.string.preview),
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val previewColor = resolveCategoryColor(categoryColor, MaterialTheme.colorScheme.primary)
                    
                    SubscriptionItemPreview(
                        name = stringResource(R.string.mock_subscription_name),
                        categoryName = categoryName.ifEmpty { stringResource(R.string.category_name) },
                        price = CurrencyFormatter.formatAmount(9.99),
                        color = previewColor
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = stringResource(R.string.category_name),
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = categoryName,
                        onValueChange = {
                            onCategoryNameChange(it)
                            if (hasAttemptedSave && it.isNotBlank()) hasAttemptedSave = false
                        },
                        placeholder = { Text(stringResource(R.string.category_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.extraLarge,
                        textStyle = MaterialTheme.typography.bodyLargeEmphasized,
                        isError = isNameError,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        )
                    )

                    if (isNameError) {
                        Text(
                            text = stringResource(R.string.category_no_name),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = stringResource(R.string.category_color),
                        style = MaterialTheme.typography.labelLargeEmphasized,
                        color = MaterialTheme.colorScheme.primary
                    )

                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        predefinedColors.forEach { colorCode ->
                            ColorChip(
                                colorCode = colorCode,
                                isSelected = colorCode == categoryColor,
                                onClick = { onCategoryColorChange(colorCode) }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (isEditMode) {
                        OutlinedButton(
                            onClick = { showDeleteDialogState.value = true },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                            shapes = ButtonDefaults.shapes(
                                shape = MaterialTheme.shapes.extraLarge,
                                pressedShape = ButtonDefaults.largePressedShape
                            ),
                            modifier = Modifier.weight(1f).height(64.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.delete))
                        }
                    }

                    Button(
                        onClick = {
                            if (categoryName.isNotBlank()) {
                                onSave(categoryName.trim(), categoryColor)
                            } else {
                                hasAttemptedSave = true
                            }
                        },
                        shapes = ButtonDefaults.shapes(
                            shape = MaterialTheme.shapes.extraLarge,
                            pressedShape = ButtonDefaults.largePressedShape
                        ),
                        modifier = Modifier.weight(1.5f).height(64.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.save),
                            style = MaterialTheme.typography.titleLargeEmphasized
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        if (showDeleteDialogState.value) {
            AlertDialog(
                onDismissRequest = { showDeleteDialogState.value = false },
                title = { Text(stringResource(R.string.delete_category_confirmation)) },
                text = { Text(stringResource(R.string.delete_category_message)) },
                confirmButton = {
                    Button(
                        onClick = {
                            onDelete()
                            showDeleteDialogState.value = false
                        },
                        shapes = ButtonDefaults.shapes(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialogState.value = false },
                        shapes = ButtonDefaults.shapes()
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SubscriptionItemPreview(
    name: String,
    categoryName: String,
    price: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryAvatar(color = color)

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = price,
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ColorChip(
    colorCode: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val color = resolveCategoryColor(colorCode, Color.Gray)
    val colorDescription = stringResource(R.string.color_option, colorCode)

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (color.luminance() > 0.5f) Color.Black else Color.White
                )
            }
        },
        shapes = FilterChipDefaults.shapes(
            shape = MaterialTheme.shapes.medium,
            selectedShape = CircleShape
        ),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = color.copy(alpha = 0.9f),
            selectedContainerColor = color,
            labelColor = Color.Transparent,
            selectedLabelColor = Color.Transparent,
            iconColor = if (color.luminance() > 0.5f) Color.Black else Color.White,
            selectedLeadingIconColor = if (color.luminance() > 0.5f) Color.Black else Color.White
        ),
        border = null,
        modifier = Modifier
            .size(56.dp)
            .semantics { contentDescription = colorDescription }
    )
}