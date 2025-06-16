package com.dd3boh.outertune.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dd3boh.outertune.LocalPlayerConnection
import com.dd3boh.outertune.R
import com.dd3boh.outertune.db.entities.Song
import com.dd3boh.outertune.extensions.togglePlayPause
import com.dd3boh.outertune.models.toMediaMetadata
import com.dd3boh.outertune.playback.queues.ListQueue
import com.dd3boh.outertune.ui.component.*
import com.dd3boh.outertune.ui.menu.SongMenu
import com.dd3boh.outertune.utils.SmartRecommendationEngine
import com.dd3boh.outertune.viewmodels.SmartRecommendationsViewModel
import kotlinx.coroutines.launch
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SmartRecommendationsScreen(
    navController: NavController,
    viewModel: SmartRecommendationsViewModel = hiltViewModel()
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val haptic = LocalHapticFeedback.current
    val menuState = LocalMenuState.current
    
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    
    val recommendations by viewModel.recommendations.collectAsState()
    val moodRecommendations by viewModel.moodRecommendations.collectAsState()
    val timeRecommendations by viewModel.timeRecommendations.collectAsState()
    val discoveryRecommendations by viewModel.discoveryRecommendations.collectAsState()
    val selectedMood by viewModel.selectedMood.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    val moods = listOf(
        MoodItem("happy", "😊", stringResource(R.string.mood_happy), Color(0xFFFFD93D)),
        MoodItem("sad", "😢", stringResource(R.string.mood_sad), Color(0xFF6495ED)),
        MoodItem("energetic", "⚡", stringResource(R.string.mood_energetic), Color(0xFFFF6B6B)),
        MoodItem("relaxed", "😌", stringResource(R.string.mood_relaxed), Color(0xFF4ECDC4)),
        MoodItem("focused", "🎯", stringResource(R.string.mood_focused), Color(0xFF95E1D3))
    )
    
    val timeOfDay = remember {
        val hour = LocalTime.now().hour
        when (hour) {
            in 5..11 -> TimeOfDayInfo(
                title = "Good Morning",
                icon = Icons.Rounded.WbSunny,
                gradient = listOf(Color(0xFFFFE259), Color(0xFFFFA751))
            )
            in 12..16 -> TimeOfDayInfo(
                title = "Good Afternoon",
                icon = Icons.Rounded.WbTwilight,
                gradient = listOf(Color(0xFF89F7FE), Color(0xFF66A6FF))
            )
            in 17..20 -> TimeOfDayInfo(
                title = "Good Evening",
                icon = Icons.Rounded.Nightlight,
                gradient = listOf(Color(0xFFFA709A), Color(0xFFFEE140))
            )
            else -> TimeOfDayInfo(
                title = "Good Night",
                icon = Icons.Rounded.DarkMode,
                gradient = listOf(Color(0xFF667EEA), Color(0xFF764BA2))
            )
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // Header with time-based greeting
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(
                    Brush.verticalGradient(
                        colors = timeOfDay.gradient
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = timeOfDay.icon,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = timeOfDay.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.smart_recommendations_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
        
        // Mood selector
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.how_are_you_feeling),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(moods) { mood ->
                    MoodChip(
                        mood = mood,
                        isSelected = selectedMood == mood.id,
                        onClick = {
                            viewModel.selectMood(mood.id)
                        }
                    )
                }
            }
        }
        
        // Recommendations sections
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // Smart recommendations based on context
            if (recommendations.isNotEmpty()) {
                RecommendationSection(
                    title = stringResource(R.string.recommended_for_you),
                    songs = recommendations,
                    onSongClick = { song ->
                        playSong(song, playerConnection, mediaMetadata, isPlaying)
                    },
                    onSongLongClick = { song ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuState.show {
                            SongMenu(
                                originalSong = song,
                                navController = navController,
                                onDismiss = menuState::dismiss
                            )
                        }
                    },
                    mediaMetadata = mediaMetadata,
                    isPlaying = isPlaying
                )
            }
            
            // Mood-based recommendations
            if (moodRecommendations.isNotEmpty()) {
                RecommendationSection(
                    title = stringResource(
                        R.string.mood_recommendations,
                        moods.find { it.id == selectedMood }?.displayName ?: ""
                    ),
                    songs = moodRecommendations,
                    onSongClick = { song ->
                        playSong(song, playerConnection, mediaMetadata, isPlaying)
                    },
                    onSongLongClick = { song ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuState.show {
                            SongMenu(
                                originalSong = song,
                                navController = navController,
                                onDismiss = menuState::dismiss
                            )
                        }
                    },
                    mediaMetadata = mediaMetadata,
                    isPlaying = isPlaying
                )
            }
            
            // Time-based recommendations
            if (timeRecommendations.isNotEmpty()) {
                RecommendationSection(
                    title = stringResource(R.string.perfect_for_now),
                    songs = timeRecommendations,
                    onSongClick = { song ->
                        playSong(song, playerConnection, mediaMetadata, isPlaying)
                    },
                    onSongLongClick = { song ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuState.show {
                            SongMenu(
                                originalSong = song,
                                navController = navController,
                                onDismiss = menuState::dismiss
                            )
                        }
                    },
                    mediaMetadata = mediaMetadata,
                    isPlaying = isPlaying
                )
            }
            
            // Discovery recommendations
            if (discoveryRecommendations.isNotEmpty()) {
                RecommendationSection(
                    title = stringResource(R.string.discover_something_new),
                    songs = discoveryRecommendations,
                    onSongClick = { song ->
                        playSong(song, playerConnection, mediaMetadata, isPlaying)
                    },
                    onSongLongClick = { song ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuState.show {
                            SongMenu(
                                originalSong = song,
                                navController = navController,
                                onDismiss = menuState::dismiss
                            )
                        }
                    },
                    mediaMetadata = mediaMetadata,
                    isPlaying = isPlaying
                )
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun MoodChip(
    mood: MoodItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    Card(
        onClick = onClick,
        modifier = Modifier
            .scale(scale)
            .width(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) mood.color else MaterialTheme.colorScheme.surface
        ),
        border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = mood.emoji,
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = mood.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RecommendationSection(
    title: String,
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    onSongLongClick: (Song) -> Unit,
    mediaMetadata: com.dd3boh.outertune.models.MediaMetadata?,
    isPlaying: Boolean
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(songs) { song ->
                SongCard(
                    song = song,
                    onClick = { onSongClick(song) },
                    onLongClick = { onSongLongClick(song) },
                    isActive = song.id == mediaMetadata?.id,
                    isPlaying = isPlaying
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SongCard(
    song: Song,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    isActive: Boolean,
    isPlaying: Boolean
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(126.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                // Thumbnail would go here
                if (isActive && isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = Color.White
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = song.song.title,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
            )
            Text(
                text = song.artists.joinToString { it.name },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun playSong(
    song: Song,
    playerConnection: com.dd3boh.outertune.playback.PlayerConnection,
    currentMetadata: com.dd3boh.outertune.models.MediaMetadata?,
    isPlaying: Boolean
) {
    if (song.id == currentMetadata?.id) {
        playerConnection.player.togglePlayPause()
    } else {
        playerConnection.playQueue(
            ListQueue(
                title = "Smart Recommendations",
                items = listOf(song.toMediaMetadata())
            )
        )
    }
}

private data class MoodItem(
    val id: String,
    val emoji: String,
    val displayName: String,
    val color: Color
)

private data class TimeOfDayInfo(
    val title: String,
    val icon: ImageVector,
    val gradient: List<Color>
) 