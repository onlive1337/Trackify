package com.onlive.trackify.ui.screens.category

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.utils.stringResource
import com.onlive.trackify.viewmodel.CategoryViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    categoryId: Long,
    onNavigateBack: () -> Unit,
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val existingCategory by categoryViewModel.getCategoryById(categoryId).observeAsState()

    CategoryDetailContent(
        title = if (categoryId == -1L) stringResource(R.string.add_category) else stringResource(R.string.edit_category),
        initialName = existingCategory?.name ?: "",
        initialColor = existingCategory?.colorCode ?: "#FF5252",
        isEditMode = categoryId != -1L,
        onNavigateBack = onNavigateBack,
        onSave = { name, color ->
            if (categoryId == -1L) {
                categoryViewModel.insert(Category(name = name, colorCode = color))
            } else {
                existingCategory?.let {
                    categoryViewModel.update(it.copy(name = name, colorCode = color))
                }
            }
            onNavigateBack()
        },
        onDelete = {
            existingCategory?.let { categoryViewModel.delete(it) }
            onNavigateBack()
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailContent(
    title: String,
    initialName: String,
    initialColor: String,
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

    var categoryName by remember(initialName) { mutableStateOf(initialName) }
    var categoryColor by remember(initialColor) { mutableStateOf(initialColor) }
    val showDeleteDialogState = remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TrackifyTopAppBar(
                title = title,
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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

                    val previewColor = try { Color(categoryColor.toColorInt()) } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                    
                    SubscriptionItemPreview(
                        name = stringResource(R.string.mock_subscription_name),
                        categoryName = categoryName.ifEmpty { stringResource(R.string.category_name) },
                        price = stringResource(R.string.mock_subscription_price),
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
                        onValueChange = { categoryName = it },
                        placeholder = { Text(stringResource(R.string.category_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = MaterialTheme.shapes.extraLarge,
                        textStyle = MaterialTheme.typography.bodyLargeEmphasized,
                        isError = categoryName.isEmpty(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        )
                    )

                    if (categoryName.isEmpty()) {
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

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 56.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.height(280.dp)
                    ) {
                        items(predefinedColors) { colorCode ->
                            ColorChip(
                                colorCode = colorCode,
                                isSelected = colorCode == categoryColor,
                                onClick = { categoryColor = colorCode }
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
                            shape = MaterialTheme.shapes.extraLarge,
                            modifier = Modifier.weight(1f).height(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.delete))
                        }
                    }

                    Button(
                        onClick = {
                            if (categoryName.isNotEmpty()) {
                                onSave(categoryName, categoryColor)
                            }
                        },
                        shape = MaterialTheme.shapes.extraLarge,
                        modifier = Modifier.weight(1.5f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = stringResource(R.string.save),
                            style = MaterialTheme.typography.titleMediumEmphasized
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialogState.value = false }) {
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color.copy(alpha = 0.1f))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(40.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(color)
            )

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

                Text(
                    text = categoryName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = price,
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = color
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
    val color = try {
        Color(colorCode.toColorInt())
    } catch (e: Exception) {
        Color.Gray
    }

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
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
        modifier = Modifier.size(56.dp)
    )
}