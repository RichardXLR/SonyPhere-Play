package com.richard.musicplayer.ui.utils

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Especificações de animação modernas
object ModernAnimationSpec {
    val fastSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val smoothSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    val bounceSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    
    val slideSpec = tween<Float>(400, easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f))
    val fadeSpec = tween<Float>(300, easing = LinearOutSlowInEasing)
    val scaleSpec = tween<Float>(350, easing = CubicBezierEasing(0.3f, 0.0f, 0.2f, 1.0f))
}

// Transições personalizadas
@OptIn(ExperimentalAnimationApi::class)
fun modernSlideInTransition() = slideInHorizontally(
    initialOffsetX = { it },
    animationSpec = tween(500, easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f))
) + fadeIn(
    animationSpec = tween(400, 100)
)

@OptIn(ExperimentalAnimationApi::class)
fun modernSlideOutTransition() = slideOutHorizontally(
    targetOffsetX = { -it / 4 },
    animationSpec = tween(400, easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f))
) + fadeOut(
    animationSpec = tween(300)
)

// Modificador para efeito de hover/press moderno
@Composable
fun Modifier.modernClickable(
    onClick: () -> Unit,
    enabled: Boolean = true
): Modifier {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = ModernAnimationSpec.bounceSpring
    )
    
    return this
        .scale(scale)
        .graphicsLayer {
            if (isPressed) {
                shadowElevation = 8.dp.toPx()
                translationY = 2.dp.toPx()
            }
        }
}

// Efeito parallax para listas
@Composable
fun Modifier.parallaxEffect(scrollOffset: Float): Modifier {
    return this.graphicsLayer {
        translationY = scrollOffset * 0.5f
        alpha = 1f - (scrollOffset / 1000f).coerceIn(0f, 0.3f)
    }
}

// Efeito de blur dinâmico
@Composable
fun Modifier.dynamicBlur(
    enabled: Boolean,
    radius: Dp = 16.dp
): Modifier {
    val blurValue by animateDpAsState(
        targetValue = if (enabled) radius else 0.dp,
        animationSpec = tween(400)
    )
    return this.blur(blurValue)
}

// Animação de entrada escalonada para listas
@Composable
fun Modifier.staggeredAnimateIn(
    index: Int,
    totalItems: Int
): Modifier {
    val delay = (index * 50).coerceAtMost(300)
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
        isVisible = true
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(600)
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = ModernAnimationSpec.bounceSpring
    )
    
    val translationY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 30f,
        animationSpec = tween(500, delay)
    )
    
    return this.graphicsLayer {
        this.alpha = alpha
        this.scaleX = scale
        this.scaleY = scale
        this.translationY = translationY
    }
} 