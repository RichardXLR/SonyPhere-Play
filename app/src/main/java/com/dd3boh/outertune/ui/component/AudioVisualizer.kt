package com.dd3boh.outertune.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlin.math.*
import kotlin.random.Random

/**
 * Modern audio visualizer component with multiple visualization modes
 */
enum class VisualizationMode {
    BARS,
    CIRCULAR,
    WAVEFORM,
    PARTICLES,
    SPECTRUM,
    PLASMA,
    GALAXY,
    PULSE,
    DNA,
    MATRIX
}

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var life: Float,
    var color: Color
)

@Composable
fun AudioVisualizer(
    frequencyBands: StateFlow<FloatArray>,
    waveformData: StateFlow<ByteArray>,
    amplitudeData: StateFlow<Float>,
    visualizationMode: VisualizationMode = VisualizationMode.BARS,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    modifier: Modifier = Modifier
) {
    val frequencies by frequencyBands.collectAsState()
    val waveform by waveformData.collectAsState()
    val amplitude by amplitudeData.collectAsState()
    
    // Smooth animation values
    var smoothedFrequencies by remember { mutableStateOf(FloatArray(32) { 0f }) }
    var smoothedAmplitude by remember { mutableStateOf(0f) }
    
    // Animation states
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Particles for particle effects
    val particles = remember { mutableStateListOf<Particle>() }
    
    // Smooth the values for fluid animation
    LaunchedEffect(frequencies, amplitude) {
        while (isActive) {
            // Smooth frequency bands
            for (i in smoothedFrequencies.indices) {
                smoothedFrequencies[i] = lerp(
                    smoothedFrequencies[i],
                    frequencies.getOrElse(i) { 0f },
                    0.3f
                )
            }
            
            // Smooth amplitude
            smoothedAmplitude = lerp(smoothedAmplitude, amplitude, 0.2f)
            
            // Update particles
            if (visualizationMode == VisualizationMode.PARTICLES || 
                visualizationMode == VisualizationMode.GALAXY) {
                updateParticles(particles, smoothedAmplitude, primaryColor, secondaryColor)
            }
            
            delay(16) // 60 FPS
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.1f),
                        Color.Transparent
                    ),
                    radius = 500f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                }
        ) {
            when (visualizationMode) {
                VisualizationMode.BARS -> drawBars(
                    smoothedFrequencies, 
                    smoothedAmplitude, 
                    primaryColor, 
                    secondaryColor,
                    pulse
                )
                VisualizationMode.CIRCULAR -> drawCircular(
                    smoothedFrequencies, 
                    smoothedAmplitude, 
                    primaryColor, 
                    secondaryColor,
                    rotation
                )
                VisualizationMode.WAVEFORM -> drawWaveform(
                    waveform, 
                    smoothedAmplitude, 
                    primaryColor, 
                    secondaryColor
                )
                VisualizationMode.PARTICLES -> drawParticles(
                    particles, 
                    smoothedAmplitude
                )
                VisualizationMode.SPECTRUM -> drawSpectrum(
                    smoothedFrequencies, 
                    smoothedAmplitude, 
                    primaryColor, 
                    secondaryColor,
                    rotation
                )
                VisualizationMode.PLASMA -> drawPlasma(
                    smoothedFrequencies,
                    smoothedAmplitude,
                    primaryColor,
                    secondaryColor,
                    rotation
                )
                VisualizationMode.GALAXY -> drawGalaxy(
                    particles,
                    smoothedFrequencies,
                    rotation,
                    primaryColor,
                    secondaryColor
                )
                VisualizationMode.PULSE -> drawPulse(
                    smoothedAmplitude,
                    pulse,
                    primaryColor,
                    secondaryColor
                )
                VisualizationMode.DNA -> drawDNA(
                    smoothedFrequencies,
                    rotation,
                    primaryColor,
                    secondaryColor
                )
                VisualizationMode.MATRIX -> drawMatrix(
                    smoothedFrequencies,
                    primaryColor,
                    secondaryColor
                )
            }
        }
    }
}

