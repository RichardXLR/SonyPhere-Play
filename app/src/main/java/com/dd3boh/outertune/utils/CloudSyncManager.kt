package com.dd3boh.outertune.utils

import android.content.Context
import com.dd3boh.outertune.db.entities.Song
import com.dd3boh.outertune.db.entities.Playlist
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.security.MessageDigest
import java.util.Date
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cloud sync manager for backup and synchronization
 */
@Singleton
class CloudSyncManager @Inject constructor(
    private val context: Context
) {
    
    @Serializable
    data class BackupData(
        val version: Int = 1,
        val timestamp: Long,
        val deviceId: String,
        val playlists: List<PlaylistBackup>,
        val favorites: List<String>, // Song IDs
        val settings: Map<String, String>,
        val listeningHistory: List<ListeningEvent>,
        val checksum: String
    )
    
    @Serializable
    data class PlaylistBackup(
        val id: String,
        val name: String,
        val description: String?,
        val songIds: List<String>,
        val createdAt: Long,
        val modifiedAt: Long
    )
    
    @Serializable
    data class ListeningEvent(
        val songId: String,
        val timestamp: Long,
        val duration: Long,
        val completed: Boolean
    )
    
    @Serializable
    data class SyncStatus(
        val lastSyncTime: Long,
        val syncInProgress: Boolean,
        val syncProgress: Float,
        val error: String? = null
    )
    
    private val _syncStatus = MutableStateFlow(
        SyncStatus(
            lastSyncTime = 0,
            syncInProgress = false,
            syncProgress = 0f
        )
    )
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    /**
     * Create a backup of user data
     */
    suspend fun createBackup(
        playlists: List<Playlist>,
        favorites: List<Song>,
        settings: Map<String, String>,
        listeningHistory: List<ListeningEvent>
    ): ByteArray = withContext(Dispatchers.IO) {
        updateSyncStatus(syncInProgress = true, syncProgress = 0.1f)
        
        try {
            val deviceId = getDeviceId()
            
            val playlistBackups = playlists.map { playlist ->
                PlaylistBackup(
                    id = playlist.id,
                    name = playlist.playlist.name,
                    description = null, // Add description field to Playlist entity if needed
                    songIds = emptyList(), // Would need to fetch songs for each playlist
                    createdAt = System.currentTimeMillis(),
                    modifiedAt = System.currentTimeMillis()
                )
            }
            
            updateSyncStatus(syncProgress = 0.3f)
            
            val backupData = BackupData(
                timestamp = System.currentTimeMillis(),
                deviceId = deviceId,
                playlists = playlistBackups,
                favorites = favorites.map { it.id },
                settings = settings,
                listeningHistory = listeningHistory,
                checksum = ""
            )
            
            updateSyncStatus(syncProgress = 0.5f)
            
            // Calculate checksum
            val dataWithoutChecksum = json.encodeToString(backupData)
            val checksum = calculateChecksum(dataWithoutChecksum)
            val finalBackupData = backupData.copy(checksum = checksum)
            
            updateSyncStatus(syncProgress = 0.7f)
            
            // Compress the backup
            val jsonData = json.encodeToString(finalBackupData)
            val compressedData = compress(jsonData.toByteArray())
            
            updateSyncStatus(syncProgress = 1.0f, syncInProgress = false, lastSyncTime = System.currentTimeMillis())
            
            return@withContext compressedData
        } catch (e: Exception) {
            updateSyncStatus(
                syncInProgress = false,
                error = "Backup failed: ${e.message}"
            )
            throw e
        }
    }
    
    /**
     * Restore from backup
     */
    suspend fun restoreBackup(backupData: ByteArray): BackupData = withContext(Dispatchers.IO) {
        updateSyncStatus(syncInProgress = true, syncProgress = 0.1f)
        
        try {
            // Decompress the backup
            val decompressedData = decompress(backupData)
            val jsonData = String(decompressedData)
            
            updateSyncStatus(syncProgress = 0.3f)
            
            // Parse the backup
            val backup = json.decodeFromString<BackupData>(jsonData)
            
            updateSyncStatus(syncProgress = 0.5f)
            
            // Verify checksum
            val dataWithoutChecksum = json.encodeToString(backup.copy(checksum = ""))
            val calculatedChecksum = calculateChecksum(dataWithoutChecksum)
            
            if (calculatedChecksum != backup.checksum) {
                throw SecurityException("Backup checksum verification failed")
            }
            
            updateSyncStatus(syncProgress = 1.0f, syncInProgress = false, lastSyncTime = System.currentTimeMillis())
            
            return@withContext backup
        } catch (e: Exception) {
            updateSyncStatus(
                syncInProgress = false,
                error = "Restore failed: ${e.message}"
            )
            throw e
        }
    }
    
    /**
     * Export backup to file
     */
    suspend fun exportBackupToFile(
        backupData: ByteArray,
        fileName: String = "streamtune_backup_${System.currentTimeMillis()}.stb"
    ): File = withContext(Dispatchers.IO) {
        val backupDir = File(context.getExternalFilesDir(null), "backups")
        if (!backupDir.exists()) {
            backupDir.mkdirs()
        }
        
        val backupFile = File(backupDir, fileName)
        backupFile.writeBytes(backupData)
        
        return@withContext backupFile
    }
    
    /**
     * Import backup from file
     */
    suspend fun importBackupFromFile(file: File): BackupData = withContext(Dispatchers.IO) {
        val backupData = file.readBytes()
        return@withContext restoreBackup(backupData)
    }
    
    /**
     * Auto backup
     */
    suspend fun performAutoBackup(
        playlists: List<Playlist>,
        favorites: List<Song>,
        settings: Map<String, String>,
        listeningHistory: List<ListeningEvent>
    ) {
        try {
            val backupData = createBackup(playlists, favorites, settings, listeningHistory)
            
            // Save to local auto-backup
            val autoBackupFile = File(context.filesDir, "auto_backup.stb")
            autoBackupFile.writeBytes(backupData)
            
            // Keep last 5 auto-backups
            cleanupOldBackups()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Get backup metadata
     */
    suspend fun getBackupMetadata(file: File): BackupMetadata? = withContext(Dispatchers.IO) {
        try {
            val backupData = file.readBytes()
            val decompressedData = decompress(backupData)
            val jsonData = String(decompressedData)
            val backup = json.decodeFromString<BackupData>(jsonData)
            
            return@withContext BackupMetadata(
                fileName = file.name,
                fileSize = file.length(),
                timestamp = backup.timestamp,
                deviceId = backup.deviceId,
                playlistCount = backup.playlists.size,
                favoriteCount = backup.favorites.size,
                version = backup.version
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun compress(data: ByteArray): ByteArray {
        return data.inputStream().use { input ->
            val output = java.io.ByteArrayOutputStream()
            GZIPOutputStream(output).use { gzip ->
                input.copyTo(gzip)
            }
            output.toByteArray()
        }
    }
    
    private fun decompress(data: ByteArray): ByteArray {
        return GZIPInputStream(data.inputStream()).use { gzip ->
            gzip.readBytes()
        }
    }
    
    private fun calculateChecksum(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
    
    private fun getDeviceId(): String {
        // In a real implementation, this would use a proper device ID
        return android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown"
    }
    
    private suspend fun cleanupOldBackups() = withContext(Dispatchers.IO) {
        val backupDir = File(context.filesDir, "backups")
        if (backupDir.exists()) {
            val backupFiles = backupDir.listFiles { file ->
                file.name.startsWith("auto_backup_") && file.name.endsWith(".stb")
            }?.sortedByDescending { it.lastModified() } ?: emptyList()
            
            // Keep only the 5 most recent backups
            backupFiles.drop(5).forEach { it.delete() }
        }
    }
    
    private fun updateSyncStatus(
        lastSyncTime: Long? = null,
        syncInProgress: Boolean? = null,
        syncProgress: Float? = null,
        error: String? = null
    ) {
        _syncStatus.value = _syncStatus.value.copy(
            lastSyncTime = lastSyncTime ?: _syncStatus.value.lastSyncTime,
            syncInProgress = syncInProgress ?: _syncStatus.value.syncInProgress,
            syncProgress = syncProgress ?: _syncStatus.value.syncProgress,
            error = error
        )
    }
    
    data class BackupMetadata(
        val fileName: String,
        val fileSize: Long,
        val timestamp: Long,
        val deviceId: String,
        val playlistCount: Int,
        val favoriteCount: Int,
        val version: Int
    )
} 