package com.richard.musicplayer.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayInputStream
import java.io.File
import java.security.MessageDigest
import kotlin.math.sqrt

// DataStore para cache persistente
private val Context.gradientDataStore: DataStore<Preferences> by preferencesDataStore(name = "gradient_cache")

@Serializable
data class CachedGradient(
    val colors: List<Long>, // Usando Long para serialização
    val timestamp: Long = System.currentTimeMillis()
)

class GradientCache(private val context: Context) {
    
    // OTIMIZAÇÃO: Cache em memória com LRU mais eficiente
    private val memoryCache = LinkedHashMap<String, List<Color>>(50, 0.75f, true)
    private val maxMemoryCacheSize = 30 // Reduzido de 50 para 30
    
    // OTIMIZAÇÃO: Configurações mais agressivas para performance
    private val cacheExpiryDays = 15 // Reduzido de 30 para 15 dias
    private val maxImageSize = 150 // Reduzido de 200 para 150px
    private val colorSampleRate = 8 // Aumentado de 4 para 8 (menos samples)
    
    // Estatísticas otimizadas
    private var cacheHits = 0
    private var cacheMisses = 0
    private var totalProcessingTime = 0L
    
    suspend fun getGradientColors(imageData: ByteArray): List<Color> {
        val startTime = System.currentTimeMillis()
        
        try {
            val cacheKey = generateMD5Hash(imageData)
            
            // 1. OTIMIZAÇÃO: Verificação de cache em memória mais rápida
            memoryCache[cacheKey]?.let { colors ->
                cacheHits++
                Log.d("GradientCache", "✅ Memory cache HIT (${cacheHits}/${cacheHits + cacheMisses}) - ${System.currentTimeMillis() - startTime}ms")
                return colors
            }
            
            // 2. OTIMIZAÇÃO: Cache persistente com timeout mais rápido
            withContext(Dispatchers.IO) {
                try {
                    val preferences = context.gradientDataStore.data.first()
                    val cachedJson = preferences[stringPreferencesKey(cacheKey)]
                    
                    cachedJson?.let { json ->
                        val cached = Json.decodeFromString<CachedGradient>(json)
                        val ageInDays = (System.currentTimeMillis() - cached.timestamp) / (1000 * 60 * 60 * 24)
                        
                        if (ageInDays < cacheExpiryDays) {
                            val colors = cached.colors.map { Color(it.toULong()) }
                            
                            // Adicionar ao cache em memória
                            synchronized(memoryCache) {
                                if (memoryCache.size >= maxMemoryCacheSize) {
                                    val firstKey = memoryCache.keys.first()
                                    memoryCache.remove(firstKey)
                                }
                                memoryCache[cacheKey] = colors
                            }
                            
                            cacheHits++
                            Log.d("GradientCache", "✅ Persistent cache HIT (${cacheHits}/${cacheHits + cacheMisses}) - ${System.currentTimeMillis() - startTime}ms")
                            return@withContext colors
                        }
                    }
                } catch (e: Exception) {
                    Log.w("GradientCache", "Cache read error: ${e.message}")
                }
                null
            }?.let { return it }
            
            // 3. OTIMIZAÇÃO: Processamento de imagem mais eficiente
            val colors = withContext(Dispatchers.Default) {
                processImageForGradient(imageData)
            }
            
            // 4. OTIMIZAÇÃO: Salvamento assíncrono para não bloquear
            withContext(Dispatchers.IO) {
                try {
                    // Salvar no cache persistente
                    val cachedGradient = CachedGradient(colors.map { it.value.toLong() })
                    val json = Json.encodeToString(cachedGradient)
                    
                    context.gradientDataStore.edit { preferences ->
                        preferences[stringPreferencesKey(cacheKey)] = json
                    }
                } catch (e: Exception) {
                    Log.w("GradientCache", "Cache save error: ${e.message}")
                }
            }
            
            // Adicionar ao cache em memória
            synchronized(memoryCache) {
                if (memoryCache.size >= maxMemoryCacheSize) {
                    val firstKey = memoryCache.keys.first()
                    memoryCache.remove(firstKey)
                }
                memoryCache[cacheKey] = colors
            }
            
            cacheMisses++
            totalProcessingTime += (System.currentTimeMillis() - startTime)
            
            Log.d("GradientCache", "⚡ Gradient processed (${cacheHits}/${cacheHits + cacheMisses}) - ${System.currentTimeMillis() - startTime}ms")
            
            return colors
            
        } catch (e: Exception) {
            Log.e("GradientCache", "Error processing gradient: ${e.message}", e)
            // Retornar cores padrão em caso de erro
            return listOf(
                Color(0xFF6200EE),
                Color(0xFF3700B3),
                Color(0xFF03DAC6)
            )
        }
    }
    
    // OTIMIZAÇÃO: Processamento de imagem mais eficiente
    private suspend fun processImageForGradient(imageData: ByteArray): List<Color> = withContext(Dispatchers.Default) {
        val bitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
            ?: throw IllegalArgumentException("Unable to decode image")
        
        // OTIMIZAÇÃO: Redimensionamento mais agressivo
        val scaledBitmap = if (bitmap.width > maxImageSize || bitmap.height > maxImageSize) {
            val scale = maxImageSize.toFloat() / maxOf(bitmap.width, bitmap.height)
            val newWidth = (bitmap.width * scale).toInt()
            val newHeight = (bitmap.height * scale).toInt()
            
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false).also {
                if (it != bitmap) bitmap.recycle()
            }
        } else {
            bitmap
        }
        
