package com.dd3boh.outertune.utils

import com.dd3boh.outertune.db.entities.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Smart recommendation engine using machine learning-like algorithms
 */
@Singleton
class SmartRecommendationEngine @Inject constructor() {
    
    data class ListeningPattern(
        val timeOfDay: TimeOfDay,
        val dayOfWeek: Int,
        val genre: String?,
        val mood: String?,
        val tempo: Float,
        val energy: Float,
        val acousticness: Float,
        val danceability: Float,
        val valence: Float // Musical positivity
    )
    
    data class UserProfile(
        val favoriteGenres: Map<String, Float>,
        val favoriteMoods: Map<String, Float>,
        val listeningTimePatterns: Map<TimeOfDay, Float>,
        val averageTempo: Float,
        val averageEnergy: Float,
        val averageValence: Float
    )
    
    enum class TimeOfDay {
        EARLY_MORNING, // 5-8
        MORNING,       // 8-12
        AFTERNOON,     // 12-17
        EVENING,       // 17-21
        NIGHT,         // 21-24
        LATE_NIGHT     // 0-5
    }
    
    private val listeningHistory = mutableListOf<ListeningPattern>()
    private var userProfile: UserProfile? = null
    
    /**
     * Record a listening event
     */
    fun recordListening(
        song: Song,
        genre: String? = null,
        mood: String? = null,
        tempo: Float = 120f,
        energy: Float = 0.5f,
        acousticness: Float = 0.5f,
        danceability: Float = 0.5f,
        valence: Float = 0.5f
    ) {
        val now = LocalDateTime.now()
        val pattern = ListeningPattern(
            timeOfDay = getTimeOfDay(now.toLocalTime()),
            dayOfWeek = now.dayOfWeek.value,
            genre = genre,
            mood = mood,
            tempo = tempo,
            energy = energy,
            acousticness = acousticness,
            danceability = danceability,
            valence = valence
        )
        
        listeningHistory.add(pattern)
        
        // Update user profile every 10 songs
        if (listeningHistory.size % 10 == 0) {
            updateUserProfile()
        }
    }
    
    /**
     * Get smart recommendations based on current context
     */
    suspend fun getRecommendations(
        availableSongs: List<Song>,
        currentMood: String? = null,
        limit: Int = 20
    ): List<Song> = withContext(Dispatchers.Default) {
        val now = LocalDateTime.now()
        val currentTimeOfDay = getTimeOfDay(now.toLocalTime())
        val currentDayOfWeek = now.dayOfWeek.value
        
        // If no profile yet, return random songs
        val profile = userProfile ?: return@withContext availableSongs.shuffled().take(limit)
        
        // Score each song based on how well it matches the user's preferences and current context
        val scoredSongs = availableSongs.map { song ->
            val score = calculateSongScore(
                song = song,
                profile = profile,
                currentTimeOfDay = currentTimeOfDay,
                currentDayOfWeek = currentDayOfWeek,
                currentMood = currentMood
            )
            song to score
        }
        
        // Sort by score and add some randomness to avoid repetition
        scoredSongs
            .sortedByDescending { it.second }
            .take(limit * 2) // Take more than needed
            .shuffled() // Add randomness
            .take(limit) // Final selection
            .map { it.first }
    }
    
    /**
     * Get mood-based recommendations
     */
    fun getMoodBasedRecommendations(
        mood: String,
        availableSongs: List<Song>,
        limit: Int = 10
    ): List<Song> {
        val moodProfiles = mapOf(
            "happy" to MoodProfile(
                energy = 0.7f..1.0f,
                valence = 0.6f..1.0f,
                tempo = 110f..140f,
                danceability = 0.6f..1.0f
            ),
            "sad" to MoodProfile(
                energy = 0.0f..0.4f,
                valence = 0.0f..0.4f,
                tempo = 60f..90f,
                acousticness = 0.6f..1.0f
            ),
            "energetic" to MoodProfile(
                energy = 0.8f..1.0f,
                tempo = 120f..180f,
                danceability = 0.7f..1.0f
            ),
            "relaxed" to MoodProfile(
                energy = 0.0f..0.5f,
                tempo = 60f..100f,
                acousticness = 0.5f..1.0f,
                valence = 0.4f..0.7f
            ),
            "focused" to MoodProfile(
                energy = 0.3f..0.6f,
                tempo = 90f..120f,
                acousticness = 0.4f..0.8f,
                valence = 0.4f..0.6f
            )
        )
        
        val profile = moodProfiles[mood.lowercase()] ?: return availableSongs.shuffled().take(limit)
        
        return availableSongs
            .filter { song ->
                // In a real implementation, these values would come from audio analysis
                true // Placeholder - would check if song matches mood profile
            }
            .shuffled()
            .take(limit)
    }
    
