/*
 * Copyright (C) 2024 z-huang/InnerTune
 * Copyright (C) 2025 O﻿ute﻿rTu﻿ne Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.richard.musicplayer.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

@Composable
fun AudioVisualizer(
    modifier: Modifier = Modifier,
    frequencyBands: FloatArray = FloatArray(32) { 0f },
    amplitude: Float = 0f,
    isPlaying: Boolean = false
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val density = LocalDensity.current
    
    // Animação das barras
    val animatedBands = remember { mutableStateListOf<Float>().apply { 
        repeat(32) { add(0f) } 
    }}
    
    // Animação suave das barras
    LaunchedEffect(frequencyBands, isPlaying) {
        if (isPlaying) {
            while (true) {
                for (i in frequencyBands.indices) {
                    // Valores mais altos e variados para ondas mais visíveis
                    val baseValue = if (frequencyBands[i] > 0) frequencyBands[i] else Random.nextFloat() * 0.8f
                    val enhancedValue = baseValue + Random.nextFloat() * 0.4f // Amplifica as ondas
                    animatedBands[i] = animatedBands[i] * 0.6f + enhancedValue.coerceAtMost(1f) * 0.4f
                }
                delay(40) // 25 FPS para mais fluidez
            }
        } else {
            // Fade out quando não está tocando
            for (i in animatedBands.indices) {
                animatedBands[i] *= 0.85f
            }
        }
    }
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        drawNativeVisualizer(
            bands = animatedBands.toFloatArray(),
            amplitude = amplitude,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            isPlaying = isPlaying
        )
    }
}

private fun DrawScope.drawNativeVisualizer(
    bands: FloatArray,
    amplitude: Float,
    primaryColor: Color,
    secondaryColor: Color,
    isPlaying: Boolean
) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val maxRadius = minOf(size.width, size.height) * 0.35f
    val barCount = 20
    
    // Fundo com gradiente circular sutil
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                primaryColor.copy(alpha = 0.1f),
                Color.Transparent
            ),
            center = Offset(centerX, centerY),
            radius = maxRadius * 1.2f
        ),
        radius = maxRadius * 1.2f,
        center = Offset(centerX, centerY)
    )
    
    // Círculo central
    drawCircle(
        color = primaryColor.copy(alpha = if (isPlaying) 0.3f else 0.1f),
        radius = maxRadius * 0.2f,
        center = Offset(centerX, centerY)
    )
    
    // Ondas circulares
    for (ring in 1..3) {
        val ringRadius = maxRadius * (0.4f + ring * 0.2f)
        val ringAlpha = if (isPlaying) (0.4f - ring * 0.1f) else 0.1f
        
        drawCircle(
            color = primaryColor.copy(alpha = ringAlpha * amplitude),
            radius = ringRadius,
            center = Offset(centerX, centerY),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )
    }
    
    // Barras radiais
    for (i in 0 until barCount) {
        val angle = (i * 360f / barCount) * (PI / 180f).toFloat()
        val bandIndex = (i * bands.size / barCount).coerceAtMost(bands.size - 1)
        val frequency = bands[bandIndex].coerceIn(0f, 1f)
        
        val baseRadius = maxRadius * 0.3f
        val barLength = frequency * maxRadius * 0.4f
        val totalLength = baseRadius + barLength
        
        val startX = centerX + cos(angle) * baseRadius
        val startY = centerY + sin(angle) * baseRadius
        val endX = centerX + cos(angle) * totalLength
        val endY = centerY + sin(angle) * totalLength
        
        // Gradiente da barra radial
        val barGradient = Brush.linearGradient(
            colors = listOf(
                primaryColor.copy(alpha = if (isPlaying) 0.4f else 0.2f),
                primaryColor.copy(alpha = if (isPlaying) 0.8f else 0.4f),
                secondaryColor.copy(alpha = if (isPlaying) 0.9f else 0.5f)
            ),
            start = Offset(startX, startY),
            end = Offset(endX, endY)
        )
        
        // Desenhar linha com gradiente
        drawLine(
            brush = barGradient,
            start = Offset(startX, startY),
            end = Offset(endX, endY),
            strokeWidth = 3.dp.toPx(),
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        
        // Ponto brilhante na ponta
        if (frequency > 0.4f && isPlaying) {
            drawCircle(
                color = Color.White.copy(alpha = frequency * 0.6f),
                radius = 2.dp.toPx(),
                center = Offset(endX, endY)
            )
        }
    }
    
    // Pulso central
    if (isPlaying && amplitude > 0.3f) {
        val pulseRadius = maxRadius * 0.15f * (1f + amplitude * 0.5f)
        drawCircle(
            color = Color.White.copy(alpha = amplitude * 0.3f),
            radius = pulseRadius,
            center = Offset(centerX, centerY)
        )
    }
} 