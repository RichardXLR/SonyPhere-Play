package com.richard.musicplayer.ui.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
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
import com.richard.musicplayer.playback.SpatialRoomType
import kotlin.math.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items

@Composable
fun SpatialAudioControl(
    isEnabled: Boolean,
    virtualizerStrength: Int,
    roomType: SpatialRoomType,
    headTracking: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onVirtualizerChange: (Int) -> Unit,
    onRoomTypeChange: (SpatialRoomType) -> Unit,
    onHeadTrackingChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
) {
    val haptic = LocalHapticFeedback.current
    
    if (isExpanded) {
        ExpandedSpatialControl(
            isEnabled = isEnabled,
            virtualizerStrength = virtualizerStrength,
            roomType = roomType,
            headTracking = headTracking,
            onEnabledChange = onEnabledChange,
            onVirtualizerChange = onVirtualizerChange,
            onRoomTypeChange = onRoomTypeChange,
            onHeadTrackingChange = onHeadTrackingChange,
            modifier = modifier
        )
    } else {
        CompactSpatialControl(
            isEnabled = isEnabled,
            virtualizerStrength = virtualizerStrength,
            onEnabledChange = onEnabledChange,
            onVirtualizerChange = onVirtualizerChange,
            modifier = modifier
        )
    }
}

@Composable
private fun ExpandedSpatialControl(
    isEnabled: Boolean,
    virtualizerStrength: Int,
    roomType: SpatialRoomType,
    headTracking: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onVirtualizerChange: (Int) -> Unit,
    onRoomTypeChange: (SpatialRoomType) -> Unit,
    onHeadTrackingChange: (Boolean) -> Unit,
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
            // Header with toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SurroundSound,
                        contentDescription = null,
                        tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Áudio Espacial",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Switch(
                    checked = isEnabled,
                    onCheckedChange = { 
                        onEnabledChange(it)
                        haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
            
            if (isEnabled) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // 3D Visualization
                Spatial3DVisualizer(
                    virtualizerStrength = virtualizerStrength,
                    roomType = roomType,
                    headTracking = headTracking,
                    modifier = Modifier
                        .size(200.dp)
                        .padding(16.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Virtualizer Strength
                Text(
                    text = "Intensidade 3D: ${virtualizerStrength / 10}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Slider(
                    value = virtualizerStrength.toFloat(),
                    onValueChange = { newValue ->
                        onVirtualizerChange(newValue.toInt())
                        if ((newValue.toInt() % 100) == 0) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
                    valueRange = 0f..1000f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Room Types
                Text(
                    text = "Ambiente",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(SpatialRoomType.values()) { room ->
                        RoomTypeButton(
                            roomType = room,
                            isSelected = roomType == room,
                            onClick = { 
                                onRoomTypeChange(room)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Head Tracking
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.TrackChanges,
                            contentDescription = null,
                            tint = if (headTracking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Rastreamento de Cabeça",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Switch(
                        checked = headTracking,
                        onCheckedChange = { 
                            onHeadTrackingChange(it)
                            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactSpatialControl(
    isEnabled: Boolean,
    virtualizerStrength: Int,
    onEnabledChange: (Boolean) -> Unit,
    onVirtualizerChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isEnabled) Icons.Rounded.SurroundSound else Icons.Rounded.Speaker,
            contentDescription = null,
            tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        
        if (isEnabled) {
            Slider(
                value = virtualizerStrength.toFloat(),
                onValueChange = { onVirtualizerChange(it.toInt()) },
                valueRange = 0f..1000f,
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
                text = "${virtualizerStrength / 10}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = { onEnabledChange(true) }
            ) {
                Text("Ativar", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun Spatial3DVisualizer(
    virtualizerStrength: Int,
    roomType: SpatialRoomType,
    headTracking: Boolean,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "spatial")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Canvas(modifier = modifier) {
        val center = size.center
        val radius = size.minDimension / 2
        val primaryColor = Color(0xFF6200EE)
        
        // Room boundary
        val roomRadius = when (roomType) {
            SpatialRoomType.SMALL_ROOM -> radius * 0.6f
            SpatialRoomType.MEDIUM_ROOM -> radius * 0.8f
            SpatialRoomType.LARGE_ROOM -> radius * 1.0f
            SpatialRoomType.CONCERT_HALL -> radius * 1.3f
            SpatialRoomType.CATHEDRAL -> radius * 1.5f
            SpatialRoomType.OUTDOOR -> radius * 2.0f
            else -> radius * 0.5f
        }
        
        // Draw room boundary
        if (roomType != SpatialRoomType.NONE) {
            drawCircle(
                color = primaryColor.copy(alpha = 0.2f),
                radius = roomRadius * pulse,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
        
        // Draw spatial field
        val spatialIntensity = (virtualizerStrength / 1000f) * pulse
        for (i in 1..3) {
            rotate(rotation + i * 120f, center) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0f),
                            primaryColor.copy(alpha = spatialIntensity * 0.5f),
                            primaryColor.copy(alpha = 0f)
                        ),
                        center = center
                    ),
                    startAngle = 0f,
                    sweepAngle = 90f,
                    useCenter = false,
                    style = Stroke(
                        width = (3.dp.toPx() * spatialIntensity).coerceAtLeast(1f),
                        cap = StrokeCap.Round
                    ),
                    size = size.copy(
                        width = size.width * (0.7f + i * 0.1f),
                        height = size.height * (0.7f + i * 0.1f)
                    ),
                    topLeft = Offset(
                        x = center.x - size.width * (0.7f + i * 0.1f) / 2,
                        y = center.y - size.height * (0.7f + i * 0.1f) / 2
                    )
                )
            }
        }
        
        // Center listener
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor,
                    primaryColor.copy(alpha = 0.7f)
                ),
                center = center
            ),
            radius = 8.dp.toPx() * if (headTracking) pulse else 1f,
            center = center
        )
        
        // Head tracking indicator
        if (headTracking) {
            val headAngle = rotation * 0.1f
            val headX = center.x + cos(Math.toRadians(headAngle.toDouble())).toFloat() * 12.dp.toPx()
            val headY = center.y + sin(Math.toRadians(headAngle.toDouble())).toFloat() * 12.dp.toPx()
            
            drawCircle(
                color = primaryColor.copy(alpha = 0.6f),
                radius = 4.dp.toPx(),
                center = Offset(headX, headY)
            )
        }
    }
}

@Composable
private fun RoomTypeButton(
    roomType: SpatialRoomType,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val (icon, label) = when (roomType) {
        SpatialRoomType.NONE -> Icons.AutoMirrored.Rounded.VolumeOff to "Nenhum"
        SpatialRoomType.SMALL_ROOM -> Icons.Rounded.Home to "Quarto"
        SpatialRoomType.MEDIUM_ROOM -> Icons.Rounded.MeetingRoom to "Sala"
        SpatialRoomType.LARGE_ROOM -> Icons.Rounded.CorporateFare to "Salão"
        SpatialRoomType.CONCERT_HALL -> Icons.Rounded.TheaterComedy to "Teatro"
        SpatialRoomType.CATHEDRAL -> Icons.Rounded.Church to "Catedral"
        SpatialRoomType.OUTDOOR -> Icons.Rounded.Nature to "Ar Livre"
    }
    
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) 
                    MaterialTheme.colorScheme.onPrimaryContainer 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
} 