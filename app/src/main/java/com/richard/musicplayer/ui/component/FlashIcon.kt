package com.richard.musicplayer.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.*

/**
 * 🗲 Ícone de Flash personalizado e moderno
 * Design minimalista inspirado em símbolos de velocidade e energia
 */
@Composable
fun FlashIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = MaterialTheme.colorScheme.onSurface,
    strokeWidth: Float = 2.5f,
    filled: Boolean = false
) {
    Canvas(
        modifier = modifier.size(size)
    ) {
        drawFlashBolt(
            color = color,
            strokeWidth = strokeWidth,
            filled = filled
        )
    }
}

/**
 * 🎨 Desenha um raio/flash moderno e elegante
 */
private fun DrawScope.drawFlashBolt(
    color: Color,
    strokeWidth: Float,
    filled: Boolean
) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f
    val scale = size.minDimension / 24f // Escala baseada no tamanho
    
    // ⚡ Definir pontos do raio em formato de flash otimizado
    val flashPath = Path().apply {
        // Começar no topo-esquerda
        moveTo(centerX - 4f * scale, centerY - 8f * scale)
        
        // Linha diagonal para baixo-direita (parte superior)
        lineTo(centerX + 2f * scale, centerY - 1f * scale)
        
        // Linha horizontal para a esquerda (meio)
        lineTo(centerX + 6f * scale, centerY - 1f * scale)
        
        // Linha diagonal para baixo-esquerda (parte inferior)
        lineTo(centerX - 2f * scale, centerY + 8f * scale)
        
        // Linha diagonal para cima-direita
        lineTo(centerX + 1f * scale, centerY + 2f * scale)
        
        // Linha horizontal para a direita
        lineTo(centerX - 3f * scale, centerY + 2f * scale)
        
        // Fechar o caminho voltando ao início
        close()
    }
    
    if (filled) {
        // ⚡ Flash preenchido
        drawPath(
            path = flashPath,
            color = color
        )
    } else {
        // ⚡ Flash apenas contorno
        drawPath(
            path = flashPath,
            color = color,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
    
    // ✨ Adicionar brilho opcional no centro para efeito de energia
    if (filled) {
        drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = 2f * scale,
            center = Offset(centerX, centerY),
            style = Stroke(width = 1f)
        )
    }
}

/**
 * 🌟 Variante animada do ícone Flash
 */
@Composable
fun AnimatedFlashIcon(
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    color: Color = MaterialTheme.colorScheme.onSurface,
    isActive: Boolean = false
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.7f,
        animationSpec = tween(300),
        label = "flash_alpha"
    )
    
    FlashIcon(
        modifier = modifier,
        size = size,
        color = color.copy(alpha = animatedAlpha),
        strokeWidth = if (isActive) 3f else 2.5f,
        filled = isActive
    )
} 