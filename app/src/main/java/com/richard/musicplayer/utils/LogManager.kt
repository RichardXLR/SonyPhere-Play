package com.richard.musicplayer.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

object LogManager {
    private const val TAG = "LogManager"
    private const val MAX_LOGS = 1000
    
    data class LogEntry(
        val timestamp: Long,
        val level: LogLevel,
        val tag: String,
        val message: String,
        val throwable: Throwable? = null
    ) {
        fun format(): String {
            val dateFormat = SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.getDefault())
            val timeStr = dateFormat.format(Date(timestamp))
            val throwableStr = throwable?.let { "\n${Log.getStackTraceString(it)}" } ?: ""
            return "$timeStr ${level.name.first()} $tag: $message$throwableStr"
        }
    }
    
    enum class LogLevel(val priority: Int) {
        VERBOSE(Log.VERBOSE),
        DEBUG(Log.DEBUG),
        INFO(Log.INFO),
        WARN(Log.WARN),
        ERROR(Log.ERROR)
    }
    
    private val _logs = MutableStateFlow<List<LogEntry>>(emptyList())
    val logs: StateFlow<List<LogEntry>> = _logs.asStateFlow()
    
    private val logsList = mutableListOf<LogEntry>()
    
    fun v(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.VERBOSE, tag, message, throwable)
    }
    
    fun d(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.DEBUG, tag, message, throwable)
    }
    
    fun i(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.INFO, tag, message, throwable)
    }
    
    fun w(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.WARN, tag, message, throwable)
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        log(LogLevel.ERROR, tag, message, throwable)
    }
    
    private fun log(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
        // Log to Android system
        when (level) {
            LogLevel.VERBOSE -> Log.v(tag, message, throwable)
            LogLevel.DEBUG -> Log.d(tag, message, throwable)
            LogLevel.INFO -> Log.i(tag, message, throwable)
            LogLevel.WARN -> Log.w(tag, message, throwable)
            LogLevel.ERROR -> Log.e(tag, message, throwable)
        }
        
        // Add to internal logs
        synchronized(logsList) {
            val entry = LogEntry(
                timestamp = System.currentTimeMillis(),
                level = level,
                tag = tag,
                message = message,
                throwable = throwable
            )
            
            logsList.add(entry)
            
            // Keep only the last MAX_LOGS entries
            if (logsList.size > MAX_LOGS) {
                logsList.removeAt(0)
            }
            
            _logs.value = logsList.toList()
        }
    }
    
    fun getAllLogs(): String {
        synchronized(logsList) {
            return logsList.joinToString("\n") { it.format() }
        }
    }
    
    fun getLogsForLevel(level: LogLevel): String {
        synchronized(logsList) {
            return logsList
                .filter { it.level.priority >= level.priority }
                .joinToString("\n") { it.format() }
        }
    }
    
    fun copyToClipboard(context: Context, content: String = getAllLogs()) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("SonsPhere Logs", content)
            clipboard.setPrimaryClip(clip)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy logs to clipboard", e)
        }
    }
    
    fun saveToFile(context: Context, content: String = getAllLogs()): String? {
        return try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val fileName = "SonsPhere_logs_$timestamp.txt"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            file.writeText(content)
            file.absolutePath
        } catch (e: IOException) {
            Log.e(TAG, "Failed to save logs to file", e)
            null
        }
    }
    
    fun clearLogs() {
        synchronized(logsList) {
            logsList.clear()
            _logs.value = emptyList()
        }
    }
    
    fun getSystemInfo(): String {
        return buildString {
            appendLine("=== SYSTEM INFO ===")
            appendLine("App Version: ${android.os.Build.VERSION.RELEASE}")
            appendLine("SDK: ${android.os.Build.VERSION.SDK_INT}")
            appendLine("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
            appendLine("Board: ${android.os.Build.BOARD}")
            appendLine("Brand: ${android.os.Build.BRAND}")
            appendLine("Hardware: ${android.os.Build.HARDWARE}")
            appendLine("Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
            appendLine("=== LOGS ===")
        }
    }
} 