private fun DrawScope.drawBars(
    frequencies: FloatArray,
    amplitude: Float,
    primaryColor: Color,
    secondaryColor: Color,
    pulse: Float
) {
    val barCount = frequencies.size
    val barWidth = size.width / (barCount * 1.5f)
    val spacing = barWidth * 0.5f
    
    frequencies.forEachIndexed { index, frequency ->
        val barHeight = frequency * size.height * 0.8f * pulse
        val x = index * (barWidth + spacing) + spacing
        
        // Draw shadow
        drawRoundRect(
            color = Color.Black.copy(alpha = 0.3f),
            topLeft = Offset(x + 2.dp.toPx(), size.height - barHeight + 2.dp.toPx()),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(barWidth / 2)
        )
        
        // Draw gradient bar
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor,
                    secondaryColor
                ),
                startY = size.height - barHeight,
                endY = size.height
            ),
            topLeft = Offset(x, size.height - barHeight),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(barWidth / 2)
        )
        
        // Draw glow effect
        if (frequency > 0.5f) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.6f * frequency),
                        Color.Transparent
                    ),
                    radius = barWidth
                ),
                radius = barWidth,
                center = Offset(x + barWidth / 2, size.height - barHeight)
            )
        }
    }
}

private fun DrawScope.drawCircular(
    frequencies: FloatArray,
    amplitude: Float,
    primaryColor: Color,
    secondaryColor: Color,
    rotation: Float
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val baseRadius = minOf(size.width, size.height) * 0.2f
    
    rotate(rotation, Offset(centerX, centerY)) {
        frequencies.forEachIndexed { index, frequency ->
            val angle = (index.toFloat() / frequencies.size) * 360f
            val radiusOffset = frequency * baseRadius * 2f
            val radius = baseRadius + radiusOffset
            
            val startAngle = angle - 5f
            val sweepAngle = 10f
            
            // Draw arc with gradient
            drawArc(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.8f),
                        secondaryColor.copy(alpha = 0.4f)
                    ),
                    center = Offset(centerX, centerY),
                    radius = radius
                ),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        // Center circle
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = amplitude),
                    secondaryColor.copy(alpha = amplitude * 0.5f),
                    Color.Transparent
                ),
                radius = baseRadius * amplitude
            ),
            radius = baseRadius * amplitude,
            center = Offset(centerX, centerY)
        )
    }
}

private fun DrawScope.drawWaveform(
    waveform: ByteArray,
    amplitude: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    if (waveform.isEmpty()) return
    
    val path = Path()
    val centerY = size.height / 2
    val stepX = size.width / waveform.size.toFloat()
    
    // Create smooth waveform path
    path.moveTo(0f, centerY)
    
    waveform.forEachIndexed { index, byte ->
        val x = index * stepX
        val y = centerY + (byte / 128f) * size.height * 0.4f * amplitude
        
        if (index == 0) {
            path.moveTo(x, y)
        } else {
            val prevX = (index - 1) * stepX
            val prevY = centerY + (waveform[index - 1] / 128f) * size.height * 0.4f * amplitude
            
            val controlX1 = prevX + stepX / 3
            val controlY1 = prevY
            val controlX2 = x - stepX / 3
            val controlY2 = y
            
            path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
        }
    }
    
    // Draw glow
    drawPath(
        path = path,
        brush = Brush.horizontalGradient(
            colors = listOf(primaryColor, secondaryColor, primaryColor)
        ),
        style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round),
        blendMode = BlendMode.Screen,
        alpha = 0.5f
    )
    
    // Draw main waveform
    drawPath(
        path = path,
        brush = Brush.horizontalGradient(
            colors = listOf(primaryColor, secondaryColor, primaryColor)
        ),
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )
}

private fun DrawScope.drawParticles(
    particles: List<Particle>,
    amplitude: Float
) {
    particles.forEach { particle ->
        if (particle.life > 0) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        particle.color.copy(alpha = particle.life),
                        Color.Transparent
                    ),
                    radius = particle.size
                ),
                radius = particle.size * (1f + amplitude * 0.5f),
                center = Offset(particle.x, particle.y)
            )
        }
    }
}

