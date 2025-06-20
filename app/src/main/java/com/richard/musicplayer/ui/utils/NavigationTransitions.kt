package com.richard.musicplayer.ui.utils

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry

/**
 * Sistema de transições fluidas para navegação
 */
object NavigationTransitions {
    
    // Duração padrão das animações
    private const val ANIMATION_DURATION = 400
    private const val FAST_ANIMATION_DURATION = 250
    
    // Easing personalizado para suavidade máxima
    private val SmoothEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)
    private val BouncyEasing = CubicBezierEasing(0.68f, -0.55f, 0.265f, 1.55f)
    
    /**
     * Transição de slide horizontal (padrão)
     */
    fun slideHorizontal(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ContentTransform {
        return {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(
                    durationMillis = ANIMATION_DURATION,
                    easing = SmoothEasing
                )
            ).togetherWith(
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(
                        durationMillis = ANIMATION_DURATION,
                        easing = SmoothEasing
                    )
                )
            )
        }
    }
    
    /**
     * Transição de slide vertical para modais
     */
    fun slideVertical(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ContentTransform {
        return {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(
                    durationMillis = ANIMATION_DURATION,
                    easing = SmoothEasing
                )
            ).togetherWith(
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(
                        durationMillis = ANIMATION_DURATION,
                        easing = SmoothEasing
                    )
                )
            )
        }
    }
    
    /**
     * Transição fade elegante
     */
    fun fade(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ContentTransform {
        return {
            fadeIn(
                animationSpec = tween(
                    durationMillis = ANIMATION_DURATION,
                    easing = SmoothEasing
                )
            ).togetherWith(
                fadeOut(
                    animationSpec = tween(
                        durationMillis = ANIMATION_DURATION,
                        easing = SmoothEasing
                    )
                )
            )
        }
    }
    
    /**
     * Transição de escala com bounce
     */
    fun scaleWithBounce(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ContentTransform {
        return {
            scaleIn(
                initialScale = 0.9f,
                animationSpec = tween(
                    durationMillis = ANIMATION_DURATION,
                    easing = BouncyEasing
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = ANIMATION_DURATION,
                    easing = SmoothEasing
                )
            ) togetherWith scaleOut(
                targetScale = 1.1f,
                animationSpec = tween(
                    durationMillis = ANIMATION_DURATION,
                    easing = SmoothEasing
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = ANIMATION_DURATION,
                    easing = SmoothEasing
                )
            )
        }
    }
    
    /**
     * Transição compartilhada para o player
     */
    fun sharedElement(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ContentTransform {
        return {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = FAST_ANIMATION_DURATION,
                    easing = SmoothEasing
                )
            ) togetherWith slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = FAST_ANIMATION_DURATION,
                    easing = SmoothEasing
                )
            )
        }
    }
    
    /**
     * Transição de retorno suave
     */
    fun popBack(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ContentTransform {
        return {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(
                    durationMillis = ANIMATION_DURATION,
                    easing = SmoothEasing
                )
            ).togetherWith(
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(
                        durationMillis = ANIMATION_DURATION,
                        easing = SmoothEasing
                    )
                )
            )
        }
    }
    
    /**
     * Transição rápida para navegação entre tabs
     */
    fun fastTab(): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ContentTransform {
        return {
            fadeIn(
                animationSpec = tween(
                    durationMillis = FAST_ANIMATION_DURATION,
                    easing = FastOutSlowInEasing
                )
            ).togetherWith(
                fadeOut(
                    animationSpec = tween(
                        durationMillis = FAST_ANIMATION_DURATION,
                        easing = FastOutSlowInEasing
                    )
                )
            )
        }
    }
    
    /**
     * Transição personalizada baseada no destino
     */
    fun getTransitionForRoute(route: String?): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ContentTransform {
        return when {
            route?.contains("player") == true -> sharedElement()
            route?.contains("search") == true -> slideVertical()
            route?.contains("album") == true -> scaleWithBounce()
            route?.contains("artist") == true -> scaleWithBounce()
            route?.contains("playlist") == true -> slideHorizontal()
            route?.contains("settings") == true -> slideVertical()
            else -> fade()
        }
    }
}

/**
 * Extensões para facilitar o uso das transições
 */

@Composable
fun rememberSmoothTransition(
    targetRoute: String?
): AnimatedContentTransitionScope<NavBackStackEntry>.() -> ContentTransform {
    return NavigationTransitions.getTransitionForRoute(targetRoute)
}

/**
 * Transições personalizadas para elementos específicos
 */
object ElementTransitions {
    
    /**
     * Animação para cards de música
     */
    fun musicCard() = slideInVertically(
        initialOffsetY = { it / 4 },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ) + fadeIn(
        animationSpec = tween(300)
    )
    
    /**
     * Animação para botões flutuantes
     */
    fun fab() = scaleIn(
        initialScale = 0.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessHigh
        )
    ) + fadeIn(
        animationSpec = tween(200)
    )
    
    /**
     * Animação para listas
     */
    fun listItem(index: Int) = slideInVertically(
        initialOffsetY = { it / 8 },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = 300 + (index * 50)
        )
    )
} 