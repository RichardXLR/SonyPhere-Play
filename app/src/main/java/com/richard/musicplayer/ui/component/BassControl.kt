package com.richard.musicplayer.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@Composable
fun BassControl(
    bassLevel: Float,
    onBassLevelChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
) {
    val haptic = LocalHapticFeedback.current
    
    if (isExpanded) {
        ExpandedBassControl(
            bassLevel = bassLevel,
            onBassLevelChange = onBassLevelChange,
            modifier = modifier
        )
    } else {
        CompactBassControl(
            bassLevel = bassLevel,
            onBassLevelChange = onBassLevelChange,
            modifier = modifier
        )
    }
}

@Composable
private fun ExpandedBassControl(
    bassLevel: Float,
    onBassLevelChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Bass Boost",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Visual Bass Indicator
            BassVisualizer(
                bassLevel = bassLevel,
                modifier = Modifier
                    .size(180.dp)
                    .padding(16.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Bass Level Text
            Text(
                text = "${bassLevel.toInt()}%",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Aviso de Segurança Dinâmico
            val (warningText, warningColor, warningIcon) = when {
                bassLevel >= 80f -> Triple(
                    "⚠️ ATENÇÃO: Nível MUITO ALTO! Pode causar danos auditivos permanentes e reduzir significativamente o volume. Use com extrema cautela.",
                    MaterialTheme.colorScheme.errorContainer,
                    Icons.AutoMirrored.Rounded.VolumeOff
                )
                bassLevel >= 60f -> Triple(
                    "⚠️ CUIDADO: Nível alto pode afetar sua audição e reduzir o volume geral. Recomendamos diminuir para níveis mais seguros.",
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                    Icons.AutoMirrored.Rounded.VolumeDown
                )
                bassLevel >= 40f -> Triple(
                    "💡 MODERADO: Nível aceitável, mas monitore o volume para proteger seus ouvidos.",
                    MaterialTheme.colorScheme.tertiaryContainer,
                    Icons.AutoMirrored.Rounded.VolumeUp
                )
                else -> Triple(
                    "✅ SEGURO: Nível recomendado para uso prolongado sem riscos auditivos.",
                    MaterialTheme.colorScheme.primaryContainer,
                    Icons.AutoMirrored.Rounded.VolumeUp
                )
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = warningColor
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = warningIcon,
                        contentDescription = null,
                        tint = when {
                            bassLevel >= 60f -> MaterialTheme.colorScheme.onErrorContainer
                            bassLevel >= 40f -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = warningText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = when {
                            bassLevel >= 60f -> MaterialTheme.colorScheme.onErrorContainer
                            bassLevel >= 40f -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        lineHeight = 13.sp
                    )
                }
            }
            
            // Slider
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Slider(
                    value = bassLevel,
                    onValueChange = { newValue ->
                        onBassLevelChange(newValue)
                        if ((newValue.toInt() % 10) == 0) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
                    valueRange = 0f..100f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                
                // Preset buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BassPresetButton(
                        text = "Off",
                        isSelected = bassLevel == 0f,
                        onClick = { 
                            onBassLevelChange(0f)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )
                    BassPresetButton(
                        text = "Low",
                        isSelected = bassLevel in 20f..40f,
                        onClick = { 
                            onBassLevelChange(30f)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )
                    BassPresetButton(
                        text = "Med",
                        isSelected = bassLevel in 40f..60f,
                        onClick = { 
                            onBassLevelChange(50f)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )
                    BassPresetButton(
                        text = "High",
                        isSelected = bassLevel in 60f..80f,
                        onClick = { 
                            onBassLevelChange(70f)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )
                    BassPresetButton(
                        text = "Max",
                        isSelected = bassLevel > 80f,
                        onClick = { 
                            onBassLevelChange(100f)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactBassControl(
    bassLevel: Float,
    onBassLevelChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val warningColor = when {
        bassLevel >= 80f -> MaterialTheme.colorScheme.error
        bassLevel >= 60f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                if (bassLevel >= 60f) 
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                else 
                    MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (bassLevel >= 80f) Icons.AutoMirrored.Rounded.VolumeOff 
                         else if (bassLevel >= 60f) Icons.AutoMirrored.Rounded.VolumeDown
                         else Icons.Rounded.GraphicEq,
            contentDescription = null,
            tint = warningColor,
            modifier = Modifier.size(24.dp)
        )
        
        Slider(
            value = bassLevel,
            onValueChange = onBassLevelChange,
            valueRange = 0f..100f,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
        
        Text(
            text = if (bassLevel >= 80f) "⚠️${bassLevel.toInt()}%" 
                   else if (bassLevel >= 60f) "⚠️${bassLevel.toInt()}%"
                   else "${bassLevel.toInt()}%",
            fontSize = 14.sp,
            fontWeight = if (bassLevel >= 60f) FontWeight.Bold else FontWeight.Medium,
            color = warningColor
        )
    }
}

@Composable
private fun BassVisualizer(
    bassLevel: Float,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bass")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
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
    
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2
        val center = size.center
        
        // Background circles
        for (i in 1..3) {
            drawCircle(
                color = Color.White.copy(alpha = 0.05f * i),
                radius = radius * (1f - i * 0.25f),
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
        
        // Animated bass rings
        val primaryColor = Color(0xFF6200EE) // Use a fixed color instead of MaterialTheme
        val bassIntensity = (bassLevel / 100f) * pulse
        for (i in 0..2) {
            rotate(rotation + i * 120f, center) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0f),
                            primaryColor.copy(alpha = bassIntensity),
                            primaryColor.copy(alpha = 0f)
                        ),
                        center = center
                    ),
                    startAngle = 0f,
                    sweepAngle = 120f,
                    useCenter = false,
                    style = Stroke(
                        width = (4.dp.toPx() * bassIntensity).coerceAtLeast(1f),
                        cap = StrokeCap.Round
                    ),
                    size = size.copy(
                        width = size.width * (0.8f + i * 0.1f),
                        height = size.height * (0.8f + i * 0.1f)
                    ),
                    topLeft = Offset(
                        x = center.x - size.width * (0.8f + i * 0.1f) / 2,
                        y = center.y - size.height * (0.8f + i * 0.1f) / 2
                    )
                )
            }
        }
        
        // Center icon
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor,
                    primaryColor.copy(alpha = 0.8f)
                ),
                center = center
            ),
            radius = radius * 0.3f,
            center = center
        )
    }
}

@Composable
private fun BassPresetButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.size(width = 56.dp, height = 36.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 