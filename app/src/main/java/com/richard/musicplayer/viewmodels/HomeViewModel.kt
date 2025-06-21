package com.richard.musicplayer.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.richard.musicplayer.constants.PlaylistFilter
import com.richard.musicplayer.constants.PlaylistSortType
import com.richard.musicplayer.db.MusicDatabase
import com.richard.musicplayer.db.entities.Album
import com.richard.musicplayer.db.entities.LocalItem
import com.richard.musicplayer.db.entities.Song
import com.richard.musicplayer.models.SimilarRecommendation
import com.richard.musicplayer.utils.SyncUtils
import com.richard.musicplayer.utils.reportException
import com.zionhuang.innertube.YouTube
import com.zionhuang.innertube.models.PlaylistItem
import com.zionhuang.innertube.models.WatchEndpoint
import com.zionhuang.innertube.models.YTItem
import com.zionhuang.innertube.pages.ExplorePage
import com.zionhuang.innertube.pages.HomePage
import com.zionhuang.innertube.utils.completed
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext context: Context,
    val database: MusicDatabase,
    val syncUtils: SyncUtils
) : ViewModel() {
    val isRefreshing = MutableStateFlow(false)
    val isLoading = MutableStateFlow(false)

    // 🚀 CACHE PARA EVITAR RECARREGAMENTOS DESNECESSÁRIOS
    private var lastLoadTime = 0L
    private val cacheExpiryTime = 10 * 60 * 1000L // 10 minutos
    
    val quickPicks = MutableStateFlow<List<Song>?>(null)
    val forgottenFavorites = MutableStateFlow<List<Song>?>(null)
    val keepListening = MutableStateFlow<List<LocalItem>?>(null)
    val similarRecommendations = MutableStateFlow<List<SimilarRecommendation>?>(null)
    val accountPlaylists = MutableStateFlow<List<PlaylistItem>?>(null)
    val homePage = MutableStateFlow<HomePage?>(null)
    val selectedChip = MutableStateFlow<HomePage.Chip?>(null)
    private val previousHomePage = MutableStateFlow<HomePage?>(null)
    val explorePage = MutableStateFlow<ExplorePage?>(null)
    
    // 🚀 LAZY LOADING para playlists e activities recentes
    val playlists = database.playlists(PlaylistFilter.LIBRARY, PlaylistSortType.NAME, true)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
    val recentActivity = database.recentActivity()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val allLocalItems = MutableStateFlow<List<LocalItem>>(emptyList())
    val allYtItems = MutableStateFlow<List<YTItem>>(emptyList())

    private suspend fun load() {
        // 🚀 CACHE CHECK: Evitar recarregamento se dados ainda são válidos
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastLoadTime < cacheExpiryTime && quickPicks.value != null) {
            return // Cache ainda válido, não recarregar
        }
        
        isLoading.value = true

        // 🚀 OPTIMIZED: Reduzir quantidade de dados para melhor performance
        quickPicks.value = database.quickPicks()
            .first().shuffled().take(15) // Reduzido de 20 para 15

        forgottenFavorites.value = database.forgottenFavorites()
            .first().shuffled().take(15) // Reduzido de 20 para 15

        val fromTimeStamp = System.currentTimeMillis() - 86400000 * 7 * 2
        val keepListeningSongs = database.mostPlayedSongs(fromTimeStamp, limit = 12, offset = 5) // Reduzido
            .first().shuffled().take(8) // Reduzido de 10 para 8
        val keepListeningAlbums = database.mostPlayedAlbums(fromTimeStamp, limit = 6, offset = 2) // Reduzido
            .first().filter { it.album.thumbnailUrl != null }.shuffled().take(4) // Reduzido
        val keepListeningArtists = database.mostPlayedArtists(fromTimeStamp)
            .first().filter { it.artist.isYouTubeArtist && it.artist.thumbnailUrl != null }.shuffled().take(4) // Reduzido
        keepListening.value = (keepListeningSongs + keepListeningAlbums + keepListeningArtists).shuffled()

        allLocalItems.value =
            (quickPicks.value.orEmpty() + forgottenFavorites.value.orEmpty() + keepListening.value.orEmpty())
                .filter { it is Song || it is Album }

        // 🚀 LAZY LOAD: YouTube data apenas se necessário
        if (YouTube.cookie != null) {
            YouTube.library("FEmusic_liked_playlists").completed().onSuccess {
                accountPlaylists.value = it.items.filterIsInstance<PlaylistItem>()
            }.onFailure {
                reportException(it)
            }
        }

        // 🚀 OPTIMIZED: Reduzir quantidade de recomendações similares
        val artistRecommendations =
            database.mostPlayedArtists(fromTimeStamp, limit = 6).first() // Reduzido de 10 para 6
                .filter { it.artist.isYouTubeArtist }
                .shuffled().take(2) // Reduzido de 3 para 2
                .mapNotNull {
                    val items = mutableListOf<YTItem>()
                    YouTube.artist(it.id).onSuccess { page ->
                        items += page.sections.getOrNull(page.sections.size - 2)?.items.orEmpty()
                        items += page.sections.lastOrNull()?.items.orEmpty()
                    }
                    SimilarRecommendation(
                        title = it,
                        items = items
                            .shuffled()
                            .ifEmpty { return@mapNotNull null }
                    )
                }
        
        val songRecommendations =
            database.mostPlayedSongs(fromTimeStamp, limit = 8).first() // Reduzido de 10 para 8
                .filter { it.album != null }
                .shuffled().take(1) // Reduzido de 2 para 1
                .mapNotNull { song ->
                    val endpoint = YouTube.next(WatchEndpoint(videoId = song.id)).getOrNull()?.relatedEndpoint ?: return@mapNotNull null
                    val page = YouTube.related(endpoint).getOrNull() ?: return@mapNotNull null
                    SimilarRecommendation(
                        title = song,
                        items = (page.songs.shuffled().take(6) + // Reduzido de 8 para 6
                                page.albums.shuffled().take(3) + // Reduzido de 4 para 3
                                page.artists.shuffled().take(3) + // Reduzido de 4 para 3
                                page.playlists.shuffled().take(3)) // Reduzido de 4 para 3
                            .shuffled()
                            .ifEmpty { return@mapNotNull null }
                    )
                }
        similarRecommendations.value = (artistRecommendations + songRecommendations).shuffled()

        // 🚀 BACKGROUND LOADING: Carregar YouTube data em background
        viewModelScope.launch(Dispatchers.IO) {
            YouTube.home().onSuccess { page ->
                homePage.value = page
            }.onFailure {
                reportException(it)
            }

            YouTube.explore().onSuccess { page ->
                explorePage.value = page
            }.onFailure {
                reportException(it)
            }

            allYtItems.value = similarRecommendations.value?.flatMap { it.items }.orEmpty() +
                    homePage.value?.sections?.flatMap { it.items }.orEmpty()
        }

        // 🚀 BACKGROUND SYNC: Executar sync em background separado
        viewModelScope.launch(Dispatchers.IO) {
            syncUtils.syncRecentActivity()
        }

        // 🚀 ATUALIZAR CACHE TIMESTAMP
        lastLoadTime = currentTime
        isLoading.value = false
    }
    
    private val _isLoadingMore = MutableStateFlow(false)
    fun loadMoreYouTubeItems(continuation: String?) {
        if (continuation == null || _isLoadingMore.value) return

        viewModelScope.launch(Dispatchers.IO) {
            _isLoadingMore.value = true
            val nextSections = YouTube.home(continuation).getOrNull() ?: run {
                _isLoadingMore.value = false
                return@launch
            }
            homePage.value = nextSections.copy(
                chips = homePage.value?.chips,
                sections = homePage.value?.sections.orEmpty() + nextSections.sections
            )
            _isLoadingMore.value = false
        }
    }

    fun toggleChip(chip: HomePage.Chip?) {
        if (chip == null || chip == selectedChip.value && previousHomePage.value != null) {
            homePage.value = previousHomePage.value
            previousHomePage.value = null
            selectedChip.value = null
            return
        }

        if (selectedChip.value == null) {
            // store the actual homepage for deselecting chips
            previousHomePage.value = homePage.value
        }
        viewModelScope.launch(Dispatchers.IO) {
            val nextSections = YouTube.home(params = chip?.endpoint?.params).getOrNull() ?: return@launch
            homePage.value = nextSections.copy(
                chips = homePage.value?.chips,
                sections = nextSections.sections,
                continuation = nextSections.continuation
            )
            selectedChip.value = chip
        }
    }

    fun refresh() {
        if (isRefreshing.value) return
        viewModelScope.launch(Dispatchers.IO) {
            isRefreshing.value = true
            load()
            isRefreshing.value = false
        }
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            load()
            viewModelScope.launch(Dispatchers.IO) { syncUtils.tryAutoSync() }
        }
    }
}
