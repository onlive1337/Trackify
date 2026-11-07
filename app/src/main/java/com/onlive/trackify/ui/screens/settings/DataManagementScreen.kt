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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.onlive.trackify.R
import com.onlive.trackify.ui.components.SettingsSection
import com.onlive.trackify.ui.components.TrackifyTopAppBar
import com.onlive.trackify.utils.DataExportImportManager
import com.onlive.trackify.utils.LocalLocaleManager
import com.onlive.trackify.utils.LocalLocalizedContext
import com.onlive.trackify.utils.NotificationHelper
import com.onlive.trackify.utils.PreferenceManager
import com.onlive.trackify.utils.ThemeManager
import com.onlive.trackify.utils.stringResource
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val BACKUP_FILE_DATE_FORMATTER: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.US)

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(
    onNavigateBack: () -> Unit,
    themeManager: ThemeManager
) {
    val context = LocalLocalizedContext.current
    val localeManager = LocalLocaleManager.current
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
                        themeManager.reloadFromPreferences()
                        val restoredLanguage = PreferenceManager(context).getLanguageCode()
                        localeManager.setLocale(restoredLanguage)
                        NotificationHelper(context).createNotificationChannel()
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

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TrackifyTopAppBar(
                title = stringResource(R.string.data_management),
                showBackButton = true,
                onBackClick = onNavigateBack,
                scrollBehavior = scrollBehavior
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

                SettingsSection(
                    title = stringResource(R.string.export_title)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(
                            text = stringResource(R.string.export_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val dateString = LocalDateTime.now().format(BACKUP_FILE_DATE_FORMATTER)
                                val fileName = "trackify_backup_$dateString.json"
                                exportLauncher.launch(fileName)
                            },
                            shapes = ButtonDefaults.shapes(),
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Text(stringResource(R.string.export_button))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                SettingsSection(
                    title = stringResource(R.string.import_title)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
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
                            shapes = ButtonDefaults.shapes(),
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
                    LoadingIndicator(color = MaterialTheme.colorScheme.primary)
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
                    },
                    shapes = ButtonDefaults.shapes()
                ) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showImportDialogState.value = false },
                    shapes = ButtonDefaults.shapes()
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showExportSuccessDialogState.value) {
        AlertDialog(
            onDismissRequest = { showExportSuccessDialogState.value = false },
            title = { Text(stringResource(R.string.export_success_title)) },
            text = { Text(stringResource(R.string.export_success, exportFilePath)) },
            confirmButton = {
                TextButton(
                    onClick = { showExportSuccessDialogState.value = false },
                    shapes = ButtonDefaults.shapes()
                ) {
                    Text(stringResource(R.string.done))
                }
            }
        )
    }

    if (showImportSuccessDialogState.value) {
        AlertDialog(
            onDismissRequest = { showImportSuccessDialogState.value = false },
            title = { Text(stringResource(R.string.import_success_title)) },
            text = { Text(stringResource(R.string.import_success)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImportSuccessDialogState.value = false
                        onNavigateBack()
                    },
                    shapes = ButtonDefaults.shapes()
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
                    onClick = { showErrorDialogState.value = false },
                    shapes = ButtonDefaults.shapes()
                ) {
                    Text(stringResource(R.string.done))
                }
            }
        )
    }
}