package com.onlive.trackify.ui.screens.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.onlive.trackify.R
import com.onlive.trackify.data.model.Category
import com.onlive.trackify.data.model.CategoryGroup
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.viewmodel.CategoryGroupViewModel
import com.onlive.trackify.viewmodel.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

    var selectedTab by remember { mutableStateOf(0) }
    var showDeleteCategoryDialog by remember { mutableStateOf<Category?>(null) }
    var showDeleteGroupDialog by remember { mutableStateOf<CategoryGroup?>(null) }

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
                    if (selectedTab == 0) {
                        onAddCategory()
                    } else {
                        onAddCategoryGroup()
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
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
                selectedTabIndex = selectedTab
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(stringResource(R.string.categories)) }
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(stringResource(R.string.category_groups)) }
                )
            }

            when (selectedTab) {
                0 -> {
                    if (categories.isEmpty()) {
                        EmptyCategoriesView()
                    } else {
                        CategoriesList(
                            categories = categories,
                            onEditCategory = onEditCategory,
                            onDeleteCategory = { category ->
                                showDeleteCategoryDialog = category
                            }
                        )
                    }
                }
                1 -> {
                    if (categoryGroups.isEmpty()) {
                        EmptyGroupsView()
                    } else {
                        CategoryGroupsList(
                            groups = categoryGroups,
                            onEditGroup = onEditCategoryGroup,
                            onDeleteGroup = { group ->
                                showDeleteGroupDialog = group
                            }
                        )
                    }
                }
            }
        }
    }

    showDeleteCategoryDialog?.let { category ->
        AlertDialog(
            onDismissRequest = { showDeleteCategoryDialog = null },
            title = { Text(stringResource(R.string.delete_category_confirmation)) },
            text = { Text(stringResource(R.string.delete_category_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        categoryViewModel.delete(category)
                        showDeleteCategoryDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCategoryDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    showDeleteGroupDialog?.let { group ->
        AlertDialog(
            onDismissRequest = { showDeleteGroupDialog = null },
            title = { Text(stringResource(R.string.delete_group_confirmation)) },
            text = { Text(stringResource(R.string.delete_group_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        categoryGroupViewModel.delete(group)
                        showDeleteGroupDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteGroupDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
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
            CategoryItem(
                category = category,
                onEditClick = { onEditCategory(category.categoryId) },
                onDeleteClick = { onDeleteCategory(category) }
            )
        }
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
            CategoryGroupItem(
                group = group,
                onEditClick = { onEditGroup(group.groupId) },
                onDeleteClick = { onDeleteGroup(group) }
            )
        }
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
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = group.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    contentDescription = stringResource(R.string.delete_group),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}