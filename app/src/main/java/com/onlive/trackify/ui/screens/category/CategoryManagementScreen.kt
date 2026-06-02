package com.onlive.trackify.ui.screens.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material3.*
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.ui.components.EmptyState
import com.onlive.trackify.ui.components.TrackifyFab
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.utils.stringResource
import com.onlive.trackify.viewmodel.CategoryViewModel
import androidx.core.graphics.toColorInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    onNavigateBack: () -> Unit,
    onAddCategory: () -> Unit,
    onEditCategory: (Long) -> Unit,
    categoryViewModel: CategoryViewModel = viewModel()
) {
    val allCategories by categoryViewModel.allCategories.observeAsState(emptyList())
    val showDeleteDialogState = remember { mutableStateOf<Category?>(null) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TrackifyTopAppBar(
                title = stringResource(R.string.manage_categories),
                showBackButton = true,
                onBackClick = onNavigateBack,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (allCategories.isEmpty()) {
                EmptyCategoriesView()
            } else {
                CategoriesList(
                    categories = allCategories,
                    onEditCategory = onEditCategory,
                    onDeleteCategory = { showDeleteDialogState.value = it }
                )
            }

            TrackifyFab(
                onClick = onAddCategory,
                contentDescription = stringResource(R.string.add_category),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            )
        }

        if (showDeleteDialogState.value != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialogState.value = null },
                title = { Text(stringResource(R.string.delete_category_confirmation)) },
                text = { Text(stringResource(R.string.delete_category_message)) },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialogState.value?.let {
                                categoryViewModel.delete(it)
                            }
                            showDeleteDialogState.value = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialogState.value = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }
}

@Composable
fun EmptyCategoriesView() {
    EmptyState(
        icon = Icons.Rounded.Category,
        title = stringResource(R.string.no_categories)
    )
}

@Composable
fun CategoriesList(
    categories: List<Category>,
    onEditCategory: (Long) -> Unit,
    onDeleteCategory: (Category) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { category ->
            CategoryItem(
                category = category,
                onEdit = { onEditCategory(category.categoryId) },
                onDelete = { onDeleteCategory(category) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CategoryItem(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = try {
        Color(category.colorCode.toColorInt())
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(categoryColor)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMediumEmphasized,
                modifier = Modifier.weight(1f)
            )

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.edit_category)) },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(R.string.delete),
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}
