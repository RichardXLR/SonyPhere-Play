package com.richard.musicplayer.playback

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.audiofx.*
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*
import java.util.Timer
import java.util.TimerTask

/**
 * Sistema avançado de áudio espacial com suporte a efeitos 3D
 */
class SpatialAudio private constructor(
    private val context: Context,
    private val audioSessionId: Int
) {
    companion object {
        private const val TAG = "SpatialAudio"
        private var instance: SpatialAudio? = null
        
        fun getInstance(context: Context, audioSessionId: Int): SpatialAudio {
            return instance ?: synchronized(this) {
                instance ?: SpatialAudio(context, audioSessionId).also { instance = it }
            }
        }
        
        fun releaseInstance() {
            instance?.release()
            instance = null
        }
    }
    
    // Audio effects
    private var virtualizer: Virtualizer? = null
    private var environmentalReverb: EnvironmentalReverb? = null
    private var bassBoost: BassBoost? = null
    private var equalizer: Equalizer? = null
    private var presetReverb: PresetReverb? = null
    
    // Spatial audio state
    private val _spatialEnabled = MutableStateFlow(false)
    val spatialEnabled: StateFlow<Boolean> = _spatialEnabled.asStateFlow()
    
    private val _virtualizerStrength = MutableStateFlow(0)
    val virtualizerStrength: StateFlow<Int> = _virtualizerStrength.asStateFlow()
    
    private val _roomType = MutableStateFlow(SpatialRoomType.NONE)
    val roomType: StateFlow<SpatialRoomType> = _roomType.asStateFlow()
    
    private val _headTracking = MutableStateFlow(false)
    val headTracking: StateFlow<Boolean> = _headTracking.asStateFlow()
    
    // 3D Position tracking
    private val _listenerPosition = MutableStateFlow(Position3D(0f, 0f, 0f))
    val listenerPosition: StateFlow<Position3D> = _listenerPosition.asStateFlow()
    
    private val _soundSourcePosition = MutableStateFlow(Position3D(0f, 0f, -1f))
    val soundSourcePosition: StateFlow<Position3D> = _soundSourcePosition.asStateFlow()
    
    // Head tracking timer
    private var headTrackingTimer: Timer? = null
    
    init {
        initializeAudioEffects()
    }
    
    private fun initializeAudioEffects() {
        try {
            // Initialize Virtualizer for 3D audio simulation with modern error handling
            try {
                if (isVirtualizerSupported()) {
                    virtualizer = Virtualizer(0, audioSessionId).apply {
                        enabled = false
                    }
                    Log.d(TAG, "Virtualizer initialized successfully")
                } else {
                    Log.w(TAG, "Virtualizer not supported on this device")
                }
            } catch (e: UnsupportedOperationException) {
                Log.w(TAG, "Virtualizer API not available on this Android version", e)
            } catch (e: Exception) {
                Log.w(TAG, "Virtualizer initialization failed, continuing without it", e)
            }
            
            // Initialize Environmental Reverb for room simulation
            try {
                environmentalReverb = EnvironmentalReverb(0, audioSessionId).apply {
                    enabled = false
                }
                Log.d(TAG, "Environmental Reverb initialized")
            } catch (e: Exception) {
                Log.w(TAG, "Environmental Reverb not available", e)
            }
            
            // Initialize Preset Reverb as fallback
            try {
                presetReverb = PresetReverb(0, audioSessionId).apply {
                    enabled = false
                }
                Log.d(TAG, "Preset Reverb initialized")
            } catch (e: Exception) {
                Log.w(TAG, "Preset Reverb not available", e)
            }
            
            // Initialize Bass Boost for low-frequency enhancement
            try {
                bassBoost = BassBoost(0, audioSessionId).apply {
                    enabled = false
                }
                Log.d(TAG, "Bass Boost initialized")
            } catch (e: Exception) {
                Log.w(TAG, "Bass Boost not available", e)
            }
            
            // Initialize Equalizer for frequency adjustment
            try {
                equalizer = Equalizer(0, audioSessionId).apply {
                    enabled = false
                }
                Log.d(TAG, "Equalizer initialized")
            } catch (e: Exception) {
                Log.w(TAG, "Equalizer not available", e)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing audio effects", e)
        }
    }
    
    /**
     * Check if Virtualizer is supported with modern compatibility checks
     */
    private fun isVirtualizerSupported(): Boolean {
        return try {
            // Try to create a temporary instance to test availability
            val testVirtualizer = try {
                Virtualizer(0, 0)
            } catch (e: Exception) {
                Log.d(TAG, "Virtualizer not available: ${e.message}")
                return false
            }
            
            // Clean up test instance
            try {
                testVirtualizer.release()
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
            
            // Additional check for Android version compatibility
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ may have restricted access to audio effects
                Log.d(TAG, "Android 13+ detected, using enhanced fallback mode")
                return true // We'll handle gracefully in the actual usage
            }
            
            true
        } catch (e: Exception) {
            Log.w(TAG, "Error checking Virtualizer availability", e)
            false
        }
    }
    
    /**
     * Enable/disable spatial audio with modern fallback support
     */
    fun setSpatialEnabled(enabled: Boolean) {
        _spatialEnabled.value = enabled
        
        // Modern Virtualizer handling with graceful degradation
        try {
            virtualizer?.let { virt ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    // Android 13+ graceful handling
                    try {
                        virt.enabled = enabled
                        Log.d(TAG, "Modern Virtualizer ${if (enabled) "enabled" else "disabled"}")
                    } catch (e: SecurityException) {
                        Log.w(TAG, "Virtualizer access restricted by system, using equalizer fallback")
                        enableEqualizerSpatialFallback(enabled)
                    }
                } else {
                    // Pre-Android 13 handling
                    virt.enabled = enabled
                    Log.d(TAG, "Legacy Virtualizer ${if (enabled) "enabled" else "disabled"}")
                }
            } ?: run {
                // No virtualizer available, use equalizer-based spatial effect
                Log.d(TAG, "No Virtualizer available, using equalizer-based spatial simulation")
                enableEqualizerSpatialFallback(enabled)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting virtualizer enabled state, falling back to equalizer", e)
            enableEqualizerSpatialFallback(enabled)
        }
        
        try {
            environmentalReverb?.let { 
                it.enabled = enabled && roomType.value != SpatialRoomType.NONE 
                Log.d(TAG, "Environmental reverb ${if (enabled && roomType.value != SpatialRoomType.NONE) "enabled" else "disabled"}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting environmental reverb enabled state", e)
        }
        
        try {
            presetReverb?.let { 
                it.enabled = enabled && roomType.value != SpatialRoomType.NONE 
                Log.d(TAG, "Preset reverb ${if (enabled && roomType.value != SpatialRoomType.NONE) "enabled" else "disabled"}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting preset reverb enabled state", e)
        }
        
        if (enabled) {
            applyCurrentSettings()
        }
        
        Log.d(TAG, "Spatial audio ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Fallback spatial audio simulation using equalizer when Virtualizer is not available
     */
    private fun enableEqualizerSpatialFallback(enabled: Boolean) {
        try {
            equalizer?.let { eq ->
                if (enabled) {
                    // Apply spatial-like EQ curve that simulates 3D audio
                    val numBands = eq.numberOfBands
                    if (numBands >= 5) {
                        // Create a spatial-like frequency response
                        eq.setBandLevel(0.toShort(), 200.toShort())           // Slight bass boost
                        eq.setBandLevel(1.toShort(), (-100).toShort())        // Slight low-mid cut
                        eq.setBandLevel((numBands / 2).toShort(), 300.toShort()) // Mid boost for presence
                        eq.setBandLevel((numBands - 2).toShort(), 400.toShort()) // High-mid boost
                        eq.setBandLevel((numBands - 1).toShort(), 600.toShort()) // High boost for clarity
                        
                        Log.d(TAG, "Equalizer-based spatial simulation enabled")
                    }
                } else {
                    // Reset to flat response
                    for (i in 0 until eq.numberOfBands) {
                        eq.setBandLevel(i.toShort(), 0.toShort())
                    }
                    Log.d(TAG, "Equalizer-based spatial simulation disabled")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error applying equalizer spatial fallback", e)
        }
    }
    
    /**
     * Set virtualizer strength with modern compatibility handling
     */
    fun setVirtualizerStrength(strength: Int) {
        val clampedStrength = strength.coerceIn(0, 1000)
        _virtualizerStrength.value = clampedStrength
        
        virtualizer?.let { virtualizer ->
            try {
                if (!virtualizer.enabled) {
                    Log.w(TAG, "Virtualizer is not enabled")
                    return@let
                }
                
                // Modern compatibility check for strength support
                val strengthSupported = try {
                    virtualizer.strengthSupported
                } catch (e: Exception) {
                    Log.w(TAG, "Cannot query strength support, assuming false")
                    false
                }
                
                if (strengthSupported) {
                    try {
                        virtualizer.setStrength(clampedStrength.toShort())
                        Log.d(TAG, "Virtualizer strength set to $clampedStrength")
                    } catch (e: SecurityException) {
                        Log.w(TAG, "Virtualizer strength setting restricted, using equalizer modulation")
                        modulateEqualizerForVirtualizerStrength(clampedStrength)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error setting virtualizer strength", e)
                        modulateEqualizerForVirtualizerStrength(clampedStrength)
                    }
                } else {
                    Log.w(TAG, "Virtualizer strength not supported, using equalizer modulation")
                    modulateEqualizerForVirtualizerStrength(clampedStrength)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting virtualizer strength", e)
                modulateEqualizerForVirtualizerStrength(clampedStrength)
            }
        } ?: run {
            // No virtualizer, use equalizer modulation
            modulateEqualizerForVirtualizerStrength(clampedStrength)
        }
    }
    
    /**
     * Simulate virtualizer strength using equalizer modulation
     */
    private fun modulateEqualizerForVirtualizerStrength(strength: Int) {
        try {
            equalizer?.let { eq ->
                if (eq.enabled && eq.numberOfBands >= 3) {
                    val factor = strength / 1000f
                    val highBoost = (factor * 800).toInt().coerceIn(0, 1500)
                    val midCut = (factor * -200).toInt().coerceIn(-1500, 0)
                    
                    val numBands = eq.numberOfBands
                    eq.setBandLevel((numBands - 1).toShort(), highBoost.toShort())
                    eq.setBandLevel((numBands / 2).toShort(), midCut.toShort())
                    
                    Log.d(TAG, "Equalizer virtualizer simulation: strength=$strength, high=$highBoost, mid=$midCut")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error modulating equalizer for virtualizer strength", e)
        }
    }

    /**
     * Get current 8D effect intensity (for UI feedback)
     */
    fun get8DIntensity(): Float {
        return if (headTracking.value && spatialEnabled.value) {
            val listener = listenerPosition.value
            return sqrt(listener.x * listener.x + listener.y * listener.y + listener.z * listener.z)
        } else 0f
    }
    
    /**
     * Set room type for environmental audio
     */
    fun setRoomType(type: SpatialRoomType) {
        _roomType.value = type
        applyRoomSettings(type)
    }
    
    private fun applyRoomSettings(type: SpatialRoomType) {
        when (type) {
            SpatialRoomType.NONE -> {
                environmentalReverb?.enabled = false
                presetReverb?.enabled = false
            }
            SpatialRoomType.SMALL_ROOM -> applySmallRoom()
            SpatialRoomType.MEDIUM_ROOM -> applyMediumRoom()
            SpatialRoomType.LARGE_ROOM -> applyLargeRoom()
            SpatialRoomType.CONCERT_HALL -> applyConcertHall()
            SpatialRoomType.CATHEDRAL -> applyCathedral()
            SpatialRoomType.OUTDOOR -> applyOutdoor()
        }
    }
    
    private fun applySmallRoom() {
        environmentalReverb?.let { reverb ->
            try {
                reverb.enabled = spatialEnabled.value
                reverb.roomLevel = (-1000).toShort()
                reverb.roomHFLevel = (-600).toShort()
                reverb.decayTime = 500
                reverb.decayHFRatio = 500.toShort()
                reverb.reflectionsLevel = (-2000).toShort()
                reverb.reflectionsDelay = 7
                reverb.reverbLevel = (-1200).toShort()
                reverb.reverbDelay = 11
                reverb.diffusion = 1000.toShort()
                reverb.density = 1000.toShort()
            } catch (e: Exception) {
                Log.w(TAG, "Error applying small room settings", e)
            }
        }
    }
    
    private fun applyMediumRoom() {
        environmentalReverb?.let { reverb ->
            try {
                reverb.enabled = spatialEnabled.value
                reverb.roomLevel = (-800).toShort()
                reverb.roomHFLevel = (-400).toShort()
                reverb.decayTime = 1000
                reverb.decayHFRatio = 700.toShort()
                reverb.reflectionsLevel = (-1500).toShort()
                reverb.reflectionsDelay = 15
                reverb.reverbLevel = (-1000).toShort()
                reverb.reverbDelay = 22
                reverb.diffusion = 1000.toShort()
                reverb.density = 1000.toShort()
            } catch (e: Exception) {
                Log.w(TAG, "Error applying medium room settings", e)
            }
        }
    }
    
    private fun applyLargeRoom() {
        environmentalReverb?.let { reverb ->
            try {
                reverb.enabled = spatialEnabled.value
                reverb.roomLevel = (-600).toShort()
                reverb.roomHFLevel = (-200).toShort()
                reverb.decayTime = 2000
                reverb.decayHFRatio = 600.toShort()
                reverb.reflectionsLevel = (-1000).toShort()
                reverb.reflectionsDelay = 25
                reverb.reverbLevel = (-800).toShort()
                reverb.reverbDelay = 40
                reverb.diffusion = 1000.toShort()
                reverb.density = 1000.toShort()
            } catch (e: Exception) {
                Log.w(TAG, "Error applying large room settings", e)
            }
        }
    }
    
    private fun applyConcertHall() {
        presetReverb?.let { reverb ->
            reverb.enabled = spatialEnabled.value
            reverb.preset = PresetReverb.PRESET_LARGEHALL
        }
    }
    
    private fun applyCathedral() {
        environmentalReverb?.let { reverb ->
            try {
                reverb.enabled = spatialEnabled.value
                reverb.roomLevel = (-400).toShort()
                reverb.roomHFLevel = (-100).toShort()
                reverb.decayTime = 4000
                reverb.decayHFRatio = 500.toShort()
                reverb.reflectionsLevel = (-800).toShort()
                reverb.reflectionsDelay = 50
                reverb.reverbLevel = (-600).toShort()
                reverb.reverbDelay = 80
                reverb.diffusion = 1000.toShort()
                reverb.density = 1000.toShort()
            } catch (e: Exception) {
                Log.w(TAG, "Error applying cathedral settings", e)
            }
        }
    }
    
    private fun applyOutdoor() {
        environmentalReverb?.let { reverb ->
            try {
                reverb.enabled = spatialEnabled.value
                reverb.roomLevel = (-1500).toShort()
                reverb.roomHFLevel = (-1000).toShort()
                reverb.decayTime = 300
                reverb.decayHFRatio = 200.toShort()
                reverb.reflectionsLevel = (-3000).toShort()
                reverb.reflectionsDelay = 2
                reverb.reverbLevel = (-2000).toShort()
                reverb.reverbDelay = 5
                reverb.diffusion = 500.toShort()
                reverb.density = 500.toShort()
            } catch (e: Exception) {
                Log.w(TAG, "Error applying outdoor settings", e)
            }
        }
    }
    
    /**
     * Set 3D position of sound source
     */
    fun setSoundSourcePosition(x: Float, y: Float, z: Float) {
        _soundSourcePosition.value = Position3D(x, y, z)
        apply3DPositioning()
    }
    
    /**
     * Set listener position (head tracking)
     */
    fun setListenerPosition(x: Float, y: Float, z: Float) {
        _listenerPosition.value = Position3D(x, y, z)
        apply3DPositioning()
    }
    
    private fun apply3DPositioning() {
        val listener = listenerPosition.value
        val source = soundSourcePosition.value
        
        // Calculate distance and angle
        val distance = sqrt(
            (source.x - listener.x).pow(2) +
            (source.y - listener.y).pow(2) +
            (source.z - listener.z).pow(2)
        )
        
        val angle = atan2(source.y - listener.y, source.x - listener.x)
        
        // Apply spatial effects based on position
        applyDistanceEffect(distance)
        applyDirectionalEffect(angle)
    }
    
    private fun applyDistanceEffect(distance: Float) {
        // Simulate distance attenuation - more pronounced effect
        val attenuationFactor = (1f / (1f + distance * 0.3f)).coerceIn(0.2f, 1f)
        
        // Apply bass reduction for distance with more noticeable effect
        try {
            bassBoost?.let { bass ->
                if (bass.enabled) {
                    val bassReduction = ((1f - attenuationFactor) * 800).toInt()
                    val finalStrength = (1000 - bassReduction).coerceIn(0, 1000)
                    bass.setStrength(finalStrength.toShort())
                    Log.v(TAG, "Distance effect: bass strength set to $finalStrength (distance: $distance)")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error applying distance effect to bass", e)
        }
        
        // Apply distance effect to reverb intensity
        try {
            environmentalReverb?.let { reverb ->
                if (reverb.enabled) {
                    val reverbAdjustment = ((1f - attenuationFactor) * 400).toInt()
                    reverb.reverbLevel = (-800 - reverbAdjustment).toShort()
                    Log.v(TAG, "Distance effect: reverb level adjusted by $reverbAdjustment")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error applying distance effect to reverb", e)
        }
    }
    
    private fun applyDirectionalEffect(angle: Float) {
        // Simulate head-related transfer function (HRTF) with more pronounced effect
        val leftIntensity = ((cos(angle) + 1f) / 2f).coerceIn(0.1f, 1f)
        val rightIntensity = ((-cos(angle) + 1f) / 2f).coerceIn(0.1f, 1f)
        
        // Apply more noticeable directional filtering through equalizer
        try {
            equalizer?.let { eq ->
                if (eq.enabled && eq.numberOfBands >= 3) {
                    // Adjust multiple frequency bands for better spatial perception
                    val directionFactor = sin(angle)
                    
                    // High frequencies (presence and clarity)
                    val highFreqAdjustment = (directionFactor * 600).toInt().coerceIn(-1500, 1500)
                    val midFreqAdjustment = (directionFactor * -300).toInt().coerceIn(-1500, 1500)
                    
                    // Apply to different frequency bands
                    if (eq.numberOfBands >= 5) {
                        eq.setBandLevel((eq.numberOfBands - 1).toShort(), highFreqAdjustment.toShort()) // Highest band
                        eq.setBandLevel((eq.numberOfBands - 2).toShort(), (highFreqAdjustment * 0.7f).toInt().toShort()) // High-mid
                        eq.setBandLevel((eq.numberOfBands / 2).toShort(), midFreqAdjustment.toShort()) // Mid band
                        
                        Log.v(TAG, "Directional effect: high=$highFreqAdjustment, mid=$midFreqAdjustment (angle: $angle)")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error applying directional effect", e)
        }
        
        // Apply directional effect to virtualizer strength
        try {
            virtualizer?.let { virt ->
                if (virt.enabled && virt.strengthSupported) {
                    val baseStrength = virtualizerStrength.value
                    val directionModifier = (abs(sin(angle)) * 200).toInt()
                    val finalStrength = (baseStrength + directionModifier).coerceIn(0, 1000)
                    virt.setStrength(finalStrength.toShort())
                    Log.v(TAG, "Directional virtualizer: $finalStrength (base: $baseStrength, modifier: $directionModifier)")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error applying directional effect to virtualizer", e)
        }
    }
    
    /**
     * Enable head tracking (simulated)
     */
    fun setHeadTracking(enabled: Boolean) {
        _headTracking.value = enabled
        
        // Cancel existing timer
        headTrackingTimer?.cancel()
        headTrackingTimer = null
        
        if (enabled) {
            // Start simulated head tracking
            startHeadTrackingSimulation()
        } else {
            // Reset to center position
            setListenerPosition(0f, 0f, 0f)
        }
    }
    
    private fun startHeadTrackingSimulation() {
        // Cancel any existing timer
        headTrackingTimer?.cancel()
        
        // Simulate more noticeable head movements for 8D effect
        var angle = 0f
        
        headTrackingTimer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    if (headTracking.value) {
                        angle += 0.15f // Even faster movement for more dramatic 8D effect
                        
                        // Create dramatic circular panning effect
                        val panX = sin(angle) * 1.2f         // Strong left-right panning
                        val panY = cos(angle * 0.8f) * 0.8f  // Vertical component
                        val panZ = sin(angle * 0.6f) * 0.6f  // Depth component
                        
                        setListenerPosition(panX, panY, panZ)
                        
                        // Counter-movement for enhanced 8D effect
                        setSoundSourcePosition(-panX * 0.8f, -panY * 0.6f, -1f + panZ * 0.4f)
                        
                        // Direct 8D panning effect using equalizer for left/right balance
                        apply8DPanningEffect(angle)
                    }
                }
            }, 0, 80) // Faster updates for smoother movement
        }
        
        Log.d(TAG, "Enhanced 8D head tracking started with direct panning")
    }
    
    /**
     * Apply direct 8D panning effect for more noticeable audio movement
     */
    private fun apply8DPanningEffect(angle: Float) {
        try {
            equalizer?.let { eq ->
                if (eq.enabled && eq.numberOfBands >= 3) {
                    // Create strong left-right panning using frequency manipulation
                    val panFactor = sin(angle)
                    val verticalFactor = cos(angle * 0.7f)
                    
                    // More aggressive frequency adjustments for clear 8D effect
                    val bassShift = (panFactor * 800).toInt().coerceIn(-1500, 1500)       // Low freq
                    val midShift = (-panFactor * 1000).toInt().coerceIn(-1500, 1500)     // Mid freq
                    val highShift = (verticalFactor * 1200).toInt().coerceIn(-1500, 1500) // High freq
                    
                    val numBands = eq.numberOfBands
                    if (numBands >= 5) {
                                                 // Apply dramatic frequency shifts across spectrum
                         eq.setBandLevel(0.toShort(), bassShift.toShort())                    // Bass
                         eq.setBandLevel(1.toShort(), (bassShift * 0.7f).toInt().toShort())  // Low-mid  
                         eq.setBandLevel((numBands / 2).toShort(), midShift.toShort())          // Mid
                         eq.setBandLevel((numBands - 2).toShort(), (highShift * 0.8f).toInt().toShort()) // High-mid
                         eq.setBandLevel((numBands - 1).toShort(), highShift.toShort())         // High
                        
                        Log.v(TAG, "8D Panning: bass=$bassShift, mid=$midShift, high=$highShift (angle=$angle)")
                    }
                }
            }
            
            // Dramatic virtualizer strength modulation for 8D effect
            virtualizer?.let { virt ->
                if (virt.enabled && virt.strengthSupported) {
                    val baseStrength = virtualizerStrength.value
                    val modulationFactor = (abs(sin(angle * 2f)) * 400).toInt() // Strong modulation
                    val dynamicStrength = (baseStrength + modulationFactor).coerceIn(200, 1000)
                    virt.setStrength(dynamicStrength.toShort())
                    Log.v(TAG, "8D Virtualizer modulation: $dynamicStrength")
                }
            }
            
                         // Dynamic reverb modulation for spatial movement
             environmentalReverb?.let { reverb ->
                 if (reverb.enabled) {
                     val reverbModulation = (sin(angle * 1.5f) * 600).toInt()
                     val dynamicReverbLevel = (-800 + reverbModulation).coerceIn(-2000, -200)
                     reverb.reverbLevel = dynamicReverbLevel.toShort()
                     
                     // Also modulate decay time for movement effect
                     val decayModulation = (cos(angle) * 500).toInt()
                     val dynamicDecay = (1000 + decayModulation).coerceIn(300, 2000)
                     reverb.decayTime = dynamicDecay
                     
                     Log.v(TAG, "8D Reverb: level=$dynamicReverbLevel, decay=$dynamicDecay")
                 }
             }
             
             // Dynamic bass boost modulation for rhythmic 8D pulsing effect
             bassBoost?.let { bass ->
                 if (bass.enabled) {
                     val bassPulse = (sin(angle * 3f) * 400).toInt() // Faster pulsing
                     val dynamicBassStrength = (600 + bassPulse).coerceIn(0, 1000)
                     bass.setStrength(dynamicBassStrength.toShort())
                     Log.v(TAG, "8D Bass pulse: $dynamicBassStrength")
                 }
             }
            
        } catch (e: Exception) {
            Log.w(TAG, "Error applying 8D panning effect", e)
        }
    }
    
    private fun applyCurrentSettings() {
        setVirtualizerStrength(virtualizerStrength.value)
        applyRoomSettings(roomType.value)
        apply3DPositioning()
    }
    
    /**
     * Get available spatial audio features
     */
    fun getSpatialCapabilities(): SpatialCapabilities {
        return SpatialCapabilities(
            hasVirtualizer = virtualizer != null,
            hasEnvironmentalReverb = environmentalReverb != null,
            hasPresetReverb = presetReverb != null,
            hasBassBoost = bassBoost != null,
            hasEqualizer = equalizer != null,
            supportsHeadTracking = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S // Android 12+
        )
    }
    
    fun release() {
        try {
            // Cancel head tracking timer
            headTrackingTimer?.cancel()
            headTrackingTimer = null
            
            // Release all audio effects
            virtualizer?.release()
            environmentalReverb?.release()
            presetReverb?.release()
            bassBoost?.release()
            equalizer?.release()
            
            virtualizer = null
            environmentalReverb = null
            presetReverb = null
            bassBoost = null
            equalizer = null
            
            Log.d(TAG, "Spatial audio effects and timer released")
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing spatial audio effects", e)
        }
    }
}

/**
 * Data classes for spatial audio
 */
data class Position3D(
    val x: Float,
    val y: Float,
    val z: Float
)

data class SpatialCapabilities(
    val hasVirtualizer: Boolean,
    val hasEnvironmentalReverb: Boolean,
    val hasPresetReverb: Boolean,
    val hasBassBoost: Boolean,
    val hasEqualizer: Boolean,
    val supportsHeadTracking: Boolean
)

/**
 * Room types for environmental audio
 */
enum class SpatialRoomType {
    NONE,
    SMALL_ROOM,
    MEDIUM_ROOM,
    LARGE_ROOM,
    CONCERT_HALL,
    CATHEDRAL,
    OUTDOOR
} 