private fun DrawScope.drawSpectrum(
    frequencies: FloatArray,
    amplitude: Float,
    primaryColor: Color,
    secondaryColor: Color,
    rotation: Float
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    
    // Create spectrum rings
    frequencies.forEachIndexed { index, frequency ->
        val radius = (index + 1) * (minOf(size.width, size.height) / frequencies.size / 2f)
        val alpha = frequency * 0.8f
        
        rotate(rotation * (index + 1) * 0.1f, Offset(centerX, centerY)) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.Transparent,
                        primaryColor.copy(alpha = alpha),
                        secondaryColor.copy(alpha = alpha * 0.5f),
                        Color.Transparent
                    ),
                    radius = radius
                ),
                radius = radius,
                center = Offset(centerX, centerY),
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }
}

private fun DrawScope.drawPlasma(
    frequencies: FloatArray,
    amplitude: Float,
    primaryColor: Color,
    secondaryColor: Color,
    rotation: Float
) {
    val time = rotation / 360f * PI.toFloat() * 2
    val gridSize = 20
    val cellWidth = size.width / gridSize
    val cellHeight = size.height / gridSize
    
    for (x in 0 until gridSize) {
        for (y in 0 until gridSize) {
            val freq = frequencies.getOrElse((x + y) % frequencies.size) { 0f }
            
            val value = sin(x * 0.3f + time) + 
                       sin(y * 0.3f + time * 1.5f) +
                       sin((x + y) * 0.2f + time * 2) +
                       sin(sqrt((x * x + y * y).toFloat()) * 0.1f + time)
            
            val normalizedValue = ((value + 4) / 8).toFloat() * freq * amplitude
            
            val color = lerp(primaryColor, secondaryColor, normalizedValue)
            
            drawRect(
                color = color.copy(alpha = normalizedValue.coerceIn(0f, 1f)),
                topLeft = Offset(x * cellWidth, y * cellHeight),
                size = Size(cellWidth, cellHeight)
            )
        }
    }
}

private fun DrawScope.drawGalaxy(
    particles: List<Particle>,
    frequencies: FloatArray,
    rotation: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    
    // Draw spiral arms
    rotate(rotation, Offset(centerX, centerY)) {
        for (arm in 0..2) {
            val armAngle = arm * 120f
            val path = Path()
            
            for (i in 0..100) {
                val t = i / 100f
                val angle = armAngle + t * 720f
                val radius = t * minOf(size.width, size.height) * 0.4f
                val freq = frequencies.getOrElse((i * frequencies.size / 100)) { 0f }
                
                val x = centerX + cos(angle * PI.toFloat() / 180).toFloat() * radius * (1 + freq * 0.3f)
                val y = centerY + sin(angle * PI.toFloat() / 180).toFloat() * radius * (1 + freq * 0.3f)
                
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.8f),
                        secondaryColor.copy(alpha = 0.3f)
                    )
                ),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
    
    // Draw particles
    drawParticles(particles, 1f)
}

