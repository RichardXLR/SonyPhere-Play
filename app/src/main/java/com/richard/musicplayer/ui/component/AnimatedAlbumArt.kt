package com.richard.musicplayer.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random

@Composable
fun AnimatedAlbumArt(
    albumArtUrl: String?,
    isPlaying: Boolean,
    audioData: ByteArray? = null,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    
    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "album")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (isPlaying) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 20000,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    // Beat detection from audio data
    val beatIntensity by remember(audioData) {
        derivedStateOf {
            audioData?.let { data ->
                if (data.isNotEmpty()) {
                    // Simple beat detection - analyze low frequencies
                    val lowFreqSum = data.take(data.size / 4).map { abs(it.toInt()) }.sum()
                    (lowFreqSum / (data.size / 4f) / 128f).coerceIn(0f, 1f)
                } else 0f
            } ?: 0f
        }
    }
    
    val animatedBeatScale by animateFloatAsState(
        targetValue = 1f + beatIntensity * 0.1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "beatScale"
    )
    
    Box(
        modifier = modifier
            .scale(scale * animatedBeatScale)
            .graphicsLayer {
                rotationZ = rotation
            },
        contentAlignment = Alignment.Center
    ) {
        // Background glow effect
        BeatReactiveGlow(
            beatIntensity = beatIntensity,
            isPlaying = isPlaying,
            modifier = Modifier.fillMaxSize()
        )
        
        // Album art with effects
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        ) {
            // Album image
            AsyncImage(
                model = albumArtUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Vinyl overlay effect
            if (isPlaying) {
                VinylOverlay(
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        
        // Visualizer rings
        if (isPlaying && audioData != null) {
            AudioVisualizerRings(
                audioData = audioData,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun BeatReactiveGlow(
    beatIntensity: Float,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
) {
    val animatedGlowRadius by animateFloatAsState(
        targetValue = if (isPlaying) 0.3f + beatIntensity * 0.2f else 0.1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "glowRadius"
    )
    
    val primaryColor = Color(0xFF6200EE) // Use a fixed color instead of MaterialTheme
    
    Canvas(modifier = modifier) {
        val center = size.center
        val maxRadius = size.minDimension / 2
        
        // Multiple glow layers
        for (i in 3 downTo 1) {
            val glowRadius = maxOf(maxRadius * (1f + animatedGlowRadius * i), 1f) // Ensure radius is at least 1
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.3f * beatIntensity),
                        primaryColor.copy(alpha = 0.1f * beatIntensity),
                        Color.Transparent
                    ),
                    center = center,
                    radius = glowRadius
                ),
                radius = glowRadius,
                center = center
            )
        }
    }
}

@Composable
private fun VinylOverlay(
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val center = size.center
        val radius = size.minDimension / 2
        
        // Vinyl grooves effect
        for (i in 1..20) {
            drawCircle(
                color = Color.Black.copy(alpha = 0.02f),
                radius = radius * (0.3f + i * 0.035f),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }
        
        // Light reflection
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = 0.1f),
                    Color.White.copy(alpha = 0.2f),
                    Color.White.copy(alpha = 0.1f),
                    Color.Transparent
                ),
                center = center
            ),
            startAngle = -45f,
            sweepAngle = 90f,
            useCenter = true,
            size = size
        )
    }
}

@Composable
private fun AudioVisualizerRings(
    audioData: ByteArray,
    modifier: Modifier = Modifier,
) {
    val barCount = 64
    val animatedBars = remember { mutableStateListOf<Float>() }
    
    LaunchedEffect(audioData) {
        if (audioData.isNotEmpty()) {
            val barValues = mutableListOf<Float>()
            val step = audioData.size / barCount
            
            for (i in 0 until barCount) {
                val startIndex = i * step
                val endIndex = minOf(startIndex + step, audioData.size)
                val slice = audioData.slice(startIndex until endIndex)
                val magnitude = slice.map { abs(it.toInt()) }.average() / 128f
                barValues.add(magnitude.toFloat().coerceIn(0f, 1f))
            }
            
            animatedBars.clear()
            animatedBars.addAll(barValues)
        }
    }
    
    Canvas(modifier = modifier) {
        val center = size.center
        val maxRadius = size.minDimension / 2
        val innerRadius = maxRadius * 0.7f
        val outerRadius = maxRadius * 1.2f
        
        animatedBars.forEachIndexed { index, magnitude ->
            val angle = (index.toFloat() / barCount) * 360f - 90f
            val angleRad = Math.toRadians(angle.toDouble()).toFloat()
            
            val barHeight = (outerRadius - innerRadius) * magnitude
            val startRadius = innerRadius
            val endRadius = innerRadius + barHeight
            
            // Draw visualizer bar
            val visualizerColor = Color(0xFF6200EE) // Use a fixed color instead of MaterialTheme
            val safeOuterRadius = maxOf(outerRadius, 1f) // Ensure radius is at least 1
            drawLine(
                brush = Brush.radialGradient(
                    colors = listOf(
                        visualizerColor,
                        visualizerColor.copy(alpha = 0.5f)
                    ),
                    center = center,
                    radius = safeOuterRadius
                ),
                start = Offset(
                    x = center.x + cos(angleRad) * startRadius,
                    y = center.y + sin(angleRad) * startRadius
                ),
                end = Offset(
                    x = center.x + cos(angleRad) * endRadius,
                    y = center.y + sin(angleRad) * endRadius
                ),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun PulsingAlbumArt(
    albumArtUrl: String?,
    isPlaying: Boolean,
    beatIntensity: Float = 0f,
    modifier: Modifier = Modifier,
) {
    val scale by animateFloatAsState(
        targetValue = if (isPlaying) 1f + beatIntensity * 0.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "pulseScale"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = albumArtUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Overlay gradient that pulses with beat
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.primary.copy(alpha = beatIntensity * 0.3f)
                        )
                    )
                )
        )
    }
} 