/*
 * Copyright (C) 2024 z-huang/InnerTune
 * Copyright (C) 2025 O﻿ute﻿rTu﻿ne Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.richard.musicplayer.ui.player

import android.content.res.Configuration
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.OndemandVideo
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import coil.compose.AsyncImage
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.richard.musicplayer.LocalPlayerConnection
import com.richard.musicplayer.R
import com.richard.musicplayer.constants.MiniPlayerHeight
import com.richard.musicplayer.constants.ThumbnailCornerRadius
import com.richard.musicplayer.extensions.togglePlayPause
import com.richard.musicplayer.models.MediaMetadata
import com.richard.musicplayer.ui.component.AsyncImageLocal
import com.richard.musicplayer.ui.utils.imageCache
import com.richard.musicplayer.ui.theme.extractGradientColors
import com.richard.musicplayer.ui.theme.GradientSystem
import com.richard.musicplayer.utils.ApplyPerformanceOptimizations
import com.richard.musicplayer.utils.rememberOptimizedDuration

@Composable
fun MiniPlayer(
    position: Long,
    duration: Long,
    modifier: Modifier = Modifier,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val playbackState by playerConnection.playbackState.collectAsState()
    val error by playerConnection.error.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()
    val context = LocalContext.current

    // Aplicar otimizações de performance para fluidez ultra
    ApplyPerformanceOptimizations()
    
    // Durações otimizadas baseadas na taxa de atualização
    val animDuration = rememberOptimizedDuration(400)

    // Cores dinâmicas extraídas da capa do álbum
    var gradientColors by remember { mutableStateOf<List<Color>>(emptyList()) }
    
    // Sistema inteligente de extração de cores da música
    LaunchedEffect(mediaMetadata) {
        withContext(Dispatchers.IO) {
            try {
                val extractedColors = if (mediaMetadata?.isLocal == true) {
                    imageCache.getLocalThumbnail(mediaMetadata?.localPath)?.extractGradientColors()
                } else {
                    val result = ImageLoader(context).execute(
                        ImageRequest.Builder(context)
                            .data(mediaMetadata?.thumbnailUrl)
                            .allowHardware(false)
                            .build()
                    )
                    (result.drawable as? BitmapDrawable)?.bitmap?.extractGradientColors()
                }
                
                // Sempre usar cores extraídas da música quando disponível
                extractedColors?.takeIf { it.isNotEmpty() }?.let {
                    gradientColors = it
                }
            } catch (e: Exception) {
                // Manter lista vazia para usar fallback neutro
                gradientColors = emptyList()
            }
        }
    }

    // Animações
    val playPauseScale by animateFloatAsState(
        targetValue = if (isPlaying) 1.1f else 1f,
        animationSpec = spring(),
        label = "playPauseScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(MiniPlayerHeight)
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
                        MaterialTheme.colorScheme.surfaceColorAtElevation(4.dp)
                    )
                )
            )
    ) {
        // Progress bar com gradiente cinematográfico perfeito
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                .background(Color.White.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .fillMaxWidth((position.toFloat() / duration).coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                    .background(
                        GradientSystem.createProgressGradient(gradientColors)
                    )
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Box(Modifier.weight(1f)) {
                mediaMetadata?.let {
                    MiniMediaInfo(
                        mediaMetadata = it,
                        error = error,
                        modifier = Modifier
                    )
                }
            }

            // Visualizador de áudio animado
            if (isPlaying) {
                Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .size(24.dp)
                        .scale(scaleX = 1f, scaleY = playPauseScale)
                )
            }

            // Cores dinâmicas extraídas da capa da música atual
            val primaryDynamicColor = if (gradientColors.isNotEmpty()) gradientColors[0] else Color.White.copy(alpha = 0.9f)
            val secondaryDynamicColor = if (gradientColors.size > 1) gradientColors[1] else Color.White.copy(alpha = 0.7f)
            val accentDynamicColor = if (gradientColors.size > 2) gradientColors[2] else Color.White.copy(alpha = 0.5f)

            // Botão Play/Pause com gradiente cinematográfico perfeito
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                // Ondas animadas quando está tocando com cores da música
                if (isPlaying) {
                    repeat(2) { index ->
                        val scale = 1f + (playPauseScale - 1f) * (1f + index * 0.3f)
                        val alpha = (2f - playPauseScale) * (0.2f - index * 0.1f)
                        
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .scale(scale)
                                .background(
                                    color = primaryDynamicColor.copy(
                                        alpha = alpha.coerceIn(0f, 0.2f)
                                    ),
                                    shape = CircleShape
                                )
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .scale(playPauseScale)
                        .clip(CircleShape)
                        .background(
                            GradientSystem.createCinematicGradient(
                                colors = gradientColors,
                                intensity = 1.1f,
                                angle = 135f
                            )
                        )
                        .clickable {
                            if (playbackState == Player.STATE_ENDED) {
                                playerConnection.player.seekTo(0, 0)
                                playerConnection.player.playWhenReady = true
                            } else {
                                playerConnection.player.togglePlayPause()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Efeito glassmorphism aprimorado
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.25f),
                                        Color.White.copy(alpha = 0.1f),
                                        Color.Transparent
                                    ),
                                    radius = 35f
                                ),
                                shape = CircleShape
                            )
                    )
                    
                    Icon(
                        imageVector = if (playbackState == Player.STATE_ENDED) Icons.Rounded.Replay 
                            else if (isPlaying) Icons.Rounded.Pause 
                            else Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Botão Skip Next com gradiente perfeito
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        GradientSystem.createButtonGradient(
                            primaryColor = primaryDynamicColor,
                            isActive = canSkipNext,
                            intensity = 0.6f
                        )
                    )
                    .clickable(enabled = canSkipNext) { 
                        if (canSkipNext) playerConnection.player.seekToNext() 
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.skip_next),
                    contentDescription = null,
                    tint = if (canSkipNext) 
                        Color.White.copy(alpha = 0.9f)
                        else Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun MiniMediaInfo(
    mediaMetadata: MediaMetadata,
    error: PlaybackException?,
    modifier: Modifier = Modifier,
) {
    val playerConnection = LocalPlayerConnection.current
    val isWaitingForNetwork by playerConnection?.waitingForNetworkConnection?.collectAsState(initial = false)
        ?: remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .padding(end = 12.dp)
                .size(56.dp)
        ) {
            var isRectangularImage by remember { mutableStateOf(false) }

            if (mediaMetadata.isLocal) {
                // local thumbnail arts
                AsyncImageLocal(
                    image = { imageCache.getLocalThumbnail(mediaMetadata.localPath, true) },
                    contentDescription = null,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .aspectRatio(ratio = 1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            } else {
                // YTM thumbnail arts
                AsyncImage(
                    model = mediaMetadata.thumbnailUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    onSuccess = { success ->
                        val width = success.result.drawable.intrinsicWidth
                        val height = success.result.drawable.intrinsicHeight
                        isRectangularImage = width.toFloat() / height != 1f
                    },
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }

            if (isRectangularImage) {
                val videoIndicatorScale by animateFloatAsState(
                    targetValue = 1f,
                    animationSpec = spring(),
                    label = "videoIndicator"
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(24.dp)
                        .offset(x = -4.dp, y = -4.dp)
                        .scale(videoIndicatorScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.7f),
                                    Color.Black.copy(alpha = 0.3f)
                                )
                            )
                        )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.OndemandVideo,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = error != null || isWaitingForNetwork,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    Modifier
                        .background(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(ThumbnailCornerRadius)
                        )
                ) {
                    if (isWaitingForNetwork) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(24.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .align(Alignment.Center)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 6.dp)
        ) {
            Text(
                text = mediaMetadata.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = mediaMetadata.artists.joinToString { it.name },
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
