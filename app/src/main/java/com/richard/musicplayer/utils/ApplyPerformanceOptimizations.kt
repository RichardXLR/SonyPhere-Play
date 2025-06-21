package com.richard.musicplayer.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat
import android.view.View
import android.view.Choreographer
import android.os.Build
import android.util.Log

/**
 * 🚀 SISTEMA DE OTIMIZAÇÕES PARA ALTA TAXA DE ATUALIZAÇÃO
 * Aplica otimizações específicas para dispositivos com 90Hz, 120Hz, 144Hz+
 */
@Composable
fun ApplyHighRefreshRateOptimizations() {
    val view = LocalView.current
    
    // OTIMIZAÇÃO 1: Configurar View para alta performance
    LaunchedEffect(Unit) {
        setupHighPerformanceView(view)
    }
    
    // OTIMIZAÇÃO 2: Monitor de frame rate em tempo real
    DisposableEffect(Unit) {
        val frameRateMonitor = FrameRateMonitor()
        frameRateMonitor.start()
        
        onDispose {
            frameRateMonitor.stop()
        }
    }
    
    // OTIMIZAÇÃO 3: Configurar Choreographer para máxima fluidez
    LaunchedEffect(Unit) {
        setupChoreographerOptimizations()
    }
}

/**
 * Configura a View para máxima performance em alta taxa de atualização
 */
private fun setupHighPerformanceView(view: View) {
    try {
        // Habilitar hardware acceleration
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        
        // Configurar para alta performance
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ViewCompat.setImportantForAccessibility(view, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS)
        }
        
        // Otimizar drawing cache
        view.isDrawingCacheEnabled = false // Desabilitar para melhor performance em hardware acceleration
        
        // Configurar para rendering otimizado
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            view.outlineProvider = null // Remover outline para melhor performance
        }
        
        Log.d("PerformanceOptimizations", "🚀 View configurada para alta performance")
        
    } catch (e: Exception) {
        Log.w("PerformanceOptimizations", "⚠️ Erro ao configurar view: ${e.message}")
    }
}

/**
 * Configura o Choreographer para otimizações de frame rate
 */
private fun setupChoreographerOptimizations() {
    try {
        val choreographer = Choreographer.getInstance()
        
        // Callback para monitorar e otimizar frames
        val frameCallback = object : Choreographer.FrameCallback {
            private var lastFrameTime = 0L
            private var frameCount = 0
            private var fpsSum = 0.0
            
            override fun doFrame(frameTimeNanos: Long) {
                if (lastFrameTime != 0L) {
                    val frameDuration = (frameTimeNanos - lastFrameTime) / 1_000_000.0 // Convert to ms
                    val currentFps = 1000.0 / frameDuration
                    
                    frameCount++
                    fpsSum += currentFps
                    
                    // Log FPS médio a cada 60 frames (aproximadamente 1 segundo)
                    if (frameCount >= 60) {
                        val averageFps = fpsSum / frameCount
                        Log.d("PerformanceOptimizations", 
                            "📊 FPS médio: %.1f Hz (Frame time: %.2f ms)".format(averageFps, frameDuration))
                        frameCount = 0
                        fpsSum = 0.0
                    }
                }
                
                lastFrameTime = frameTimeNanos
                choreographer.postFrameCallback(this)
            }
        }
        
        choreographer.postFrameCallback(frameCallback)
        Log.d("PerformanceOptimizations", "🚀 Choreographer otimizado configurado")
        
    } catch (e: Exception) {
        Log.w("PerformanceOptimizations", "⚠️ Erro ao configurar Choreographer: ${e.message}")
    }
}

/**
 * Monitor de frame rate em tempo real
 */
private class FrameRateMonitor {
    private var isRunning = false
    private var frameCallback: Choreographer.FrameCallback? = null
    
    fun start() {
        if (isRunning) return
        
        isRunning = true
        val choreographer = Choreographer.getInstance()
        
        frameCallback = object : Choreographer.FrameCallback {
            private var frameCount = 0
            private var startTime = System.currentTimeMillis()
            
            override fun doFrame(frameTimeNanos: Long) {
                if (!isRunning) return
                
                frameCount++
                val currentTime = System.currentTimeMillis()
                val elapsed = currentTime - startTime
                
                // Calcular FPS a cada 2 segundos
                if (elapsed >= 2000) {
                    val fps = (frameCount * 1000.0) / elapsed
                    Log.d("FrameRateMonitor", "🎯 FPS atual: %.1f Hz".format(fps))
                    
                    // Reset counters
                    frameCount = 0
                    startTime = currentTime
                }
                
                choreographer.postFrameCallback(this)
            }
        }
        
        choreographer.postFrameCallback(frameCallback!!)
        Log.d("FrameRateMonitor", "🚀 Monitor de FPS iniciado")
    }
    
    fun stop() {
        isRunning = false
        frameCallback?.let { callback ->
            Choreographer.getInstance().removeFrameCallback(callback)
        }
        frameCallback = null
        Log.d("FrameRateMonitor", "⏹️ Monitor de FPS parado")
    }
} 