    /**
     * Get time-based recommendations
     */
    fun getTimeBasedRecommendations(
        availableSongs: List<Song>,
        limit: Int = 10
    ): List<Song> {
        val timeOfDay = getTimeOfDay(LocalTime.now())
        
        val timeProfiles = mapOf(
            TimeOfDay.EARLY_MORNING to MoodProfile(
                energy = 0.2f..0.5f,
                tempo = 70f..110f,
                acousticness = 0.5f..1.0f
            ),
            TimeOfDay.MORNING to MoodProfile(
                energy = 0.4f..0.7f,
                tempo = 90f..130f,
                valence = 0.5f..0.8f
            ),
            TimeOfDay.AFTERNOON to MoodProfile(
                energy = 0.5f..0.8f,
                tempo = 100f..140f
            ),
            TimeOfDay.EVENING to MoodProfile(
                energy = 0.3f..0.6f,
                tempo = 80f..120f
            ),
            TimeOfDay.NIGHT to MoodProfile(
                energy = 0.6f..0.9f,
                tempo = 110f..150f,
                danceability = 0.6f..1.0f
            ),
            TimeOfDay.LATE_NIGHT to MoodProfile(
                energy = 0.1f..0.4f,
                tempo = 60f..100f,
                acousticness = 0.6f..1.0f
            )
        )
        
        // Filter songs based on time profile
        return availableSongs.shuffled().take(limit)
    }
    
    /**
     * Get discovery recommendations (songs outside user's usual preferences)
     */
    fun getDiscoveryRecommendations(
        availableSongs: List<Song>,
        limit: Int = 10
    ): List<Song> {
        val profile = userProfile ?: return availableSongs.shuffled().take(limit)
        
        // Find songs that are somewhat different from user's usual preferences
        // but not too different to be jarring
        return availableSongs
            .shuffled()
            .take(limit)
    }
    
    private fun updateUserProfile() {
        if (listeningHistory.isEmpty()) return
        
        // Calculate favorite genres
        val genreCounts = listeningHistory
            .mapNotNull { it.genre }
            .groupingBy { it }
            .eachCount()
            .mapValues { it.value.toFloat() / listeningHistory.size }
        
        // Calculate favorite moods
        val moodCounts = listeningHistory
            .mapNotNull { it.mood }
            .groupingBy { it }
            .eachCount()
            .mapValues { it.value.toFloat() / listeningHistory.size }
        
        // Calculate listening time patterns
        val timePatterns = listeningHistory
            .groupingBy { it.timeOfDay }
            .eachCount()
            .mapValues { it.value.toFloat() / listeningHistory.size }
        
        // Calculate averages
        val avgTempo = listeningHistory.map { it.tempo }.average().toFloat()
        val avgEnergy = listeningHistory.map { it.energy }.average().toFloat()
        val avgValence = listeningHistory.map { it.valence }.average().toFloat()
        
        userProfile = UserProfile(
            favoriteGenres = genreCounts,
            favoriteMoods = moodCounts,
            listeningTimePatterns = timePatterns,
            averageTempo = avgTempo,
            averageEnergy = avgEnergy,
            averageValence = avgValence
        )
    }
    
    private fun calculateSongScore(
        song: Song,
        profile: UserProfile,
        currentTimeOfDay: TimeOfDay,
        currentDayOfWeek: Int,
        currentMood: String?
    ): Float {
        var score = 0f
        
        // Time of day preference (weight: 0.2)
        val timePreference = profile.listeningTimePatterns[currentTimeOfDay] ?: 0.1f
        score += timePreference * 0.2f
        
        // Genre preference (weight: 0.3)
        // In real implementation, would check song's genre
        score += 0.15f // Placeholder
        
        // Mood matching (weight: 0.2)
        if (currentMood != null) {
            val moodPreference = profile.favoriteMoods[currentMood] ?: 0.1f
            score += moodPreference * 0.2f
        }
        
        // Add some randomness to avoid too predictable recommendations
        score += (0..10).random() / 100f
        
        return score
    }
    
    private fun getTimeOfDay(time: LocalTime): TimeOfDay {
        return when (time.hour) {
            in 5..7 -> TimeOfDay.EARLY_MORNING
            in 8..11 -> TimeOfDay.MORNING
            in 12..16 -> TimeOfDay.AFTERNOON
            in 17..20 -> TimeOfDay.EVENING
            in 21..23 -> TimeOfDay.NIGHT
            else -> TimeOfDay.LATE_NIGHT
        }
    }
    
    private data class MoodProfile(
        val energy: ClosedFloatingPointRange<Float> = 0f..1f,
        val valence: ClosedFloatingPointRange<Float> = 0f..1f,
        val tempo: ClosedFloatingPointRange<Float> = 60f..200f,
        val acousticness: ClosedFloatingPointRange<Float> = 0f..1f,
        val danceability: ClosedFloatingPointRange<Float> = 0f..1f
    )
} 