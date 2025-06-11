package com.onlive.trackify.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.onlive.trackify.utils.stringResource
import androidx.compose.ui.unit.dp
import com.onlive.trackify.R
import com.onlive.trackify.ui.components.TrackifyCard
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.utils.DataExportImportManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DataManagementScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val dataManager = remember { DataExportImportManager(context) }

    var showImportDialog by remember { mutableStateOf(false) }
    var showExportSuccessDialog by remember { mutableStateOf(false) }
    var showImportSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var exportFilePath by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            isLoading = true
            coroutineScope.launch {
                try {
                    val success = withContext(Dispatchers.IO) {
                        dataManager.exportData(it)
                    }
                    if (success) {
                        exportFilePath = it.lastPathSegment ?: it.toString()
                        showExportSuccessDialog = true
                    } else {
                        errorMessage = context.getString(R.string.data_export_error)
                        showErrorDialog = true
                    }
                } catch (e: Exception) {
                    errorMessage = e.message ?: context.getString(R.string.data_export_error)
                    showErrorDialog = true
                } finally {
                    isLoading = false
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            isLoading = true
            coroutineScope.launch {
                try {
                    val success = withContext(Dispatchers.IO) {
                        dataManager.importData(it)
                    }
                    if (success) {
                        showImportSuccessDialog = true
                    } else {
                        errorMessage = context.getString(R.string.data_import_error)
                        showErrorDialog = true
                    }
                } catch (e: Exception) {
                    errorMessage = e.message ?: context.getString(R.string.data_import_error)
                    showErrorDialog = true
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TrackifyTopAppBar(
                title = stringResource(R.string.data_management),
                showBackButton = true,
                onBackClick = onNavigateBack
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
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

                TrackifyCard(
                    title = stringResource(R.string.export_title)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.export_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                                val dateString = dateFormat.format(Date())
                                val fileName = "trackify_backup_$dateString.json"

                                exportLauncher.launch(fileName)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Text(stringResource(R.string.export_button))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                TrackifyCard(
                    title = stringResource(R.string.import_title)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.import_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { showImportDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Text(stringResource(R.string.import_button))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text(stringResource(R.string.import_confirmation_title)) },
            text = { Text(stringResource(R.string.import_confirmation_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        showImportDialog = false
                        importLauncher.launch(arrayOf("application/json"))
                    }
                ) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showImportDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showExportSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showExportSuccessDialog = false },
            title = { Text(stringResource(R.string.export_success, exportFilePath)) },
            text = { Text(stringResource(R.string.export_success, exportFilePath)) },
            confirmButton = {
                TextButton(
                    onClick = { showExportSuccessDialog = false }
                ) {
                    Text(stringResource(R.string.done))
                }
            }
        )
    }

    if (showImportSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showImportSuccessDialog = false },
            title = { Text(stringResource(R.string.import_success)) },
            text = {
                Column {
                    Text(stringResource(R.string.import_success))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.import_restart_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImportSuccessDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text(stringResource(R.string.done))
                }
            }
        )
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text(stringResource(R.string.import_error)) },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(
                    onClick = { showErrorDialog = false }
                ) {
                    Text(stringResource(R.string.done))
                }
            }
        )
    }
}