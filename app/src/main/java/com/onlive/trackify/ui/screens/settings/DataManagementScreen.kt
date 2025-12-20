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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.onlive.trackify.R
import com.onlive.trackify.ui.components.TrackifyOutlinedCard
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.utils.DataExportImportManager
import com.onlive.trackify.utils.LocalLocalizedContext
import com.onlive.trackify.utils.stringResource
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
    val context = LocalLocalizedContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val dataManager = remember { DataExportImportManager(context) }

    val showImportDialogState = remember { mutableStateOf(false) }
    val showExportSuccessDialogState = remember { mutableStateOf(false) }
    val showImportSuccessDialogState = remember { mutableStateOf(false) }
    val showErrorDialogState = remember { mutableStateOf(false) }
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
                        showExportSuccessDialogState.value = true
                    } else {
                        errorMessage = context.getString(R.string.data_export_error)
                        showErrorDialogState.value = true
                    }
                } catch (e: Exception) {
                    errorMessage = e.message ?: context.getString(R.string.data_export_error)
                    showErrorDialogState.value = true
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
                        showImportSuccessDialogState.value = true
                    } else {
                        errorMessage = context.getString(R.string.data_import_error)
                        showErrorDialogState.value = true
                    }
                } catch (e: Exception) {
                    errorMessage = e.message ?: context.getString(R.string.data_import_error)
                    showErrorDialogState.value = true
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

                TrackifyOutlinedCard(
                    title = stringResource(R.string.export_title)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = stringResource(R.string.export_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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

                TrackifyOutlinedCard(
                    title = stringResource(R.string.import_title)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = stringResource(R.string.import_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                             onClick = {
                                 haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                 showImportDialogState.value = true
                             },
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

    if (showImportDialogState.value) {
        AlertDialog(
            onDismissRequest = { showImportDialogState.value = false },
            title = { Text(stringResource(R.string.import_confirmation_title)) },
            text = { Text(stringResource(R.string.import_confirmation_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        showImportDialogState.value = false
                        importLauncher.launch(arrayOf("application/json"))
                    }
                ) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showImportDialogState.value = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showExportSuccessDialogState.value) {
        AlertDialog(
            onDismissRequest = { showExportSuccessDialogState.value = false },
            title = { Text(stringResource(R.string.export_success, exportFilePath)) },
            text = { Text(stringResource(R.string.export_success, exportFilePath)) },
            confirmButton = {
                TextButton(
                    onClick = { showExportSuccessDialogState.value = false }
                ) {
                    Text(stringResource(R.string.done))
                }
            }
        )
    }

    if (showImportSuccessDialogState.value) {
        AlertDialog(
            onDismissRequest = { showImportSuccessDialogState.value = false },
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
                        showImportSuccessDialogState.value = false
                        onNavigateBack()
                    }
                ) {
                    Text(stringResource(R.string.done))
                }
            }
        )
    }

    if (showErrorDialogState.value) {
        AlertDialog(
            onDismissRequest = { showErrorDialogState.value = false },
            title = { Text(stringResource(R.string.import_error)) },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(
                    onClick = { showErrorDialogState.value = false }
                ) {
                    Text(stringResource(R.string.done))
                }
            }
        )
    }
}