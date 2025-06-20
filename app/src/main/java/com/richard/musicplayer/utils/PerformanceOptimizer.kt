/*
 * Copyright (C) 2025 OuterTune Project
 * 
 * Sistema de Otimização Ultra para 60/90/120 FPS
 */

package com.richard.musicplayer.utils

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

object PerformanceOptimizer {
    
    /**
     * Otimiza a atividade para máxima performance
     */
    fun optimizeActivity(activity: Activity) {
        // Habilitar aceleração por hardware
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        
        // Otimizar para alta taxa de atualização
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            optimizeHighRefreshRate(activity)
        }
        
        // Configurar modo edge-to-edge moderno
        WindowCompat.setDecorFitsSystemWindows(activity.window, false)
        
        // Usar API moderna para controlar UI do sistema (substitui systemUiVisibility deprecado)
        val windowInsetsController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        windowInsetsController.apply {
            // Configurar comportamento para barra de status e navegação
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            
            // Configurar aparência das barras
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
        
        // Configurar transparência das barras
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.statusBarColor = Color.TRANSPARENT
            activity.window.navigationBarColor = Color.TRANSPARENT
        } else {
            activity.window.statusBarColor = Color.TRANSPARENT
            activity.window.navigationBarColor = Color.TRANSPARENT
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.R)
    private fun optimizeHighRefreshRate(activity: Activity) {
        val display = activity.display
        val refreshRates = display?.supportedModes?.map { it.refreshRate } ?: emptyList()
        
        // Selecionar a maior taxa de atualização disponível
        val maxRefreshRate = refreshRates.maxOrNull() ?: 60f
        
        // Configurar taxa de atualização preferencial
        val params = activity.window.attributes
        params.preferredRefreshRate = maxRefreshRate
        params.preferredDisplayModeId = display?.supportedModes
            ?.find { it.refreshRate == maxRefreshRate }?.modeId ?: 0
        activity.window.attributes = params
    }
    
    /**
     * Detecta e retorna a taxa de atualização do dispositivo
     */
    fun getDeviceRefreshRate(context: Context): Float {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            windowManager.currentWindowMetrics.bounds.let {
                windowManager.defaultDisplay.refreshRate
            }
        } else {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.refreshRate
        }
    }
    
    /**
     * Configurações de animação baseadas na taxa de atualização
     */
    fun getOptimizedAnimationDuration(context: Context, baseMs: Int): Int {
        val refreshRate = getDeviceRefreshRate(context)
        return when {
            refreshRate >= 120f -> (baseMs * 0.7f).toInt() // 30% mais rápido em 120Hz
            refreshRate >= 90f -> (baseMs * 0.85f).toInt()  // 15% mais rápido em 90Hz
            else -> baseMs // Padrão em 60Hz
        }
    }
    
    /**
     * Configurar performance do Compose
     */
    fun optimizeCompose(activity: Activity) {
        // Habilitar GPU acelerado para Compose
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
        
        // Configurar para renderização suave
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.window.isNavigationBarContrastEnforced = false
        }
    }
    
    /**
     * Esconder barras do sistema de forma moderna
     */
    fun hideSystemBars(activity: Activity) {
        val windowInsetsController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }
    
    /**
     * Mostrar barras do sistema de forma moderna
     */
    fun showSystemBars(activity: Activity) {
        val windowInsetsController = WindowCompat.getInsetsController(activity.window, activity.window.decorView)
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
    }
}

/**
 * Hook Compose para aplicar otimizações automaticamente
 */
@Composable
fun ApplyPerformanceOptimizations() {
    val context = LocalContext.current
    
    LaunchedEffect(context) {
        if (context is Activity) {
            PerformanceOptimizer.optimizeActivity(context)
            PerformanceOptimizer.optimizeCompose(context)
        }
    }
}

/**
 * Hook para obter duração de animação otimizada
 */
@Composable
fun rememberOptimizedDuration(baseMs: Int): Int {
    val context = LocalContext.current
    return remember(context) {
        PerformanceOptimizer.getOptimizedAnimationDuration(context, baseMs)
    }
} 