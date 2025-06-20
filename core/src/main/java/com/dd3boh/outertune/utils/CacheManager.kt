package com.dd3boh.outertune.utils

import android.content.Context
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Advanced cache manager for improved performance
 */
@Singleton
class CacheManager @Inject constructor(
    private val context: Context
) {
    private val memoryCache = LruCache<String, Any>(
        (Runtime.getRuntime().maxMemory() / 1024 / 8).toInt() // Use 1/8 of available memory
    )
    
    private val diskCacheDir = File(context.cacheDir, "app_cache")
    
    init {
        if (!diskCacheDir.exists()) {
            diskCacheDir.mkdirs()
        }
    }
    
    /**
     * Store data in memory cache
     */
    fun putInMemory(key: String, value: Any) {
        memoryCache.put(key, value)
    }
    
    /**
     * Get data from memory cache
     */
    fun <T> getFromMemory(key: String): T? {
        @Suppress("UNCHECKED_CAST")
        return memoryCache.get(key) as? T
    }
    
    /**
     * Store data in disk cache
     */
    suspend fun putInDisk(key: String, data: ByteArray) = withContext(Dispatchers.IO) {
        try {
            val file = File(diskCacheDir, key.hashCode().toString())
            file.writeBytes(data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Get data from disk cache
     */
    suspend fun getFromDisk(key: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val file = File(diskCacheDir, key.hashCode().toString())
            if (file.exists()) {
                return@withContext file.readBytes()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }
    
    /**
     * Clear old cache files
     */
    suspend fun clearOldCache(maxAge: Long = TimeUnit.DAYS.toMillis(7)) = withContext(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()
        diskCacheDir.listFiles()?.forEach { file ->
            if (currentTime - file.lastModified() > maxAge) {
                file.delete()
            }
        }
    }
    
    /**
     * Clear all cache
     */
    fun clearAll() {
        memoryCache.evictAll()
        diskCacheDir.deleteRecursively()
        diskCacheDir.mkdirs()
    }
    
    /**
     * Get cache size in bytes
     */
    fun getCacheSize(): Long {
        return diskCacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    }
} 