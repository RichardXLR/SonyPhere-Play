/*
 * Copyright (C) 2025 OuterTune Project
 * 
 * Sistema Avançado de Gradientes Perfeitos
 */

package com.richard.musicplayer.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import kotlin.math.*

object GradientSystem {
    
    /**
     * Gradiente cinematográfico perfeito baseado em cores extraídas
     */
    @Composable
    fun createCinematicGradient(
        colors: List<Color>,
        intensity: Float = 1f,
        angle: Float = 135f
    ): Brush {
        return remember(colors, intensity, angle) {
            when {
                colors.isEmpty() -> createDefaultCinematicGradient()
                colors.size == 1 -> createMonochromaticGradient(colors[0], intensity)
                colors.size == 2 -> createDualColorGradient(colors[0], colors[1], intensity, angle)
                else -> createMultiColorGradient(colors, intensity, angle)
            }
        }
    }
    
    /**
     * Gradiente padrão quando não há cores extraídas
     */
    private fun createDefaultCinematicGradient(): Brush {
        return Brush.linearGradient(
            colors = listOf(
                Color(0xFF0F0F0F), // Preto profundo
                Color(0xFF1A1A2E), // Azul muito escuro
                Color(0xFF16213E), // Azul marinho
                Color(0xFF0F3460), // Azul escuro
                Color(0xFF0A0A0A)  // Preto
            ),
            start = Offset(0f, 0f),
            end = Offset(1000f, 1000f)
        )
    }
    
    /**
     * Gradiente monocromático com múltiplas variações
     */
    private fun createMonochromaticGradient(baseColor: Color, intensity: Float): Brush {
        val variations = createColorVariations(baseColor, intensity)
        return Brush.radialGradient(
            colors = variations,
            radius = 1200f,
            center = Offset(0.3f, 0.2f)
        )
    }
    
    /**
     * Gradiente dual com transições perfeitas
     */
    private fun createDualColorGradient(
        color1: Color, 
        color2: Color, 
        intensity: Float,
        angle: Float
    ): Brush {
        val enhancedColors = enhanceDualColors(color1, color2, intensity)
        val angleRad = angle * PI / 180.0
        
        return Brush.linearGradient(
            colors = enhancedColors,
            start = Offset(0f, 0f),
            end = Offset(
                (cos(angleRad) * 1000f).toFloat(),
                (sin(angleRad) * 1000f).toFloat()
            )
        )
    }
    
    /**
     * Gradiente multi-color cinematográfico
     */
    private fun createMultiColorGradient(
        colors: List<Color>, 
        intensity: Float,
        angle: Float
    ): Brush {
        val enhancedColors = enhanceMultiColors(colors, intensity)
        val angleRad = angle * PI / 180.0
        
        return Brush.linearGradient(
            colors = enhancedColors,
            start = Offset(0f, 0f),
            end = Offset(
                (cos(angleRad) * 1000f).toFloat(),
                (sin(angleRad) * 1000f).toFloat()
            )
        )
    }
    
    /**
     * Cria variações sofisticadas de uma cor base
     */
    private fun createColorVariations(baseColor: Color, intensity: Float): List<Color> {
        return listOf(
            // Tom mais claro com transparência
            baseColor.copy(alpha = 0.1f * intensity),
            // Tom principal
            baseColor.copy(alpha = 0.8f * intensity),
            // Tom mais escuro
            darkenColor(baseColor, 0.3f).copy(alpha = 0.6f * intensity),
            // Tom muito escuro
            darkenColor(baseColor, 0.6f).copy(alpha = 0.4f * intensity),
            // Preto suave
            Color.Black.copy(alpha = 0.8f)
        )
    }
    
    /**
     * Melhora cores duais com tons intermediários
     */
    private fun enhanceDualColors(color1: Color, color2: Color, intensity: Float): List<Color> {
        val mixedColor = mixColors(color1, color2, 0.5f)
        
        return listOf(
            // Cor 1 clara
            color1.copy(alpha = 0.9f * intensity),
            // Tom intermediário claro
            mixColors(color1, mixedColor, 0.7f).copy(alpha = 0.8f * intensity),
            // Cor misturada
            mixedColor.copy(alpha = 0.7f * intensity),
            // Tom intermediário escuro
            mixColors(color2, mixedColor, 0.7f).copy(alpha = 0.6f * intensity),
            // Cor 2 escura
            darkenColor(color2, 0.3f).copy(alpha = 0.5f * intensity),
            // Tom final
            Color.Black.copy(alpha = 0.7f)
        )
    }
    