private fun DrawScope.drawPulse(
    amplitude: Float,
    pulse: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    val centerX = size.width / 2
    val centerY = size.height / 2
    val maxRadius = minOf(size.width, size.height) * 0.4f
    
    // Draw multiple pulsing circles
    for (i in 0..4) {
        val delay = i * 0.2f
        val adjustedPulse = ((pulse - 0.8f) / 0.4f + delay) % 1f
        val radius = maxRadius * adjustedPulse * amplitude
        val alpha = (1f - adjustedPulse) * amplitude
        
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = alpha * 0.8f),
                    secondaryColor.copy(alpha = alpha * 0.4f),
                    Color.Transparent
                ),
                radius = radius
            ),
            radius = radius,
            center = Offset(centerX, centerY)
        )
        
        drawCircle(
            color = primaryColor.copy(alpha = alpha * 0.5f),
            radius = radius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

private fun DrawScope.drawDNA(
    frequencies: FloatArray,
    rotation: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    val centerX = size.width / 2
    val points = 50
    val helixRadius = size.width * 0.3f
    
    for (helix in 0..1) {
        val path = Path()
        val phaseShift = if (helix == 0) 0f else PI.toFloat()
        
        for (i in 0..points) {
            val t = i.toFloat() / points
            val y = t * size.height
            val angle = rotation * PI.toFloat() / 180 + t * PI.toFloat() * 4 + phaseShift
            val freq = frequencies.getOrElse((i * frequencies.size / points)) { 0f }
            
            val x = centerX + sin(angle) * helixRadius * (1 + freq * 0.5f)
            
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            
            // Draw connection between helixes
            if (i % 5 == 0 && helix == 0) {
                val x2 = centerX + sin(angle + PI.toFloat()) * helixRadius * (1 + freq * 0.5f)
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(primaryColor, secondaryColor)
                    ),
                    start = Offset(x, y),
                    end = Offset(x2, y),
                    strokeWidth = 2.dp.toPx(),
                    alpha = 0.5f
                )
            }
        }
        
        drawPath(
            path = path,
            color = if (helix == 0) primaryColor else secondaryColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}

private fun DrawScope.drawMatrix(
    frequencies: FloatArray,
    primaryColor: Color,
    secondaryColor: Color
) {
    val columns = 16
    val rows = 12
    val cellWidth = size.width / columns
    val cellHeight = size.height / rows
    
    for (col in 0 until columns) {
        val freq = frequencies.getOrElse(col % frequencies.size) { 0f }
        val activeRows = (rows * freq).toInt()
        
        for (row in 0 until rows) {
            val y = size.height - (row + 1) * cellHeight
            val isActive = row < activeRows
            val alpha = if (isActive) 0.8f else 0.1f
            val color = if (row < rows * 0.3f) secondaryColor 
                       else if (row < rows * 0.7f) lerp(secondaryColor, primaryColor, 0.5f)
                       else primaryColor
            
            drawRoundRect(
                color = color.copy(alpha = alpha),
                topLeft = Offset(col * cellWidth + 2.dp.toPx(), y + 2.dp.toPx()),
                size = Size(cellWidth - 4.dp.toPx(), cellHeight - 4.dp.toPx()),
                cornerRadius = CornerRadius(2.dp.toPx())
            )
        }
    }
}

private fun updateParticles(
    particles: MutableList<Particle>,
    amplitude: Float,
    primaryColor: Color,
    secondaryColor: Color
) {
    // Remove dead particles
    particles.removeAll { it.life <= 0 }
    
    // Update existing particles
    particles.forEach { particle ->
        particle.x += particle.vx
        particle.y += particle.vy
        particle.vy += 0.1f // gravity
        particle.life -= 0.02f
        particle.size *= 0.98f
    }
    
    // Add new particles based on amplitude
    if (amplitude > 0.3f && particles.size < 100) {
        repeat((amplitude * 5).toInt()) {
            particles.add(
                Particle(
                    x = Random.nextFloat() * 1000f,
                    y = Random.nextFloat() * 500f,
                    vx = Random.nextFloat() * 4f - 2f,
                    vy = Random.nextFloat() * -4f - 1f,
                    size = Random.nextFloat() * 10f + 5f,
                    life = 1f,
                    color = if (Random.nextBoolean()) primaryColor else secondaryColor
                )
            )
        }
    }
}

private fun lerp(start: Float, stop: Float, fraction: Float): Float {
    return start + fraction * (stop - start)
}

private fun lerp(start: Color, stop: Color, fraction: Float): Color {
    return Color(
        red = lerp(start.red, stop.red, fraction),
        green = lerp(start.green, stop.green, fraction),
        blue = lerp(start.blue, stop.blue, fraction),
        alpha = lerp(start.alpha, stop.alpha, fraction)
    )
} 