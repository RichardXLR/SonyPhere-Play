package com.richard.musicplayer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.richard.musicplayer.utils.LogManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val logs by LogManager.logs.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    
    var selectedFilter by remember { mutableStateOf(LogManager.LogLevel.VERBOSE) }
    var showFilterMenu by remember { mutableStateOf(false) }
    
    val filteredLogs = remember(logs, selectedFilter) {
        logs.filter { it.level.priority >= selectedFilter.priority }
    }
    
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Logs do Sistema",
                        fontFamily = com.richard.musicplayer.ui.theme.SoraFontFamily,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showFilterMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filtros"
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false }
                        ) {
                            LogManager.LogLevel.entries.forEach { level ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            "${level.name} (${logs.count { it.level == level }})",
                                            color = getLogLevelColor(level)
                                        )
                                    },
                                    onClick = {
                                        selectedFilter = level
                                        showFilterMenu = false
                                    },
                                    leadingIcon = {
                                        RadioButton(
                                            selected = selectedFilter == level,
                                            onClick = null
                                        )
                                    }
                                )
                            }
                        }
                    }
                    
                    IconButton(
                        onClick = {
                            scope.launch {
                                val content = LogManager.getSystemInfo() + LogManager.getAllLogs()
                                LogManager.copyToClipboard(context, content)
                                snackbarHostState.showSnackbar("Logs copiados para área de transferência")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copiar Logs"
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            scope.launch {
                                val content = LogManager.getSystemInfo() + LogManager.getAllLogs()
                                val filePath = LogManager.saveToFile(context, content)
                                if (filePath != null) {
                                    snackbarHostState.showSnackbar("Logs salvos em: $filePath")
                                } else {
                                    snackbarHostState.showSnackbar("Erro ao salvar logs")
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = "Salvar Logs"
                        )
                    }
                    
                    IconButton(
                        onClick = {
                            scope.launch {
                                LogManager.clearLogs()
                                snackbarHostState.showSnackbar("Logs limpos")
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpar Logs"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header with stats
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LogManager.LogLevel.entries.forEach { level ->
                        val count = logs.count { it.level == level }
                        if (count > 0) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = count.toString(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = getLogLevelColor(level)
                                )
                                Text(
                                    text = level.name.first().toString(),
                                    fontSize = 12.sp,
                                    color = getLogLevelColor(level)
                                )
                            }
                        }
                    }
                }
            }
            
            // Filter info
            if (selectedFilter != LogManager.LogLevel.VERBOSE) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "Filtrando: ${selectedFilter.name} e acima (${filteredLogs.size} logs)",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            // Logs list
            if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum log encontrado",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredLogs) { logEntry ->
                        LogEntryItem(logEntry = logEntry)
                    }
                }
            }
        }
    }
}

@Composable
private fun LogEntryItem(logEntry: LogManager.LogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = getLogLevelBackgroundColor(logEntry.level)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = logEntry.tag,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = getLogLevelColor(logEntry.level)
                )
                Text(
                    text = java.text.SimpleDateFormat("HH:mm:ss.SSS", java.util.Locale.getDefault())
                        .format(java.util.Date(logEntry.timestamp)),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = logEntry.message,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            logEntry.throwable?.let { throwable ->
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = android.util.Log.getStackTraceString(throwable),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun getLogLevelColor(level: LogManager.LogLevel): Color {
    return when (level) {
        LogManager.LogLevel.VERBOSE -> Color(0xFF808080)
        LogManager.LogLevel.DEBUG -> Color(0xFF2196F3)
        LogManager.LogLevel.INFO -> Color(0xFF4CAF50)
        LogManager.LogLevel.WARN -> Color(0xFFFF9800)
        LogManager.LogLevel.ERROR -> Color(0xFFF44336)
    }
}

@Composable
private fun getLogLevelBackgroundColor(level: LogManager.LogLevel): Color {
    val alpha = 0.1f
    return when (level) {
        LogManager.LogLevel.VERBOSE -> Color(0xFF808080).copy(alpha = alpha)
        LogManager.LogLevel.DEBUG -> Color(0xFF2196F3).copy(alpha = alpha)
        LogManager.LogLevel.INFO -> Color(0xFF4CAF50).copy(alpha = alpha)
        LogManager.LogLevel.WARN -> Color(0xFFFF9800).copy(alpha = alpha)
        LogManager.LogLevel.ERROR -> Color(0xFFF44336).copy(alpha = alpha)
    }
} 