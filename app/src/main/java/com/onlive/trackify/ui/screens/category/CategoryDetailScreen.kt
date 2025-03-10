package com.onlive.trackify.ui.screens.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.viewmodel.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    categoryId: Long,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val predefinedColors = listOf(
        "#FF5252", "#FF4081", "#E040FB", "#7C4DFF", "#536DFE", "#448AFF", "#40C4FF", "#18FFFF",
        "#64FFDA", "#69F0AE", "#B2FF59", "#EEFF41", "#FFFF00", "#FFD740", "#FFAB40", "#FF6E40",
        "#8D6E63", "#BDBDBD", "#212121"
    )

    var categoryName by remember { mutableStateOf("") }
    var categoryColor by remember { mutableStateOf(predefinedColors[0]) }

    val existingCategory by categoryViewModel.getCategoryById(categoryId).observeAsState()

    existingCategory?.let {
        categoryName = it.name
        categoryColor = it.colorCode
    }

    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TrackifyTopAppBar(
                title = if (categoryId == -1L) stringResource(R.string.add_category) else stringResource(R.string.edit_category),
                showBackButton = true,
                onBackClick = onNavigateBack
            )
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

            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                label = { Text(stringResource(R.string.category_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = categoryName.isEmpty()
            )

            if (categoryName.isEmpty()) {
                Text(
                    text = stringResource(R.string.category_no_name),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.category_color),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            try {
                                Color(android.graphics.Color.parseColor(categoryColor))
                            } catch (e: Exception) {
                                Color.Gray
                            }
                        )
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = categoryColor,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(200.dp)
            ) {
                items(predefinedColors) { colorCode ->
                    ColorPickerItem(
                        colorCode = colorCode,
                        isSelected = colorCode == categoryColor,
                        onClick = { categoryColor = colorCode }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (categoryId != -1L) {
                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.delete))
                    }

                    Spacer(modifier = Modifier.width(16.dp))
                }

                Button(
                    onClick = {
                        if (categoryName.isNotEmpty()) {
                            if (categoryId == -1L) {
                                val newCategory = Category(
                                    name = categoryName,
                                    colorCode = categoryColor
                                )
                                categoryViewModel.insert(newCategory)
                            } else {
                                existingCategory?.let {
                                    val updatedCategory = it.copy(
                                        name = categoryName,
                                        colorCode = categoryColor
                                    )
                                    categoryViewModel.update(updatedCategory)
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

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text(stringResource(R.string.delete_category_confirmation)) },
                text = { Text(stringResource(R.string.delete_category_message)) },
                confirmButton = {
                    Button(
                        onClick = {
                            existingCategory?.let {
                                categoryViewModel.delete(it)
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
fun ColorPickerItem(
    colorCode: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderWidth = if (isSelected) 3.dp else 1.dp
    val color = try {
        Color(android.graphics.Color.parseColor(colorCode))
    } catch (e: Exception) {
        Color.Gray
    }

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = borderWidth,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    )
}