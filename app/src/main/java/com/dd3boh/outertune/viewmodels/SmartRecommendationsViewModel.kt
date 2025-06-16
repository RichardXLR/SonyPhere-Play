package com.dd3boh.outertune.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dd3boh.outertune.db.MusicDatabase
import com.dd3boh.outertune.db.entities.Song
import com.dd3boh.outertune.utils.SmartRecommendationEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmartRecommendationsViewModel @Inject constructor(
    private val database: MusicDatabase,
    private val recommendationEngine: SmartRecommendationEngine
) : ViewModel() {
    
    private val _recommendations = MutableStateFlow<List<Song>>(emptyList())
    val recommendations: StateFlow<List<Song>> = _recommendations.asStateFlow()
    
    private val _moodRecommendations = MutableStateFlow<List<Song>>(emptyList())
    val moodRecommendations: StateFlow<List<Song>> = _moodRecommendations.asStateFlow()
    
    private val _timeRecommendations = MutableStateFlow<List<Song>>(emptyList())
    val timeRecommendations: StateFlow<List<Song>> = _timeRecommendations.asStateFlow()
    
    private val _discoveryRecommendations = MutableStateFlow<List<Song>>(emptyList())
    val discoveryRecommendations: StateFlow<List<Song>> = _discoveryRecommendations.asStateFlow()
    
    private val _selectedMood = MutableStateFlow<String?>(null)
    val selectedMood: StateFlow<String?> = _selectedMood.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    init {
        loadRecommendations()
    }
    
    fun selectMood(mood: String) {
        _selectedMood.value = mood
        loadMoodRecommendations(mood)
    }
    
    private fun loadRecommendations() {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                // Get all available songs
                val allSongs = database.songs(
                    sortType = com.dd3boh.outertune.constants.SongSortType.CREATE_DATE,
                    descending = false
                ).first()
                
                // Get smart recommendations
                _recommendations.value = recommendationEngine.getRecommendations(
                    availableSongs = allSongs,
                    currentMood = _selectedMood.value,
                    limit = 20
                )
                
                // Get time-based recommendations
                _timeRecommendations.value = recommendationEngine.getTimeBasedRecommendations(
                    availableSongs = allSongs,
                    limit = 15
                )
                
                // Get discovery recommendations
                _discoveryRecommendations.value = recommendationEngine.getDiscoveryRecommendations(
                    availableSongs = allSongs,
                    limit = 15
                )
                
                // Load mood recommendations if a mood is selected
                _selectedMood.value?.let { mood ->
                    loadMoodRecommendations(mood)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun loadMoodRecommendations(mood: String) {
        viewModelScope.launch {
            try {
                val allSongs = database.songs(
                    sortType = com.dd3boh.outertune.constants.SongSortType.CREATE_DATE,
                    descending = false
                ).first()
                _moodRecommendations.value = recommendationEngine.getMoodBasedRecommendations(
                    mood = mood,
                    availableSongs = allSongs,
                    limit = 15
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun refresh() {
        loadRecommendations()
    }
    
    fun recordListening(song: Song) {
        // Record the listening event for better recommendations
        recommendationEngine.recordListening(
            song = song,
            // In a real implementation, these values would come from audio analysis
            genre = null,
            mood = _selectedMood.value,
            tempo = 120f,
            energy = 0.5f,
            acousticness = 0.5f,
            danceability = 0.5f,
            valence = 0.5f
        )
    }
} 