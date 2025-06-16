package com.dd3boh.outertune.ui.screens

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.dd3boh.outertune.LocalDatabase
import com.dd3boh.outertune.LocalPlayerAwareWindowInsets
import com.dd3boh.outertune.LocalPlayerConnection
import com.dd3boh.outertune.R
import com.dd3boh.outertune.constants.*
import com.dd3boh.outertune.db.entities.*
import com.dd3boh.outertune.extensions.toMediaItem
import com.dd3boh.outertune.extensions.toMediaMetadata
import com.dd3boh.outertune.models.*
import com.dd3boh.outertune.playback.queues.ListQueue
import com.dd3boh.outertune.playback.queues.YouTubeAlbumRadio
import com.dd3boh.outertune.playback.queues.YouTubeQueue
import com.dd3boh.outertune.ui.component.*
import com.dd3boh.outertune.ui.menu.YouTubePlaylistMenu
import com.dd3boh.outertune.ui.utils.getLocalThumbnail
import com.dd3boh.outertune.viewmodels.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min
import kotlin.random.Random
import java.time.LocalTime

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
) {
    val menuState = LocalMenuState.current
    val database = LocalDatabase.current
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val quickPicks by database.quickPicks().collectAsState(initial = emptyList())
    val forgottenFavorites by database.forgottenFavorites().collectAsState(initial = emptyList())
    val mostPlayedSongs by database.mostPlayedSongs(7).collectAsState(initial = emptyList())
    val mostPlayedArtists by database.mostPlayedArtists(7).collectAsState(initial = emptyList())
    val mostPlayedAlbums by database.mostPlayedAlbums(7).collectAsState(initial = emptyList())
    val recentlyAdded by database.recentlyAdded(7).collectAsState(initial = emptyList())
    val recentlyPlayed by database.recentlyPlayed(7).collectAsState(initial = emptyList())

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Animation states
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }

    val viewModel: HomeViewModel = hiltViewModel()
    val homePage by viewModel.homePage.collectAsStateWithLifecycle()
    val homePageContinuation by viewModel.homePageContinuation.collectAsStateWithLifecycle()
    val explorePage by viewModel.explorePage.collectAsStateWithLifecycle()
    val accountInfo by viewModel.accountInfo.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val forgottenFavoritesSnapLayoutInfoProvider = rememberLazyGridSnapLayoutInfoProvider(
        lazyGridState = rememberLazyGridState()
    )
    val quickPicksSnapLayoutInfoProvider = rememberLazyGridSnapLayoutInfoProvider(
        lazyGridState = rememberLazyGridState()
    )

    val pullRefreshState = rememberPullToRefreshState()
    if (pullRefreshState.isRefreshing && !isRefreshing) {
        LaunchedEffect(Unit) {
            viewModel.refresh()
        }
    }

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) {
            pullRefreshState.endRefresh()
        }
    }

    val queuePlaylistId = mediaMetadata?.extras?.getString("playlistId")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullToRefresh(pullRefreshState)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Animated header with gradient
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -it },
                exit = fadeOut(tween(300))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                    Color.Transparent
                                )
                            )
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = getGreeting(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "O que você quer ouvir hoje?",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Quick actions with animations
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(tween(600, delayMillis = 200)) + slideInHorizontally(tween(600, delayMillis = 200)) { -it }
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        QuickActionCard(
                            title = "Mix do Dia",
                            icon = Icons.Rounded.MusicNote,
                            gradient = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF6366F1),
                                    Color(0xFF8B5CF6)
                                )
                            ),
                            onClick = {
                                coroutineScope.launch {
                                    val songs = quickPicks.shuffled().take(30)
                                    playerConnection.playQueue(
                                        ListQueue(
                                            title = "Mix do Dia",
                                            items = songs.map { it.toMediaItem() }
                                        )
                                    )
                                }
                            }
                        )
                    }
                    
                    item {
                        QuickActionCard(
                            title = "Descobrir",
                            icon = Icons.Rounded.Explore,
                            gradient = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFF59E0B),
                                    Color(0xFFEF4444)
                                )
                            ),
                            onClick = {
                                navController.navigate("mood_and_genres")
                            }
                        )
                    }
                    
                    item {
                        QuickActionCard(
                            title = "Favoritas",
                            icon = Icons.Rounded.Favorite,
                            gradient = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFEC4899),
                                    Color(0xFFF43F5E)
                                )
                            ),
                            onClick = {
                                coroutineScope.launch {
                                    val songs = database.likedSongs().first()
                                    playerConnection.playQueue(
                                        ListQueue(
                                            title = "Favoritas",
                                            items = songs.map { it.toMediaItem() }
                                        )
                                    )
                                }
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Navigation tiles with stagger animation
            val tileAnimations = remember { List(6) { mutableStateOf(false) } }
            
            LaunchedEffect(isVisible) {
                if (isVisible) {
                    tileAnimations.forEachIndexed { index, animState ->
                        delay(100L * index)
                        animState.value = true
                    }
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnimatedNavigationTile(
                        title = stringResource(R.string.history),
                        icon = Icons.Rounded.History,
                        isVisible = tileAnimations[0].value,
                        onClick = { navController.navigate("history") },
                        modifier = Modifier.weight(1f)
                    )

                    AnimatedNavigationTile(
                        title = stringResource(R.string.stats),
                        icon = Icons.AutoMirrored.Rounded.TrendingUp,
                        isVisible = tileAnimations[1].value,
                        onClick = { navController.navigate("stats") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnimatedNavigationTile(
                        title = stringResource(R.string.mood_and_genres),
                        icon = Icons.Rounded.Interests,
                        isVisible = tileAnimations[2].value,
                        onClick = { navController.navigate("mood_and_genres") },
                        modifier = Modifier.weight(1f)
                    )

                    AnimatedNavigationTile(
                        title = stringResource(R.string.account),
                        icon = Icons.Rounded.Person,
                        isVisible = tileAnimations[3].value,
                        onClick = { navController.navigate("account") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    AnimatedNavigationTile(
                        title = stringResource(R.string.smart_recommendations),
                        icon = Icons.Rounded.Psychology,
                        isVisible = tileAnimations[4].value,
                        onClick = { navController.navigate("smart_recommendations") },
                        modifier = Modifier.weight(1f)
                    )

                    AnimatedNavigationTile(
                        title = stringResource(R.string.settings),
                        icon = Icons.Rounded.Settings,
                        isVisible = tileAnimations[5].value,
                        onClick = { navController.navigate("settings") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Rest of the content sections
            quickPicks?.takeIf { it.isNotEmpty() }?.let { quickPicks ->
                NavigationTitle(
                    title = stringResource(R.string.quick_picks),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickPicks.take(10)) { song ->
                        SongCard(
                            song = song,
                            onClick = {
                                playerConnection.playQueue(YouTubeQueue.radio(song.toMediaMetadata()))
                            }
                        )
                    }
                }
                
                Spacer(Modifier.height(16.dp))
            }
        }

        Indicator(
            isRefreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(LocalPlayerAwareWindowInsets.current.asPaddingValues()),
        )
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    gradient: Brush,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .size(120.dp)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(gradient)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun AnimatedNavigationTile(
    title: String,
    icon: ImageVector,
    isVisible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(400)) + scaleIn(tween(400)),
        modifier = modifier
    ) {
        NavigationTile(
            title = title,
            icon = icon,
            onClick = onClick,
            modifier = Modifier
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(16.dp),
                    clip = false
                )
        )
    }
}

@Composable
fun SongCard(
    song: Song,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(150.dp)
            .height(180.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            AsyncImage(
                model = song.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = song.artists.joinToString { it.name },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

fun getGreeting(): String {
    val hour = LocalTime.now().hour
    return when (hour) {
        in 0..11 -> "Bom dia!"
        in 12..17 -> "Boa tarde!"
        else -> "Boa noite!"
    }
} 