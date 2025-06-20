package com.richard.musicplayer.playback

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Visualizer
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import kotlin.math.sin

class AudioEffects(private val audioSessionId: Int) {
    private var bassBoost: BassBoost? = null
    private var equalizer: Equalizer? = null
    private var visualizer: Visualizer? = null
    
    private val _bassLevel = MutableStateFlow(0f) // 0-100
    private val bassLevel: StateFlow<Float> = _bassLevel.asStateFlow()
    
    private val _waveform = MutableStateFlow(ByteArray(0))
    val waveform: StateFlow<ByteArray> = _waveform.asStateFlow()
    
    private val _fft = MutableStateFlow(ByteArray(0))
    val fft: StateFlow<ByteArray> = _fft.asStateFlow()
    
    private val _frequencyBands = MutableStateFlow(FloatArray(32) { 0f })
    val frequencyBands: StateFlow<FloatArray> = _frequencyBands.asStateFlow()
    
    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()
    
    // Fake 8D Audio System
    private var fake8DEnabled = false
    private var fake8DTimer: Timer? = null
    private var fake8DAngle = 0f
    
    init {
        initializeEffects()
    }
    
    private fun initializeEffects() {
        Log.d("AudioEffects", "Initializing audio effects for session ID: $audioSessionId")
        
        // Verificar se o audioSessionId é válido
        if (audioSessionId <= 0) {
            Log.e("AudioEffects", "Invalid audio session ID: $audioSessionId")
            return
        }
        
        Log.d("AudioEffects", "Starting initialization with valid session ID: $audioSessionId")
        
        // Initialize Bass Boost
        try {
            bassBoost = BassBoost(0, audioSessionId).apply {
                // Verificar se o dispositivo suporta strength antes de habilitar
                if (strengthSupported) {
                    enabled = true
                    Log.d("AudioEffects", "BassBoost initialized successfully with strength support")
                } else {
                    Log.w("AudioEffects", "BassBoost strength not supported, will use equalizer-only bass")
                    // Não habilitar se não suporta strength, usaremos só equalizer
                    enabled = false
                }
            }
        } catch (e: Exception) {
            Log.e("AudioEffects", "Failed to initialize BassBoost, will use equalizer-only bass", e)
            bassBoost = null
        }
        
        // Initialize Equalizer
        try {
            equalizer = Equalizer(0, audioSessionId)
            equalizer?.let { eq ->
                // Tentar habilitar o equalizer
                try {
                    eq.enabled = true
                    Log.d("AudioEffects", "Equalizer enabled successfully")
                } catch (enableE: Exception) {
                    Log.e("AudioEffects", "Failed to enable Equalizer", enableE)
                    // Tentar forçar habilitação após um delay
                    try {
                        Thread.sleep(100)
                        eq.enabled = true
                        Log.d("AudioEffects", "Equalizer enabled on retry")
                    } catch (retryE: Exception) {
                        Log.e("AudioEffects", "Failed to enable Equalizer on retry", retryE)
                    }
                }
                
                Log.d("AudioEffects", "Equalizer initialized - enabled: ${eq.enabled}, bands: ${eq.numberOfBands}")
                
                // Log das frequências para debug
                for (i in 0 until eq.numberOfBands) {
                    val freq = eq.getCenterFreq(i.toShort())
                    Log.v("AudioEffects", "Band $i: ${freq/1000}kHz")
                }
            }
        } catch (e: Exception) {
            Log.e("AudioEffects", "Failed to initialize Equalizer", e)
            equalizer = null
        }
        
        // Initialize Visualizer for animations
        try {
            visualizer = Visualizer(audioSessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1] // Max size
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int
                        ) {
                            waveform?.let { 
                                _waveform.value = it
                                // Calculate amplitude from waveform
                                val amplitude = it.map { byte -> kotlin.math.abs(byte.toInt()) }.average() / 128f
                                _amplitude.value = amplitude.toFloat().coerceIn(0f, 1f)
                            }
                        }
                        
                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) {
                            fft?.let { 
                                _fft.value = it
                                // Process FFT data into frequency bands
                                processFFTData(it)
                            }
                        }
                    },
                    Visualizer.getMaxCaptureRate() / 2,
                    true,
                    true
                )
                enabled = true
            }
            Log.d("AudioEffects", "Visualizer initialized successfully")
        } catch (e: Exception) {
            Log.e("AudioEffects", "Failed to initialize Visualizer", e)
            visualizer = null
        }
        
        val effectsCount = listOfNotNull(bassBoost, equalizer, visualizer).size
        Log.d("AudioEffects", "Audio effects initialization completed. $effectsCount/3 effects available")
        
        // Verificar estado final dos efeitos
        Log.d("AudioEffects", "Final status:")
        Log.d("AudioEffects", "- BassBoost: ${if (bassBoost != null) "available (enabled: ${bassBoost?.enabled}, strengthSupported: ${bassBoost?.strengthSupported})" else "null"}")
        Log.d("AudioEffects", "- Equalizer: ${if (equalizer != null) "available (enabled: ${equalizer?.enabled}, bands: ${equalizer?.numberOfBands})" else "null"}")
        Log.d("AudioEffects", "- Visualizer: ${if (visualizer != null) "available (enabled: ${visualizer?.enabled})" else "null"}")
    }
    
    fun setBassLevel(level: Float) {
        val clampedLevel = level.coerceIn(0f, 100f)
        _bassLevel.value = clampedLevel
        
        Log.d("AudioEffects", "Setting bass level to: $clampedLevel%")
        
        var bassApplied = false
        
        // Tentar usar BassBoost primeiro (se disponível e funcional)
        bassBoost?.let { boost ->
            try {
                if (boost.strengthSupported && boost.enabled) {
                    val strength = (clampedLevel * 10).toInt().coerceIn(0, 1000)
                    boost.setStrength(strength.toShort())
                    Log.d("AudioEffects", "BassBoost strength set to: $strength")
                    bassApplied = true
                } else {
                    Log.d("AudioEffects", "BassBoost not available, using equalizer-only approach")
                }
            } catch (e: Exception) {
                Log.w("AudioEffects", "BassBoost failed, falling back to equalizer", e)
            }
        }
        
        // Sempre aplicar também via equalizer (para reforçar o efeito ou como fallback)
        equalizer?.let { eq ->
            try {
                // Verificar se equalizer está habilitado, se não, tentar habilitar
                if (!eq.enabled) {
                    Log.w("AudioEffects", "Equalizer is not enabled, trying to enable it")
                    try {
                        eq.enabled = true
                        Log.d("AudioEffects", "Successfully enabled Equalizer")
                    } catch (enableE: Exception) {
                        Log.e("AudioEffects", "Failed to enable Equalizer during bass adjustment", enableE)
                        return@let
                    }
                }
                
                val numBands = eq.numberOfBands
                Log.d("AudioEffects", "Applying bass boost via equalizer (${numBands} bands) - enabled: ${eq.enabled}")
                
                // Usar gain mais forte se BassBoost não funcionou
                val gainMultiplier = if (bassApplied) 0.15f else 0.5f
                
                for (i in 0 until numBands) {
                    try {
                        val freq = eq.getCenterFreq(i.toShort())
                        
                        // Aplicar boost em diferentes frequências de bass
                        val gain = when {
                            freq < 80000 -> { // Sub-bass (20-80Hz) - boost máximo
                                (clampedLevel * gainMultiplier * 1.5f * 1000).toInt().coerceIn(-1500, 1500)
                            }
                            freq < 250000 -> { // Bass (80-250Hz) - boost médio
                                (clampedLevel * gainMultiplier * 1000).toInt().coerceIn(-1500, 1500)
                            }
                            freq < 500000 -> { // Low-mid (250-500Hz) - boost suave
                                (clampedLevel * gainMultiplier * 0.5f * 1000).toInt().coerceIn(-1500, 1500)
                            }
                            else -> 0 // Não mexer nas frequências mais altas
                        }
                        
                        // SEMPRE aplicar o gain (mesmo se for 0) para resetar quando necessário
                        if (freq < 500000) { // Aplicar apenas nas frequências de bass
                            eq.setBandLevel(i.toShort(), gain.toShort())
                            Log.v("AudioEffects", "Band $i (${freq/1000}kHz): bass gain set to $gain")
                        }
                    } catch (e: Exception) {
                        Log.w("AudioEffects", "Failed to adjust band $i", e)
                    }
                }
                
                Log.d("AudioEffects", "Equalizer bass boost applied successfully")
                bassApplied = true
                
            } catch (e: Exception) {
                Log.e("AudioEffects", "Error applying equalizer bass boost", e)
            }
        }
        
        if (bassApplied) {
            Log.d("AudioEffects", "Bass level successfully applied: $clampedLevel%")
        } else {
            Log.e("AudioEffects", "Failed to apply bass boost - no working audio effects available")
        }
    }
    
    fun getBassLevel(): Float = _bassLevel.value
    
    fun release() {
        bassBoost?.release()
        equalizer?.release()
        visualizer?.release()
        
        bassBoost = null
        equalizer = null
        visualizer = null
    }
    
    // Fake 8D Audio System
    /**
     * Enable/disable Fake 8D effect - much more perceptible than spatial audio
     */
    fun setFake8DEnabled(enabled: Boolean) {
        fake8DEnabled = enabled
        
        if (enabled) {
            startFake8DEffect()
            Log.d("AudioEffects", "Fake 8D effect enabled - should be very noticeable!")
        } else {
            stopFake8DEffect()
            Log.d("AudioEffects", "Fake 8D effect disabled")
        }
    }
    
    private fun startFake8DEffect() {
        fake8DTimer?.cancel()
        fake8DAngle = 0f
        
        fake8DTimer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    if (fake8DEnabled) {
                        fake8DAngle += 0.3f // Ultra fast spatial movement
                        applyFake8DEffect(fake8DAngle)
                    }
                }
            }, 0, 80) // Update every 80ms for smoother effect
        }
    }
    
    private fun stopFake8DEffect() {
        fake8DTimer?.cancel()
        fake8DTimer = null
        
        // Reset equalizer to flat
        try {
            equalizer?.let { eq ->
                if (eq.enabled) {
                    for (i in 0 until eq.numberOfBands) {
                        eq.setBandLevel(i.toShort(), 0.toShort())
                    }
                    Log.d("AudioEffects", "Equalizer reset to flat after disabling Fake 8D")
                }
            }
        } catch (e: Exception) {
            Log.w("AudioEffects", "Error resetting equalizer", e)
        }
    }
    
    private fun applyFake8DEffect(angle: Float) {
        try {
            equalizer?.let { eq ->
                if (eq.enabled) {
                    val numBands = eq.numberOfBands
                    if (numBands >= 3) {
                        // Create TRUE spatial movement using alternating frequency patterns
                        val leftWave = sin(angle)
                        val rightWave = sin(angle + Math.PI.toFloat()) // 180 degrees out of phase
                        
                        // Different frequencies oscillate in opposite directions - this creates spatial illusion
                        val bassLeft = (leftWave * 1200).toInt().coerceIn(-1500, 1500)
                        val bassRight = (rightWave * 800).toInt().coerceIn(-1500, 1500)
                        
                        val midLeft = (rightWave * 1000).toInt().coerceIn(-1500, 1500)  // Opposite phase to bass
                        val midRight = (leftWave * 600).toInt().coerceIn(-1500, 1500)
                        
                        val highLeft = (leftWave * 800).toInt().coerceIn(-1500, 1500)
                        val highRight = (rightWave * 1200).toInt().coerceIn(-1500, 1500)
                        
                        // Apply alternating pattern to create L/R movement illusion
                        if (numBands >= 5) {
                            val centerBand = numBands / 2
                            
                            // Low frequencies - simulate left ear emphasis
                            eq.setBandLevel(0.toShort(), bassLeft.toShort())
                            eq.setBandLevel(1.toShort(), bassRight.toShort())
                            
                            // Mid frequencies - opposite pattern
                            eq.setBandLevel(centerBand.toShort(), midLeft.toShort())
                            if (centerBand + 1 < numBands) {
                                eq.setBandLevel((centerBand + 1).toShort(), midRight.toShort())
                            }
                            
                            // High frequencies - back to original pattern
                            eq.setBandLevel((numBands - 2).toShort(), highLeft.toShort())
                            eq.setBandLevel((numBands - 1).toShort(), highRight.toShort())
                            
                            // Add phase-shifted modulation for enhanced 3D effect
                            val phase2 = sin(angle * 1.5f)
                            if (numBands >= 7) {
                                eq.setBandLevel(2.toShort(), (phase2 * 600).toInt().toShort())
                                eq.setBandLevel((numBands - 3).toShort(), (-phase2 * 800).toInt().toShort())
                            }
                        }
                        
                        Log.v("AudioEffects", "True8D: bassL=$bassLeft, bassR=$bassRight, midL=$midLeft, midR=$midRight, highL=$highLeft, highR=$highRight")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("AudioEffects", "Error applying True 8D effect", e)
        }
    }
    
    fun isFake8DEnabled(): Boolean = fake8DEnabled
    
    private fun processFFTData(fft: ByteArray) {
        try {
            val bandCount = 32
            val bands = FloatArray(bandCount)
            val fftSize = fft.size / 2 // FFT data is complex, so real size is half
            val bandSize = fftSize / bandCount
            
            for (i in 0 until bandCount) {
                var bandSum = 0f
                val startIndex = i * bandSize * 2 // Multiply by 2 for complex data
                val endIndex = kotlin.math.min((i + 1) * bandSize * 2, fft.size)
                
                for (j in startIndex until endIndex step 2) {
                    if (j + 1 < fft.size) {
                        val real = fft[j].toFloat()
                        val imag = fft[j + 1].toFloat()
                        val magnitude = kotlin.math.sqrt(real * real + imag * imag)
                        bandSum += magnitude
                    }
                }
                
                // Normalize
                bands[i] = (bandSum / bandSize / 128f).coerceIn(0f, 1f)
            }
            
            _frequencyBands.value = bands
        } catch (e: Exception) {
            Log.e("AudioEffects", "Error processing FFT data", e)
        }
    }
    
    companion object {
        @Volatile
        private var instance: AudioEffects? = null
        
        fun getInstance(audioSessionId: Int): AudioEffects {
            return instance ?: synchronized(this) {
                instance ?: AudioEffects(audioSessionId).also { instance = it }
            }
        }
        
        fun releaseInstance() {
            instance?.release()
            instance = null
        }
    }
} 