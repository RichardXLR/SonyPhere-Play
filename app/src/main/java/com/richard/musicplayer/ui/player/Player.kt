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
import android.os.PowerManager
import android.util.Log
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
import com.richard.musicplayer.ui.component.BassControl
import com.richard.musicplayer.ui.component.SpatialAudioControl
import com.richard.musicplayer.ui.component.Lyrics
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

    val playerBackground by rememberEnumPreference(key = PlayerBackgroundStyleKey, defaultValue = PlayerBackgroundStyle.GRADIENT)

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

    // Bass control state
    var showBassControl by remember { mutableStateOf(false) }
    var bassLevel by rememberPreference(androidx.datastore.preferences.core.floatPreferencesKey("bass_level"), defaultValue = 0f)
    
    // Visualizer sempre ativo
    val showVisualizer = true
    
    // Audio effects and visualizer - Get session ID dynamically
    val audioSessionId by remember {
        derivedStateOf { 
            val sessionId = playerConnection.player.audioSessionId
            Log.d("Player", "Audio Session ID: $sessionId")
            sessionId
        }
    }
    
    val audioEffects = remember(audioSessionId) {
        Log.d("Player", "Initializing AudioEffects with session ID: $audioSessionId")
        if (audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
            try {
                AudioEffects.getInstance(audioSessionId)
            } catch (e: Exception) {
                Log.e("Player", "Failed to initialize AudioEffects", e)
                null
            }
        } else {
            Log.w("Player", "Audio session ID is unset, skipping AudioEffects initialization")
            null
        }
    }
    
    // Audio data for visualizations
    val audioData by audioEffects?.waveform?.collectAsState() ?: remember { mutableStateOf(ByteArray(0)) }
    val frequencyData by audioEffects?.frequencyBands?.collectAsState() ?: remember { mutableStateOf(FloatArray(0)) }
    val amplitudeData by audioEffects?.amplitude?.collectAsState() ?: remember { mutableStateOf(0f) }
    
    // Update bass level when changed
    LaunchedEffect(bassLevel) {
        audioEffects?.setBassLevel(bassLevel)
    }
    

    
    // Cleanup audio effects
    DisposableEffect(Unit) {
        onDispose {
            AudioEffects.releaseInstance()
        }
    }

    // Sistema de extração inteligente de cores da música atual
    LaunchedEffect(mediaMetadata) {
        if (powerManager.isPowerSaveMode) return@LaunchedEffect

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
                
                // Garantir que sempre temos cores extraídas da música
                extractedColors?.takeIf { it.isNotEmpty() }?.let {
                    gradientColors = it
                }
            } catch (e: Exception) {
                // Em caso de erro, manter cores neutras
                gradientColors = emptyList()
            }
        }
    }

    LaunchedEffect(playbackState) {
        if (playbackState == STATE_READY) {
            while (isActive) {
                delay(500)
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
            MiniPlayer(
                position = position,
                duration = duration
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



            // Bass Control
            AnimatedVisibility(
                visible = showBassControl,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                BassControl(
                    bassLevel = bassLevel,
                    onBassLevelChange = { newLevel ->
                        bassLevel = newLevel
                    },
                    isExpanded = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = PlayerHorizontalPadding, vertical = 8.dp)
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
                // Botão Equalizer com gradiente perfeito
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            GradientSystem.createButtonGradient(
                                primaryColor = primaryDynamicColor,
                                isActive = showBassControl,
                                intensity = 1f
                            )
                        )
                        .clickable {
                            showBassControl = !showBassControl
                            haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Equalizer,
                        contentDescription = null,
                        tint = if (showBassControl) Color.White else onBackgroundColor,
                        modifier = Modifier.size(24.dp)
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
