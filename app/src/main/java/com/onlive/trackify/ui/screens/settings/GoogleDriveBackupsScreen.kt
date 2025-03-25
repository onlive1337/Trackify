package com.onlive.trackify.ui.screens.settings

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.onlive.trackify.R
import com.onlive.trackify.ui.components.TrackifyCard
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.utils.DataExportImportManager
import com.onlive.trackify.utils.GoogleDriveManager
import kotlinx.coroutines.launch
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleDriveBackupsScreen(
    onNavigateBack: () -> Unit,
    dataManager: DataExportImportManager? = null
) {
    val context = LocalContext.current
    val actualDataManager = dataManager ?: DataExportImportManager(context)
    val driveManager = actualDataManager.getGoogleDriveManager()

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isSignedIn by remember { mutableStateOf(driveManager.isUserSignedIn()) }
    var isLoading by remember { mutableStateOf(false) }
    var backups by remember { mutableStateOf<List<GoogleDriveManager.BackupFileInfo>>(emptyList()) }

    var showRestoreDialog by remember { mutableStateOf<GoogleDriveManager.BackupFileInfo?>(null) }
    var showDeleteDialog by remember { mutableStateOf<GoogleDriveManager.BackupFileInfo?>(null) }
    var showSignOutDialog by remember { mutableStateOf(false) }

    val signInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task, driveManager) { success ->
                isSignedIn = success
                if (success) {
                    coroutineScope.launch {
                        loadBackups(driveManager) { loadedBackups ->
                            backups = loadedBackups
                        }
                    }
                } else {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.google_drive_sign_in_failed)
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(isSignedIn) {
        if (isSignedIn) {
            loadBackups(driveManager) { loadedBackups ->
                backups = loadedBackups
            }
        }
    }

    Scaffold(
        topBar = {
            TrackifyTopAppBar(
                title = stringResource(R.string.google_drive_backups),
                showBackButton = true,
                onBackClick = onNavigateBack,
                actions = {
                    if (isSignedIn) {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                loadBackups(driveManager) { loadedBackups ->
                                    backups = loadedBackups
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(R.string.refresh)
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            if (isSignedIn) {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            try {
                                val result = actualDataManager.exportDataToGoogleDrive()
                                if (result.isSuccess) {
                                    snackbarHostState.showSnackbar(
                                        message = context.getString(R.string.backup_created_successfully)
                                    )
                                    loadBackups(driveManager) { loadedBackups ->
                                        backups = loadedBackups
                                    }
                                } else {
                                    snackbarHostState.showSnackbar(
                                        message = context.getString(R.string.backup_creation_failed)
                                    )
                                }
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar(
                                    message = context.getString(R.string.backup_creation_failed)
                                )
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Backup,
                        contentDescription = stringResource(R.string.create_backup)
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                if (!isSignedIn) {
                    SignInCard(
                        onSignInClick = {
                            val signInIntent = driveManager.getSignInIntent()
                            signInLauncher.launch(signInIntent)
                        }
                    )
                } else {
                    UserInfoCard(
                        onSignOutClick = {
                            showSignOutDialog = true
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    BackupsList(
                        backups = backups,
                        onRestoreClick = { backup ->
                            showRestoreDialog = backup
                        },
                        onDeleteClick = { backup ->
                            showDeleteDialog = backup
                        }
                    )
                }

                Spacer(modifier = Modifier.height(80.dp)) // Для FloatingActionButton
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    if (showRestoreDialog != null) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = null },
            title = { Text(stringResource(R.string.restore_from_backup)) },
            text = { Text(stringResource(R.string.restore_from_backup_confirm, showRestoreDialog?.name ?: "")) },
            confirmButton = {
                Button(
                    onClick = {
                        val backup = showRestoreDialog
                        showRestoreDialog = null

                        if (backup != null) {
                            coroutineScope.launch {
                                isLoading = true
                                try {
                                    val success = actualDataManager.importDataFromGoogleDrive(backup.id)
                                    if (success) {
                                        snackbarHostState.showSnackbar(
                                            message = context.getString(R.string.restore_successful)
                                        )
                                    } else {
                                        snackbarHostState.showSnackbar(
                                            message = context.getString(R.string.restore_failed)
                                        )
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar(
                                        message = context.getString(R.string.restore_failed)
                                    )
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    }
                ) {
                    Text(stringResource(R.string.restore))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRestoreDialog = null }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(stringResource(R.string.delete_backup)) },
            text = { Text(stringResource(R.string.delete_backup_confirm, showDeleteDialog?.name ?: "")) },
            confirmButton = {
                Button(
                    onClick = {
                        val backup = showDeleteDialog
                        showDeleteDialog = null

                        if (backup != null) {
                            coroutineScope.launch {
                                isLoading = true
                                try {
                                    val result = driveManager.deleteBackup(backup.id)
                                    if (result.isSuccess && result.getOrNull() == true) {
                                        snackbarHostState.showSnackbar(
                                            message = context.getString(R.string.backup_deleted)
                                        )
                                        loadBackups(driveManager) { loadedBackups ->
                                            backups = loadedBackups
                                        }
                                    } else {
                                        snackbarHostState.showSnackbar(
                                            message = context.getString(R.string.backup_deletion_failed)
                                        )
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar(
                                        message = context.getString(R.string.backup_deletion_failed)
                                    )
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = null }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text(stringResource(R.string.sign_out)) },
            text = { Text(stringResource(R.string.sign_out_confirm)) },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        driveManager.signOut()
                        isSignedIn = false
                        backups = emptyList()
                    }
                ) {
                    Text(stringResource(R.string.sign_out))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSignOutDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

private suspend fun loadBackups(
    driveManager: GoogleDriveManager,
    onComplete: (List<GoogleDriveManager.BackupFileInfo>) -> Unit
) {
    try {
        val result = driveManager.getBackupsList()
        if (result.isSuccess) {
            val backups = result.getOrNull() ?: emptyList()

            val sortedBackups = backups.sortedByDescending {
                try {
                    val timeStr = it.createdTime
                    if (timeStr.isNotEmpty()) {
                        return@sortedByDescending timeStr
                    }
                } catch (e: ParseException) {
                    Log.e("GoogleDriveBackups", "Error parsing date: ${e.message}")
                }
                ""
            }
            onComplete(sortedBackups)
        } else {
            onComplete(emptyList())
        }
    } catch (e: Exception) {
        Log.e("GoogleDriveBackups", "Error loading backups: ${e.message}", e)
        onComplete(emptyList())
    }
}

private fun handleSignInResult(
    completedTask: Task<GoogleSignInAccount>,
    driveManager: GoogleDriveManager,
    onResult: (Boolean) -> Unit
) {
    try {
        val account = completedTask.getResult(ApiException::class.java)
        onResult(driveManager.handleSignInResult(account))
    } catch (e: ApiException) {
        Log.e("GoogleDriveBackups", "Sign in failed: ${e.statusCode}", e)
        onResult(false)
    } catch (e: Exception) {
        Log.e("GoogleDriveBackups", "Sign in failed with exception: ${e.message}", e)
        onResult(false)
    }
}

@Composable
fun SignInCard(onSignInClick: () -> Unit) {
    TrackifyCard {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.google_drive_signin_title),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.google_drive_signin_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSignInClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.sign_in_with_google))
            }
        }
    }
}

@Composable
fun UserInfoCard(onSignOutClick: () -> Unit) {
    val account = GoogleSignIn.getLastSignedInAccount(LocalContext.current)

    TrackifyCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(R.string.signed_in_as),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = account?.email ?: stringResource(R.string.unknown_account),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Button(
                onClick = onSignOutClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(stringResource(R.string.sign_out))
            }
        }
    }
}

@Composable
fun BackupsList(
    backups: List<GoogleDriveManager.BackupFileInfo>,
    onRestoreClick: (GoogleDriveManager.BackupFileInfo) -> Unit,
    onDeleteClick: (GoogleDriveManager.BackupFileInfo) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.backups_on_drive),
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (backups.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_backups_found),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.height(400.dp)
            ) {
                items(backups) { backup ->
                    BackupItem(
                        backup = backup,
                        onRestoreClick = { onRestoreClick(backup) },
                        onDeleteClick = { onDeleteClick(backup) }
                    )
                }
            }
        }
    }
}

@Composable
fun BackupItem(
    backup: GoogleDriveManager.BackupFileInfo,
    onRestoreClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val fileName = backup.name
    val displayName = formatBackupFileName(fileName)
    val createdDate = formatBackupDate(backup.createdTime)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = createdDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = formatFileSize(backup.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onRestoreClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(stringResource(R.string.restore))
                }

                TextButton(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(stringResource(R.string.delete))
                }
            }
        }
    }
}

private fun formatBackupFileName(fileName: String): String {
    val regex = Regex("trackify_backup_(\\d{8}_\\d{6})\\.json")
    val matchResult = regex.find(fileName)

    return if (matchResult != null) {
        val dateStr = matchResult.groupValues[1]
        try {
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val date = dateFormat.parse(dateStr)
            if (date != null) {
                val displayFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                displayFormat.format(date)
            } else {
                fileName
            }
        } catch (e: Exception) {
            fileName
        }
    } else {
        fileName
    }
}

private fun formatBackupDate(dateTimeStr: String): String {
    if (dateTimeStr.isEmpty()) return ""

    try {
        val inputDateStr = dateTimeStr.replace("Z", "+00:00")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        val date = dateFormat.parse(inputDateStr)

        if (date != null) {
            val displayFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            return displayFormat.format(date)
        }
    } catch (e: Exception) {
        Log.e("GoogleDriveBackups", "Error formatting date: $dateTimeStr", e)
    }

    return dateTimeStr
}

private fun formatFileSize(sizeStr: String): String {
    if (sizeStr.isEmpty() || sizeStr == "Unknown") return ""

    try {
        val size = sizeStr.toLong()
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> String.format("%.1f MB", size / (1024.0 * 1024.0))
        }
    } catch (e: NumberFormatException) {
        return sizeStr
    }
}