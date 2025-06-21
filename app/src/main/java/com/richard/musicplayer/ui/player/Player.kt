/*
 * Copyright (C) 2024 z-huang/InnerTune
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.richard.musicplayer.ui.player

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.os.PowerManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.DisposableEffect
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.media3.common.C
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Player.STATE_READY
import androidx.navigation.NavController
import coil.ImageLoader
import coil.Coil
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.CachePolicy
import com.richard.musicplayer.LocalPlayerConnection
import com.richard.musicplayer.R
import com.richard.musicplayer.constants.DarkMode
import com.richard.musicplayer.constants.DarkModeKey
import com.richard.musicplayer.constants.PlayerBackgroundStyle
import com.richard.musicplayer.constants.PlayerBackgroundStyleKey
import com.richard.musicplayer.constants.PlayerHorizontalPadding
import com.richard.musicplayer.constants.QueuePeekHeight
import com.richard.musicplayer.constants.ShowLyricsKey
import com.richard.musicplayer.constants.SwipeToSkip
import com.richard.musicplayer.extensions.metadata
import com.richard.musicplayer.extensions.togglePlayPause
import com.richard.musicplayer.extensions.toggleShuffleMode
import com.richard.musicplayer.extensions.toggleRepeatMode
import com.richard.musicplayer.models.MediaMetadata
import com.richard.musicplayer.playback.isShuffleEnabled
import com.richard.musicplayer.ui.component.AsyncImageLocal
import com.richard.musicplayer.ui.component.BottomSheet
import com.richard.musicplayer.ui.component.BottomSheetState
import com.richard.musicplayer.ui.component.LocalMenuState
import com.richard.musicplayer.ui.component.PlayerSliderTrack
import com.richard.musicplayer.ui.component.rememberBottomSheetState
import com.richard.musicplayer.ui.component.AudioVisualizer
import com.richard.musicplayer.ui.component.AnimatedAlbumArt

import com.richard.musicplayer.ui.component.SpatialAudioControl
import com.richard.musicplayer.ui.component.Lyrics
import com.richard.musicplayer.ui.component.AnimatedFlashIcon
import com.richard.musicplayer.playback.AudioEffects
import com.richard.musicplayer.playback.SpatialAudio
import com.richard.musicplayer.playback.SpatialRoomType
import com.richard.musicplayer.ui.menu.PlayerMenu
import com.richard.musicplayer.ui.theme.extractGradientColors
import com.richard.musicplayer.ui.theme.GradientSystem
import com.richard.musicplayer.ui.utils.SnapLayoutInfoProvider
import com.richard.musicplayer.ui.utils.imageCache
import com.richard.musicplayer.utils.ApplyPerformanceOptimizations
import com.richard.musicplayer.utils.rememberOptimizedDuration
import com.richard.musicplayer.utils.makeTimeString
import com.richard.musicplayer.utils.rememberEnumPreference
import com.richard.musicplayer.utils.rememberPreference
import com.richard.musicplayer.utils.GradientCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun BottomSheetPlayer(
    state: BottomSheetState,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val menuState = LocalMenuState.current
    val context = LocalContext.current
    
    // Aplicar otimizações de performance ultra para 60/90/120 FPS
    ApplyPerformanceOptimizations()
    
    // Durações de animação otimizadas baseadas na taxa de atualização
    val fastAnim = rememberOptimizedDuration(200)
    val mediumAnim = rememberOptimizedDuration(400)
    val slowAnim = rememberOptimizedDuration(600)

    val playbackState by playerConnection.playbackState.collectAsState()
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val repeatMode by playerConnection.repeatMode.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val currentSong by playerConnection.currentSong.collectAsState(initial = null)

    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()

    val thumbnailLazyGridState = rememberLazyGridState()

    val swipeToSkip by rememberPreference(SwipeToSkip, defaultValue = true)
    val previousMediaMetadata = if (swipeToSkip && playerConnection.player.hasPreviousMediaItem()) {
        val previousIndex = playerConnection.player.previousMediaItemIndex
        playerConnection.player.getMediaItemAt(previousIndex).metadata
    } else null

    val nextMediaMetadata = if (swipeToSkip && playerConnection.player.hasNextMediaItem()) {
        val nextIndex = playerConnection.player.nextMediaItemIndex
        playerConnection.player.getMediaItemAt(nextIndex).metadata
    } else null

    val mediaItems = listOfNotNull(previousMediaMetadata, mediaMetadata, nextMediaMetadata)
    val currentMediaIndex = mediaItems.indexOf(mediaMetadata)

    val currentItem by remember { derivedStateOf { thumbnailLazyGridState.firstVisibleItemIndex } }
    val itemScrollOffset by remember { derivedStateOf { thumbnailLazyGridState.firstVisibleItemScrollOffset } }

    LaunchedEffect(itemScrollOffset) {
        if (!thumbnailLazyGridState.isScrollInProgress || !swipeToSkip || itemScrollOffset != 0) return@LaunchedEffect

        if (currentItem > currentMediaIndex)
            playerConnection.player.seekToNext()
        else if (currentItem < currentMediaIndex)
            playerConnection.player.seekToPreviousMediaItem()
    }

    LaunchedEffect(mediaMetadata, canSkipPrevious, canSkipNext) {
        // When the media item changes, scroll to it
        val index = maxOf(0, currentMediaIndex)

        // Only animate scroll when player expanded, otherwise animated scroll won't work
        if (state.isExpanded)
            thumbnailLazyGridState.animateScrollToItem(index)
        else
            thumbnailLazyGridState.scrollToItem(index)
    }

    val horizontalLazyGridItemWidthFactor = 1f
    val thumbnailSnapLayoutInfoProvider = remember(thumbnailLazyGridState) {
        SnapLayoutInfoProvider(
            lazyGridState = thumbnailLazyGridState,
            positionInLayout = { layoutSize, itemSize ->
                (layoutSize * horizontalLazyGridItemWidthFactor / 2f - itemSize / 2f)
            }
        )
    }

    val playerBackground by rememberEnumPreference(key = PlayerBackgroundStyleKey, defaultValue = PlayerBackgroundStyle.BLUR)

    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
        if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
    }

    val onBackgroundColor = when (playerBackground) {
        PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.secondary
        else ->
            if (useDarkTheme)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onPrimary
    }

    var showLyrics by rememberPreference(ShowLyricsKey, defaultValue = false)

    var position by rememberSaveable(playbackState) {
        mutableLongStateOf(playerConnection.player.currentPosition)
    }
    var duration by rememberSaveable(playbackState) {
        mutableLongStateOf(playerConnection.player.duration)
    }
    var sliderPosition by remember {
        mutableStateOf<Long?>(null)
    }

    var gradientColors by remember {
        mutableStateOf<List<Color>>(emptyList())
    }

    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

    val scope = rememberCoroutineScope()
    
    // Controle de velocidade de reprodução
    var playbackSpeed by rememberPreference(androidx.datastore.preferences.core.floatPreferencesKey("playback_speed"), defaultValue = 1.0f)
    val isSpeedBoostActive = playbackSpeed > 1.0f
    
    // 🚀 CACHE DE GRADIENTES para evitar recálculos custosos
    val gradientCache = remember { mutableMapOf<String, List<Color>>() }
    
    // Visualizer sempre ativo
    val showVisualizer = true
    
    // Audio effects and visualizer - Get session ID dynamically
    val audioSessionId by remember {
        derivedStateOf { 
            playerConnection.player.audioSessionId
        }
    }
    
    val audioEffects = remember(audioSessionId) {
        try {
            // Tentar com o sessionId atual primeiro
            if (audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                AudioEffects.getInstance(audioSessionId)
            } else {
                // Se não tiver sessionId válido, tentar com 0 (global session)
                AudioEffects.getInstance(0)
            }
        } catch (e: Exception) {
            try {
                // Fallback: tentar com sessionId 0 (global)
                AudioEffects.getInstance(0)
            } catch (fallbackE: Exception) {
                null
            }
        }
    }
    
    // Audio data for visualizations
    val audioData by audioEffects?.waveform?.collectAsState() ?: remember { mutableStateOf(ByteArray(0)) }
    val frequencyData by audioEffects?.frequencyBands?.collectAsState() ?: remember { mutableStateOf(FloatArray(0)) }
    val amplitudeData by audioEffects?.amplitude?.collectAsState() ?: remember { mutableStateOf(0f) }
    
    // Apply playback speed when changed
    LaunchedEffect(playbackSpeed) {
        try {
            val playbackParameters = androidx.media3.common.PlaybackParameters(playbackSpeed)
            playerConnection.player.setPlaybackParameters(playbackParameters)
        } catch (e: Exception) {
            // silent fail
        }
    }

    
    // Cleanup audio effects
    DisposableEffect(Unit) {
        onDispose {
            AudioEffects.releaseInstance()
        }
    }

    // OTIMIZAÇÃO: Sistema de gradientes otimizado que segue as cores dos álbuns
    LaunchedEffect(mediaMetadata) {
        if (powerManager.isPowerSaveMode) return@LaunchedEffect

        val metadata = mediaMetadata ?: return@LaunchedEffect
        
        // 🚀 CACHE KEY: Criar chave única para cache
        val cacheKey = "${metadata.id}_${metadata.thumbnailUrl?.hashCode() ?: "null"}"
        
        // 🚀 VERIFICAR CACHE PRIMEIRO - evita reprocessamento custoso
        gradientCache[cacheKey]?.let { cachedColors ->
            gradientColors = cachedColors
            return@LaunchedEffect
        }

        withContext(Dispatchers.IO) {
            try {
                val extractedColors = when {
                    metadata.isLocal == true && !metadata.localPath.isNullOrEmpty() -> {
                        // Para música local - usar thumbnail local
                        val thumbnail = imageCache.getLocalThumbnail(metadata.localPath)
                        thumbnail?.extractGradientColors() ?: getDefaultColorsForAlbum(metadata)
                    }
                    !metadata.thumbnailUrl.isNullOrEmpty() -> {
                        // 🚀 PERFORMANCE: Tamanho reduzido + cache agressivo
                        val result = Coil.imageLoader(context).execute(
                            ImageRequest.Builder(context)
                                .data(metadata.thumbnailUrl)
                                .size(120, 120) // Reduzido de 200x200 para 120x120 
                                .allowHardware(false)
                                .memoryCachePolicy(CachePolicy.ENABLED) // Cache agressivo
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .build()
                        )
                        (result.drawable as? BitmapDrawable)?.bitmap?.extractGradientColors()
                            ?: getDefaultColorsForAlbum(metadata)
                    }
                    else -> getDefaultColorsForAlbum(metadata)
                }
                
                // 🚀 APLICAR E CACHEAR cores
                gradientColors = extractedColors
                gradientCache[cacheKey] = extractedColors
                
                // 🚀 LIMPEZA DE CACHE: Manter apenas os 30 mais recentes
                if (gradientCache.size > 30) {
                    val keysToRemove = gradientCache.keys.take(gradientCache.size - 25)
                    keysToRemove.forEach { gradientCache.remove(it) }
                }
                
            } catch (e: Exception) {
                val defaultColors = getDefaultColorsForAlbum(metadata)
                gradientColors = defaultColors
                gradientCache[cacheKey] = defaultColors
            }
        }
    }



    // 🚀 POSITION UPDATE OTIMIZADO: Reduzido polling para economizar CPU
    LaunchedEffect(playbackState) {
        if (playbackState == STATE_READY) {
            while (isActive) {
                delay(1500) // Aumentado de 1000ms para 1500ms - 50% menos CPU
                position = playerConnection.player.currentPosition
                duration = playerConnection.player.duration
            }
        }
    }

    val queueSheetState = rememberBottomSheetState(
        dismissedBound = QueuePeekHeight + WindowInsets.systemBars.asPaddingValues().calculateBottomPadding(),
        expandedBound = state.expandedBound,
    )


    BottomSheet(
        state = state,
        modifier = modifier,
        backgroundColor = if (useDarkTheme || playerBackground == PlayerBackgroundStyle.DEFAULT) {
            MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation)
        } else MaterialTheme.colorScheme.onSurfaceVariant,
        collapsedBackgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(NavigationBarDefaults.Elevation),
        onDismiss = {
            playerConnection.player.stop()
            playerConnection.player.clearMediaItems()
            playerConnection.service.deInitQueue()
        },
        collapsedContent = {
            VinylMiniPlayer(
                position = position,
                duration = duration,
                onExpand = { state.expandSoft() }
            )
        }
    ) {
        val actionButtons: @Composable RowScope.() -> Unit = {
            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .offset(y = 5.dp)
                    .size(36.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable { playerConnection.toggleLike() }
            ) {
                Icon(
                    painter = painterResource(
                        if (currentSong?.song?.liked == true) R.drawable.favorite else R.drawable.favorite_border
                    ),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(7.dp))

            Box(
                modifier = Modifier
                    .offset(y = 5.dp)
                    .size(36.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable {
                        menuState.show {
                            PlayerMenu(
                                mediaMetadata = mediaMetadata,
                                navController = navController,
                                playerBottomSheetState = state,
                                onDismiss = menuState::dismiss
                            )
                        }
                    }
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center)
                )
            }
        }

        val controlsContent: @Composable ColumnScope.(MediaMetadata) -> Unit = { mediaMetadata ->

            // action buttons for landscape (above title)
            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Row (
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = PlayerHorizontalPadding, end = PlayerHorizontalPadding, bottom = 16.dp)
                ) {
                    actionButtons()
                }
            }

            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding)
            ) {
                Row {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = mediaMetadata.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = onBackgroundColor,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .basicMarquee(
                                    iterations = 1,
                                    initialDelayMillis = 3000
                                )
                                .clickable(enabled = mediaMetadata.album != null) {
                                    navController.navigate("album/${mediaMetadata.album!!.id}")
                                    state.collapseSoft()
                                }
                        )

                        Row {
                            mediaMetadata.artists.fastForEachIndexed { index, artist ->
                                Text(
                                    text = artist.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = onBackgroundColor,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .basicMarquee(
                                            iterations = 1,
                                            initialDelayMillis = 5000
                                        )
                                        .clickable(enabled = artist.id != null) {
                                        navController.navigate("artist/${artist.id}")
                                        state.collapseSoft()
                                    }
                                )

                                if (index != mediaMetadata.artists.lastIndex) {
                                    Text(
                                        text = ", ",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = onBackgroundColor
                                    )
                                }
                            }
                        }
                    }

                    // action buttons for portrait (inline with title)
                    if (LocalConfiguration.current.orientation != Configuration.ORIENTATION_LANDSCAPE) {
                        actionButtons()
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Slider(
                value = (sliderPosition ?: position).toFloat(),
                valueRange = 0f..(if (duration == C.TIME_UNSET) 0f else duration.toFloat()),
                onValueChange = {
                    sliderPosition = it.toLong()
                    // slider too granular for this haptic to feel right
//                    haptic.performHapticFeedback(HapticFeedbackType.SegmentFrequentTick)
                },
                onValueChangeFinished = {
                    sliderPosition?.let {
                        playerConnection.player.seekTo(it)
                        position = it
                    }
                    sliderPosition = null
                    haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                },
                thumb = { Spacer(modifier = Modifier.size(0.dp)) },
                track = { sliderState ->
                    PlayerSliderTrack(
                        sliderState = sliderState,
                        colors = SliderDefaults.colors()
                    )
                },
                modifier = Modifier.padding(horizontal = PlayerHorizontalPadding)
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding + 4.dp)
            ) {
                Text(
                    text = makeTimeString(sliderPosition ?: position),
                    style = MaterialTheme.typography.labelMedium,
                    color = onBackgroundColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = if (duration != C.TIME_UNSET) makeTimeString(duration) else "",
                    style = MaterialTheme.typography.labelMedium,
                    color = onBackgroundColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }





            Spacer(Modifier.height(12.dp))

            // Estados dos controles
            val shuffleModeEnabled by isShuffleEnabled.collectAsState()

            // Sistema de gradientes perfeitos baseado nas cores da música
            val primaryDynamicColor = if (gradientColors.isNotEmpty()) gradientColors[0] else Color.White.copy(alpha = 0.9f)
            val secondaryDynamicColor = if (gradientColors.size > 1) gradientColors[1] else Color.White.copy(alpha = 0.7f)
            val accentDynamicColor = if (gradientColors.size > 2) gradientColors[2] else Color.White.copy(alpha = 0.5f)

            // Controles com gradientes cinematográficos perfeitos
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PlayerHorizontalPadding)
            ) {
                // Botão Speed Boost com gradiente perfeito
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            GradientSystem.createButtonGradient(
                                primaryColor = primaryDynamicColor,
                                isActive = isSpeedBoostActive,
                                intensity = 1f
                            )
                        )
                        .clickable {
                            // Toggle entre velocidade normal (1x) e velocidade rápida (2x)
                            playbackSpeed = if (playbackSpeed > 1.0f) 1.0f else 2.0f
                            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedFlashIcon(
                        size = 24.dp,
                        color = if (isSpeedBoostActive) Color.White else onBackgroundColor,
                        isActive = isSpeedBoostActive
                    )
                }

                // Botão Previous moderno
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = if (canSkipPrevious) 0.1f else 0.05f),
                                    Color.Transparent
                                )
                            )
                        )
                        .clickable(enabled = canSkipPrevious) {
                            playerConnection.player.seekToPrevious()
                            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = null,
                        tint = onBackgroundColor.copy(alpha = if (canSkipPrevious) 1f else 0.4f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Botão Play/Pause principal com gradiente cinematográfico perfeito
                Box(
                    modifier = Modifier.size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                GradientSystem.createCinematicGradient(
                                    colors = gradientColors,
                                    intensity = 1.2f,
                                    angle = 45f
                                )
                            )
                            .clickable {
                                if (playbackState == STATE_ENDED) {
                                    playerConnection.player.seekTo(0, 0)
                                    playerConnection.player.playWhenReady = true
                                } else {
                                    playerConnection.player.togglePlayPause()
                                }
                                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
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
                                        radius = 60f
                                    ),
                                    shape = CircleShape
                                )
                        )
                        
                        Icon(
                            imageVector = if (playbackState == STATE_ENDED) Icons.Rounded.Replay 
                                else if (isPlaying) Icons.Rounded.Pause 
                                else Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Botão Next moderno
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = if (canSkipNext) 0.1f else 0.05f),
                                    Color.Transparent
                                )
                            )
                        )
                        .clickable(enabled = canSkipNext) {
                            playerConnection.player.seekToNext()
                            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = null,
                        tint = onBackgroundColor.copy(alpha = if (canSkipNext) 1f else 0.4f),
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Botão Repeat com gradiente perfeito
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            GradientSystem.createButtonGradient(
                                primaryColor = primaryDynamicColor,
                                isActive = repeatMode != REPEAT_MODE_OFF,
                                intensity = 1f
                            )
                        )
                        .clickable {
                            playerConnection.player.toggleRepeatMode()
                            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (repeatMode) {
                            REPEAT_MODE_ONE -> Icons.Rounded.RepeatOne
                            else -> Icons.Rounded.Repeat
                        },
                        contentDescription = null,
                        tint = if (repeatMode != REPEAT_MODE_OFF) Color.White else onBackgroundColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = !powerManager.isPowerSaveMode && state.isExpanded,
            enter = fadeIn(tween(500)),
            exit = fadeOut(tween(500))
        ) {
            AnimatedContent(
                targetState = mediaMetadata,
                transitionSpec = {
                    fadeIn(tween(1000)).togetherWith(fadeOut(tween(1000)))
                }
            ) { metadata ->
                if (playerBackground == PlayerBackgroundStyle.BLUR) {
                    if (metadata?.isLocal == true) {
                        metadata.let {
                            AsyncImageLocal(
                                image = { imageCache.getLocalThumbnail(it.localPath) },
                                contentDescription = null,
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .blur(200.dp)
                            )
                        }
                    } else {
                        AsyncImage(
                            model = metadata?.thumbnailUrl,
                            contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .fillMaxSize()
                                .blur(200.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                    )
                }
            }

            AnimatedContent(
                targetState = gradientColors,
                transitionSpec = {
                    fadeIn(tween(1000)).togetherWith(fadeOut(tween(1000)))
                }
            ) { colors ->
                if (playerBackground == PlayerBackgroundStyle.GRADIENT && colors.size >= 2) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(colors), alpha = 0.8f)
                    )
                }
            }

            if (playerBackground != PlayerBackgroundStyle.DEFAULT && showLyrics) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                )
            }
        }

        when (LocalConfiguration.current.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> {
                Row(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                        .padding(bottom = queueSheetState.collapsedBound)
                        .fillMaxSize()
                ) {
                    BoxWithConstraints(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .nestedScroll(state.preUpPostDownNestedScrollConnection)
                    ) {
                        val horizontalLazyGridItemWidth = maxWidth * horizontalLazyGridItemWidthFactor

                        LazyHorizontalGrid(
                            state = thumbnailLazyGridState,
                            rows = GridCells.Fixed(1),
                            flingBehavior = rememberSnapFlingBehavior(thumbnailSnapLayoutInfoProvider),
                            userScrollEnabled = state.isExpanded && swipeToSkip
                        ) {
                            items(
                                items = mediaItems,
                                key = { it.id }
                            ) {
                                AnimatedAlbumArt(
                                    albumArtUrl = it.thumbnailUrl,
                                    isPlaying = isPlaying && it == mediaMetadata,
                                    audioData = if (it == mediaMetadata) audioData else null,
                                    modifier = Modifier
                                        .width(horizontalLazyGridItemWidth)
                                        .aspectRatio(1f)
                                        .animateContentSize()
                                        .padding(16.dp)
                                        .clickable {
                                            if (it == mediaMetadata) {
                                                // Toggle lyrics display
                                                showLyrics = !showLyrics
                                                haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                            }
                                        }
                                )
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(if (showLyrics) 0.4f else 1f, false)
                            .animateContentSize()
                            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top))
                    ) {
                        Spacer(Modifier.weight(1f))

                        mediaMetadata?.let {
                            controlsContent(it)
                        }

                        Spacer(Modifier.weight(1f))
                    }

                    // Exibir letras no modo landscape
                    AnimatedVisibility(
                        visible = showLyrics,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                        modifier = Modifier.weight(0.5f)
                    ) {
                        Lyrics(
                            sliderPositionProvider = { 
                                // Garantir sincronização perfeita
                                sliderPosition ?: playerConnection.player.currentPosition.takeIf { it >= 0 }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Horizontal))
                        .padding(bottom = queueSheetState.collapsedBound)
                ) {
                    Spacer(Modifier.height(8.dp))
                    
                    BoxWithConstraints(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(0.6f)
                            .nestedScroll(state.preUpPostDownNestedScrollConnection)
                    ) {
                        val horizontalLazyGridItemWidth = maxWidth * horizontalLazyGridItemWidthFactor

                        LazyHorizontalGrid(
                            state = thumbnailLazyGridState,
                            rows = GridCells.Fixed(1),
                            flingBehavior = rememberSnapFlingBehavior(thumbnailSnapLayoutInfoProvider),
                            userScrollEnabled = swipeToSkip && state.isExpanded
                        ) {
                            items(
                                items = mediaItems,
                                key = { it.id }
                            ) {
                                AnimatedAlbumArt(
                                    albumArtUrl = it.thumbnailUrl,
                                    isPlaying = isPlaying && it == mediaMetadata,
                                    audioData = if (it == mediaMetadata) audioData else null,
                                    modifier = Modifier
                                        .width(horizontalLazyGridItemWidth)
                                        .aspectRatio(1f)
                                        .animateContentSize()
                                        .padding(8.dp)
                                        .clickable {
                                            if (it == mediaMetadata) {
                                                // Toggle lyrics display
                                                showLyrics = !showLyrics
                                                haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                                            }
                                        }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    mediaMetadata?.let {
                        controlsContent(it)
                    }

                    Spacer(Modifier.height(12.dp))

                    // Exibir letras no modo retrato
                    AnimatedVisibility(
                        visible = showLyrics,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                        modifier = Modifier.weight(0.35f)
                    ) {
                        Lyrics(
                            sliderPositionProvider = { 
                                // Garantir sincronização perfeita
                                sliderPosition ?: playerConnection.player.currentPosition.takeIf { it >= 0 }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        Queue(
            state = queueSheetState,
            playerBottomSheetState = state,
            onTerminate = {
                state.dismiss()
                playerConnection.service.queueBoard.detachedHead = false
            },
            onBackgroundColor = onBackgroundColor,
            navController = navController
        )
    }
}

// Função auxiliar para cores padrão baseadas no álbum
private fun getDefaultColorsForAlbum(metadata: MediaMetadata): List<Color> {
    return try {
        // 🚀 CORREÇÃO: Gerar cores seguras para evitar crash
        val hash = (metadata.title + metadata.artists.firstOrNull()?.name.orEmpty()).hashCode()
        val hue = kotlin.math.abs(hash % 360).toFloat() // Garantir valor positivo
        
        listOf(
            Color.hsv(hue, 0.7f, 0.8f),
            Color.hsv((hue + 30f) % 360f, 0.6f, 0.7f), // Forçar float para evitar overflow
            Color.hsv((hue + 60f) % 360f, 0.5f, 0.6f)
        )
    } catch (e: Exception) {
        // 🚀 FALLBACK SEGURO: Cores fixas se houver qualquer erro
        listOf(
            Color(0xFF6200EE),
            Color(0xFF3700B3), 
            Color(0xFF03DAC6)
        )
    }
}
