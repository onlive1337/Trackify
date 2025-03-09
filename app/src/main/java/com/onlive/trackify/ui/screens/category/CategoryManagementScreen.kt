package com.onlive.trackify.ui.screens.category

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.model.CategoryGroup
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.viewmodel.CategoryGroupViewModel
import com.onlive.trackify.viewmodel.CategoryViewModel

enum class CategoryTab {
    CATEGORIES, GROUPS
}

@Composable
fun CategoryManagementScreen(
    onNavigateBack: () -> Unit,
    onAddCategory: () -> Unit,
    onEditCategory: (Long) -> Unit,
    onAddCategoryGroup: () -> Unit,
    onEditCategoryGroup: (Long) -> Unit,
    modifier: Modifier = Modifier,
    categoryViewModel: CategoryViewModel = viewModel(),
    categoryGroupViewModel: CategoryGroupViewModel = viewModel()
) {
    val categories by categoryViewModel.allCategories.observeAsState(emptyList())
    val categoryGroups by categoryGroupViewModel.allGroups.observeAsState(emptyList())

    var selectedTab by remember { mutableStateOf(CategoryTab.CATEGORIES) }

    Scaffold(
        topBar = {
            TrackifyTopAppBar(
                title = stringResource(R.string.manage_categories),
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (selectedTab == CategoryTab.CATEGORIES) {
                        onAddCategory()
                    } else {
                        onAddCategoryGroup()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = null)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTab.ordinal
            ) {
                Tab(
                    selected = selectedTab == CategoryTab.CATEGORIES,
                    onClick = { selectedTab = CategoryTab.CATEGORIES },
                    text = { Text(stringResource(R.string.categories)) }
                )

                Tab(
                    selected = selectedTab == CategoryTab.GROUPS,
                    onClick = { selectedTab = CategoryTab.GROUPS },
                    text = { Text(stringResource(R.string.category_groups)) }
                )
            }

            when (selectedTab) {
                CategoryTab.CATEGORIES -> {
                    if (categories.isEmpty()) {
                        EmptyCategoriesView()
                    } else {
                        CategoriesList(
                            categories = categories,
                            onEditCategory = onEditCategory,
                            onDeleteCategory = { category ->
                                categoryViewModel.delete(category)
                            }
                        )
                    }
                }
                CategoryTab.GROUPS -> {
                    if (categoryGroups.isEmpty()) {
                        EmptyGroupsView()
                    } else {
                        CategoryGroupsList(
                            groups = categoryGroups,
                            onEditGroup = onEditCategoryGroup,
                            onDeleteGroup = { group ->
                                categoryGroupViewModel.delete(group)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyCategoriesView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.no_categories),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun EmptyGroupsView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.no_category_groups),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun CategoriesList(
    categories: List<Category>,
    onEditCategory: (Long) -> Unit,
    onDeleteCategory: (Category) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            CategoryItemWithDialog(
                category = category,
                onEditClick = { onEditCategory(category.categoryId) },
                onDeleteClick = { onDeleteCategory(category) }
            )
        }
    }
}

@Composable
fun CategoryItemWithDialog(
    category: Category,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    CategoryItem(
        category = category,
        onEditClick = onEditClick,
        onDeleteClick = { showDeleteDialog = true }
    )

    if (showDeleteDialog) {
        DeleteCategoryDialog(
            categoryName = category.name,
            onConfirm = {
                onDeleteClick()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
fun CategoryItem(
    category: Category,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        try {
                            Color(android.graphics.Color.parseColor(category.colorCode))
                        } catch (e: Exception) {
                            Color.Gray
                        }
                    )
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_category),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_category),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun DeleteCategoryDialog(
    categoryName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_category_confirmation)) },
        text = { Text(stringResource(R.string.delete_category_message)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun CategoryGroupsList(
    groups: List<CategoryGroup>,
    onEditGroup: (Long) -> Unit,
    onDeleteGroup: (CategoryGroup) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(groups) { group ->
            CategoryGroupItemWithDialog(
                group = group,
                onEditClick = { onEditGroup(group.groupId) },
                onDeleteClick = { onDeleteGroup(group) }
            )
        }
    }
}

@Composable
fun CategoryGroupItemWithDialog(
    group: CategoryGroup,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    CategoryGroupItem(
        group = group,
        onEditClick = onEditClick,
        onDeleteClick = { showDeleteDialog = true }
    )

    if (showDeleteDialog) {
        DeleteGroupDialog(
            groupName = group.name,
            onConfirm = {
                onDeleteClick()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
fun CategoryGroupItem(
    group: CategoryGroup,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEditClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            try {
                                Color(android.graphics.Color.parseColor(group.colorCode))
                            } catch (e: Exception) {
                                Color.Gray
                            }
                        )
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.titleMedium
                    )

                    if (!group.description.isNullOrEmpty()) {
                        Text(
                            text = group.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit_category),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_category),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteGroupDialog(
    groupName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_group_confirmation)) },
        text = { Text(stringResource(R.string.delete_group_message)) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}