        try {
            // OTIMIZAÇÃO: Amostragem mais esparsa para melhor performance
            val colorCounts = mutableMapOf<Int, Int>()
            val width = scaledBitmap.width
            val height = scaledBitmap.height
            
            // Amostragem com step maior para reduzir processamento
            for (y in 0 until height step colorSampleRate) {
                for (x in 0 until width step colorSampleRate) {
                    val pixel = scaledBitmap.getPixel(x, y)
                    val color = quantizeColor(pixel) // Quantização mais agressiva
                    colorCounts[color] = colorCounts.getOrDefault(color, 0) + 1
                }
            }
            
            // OTIMIZAÇÃO: Seleção de cores mais eficiente
            val sortedColors = colorCounts.entries
                .sortedByDescending { it.value }
                .take(8) // Reduzido para menos cores
                                 .map { Color(it.key) }
            
            // Criar gradiente com cores mais distintas
            createOptimalGradient(sortedColors)
            
        } finally {
            scaledBitmap.recycle()
        }
    }
    
    // OTIMIZAÇÃO: Quantização de cores mais agressiva
    private fun quantizeColor(color: Int): Int {
        val r = (android.graphics.Color.red(color) / 32) * 32 // Mais agressivo
        val g = (android.graphics.Color.green(color) / 32) * 32
        val b = (android.graphics.Color.blue(color) / 32) * 32
        return android.graphics.Color.rgb(r, g, b)
    }
    
    // OTIMIZAÇÃO: Criação de gradiente mais eficiente
    private fun createOptimalGradient(colors: List<Color>): List<Color> {
        if (colors.isEmpty()) {
            return listOf(Color(0xFF6200EE), Color(0xFF3700B3))
        }
        
        // Filtrar cores muito similares para gradiente mais limpo
        val distinctColors = mutableListOf<Color>()
        colors.forEach { color ->
            if (distinctColors.isEmpty() || distinctColors.none { isColorSimilar(it, color) }) {
                distinctColors.add(color)
            }
        }
        
        return when {
            distinctColors.size >= 3 -> distinctColors.take(3)
            distinctColors.size == 2 -> distinctColors
            else -> listOf(distinctColors.first(), adjustBrightness(distinctColors.first(), 0.7f))
        }
    }
    
    // OTIMIZAÇÃO: Comparação de cores simplificada
    private fun isColorSimilar(color1: Color, color2: Color): Boolean {
        val threshold = 0.15f // Mais permissivo
        val dr = kotlin.math.abs(color1.red - color2.red)
        val dg = kotlin.math.abs(color1.green - color2.green)
        val db = kotlin.math.abs(color1.blue - color2.blue)
        
        return (dr + dg + db) / 3f < threshold
    }
    
    private fun adjustBrightness(color: Color, factor: Float): Color {
        return Color(
            red = (color.red * factor).coerceIn(0f, 1f),
            green = (color.green * factor).coerceIn(0f, 1f),
            blue = (color.blue * factor).coerceIn(0f, 1f),
            alpha = color.alpha
        )
    }
    
    // OTIMIZAÇÃO: Hash MD5 mais eficiente
    private fun generateMD5Hash(data: ByteArray): String {
        return try {
            val md = MessageDigest.getInstance("MD5")
            val digest = md.digest(data)
            digest.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "fallback_${data.size}_${System.currentTimeMillis()}"
        }
    }
    
    // OTIMIZAÇÃO: Limpeza de cache mais eficiente
    suspend fun cleanupExpiredCache() = withContext(Dispatchers.IO) {
        try {
            val currentTime = System.currentTimeMillis()
            val preferences = context.gradientDataStore.data.first()
            val keysToRemove = mutableListOf<String>()
            
            preferences.asMap().forEach { (key, value) ->
                if (key.name.startsWith("gradient_")) {
                    try {
                        val cached = Json.decodeFromString<CachedGradient>(value as String)
                        val ageInDays = (currentTime - cached.timestamp) / (1000 * 60 * 60 * 24)
                        
                        if (ageInDays > cacheExpiryDays) {
                            keysToRemove.add(key.name)
                        }
                    } catch (e: Exception) {
                        keysToRemove.add(key.name)
                    }
                }
            }
            
            if (keysToRemove.isNotEmpty()) {
                context.gradientDataStore.edit { prefs ->
                    keysToRemove.forEach { key ->
                        prefs.remove(stringPreferencesKey(key))
                    }
                }
                Log.d("GradientCache", "🧹 Cleaned ${keysToRemove.size} expired cache entries")
            }
        } catch (e: Exception) {
            Log.w("GradientCache", "Cache cleanup error: ${e.message}")
        }
    }
    
    // Estatísticas otimizadas
    fun getStats(): String {
        val total = cacheHits + cacheMisses
        val hitRate = if (total > 0) (cacheHits * 100) / total else 0
        val avgProcessingTime = if (cacheMisses > 0) totalProcessingTime / cacheMisses else 0
        
        return "Cache: ${hitRate}% hit rate (${cacheHits}/${total}), Memory: ${memoryCache.size}/${maxMemoryCacheSize}, Avg: ${avgProcessingTime}ms"
    }
} 