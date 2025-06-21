/*
 * Copyright (C) 2024 z-huang/InnerTune
 * Copyright (C) 2025 O﻿ute﻿rTu﻿ne Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.richard.musicplayer.ui.player

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.richard.musicplayer.LocalPlayerConnection
import com.richard.musicplayer.R
import com.richard.musicplayer.constants.MiniPlayerHeight
import com.richard.musicplayer.extensions.togglePlayPause
import com.richard.musicplayer.models.MediaMetadata
import com.richard.musicplayer.ui.component.AsyncImageLocal
import com.richard.musicplayer.ui.utils.imageCache

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VinylMiniPlayer(
    position: Long,
    duration: Long,
    modifier: Modifier = Modifier,
    onExpand: (() -> Unit)? = null
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val playbackState by playerConnection.playbackState.collectAsState()
    val error by playerConnection.error.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()
    
    // Animação de rotação do vinil
    var currentRotation by remember { mutableStateOf(0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl")
    
    // Velocidade de rotação baseada no gênero/BPM (simulada)
    val rotationDuration = remember(mediaMetadata) { 
        // Simulação: músicas mais rápidas giram mais rápido
        val baseDuration = 10000 // 10 segundos para uma rotação completa
        if (mediaMetadata?.title?.contains("fast", ignoreCase = true) == true ||
            mediaMetadata?.title?.contains("rock", ignoreCase = true) == true) {
            baseDuration * 0.7f
        } else if (mediaMetadata?.title?.contains("slow", ignoreCase = true) == true ||
                   mediaMetadata?.title?.contains("ballad", ignoreCase = true) == true) {
            baseDuration * 1.3f
        } else {
            baseDuration.toFloat()
        }
    }.toInt()
    
    val vinylRotation by infiniteTransition.animateFloat(
        initialValue = currentRotation,
        targetValue = currentRotation + 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = rotationDuration,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinylRotation"
    )
    
    // Atualizar rotação apenas quando estiver tocando
    LaunchedEffect(isPlaying) {
        if (!isPlaying) {
            currentRotation = vinylRotation
        }
    }
    
    // Animação de escala para transições suaves
    val playerScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "playerScale"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(MiniPlayerHeight)
            .scale(playerScale)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                ambientColor = Color.Black.copy(alpha = 0.15f),
                spotColor = Color.Black.copy(alpha = 0.25f)
            )
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                )
            )
    ) {
        // Barra de progresso ultra fina e elegante
        LinearProgressIndicator(
            progress = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f,
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Vinyl com capa do álbum circular
            mediaMetadata?.let { metadata ->
                VinylAlbumArt(
                    mediaMetadata = metadata,
                    isPlaying = isPlaying,
                    rotation = if (isPlaying) vinylRotation else currentRotation,
                    error = error,
                    modifier = Modifier.padding(end = 14.dp),
                    onExpand = onExpand
                )
            }
            
            // Informações da música
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = mediaMetadata?.title ?: "",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = mediaMetadata?.artists?.joinToString { it.name } ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Controles
            PlayPauseButton(
                isPlaying = isPlaying,
                playbackState = playbackState,
                onClick = {
                    if (playbackState == Player.STATE_ENDED) {
                        playerConnection.player.seekTo(0, 0)
                        playerConnection.player.playWhenReady = true
                    } else {
                        playerConnection.player.togglePlayPause()
                    }
                }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Botão Next
            IconButton(
                onClick = { 
                    if (canSkipNext) playerConnection.player.seekToNext() 
                },
                enabled = canSkipNext,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.skip_next),
                    contentDescription = "Next",
                    tint = if (canSkipNext) 
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun VinylAlbumArt(
    mediaMetadata: MediaMetadata,
    isPlaying: Boolean,
    rotation: Float,
    error: PlaybackException?,
    modifier: Modifier = Modifier,
    onExpand: (() -> Unit)? = null
) {
    val playerConnection = LocalPlayerConnection.current
    val isWaitingForNetwork by playerConnection?.waitingForNetworkConnection?.collectAsState(initial = false)
        ?: remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    
    Box(
        modifier = modifier.size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        // Sombra do vinil
        Box(
            modifier = Modifier
                .size(56.dp)
                .offset(y = 2.dp)
                .clip(CircleShape)
                .background(
                    Color.Black.copy(alpha = 0.2f)
                )
                .blur(8.dp)
        )
        
        // Container do vinil
        Box(
            modifier = Modifier
                .size(56.dp)
                .clickable { 
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onExpand?.invoke() 
                }
                .graphicsLayer {
                    rotationZ = rotation
                    shadowElevation = if (isPlaying) 8.dp.toPx() else 4.dp.toPx()
                }
        ) {
            // Fundo do vinil com grooves (sulcos) realistas
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.Black)
            ) {
                // Grooves concêntricos
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val maxRadius = size.minDimension / 2
                    
                    // Desenhar sulcos concêntricos
                    for (i in 0..20) {
                        val radius = maxRadius * (0.3f + (i * 0.035f))
                        drawCircle(
                            color = Color.Gray.copy(alpha = 0.1f + (i % 2) * 0.05f),
                            radius = radius,
                            center = center,
                            style = Stroke(width = 0.5f.dp.toPx())
                        )
                    }
                }
                
                // Reflexos de luz no vinil
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.15f),
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.1f),
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.05f),
                                    Color.Transparent
                                ),
                                center = Offset.Unspecified
                            )
                        )
                )
            }
            
            // Capa do álbum circular (sem buraco central)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(2.dp)
                    .clip(CircleShape)
            ) {
                if (mediaMetadata.isLocal) {
                    AsyncImageLocal(
                        image = { imageCache.getLocalThumbnail(mediaMetadata.localPath, true) },
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    AsyncImage(
                        model = mediaMetadata.thumbnailUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )
                }
                
                // Overlay para dar profundidade
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.1f)
                                ),
                                radius = 150f
                            )
                        )
                )
            }
        }
        
        // Indicador de erro/carregamento
        androidx.compose.animation.AnimatedVisibility(
            visible = error != null || isWaitingForNetwork,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                if (isWaitingForNetwork) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayPauseButton(
    isPlaying: Boolean,
    playbackState: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )
    
    Box(
        modifier = modifier
            .size(44.dp)
            .scale(buttonScale),
        contentAlignment = Alignment.Center
    ) {
        // Pulse effect quando tocando
        if (isPlaying) {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.1f,
                targetValue = 0.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseAlpha"
            )
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(1.2f)
                    .clip(CircleShape)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha)
                    )
            )
        }
        
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            onClick = onClick
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = when {
                        playbackState == Player.STATE_ENDED -> Icons.Rounded.Replay
                        isPlaying -> Icons.Rounded.Pause
                        else -> Icons.Rounded.PlayArrow
                    },
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
} 