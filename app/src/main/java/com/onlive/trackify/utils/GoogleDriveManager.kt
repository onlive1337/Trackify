package com.onlive.trackify.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GoogleDriveManager(private val context: Context) {

    companion object {
        private const val TAG = "GoogleDriveManager"
        private const val BACKUP_FOLDER_NAME = "TrackifyBackups"
        private const val BACKUP_FILE_PREFIX = "trackify_backup_"
        private const val BACKUP_FILE_EXTENSION = ".json"
    }

    private var driveServiceHelper: DriveServiceHelper? = null
    private lateinit var googleSignInClient: GoogleSignInClient

    init {
        initializeGoogleSignIn()
    }

    private fun initializeGoogleSignIn() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, signInOptions)
    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    fun signOut() {
        googleSignInClient.signOut()
        driveServiceHelper = null
    }

    fun handleSignInResult(account: GoogleSignInAccount): Boolean {
        return try {
            val credential = GoogleAccountCredential.usingOAuth2(
                context, listOf(DriveScopes.DRIVE_FILE)
            )
            credential.selectedAccount = account.account

            val drive = Drive.Builder(
                NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName("Trackify")
                .build()

            driveServiceHelper = DriveServiceHelper(drive)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Google Drive service: ${e.message}", e)
            false
        }
    }

    fun isUserSignedIn(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return account != null && driveServiceHelper != null
    }

    suspend fun backupToDrive(data: String): Result<String> = withContext(Dispatchers.IO) {
        if (driveServiceHelper == null) {
            return@withContext Result.Error("Not signed in to Google Drive")
        }

        try {
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val dateString = dateFormat.format(Date())
            val fileName = "$BACKUP_FILE_PREFIX$dateString$BACKUP_FILE_EXTENSION"

            val folderId = getOrCreateBackupFolder()
            val fileId = driveServiceHelper?.createFile(fileName, data, folderId)

            if (fileId != null) {
                return@withContext Result.Success(fileId)
            } else {
                return@withContext Result.Error("Failed to create backup file")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error backing up to Drive: ${e.message}", e)
            return@withContext Result.Error(e.message ?: "Error backing up to Drive")
        }
    }

    suspend fun getBackupsList(): Result<List<BackupFileInfo>> = withContext(Dispatchers.IO) {
        if (driveServiceHelper == null) {
            return@withContext Result.Error("Not signed in to Google Drive")
        }

        try {
            val folderId = getOrCreateBackupFolder()
            val files = driveServiceHelper?.listFilesInFolder(folderId) ?: emptyList()

            val backupFiles = files.filter {
                it.name?.startsWith(BACKUP_FILE_PREFIX) == true &&
                        it.name?.endsWith(BACKUP_FILE_EXTENSION) == true
            }.map { file ->
                BackupFileInfo(
                    id = file.id,
                    name = file.name ?: "unknown",
                    createdTime = file.createdTime?.toString() ?: "",
                    size = file.size.toString()
                )
            }

            return@withContext Result.Success(backupFiles)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting backups list: ${e.message}", e)
            return@withContext Result.Error(e.message ?: "Error getting backups list")
        }
    }

    suspend fun restoreFromDrive(fileId: String): Result<String> = withContext(Dispatchers.IO) {
        if (driveServiceHelper == null) {
            return@withContext Result.Error("Not signed in to Google Drive")
        }

        try {
            val content = driveServiceHelper?.readFile(fileId)
            if (content != null) {
                return@withContext Result.Success(content)
            } else {
                return@withContext Result.Error("Failed to read backup file")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring from Drive: ${e.message}", e)
            return@withContext Result.Error(e.message ?: "Error restoring from Drive")
        }
    }

    suspend fun deleteBackup(fileId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        if (driveServiceHelper == null) {
            return@withContext Result.Error("Not signed in to Google Drive")
        }

        try {
            val success = driveServiceHelper?.deleteFile(fileId) == true
            return@withContext Result.Success(success)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting backup: ${e.message}", e)
            return@withContext Result.Error(e.message ?: "Error deleting backup")
        }
    }

    private suspend fun getOrCreateBackupFolder(): String = withContext(Dispatchers.IO) {
        driveServiceHelper?.let { helper ->
            val existingFolder = helper.findFolder(BACKUP_FOLDER_NAME)
            if (existingFolder != null) {
                return@withContext existingFolder
            }

            return@withContext helper.createFolder(BACKUP_FOLDER_NAME)
                ?: throw Exception("Failed to create backup folder")
        } ?: throw Exception("DriveServiceHelper is null")
    }

    data class BackupFileInfo(
        val id: String,
        val name: String,
        val createdTime: String,
        val size: String
    )

    private inner class DriveServiceHelper(private val drive: Drive) {

        suspend fun createFile(name: String, content: String, parentFolderId: String? = null): String? =
            withContext(Dispatchers.IO) {
                try {
                    val fileMetadata = File().apply {
                        this.name = name
                        if (parentFolderId != null) {
                            parents = listOf(parentFolderId)
                        }
                    }

                    val file = drive.files().create(fileMetadata,
                        com.google.api.client.http.ByteArrayContent.fromString("application/json", content))
                        .setFields("id")
                        .execute()

                    return@withContext file.id
                } catch (e: Exception) {
                    Log.e(TAG, "Error creating file: ${e.message}", e)
                    return@withContext null
                }
            }

        suspend fun readFile(fileId: String): String? = withContext(Dispatchers.IO) {
            try {
                drive.files().get(fileId).executeMediaAsInputStream().use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        return@withContext reader.readText()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reading file: ${e.message}", e)
                return@withContext null
            }
        }

        suspend fun listFilesInFolder(folderId: String): List<File> = withContext(Dispatchers.IO) {
            try {
                val query = if (folderId.isNotEmpty()) {
                    "'$folderId' in parents"
                } else {
                    "trashed = false"
                }

                val result = drive.files().list()
                    .setQ(query)
                    .setSpaces("drive")
                    .setFields("files(id, name, createdTime, size)")
                    .execute()

                return@withContext result.files ?: emptyList()
            } catch (e: Exception) {
                Log.e(TAG, "Error listing files: ${e.message}", e)
                return@withContext emptyList()
            }
        }

        suspend fun findFolder(folderName: String): String? = withContext(Dispatchers.IO) {
            try {
                val query = "mimeType='application/vnd.google-apps.folder' and name='$folderName' and trashed=false"
                val result = drive.files().list()
                    .setQ(query)
                    .setSpaces("drive")
                    .setFields("files(id)")
                    .execute()

                val folders = result.files
                return@withContext if (folders != null && folders.isNotEmpty()) {
                    folders[0].id
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error finding folder: ${e.message}", e)
                return@withContext null
            }
        }

        suspend fun createFolder(folderName: String): String? = withContext(Dispatchers.IO) {
            try {
                val fileMetadata = File().apply {
                    name = folderName
                    mimeType = "application/vnd.google-apps.folder"
                }

                val file = drive.files().create(fileMetadata)
                    .setFields("id")
                    .execute()

                return@withContext file.id
            } catch (e: Exception) {
                Log.e(TAG, "Error creating folder: ${e.message}", e)
                return@withContext null
            }
        }

        suspend fun deleteFile(fileId: String): Boolean = withContext(Dispatchers.IO) {
            return@withContext try {
                drive.files().delete(fileId).execute()
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting file: ${e.message}", e)
                false
            }
        }
    }
}