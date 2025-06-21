/*
 * Copyright (C) 2024 z-huang/InnerTune
 * Copyright (C) 2025 OuterTune Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.richard.musicplayer.ui.component

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.platform.LocalConfiguration

import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.richard.musicplayer.LocalPlayerConnection
import com.richard.musicplayer.R
import android.util.Log
import com.richard.musicplayer.constants.DarkMode
import com.richard.musicplayer.constants.DarkModeKey
import com.richard.musicplayer.constants.LyricFontSizeKey
import com.richard.musicplayer.constants.LyricTrimKey
import com.richard.musicplayer.constants.LyricsTextPositionKey
import com.richard.musicplayer.constants.MultilineLrcKey
import com.richard.musicplayer.constants.PlayerBackgroundStyle
import com.richard.musicplayer.constants.PlayerBackgroundStyleKey
import com.richard.musicplayer.constants.ShowLyricsKey
import com.richard.musicplayer.db.entities.LyricsEntity.Companion.LYRICS_NOT_FOUND
import com.richard.musicplayer.lyrics.LyricsEntry
import com.richard.musicplayer.lyrics.LyricsEntry.Companion.HEAD_LYRICS_ENTRY
import com.richard.musicplayer.lyrics.LyricsUtils
import com.richard.musicplayer.lyrics.LyricsUtils.findCurrentLineIndex
import com.richard.musicplayer.lyrics.LyricsUtils.loadAndParseLyricsString
import com.richard.musicplayer.ui.component.shimmer.ShimmerHost
import com.richard.musicplayer.ui.component.shimmer.TextPlaceholder
import com.richard.musicplayer.ui.menu.LyricsMenu
import com.richard.musicplayer.constants.LyricsPosition
import com.richard.musicplayer.ui.utils.fadingEdge
import com.richard.musicplayer.utils.rememberEnumPreference
import com.richard.musicplayer.utils.rememberPreference
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration.Companion.seconds

// OTIMIZAÇÃO: Removidas classes de partículas desnecessárias
@Composable
fun Lyrics(
    sliderPositionProvider: () -> Long?,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val menuState = LocalMenuState.current

    var showLyrics by rememberPreference(ShowLyricsKey, false)
    val landscapeOffset = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

    val lyricsTextPosition by rememberEnumPreference(LyricsTextPositionKey, LyricsPosition.CENTER)
    val lyricsFontSize by rememberPreference(LyricFontSizeKey, 20)
    
    // OTIMIZAÇÃO: Estados simplificados - removidos efeitos pesados
    var showControls by remember { mutableStateOf(true) }
    var lastInteractionTime by remember { mutableStateOf(System.currentTimeMillis()) }

    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val lyricsEntity by playerConnection.currentLyrics.collectAsState(initial = null)
    val lyrics = remember(lyricsEntity) { lyricsEntity?.lyrics?.trim() }
    val multilineLrc = rememberPreference(MultilineLrcKey, defaultValue = true)
    val lyricTrim = rememberPreference(LyricTrimKey, defaultValue = false)

    val playerBackground by rememberEnumPreference(key = PlayerBackgroundStyleKey, defaultValue = PlayerBackgroundStyle.BLUR)

    val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
    val isSystemInDarkTheme = isSystemInDarkTheme()
    val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
        if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
    }

    val lines: List<LyricsEntry> = remember(lyrics) {
        if (lyrics == null || lyrics == LYRICS_NOT_FOUND) {
            emptyList()
        } else if (lyrics.startsWith("[")) {
            listOf(HEAD_LYRICS_ENTRY) +
                loadAndParseLyricsString(lyrics, LyricsUtils.LrcParserOptions(lyricTrim.value, multilineLrc.value, "Unable to parse lyrics"))
        } else {
            lyrics.lines().mapIndexed { index, line -> LyricsEntry(index * 100L, line) }
        }
    }
    val isSynced = remember(lyrics) {
        !lyrics.isNullOrEmpty() && lyrics.startsWith("[")
    }

    val textColor = when (playerBackground) {
        PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.secondary
        else ->
            if (useDarkTheme)
                MaterialTheme.colorScheme.onSurface
            else
                MaterialTheme.colorScheme.onPrimary
    }

    var currentLineIndex by remember {
        mutableIntStateOf(-1)
    }
    var deferredCurrentLineIndex by rememberSaveable {
        mutableIntStateOf(0)
    }

    var lastPreviewTime by rememberSaveable {
        mutableLongStateOf(0L)
    }
    var isSeeking by remember {
        mutableStateOf(false)
    }

    // OTIMIZAÇÃO: Sincronização simplificada - delay aumentado para 200ms
    LaunchedEffect(lyrics, playerConnection.isPlaying.collectAsState().value) {
        if (lyrics.isNullOrEmpty() || !lyrics.startsWith("[")) {
            currentLineIndex = -1
            return@LaunchedEffect
        }
        
        while (isActive) {
            val sliderPosition = sliderPositionProvider()
            isSeeking = sliderPosition != null
            val playerPosition = playerConnection.player.currentPosition
            val actualPosition = sliderPosition ?: playerPosition
            
            if (actualPosition >= 0 && lines.isNotEmpty()) {
                // Busca otimizada: usar binary search para melhor performance
                val newIndex = lines.findCurrentLineIndex(actualPosition)
                
                if (newIndex != currentLineIndex) {
                    currentLineIndex = newIndex
                }
            }
            
            // OTIMIZAÇÃO: Delay aumentado para reduzir CPU usage
            delay(200) // Reduzido de 100ms para 200ms
        }
    }
    
    // OTIMIZAÇÃO: Auto-ocultar controles otimizado
    LaunchedEffect(lastInteractionTime) {
        delay(4000) // Aumentado para 4 segundos
        if (System.currentTimeMillis() - lastInteractionTime >= 4000) {
            showControls = false
        }
    }

    LaunchedEffect(isSeeking, lastPreviewTime) {
        if (isSeeking) {
            lastPreviewTime = 0L
        } else if (lastPreviewTime != 0L) {
            delay(LyricsPreviewTime)
            lastPreviewTime = 0L
        }
    }

    val lazyListState = rememberLazyListState()

    LaunchedEffect(currentLineIndex, lastPreviewTime) {
        if (!isSynced || currentLineIndex < 0 || currentLineIndex >= lines.size) return@LaunchedEffect
        
        deferredCurrentLineIndex = currentLineIndex
        
        if (lastPreviewTime == 0L) {
            try {
                if (isSeeking) {
                    lazyListState.scrollToItem(currentLineIndex)
                } else {
                    lazyListState.animateScrollToItem(index = currentLineIndex)
                }
            } catch (e: Exception) {
                // Ignore scroll errors
            }
        }
    }

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 4.dp)
            .pointerInput(Unit) {
                detectTapGestures { 
                    showControls = true
                    lastInteractionTime = System.currentTimeMillis()
                }
            }
    ) {
        // OTIMIZAÇÃO: Removido Canvas e efeitos visuais pesados
        
        LazyColumn(
            state = lazyListState,
            contentPadding = WindowInsets.systemBars
                .only(WindowInsetsSides.Top)
                .add(WindowInsets(top = maxHeight / 4, bottom = maxHeight / 4))
                .asPaddingValues(),
            modifier = Modifier
                .fadingEdge(vertical = 32.dp)
                .nestedScroll(remember {
                    object : NestedScrollConnection {
                        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                            lastPreviewTime = System.currentTimeMillis()
                            return super.onPostScroll(consumed, available, source)
                        }

                        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                            lastPreviewTime = System.currentTimeMillis()
                            return super.onPostFling(consumed, available)
                        }
                    }
                })
        ) {
            val displayedCurrentLineIndex = if (isSeeking) deferredCurrentLineIndex else currentLineIndex

            if (lyrics == null) {
                item {
                    ShimmerHost {
                        repeat(10) {
                            Box(
                                contentAlignment = when (lyricsTextPosition) {
                                    LyricsPosition.LEFT -> Alignment.CenterStart
                                    LyricsPosition.CENTER -> Alignment.Center
                                    LyricsPosition.RIGHT -> Alignment.CenterEnd
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 2.dp)
                            ) {
                                TextPlaceholder()
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(
                    items = lines
                ) { index, item ->
                    val isCurrentLine = index == displayedCurrentLineIndex
                    
                    // OTIMIZAÇÃO: Animações simplificadas - apenas essenciais
                    val textColorAnimated by animateColorAsState(
                        targetValue = if (isCurrentLine) {
                            when (playerBackground) {
                                PlayerBackgroundStyle.DEFAULT -> MaterialTheme.colorScheme.primary
                                else -> 
                                    if (useDarkTheme) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        } else textColor,
                        animationSpec = tween(durationMillis = 300), // Reduzido de 400ms
                        label = "textColor"
                    )
                    
                    // OTIMIZAÇÃO: Removidos efeitos de escala, brilho e background
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = isSynced) {
                                playerConnection.player.seekTo(item.timeStamp)
                                lastPreviewTime = 0L
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = item.content,
                            fontSize = if (isCurrentLine) (lyricsFontSize + 2).sp else lyricsFontSize.sp,
                            color = textColorAnimated,
                            textAlign = when (lyricsTextPosition) {
                                LyricsPosition.LEFT -> TextAlign.Left
                                LyricsPosition.CENTER -> TextAlign.Center
                                LyricsPosition.RIGHT -> TextAlign.Right
                            },
                            fontWeight = if (isCurrentLine) FontWeight.Bold else FontWeight.Normal,
                            lineHeight = if (isCurrentLine) (lyricsFontSize + 4).sp else (lyricsFontSize + 2).sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .alpha(if (!isSynced || isCurrentLine) 1f else 0.7f) // Simplificado
                        )
                    }
                }
            }
        }

        if (lyrics == LYRICS_NOT_FOUND) {
            Text(
                text = stringResource(R.string.lyrics_not_found),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = when (lyricsTextPosition) {
                    LyricsPosition.LEFT -> TextAlign.Left
                    LyricsPosition.CENTER -> TextAlign.Center
                    LyricsPosition.RIGHT -> TextAlign.Right
                },
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .alpha(0.6f)
            )
        }

        // OTIMIZAÇÃO: Controles simplificados
        mediaMetadata?.let { mediaMetadata ->
            AnimatedVisibility(
                visible = showControls,
                enter = fadeIn(animationSpec = tween(200)), // Reduzido de 300ms
                exit = fadeOut(animationSpec = tween(200)),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Row(
                    modifier = Modifier
                        .padding(
                            top = WindowInsets.systemBars
                                .only(WindowInsetsSides.Top)
                                .asPaddingValues()
                                .calculateTopPadding() + 8.dp,
                            end = 8.dp
                        )
                        .clip(CircleShape)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                            shape = CircleShape
                        )
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Menu de opções
                    IconButton(
                        onClick = {
                            lastInteractionTime = System.currentTimeMillis()
                            menuState.show {
                                LyricsMenu(
                                    lyricsProvider = { lyricsEntity },
                                    mediaMetadataProvider = { mediaMetadata },
                                    onDismiss = menuState::dismiss
                                )
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "More Options",
                            tint = textColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    
                    // Fechar
                    IconButton(
                        onClick = { showLyrics = false },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = textColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// OTIMIZAÇÃO: Removidas funções auxiliares pesadas de desenho

// OTIMIZAÇÃO: Função de busca otimizada usando binary search
fun List<LyricsEntry>.findCurrentLineIndex(position: Long): Int {
    if (isEmpty()) return -1
    
    var left = 0
    var right = size - 1
    var result = -1
    
    while (left <= right) {
        val mid = (left + right) / 2
        
        if (this[mid].timeStamp <= position) {
            result = mid
            left = mid + 1
        } else {
            right = mid - 1
        }
    }
    
    return result
}

// OTIMIZAÇÃO: Constantes simplificadas
const val animateScrollDuration = 150L // Reduzido de 200L
val LyricsPreviewTime = 1.5.seconds // Reduzido de 2 segundos