    /**
     * Melhora múltiplas cores com transições suaves
     */
    private fun enhanceMultiColors(colors: List<Color>, intensity: Float): List<Color> {
        val enhancedList = mutableListOf<Color>()
        
        // Primeira cor clara
        enhancedList.add(colors[0].copy(alpha = 0.9f * intensity))
        
        // Cores intermediárias com transições
        for (i in 0 until colors.size - 1) {
            val current = colors[i]
            val next = colors[i + 1]
            
            // Cor atual
            enhancedList.add(current.copy(alpha = (0.8f - i * 0.1f) * intensity))
            
            // Transição suave
            enhancedList.add(mixColors(current, next, 0.5f).copy(alpha = (0.7f - i * 0.1f) * intensity))
        }
        
        // Última cor escura
        enhancedList.add(darkenColor(colors.last(), 0.4f).copy(alpha = 0.5f * intensity))
        
        // Tom final preto suave
        enhancedList.add(Color.Black.copy(alpha = 0.8f))
        
        return enhancedList
    }
    
    /**
     * Escurece uma cor
     */
    private fun darkenColor(color: Color, factor: Float): Color {
        return Color(
            red = color.red * (1f - factor),
            green = color.green * (1f - factor),
            blue = color.blue * (1f - factor),
            alpha = color.alpha
        )
    }
    
    /**
     * Mistura duas cores
     */
    private fun mixColors(color1: Color, color2: Color, ratio: Float): Color {
        return Color(
            red = color1.red * (1f - ratio) + color2.red * ratio,
            green = color1.green * (1f - ratio) + color2.green * ratio,
            blue = color1.blue * (1f - ratio) + color2.blue * ratio,
            alpha = color1.alpha * (1f - ratio) + color2.alpha * ratio
        )
    }
    
    /**
     * Gradiente para botões com glassmorphism
     */
    @Composable
    fun createButtonGradient(
        primaryColor: Color,
        isActive: Boolean = false,
        intensity: Float = 1f
    ): Brush {
        return remember(primaryColor, isActive, intensity) {
            if (isActive) {
                Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.9f * intensity),
                        primaryColor.copy(alpha = 0.7f * intensity),
                        darkenColor(primaryColor, 0.2f).copy(alpha = 0.6f * intensity),
                        darkenColor(primaryColor, 0.4f).copy(alpha = 0.4f * intensity)
                    ),
                    radius = 100f
                )
            } else {
                Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f * intensity),
                        Color.White.copy(alpha = 0.08f * intensity),
                        Color.Transparent
                    ),
                    radius = 80f
                )
            }
        }
    }
    
    /**
     * Gradiente para progress bar dinâmico
     */
    @Composable
    fun createProgressGradient(colors: List<Color>): Brush {
        return remember(colors) {
            when {
                colors.isEmpty() -> {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF6200EE),
                            Color(0xFF9C27B0),
                            Color(0xFFE91E63),
                            Color(0xFFFF5722)
                        )
                    )
                }
                colors.size == 1 -> {
                    val base = colors[0]
                    Brush.horizontalGradient(
                        colors = listOf(
                            base.copy(alpha = 0.9f),
                            base.copy(alpha = 0.8f),
                            base.copy(alpha = 0.7f)
                        )
                    )
                }
                colors.size == 2 -> {
                    Brush.horizontalGradient(
                        colors = listOf(
                            colors[0],
                            mixColors(colors[0], colors[1], 0.5f),
                            colors[1]
                        )
                    )
                }
                else -> {
                    Brush.horizontalGradient(colors = colors.take(4))
                }
            }
        }
    }
    
    /**
     * Gradiente animado para splash screen
     */
    @Composable
    fun createAnimatedSplashGradient(
        animationProgress: Float,
        baseColors: List<Color> = listOf(
            Color(0xFF0A0A0A),
            Color(0xFF1A0A2E),
            Color(0xFF16213E),
            Color(0xFF0F3460),
            Color(0xFF0A0A0A)
        )
    ): Brush {
        return remember(animationProgress, baseColors) {
            val animatedColors = baseColors.map { color ->
                val intensity = 0.7f + 0.3f * sin(animationProgress * PI.toFloat() * 2f)
                color.copy(alpha = intensity)
            }
            
            val centerX = 0.5f + 0.3f * cos(animationProgress * PI.toFloat() * 2f)
            val centerY = 0.5f + 0.3f * sin(animationProgress * PI.toFloat() * 1.5f)
            
            Brush.radialGradient(
                colors = animatedColors,
                center = Offset(centerX, centerY),
                radius = 800f + 200f * sin(animationProgress * PI.toFloat())
            )
        }
    }
} 