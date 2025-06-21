package com.richard.musicplayer.playback


import android.media.audiofx.Equalizer
import android.media.audiofx.Visualizer
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import kotlin.math.sin
import kotlinx.coroutines.withContext

class AudioEffects private constructor(private val audioSessionId: Int) {

    private var equalizer: Equalizer? = null
    private var visualizer: Visualizer? = null
    

    
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
    
    private val _visualizerData = MutableStateFlow<ByteArray?>(null)
    val visualizerData: StateFlow<ByteArray?> = _visualizerData.asStateFlow()
    

    
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
    
    init {

        
        // Initialize Equalizer
        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                try {
                    enabled = true
                } catch (e: Exception) {
                    // silent fail
                }
            }
        } catch (e: Exception) {
            Log.e("AudioEffects", "Failed to initialize Equalizer", e)
            equalizer = null
        }
        
        // Initialize Visualizer
        try {
            visualizer = Visualizer(audioSessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]
                setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(visualizer: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
                        _visualizerData.value = waveform
                    }
                    
                    override fun onFftDataCapture(visualizer: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                        // Not used for now
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, false)
                
                enabled = true
            }
        } catch (e: Exception) {
            Log.e("AudioEffects", "Failed to initialize Visualizer", e)
            visualizer = null
        }
    }
    

    
    // 🔄 FUNÇÃO DE RESET COMPLETO PARA EVITAR ACÚMULO
    private suspend fun resetAllAudioEffects() {

        
        // Reset Equalizer
        equalizer?.let { eq ->
            try {
                if (eq.enabled) {
                    for (i in 0 until eq.numberOfBands) {
                        eq.setBandLevel(i.toShort(), 0.toShort())
                    }
                }
            } catch (e: Exception) {
                Log.w("AudioEffects", "Error resetting Equalizer", e)
            }
        }
        
        // Wait for hardware to process
        delay(100)
    }


    

    
    private fun tryRecoverEqualizer() {
        try {
            equalizer?.release() // Liberar o antigo primeiro
            equalizer = null
            
            // Recriar com a mesma sessão
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }
        } catch (e: Exception) {
            Log.e("AudioEffects", "❌ Failed to recover Equalizer", e)
            equalizer = null
        }
    }
    
    fun release() {
        try {
            equalizer?.release()
            visualizer?.release()
            instance = null
        } catch (e: Exception) {
            Log.e("AudioEffects", "Error releasing AudioEffects", e)
        }
    }
    
    // Fake 8D Audio System
    /**
     * Enable/disable Fake 8D effect - much more perceptible than spatial audio
     */
    fun setFake8DEnabled(enabled: Boolean) {
        fake8DEnabled = enabled
        
        if (enabled) {
            startFake8DEffect()
        } else {
            stopFake8DEffect()
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
                    }
                }
            }
        } catch (e: Exception) {
            Log.w("AudioEffects", "Error applying True 8D effect", e)
        }
    }
    
    fun isFake8DEnabled(): Boolean = fake8DEnabled
    

    
    // 🚨 FUNÇÃO DE EMERGÊNCIA: Reset completo do sistema de áudio
    suspend fun emergencyAudioReset() = withContext(Dispatchers.IO) {
        try {
            // 1. Use the centralized reset function
            resetAllAudioEffects()
            
            // 2. Disable and re-enable effects
            equalizer?.let { eq ->
                try {
                    eq.enabled = false
                    delay(100)
                    eq.enabled = true
                    delay(100)
                } catch (e: Exception) {
                    Log.w("AudioEffects", "Erro ao resetar Equalizer", e)
                }
            }
            
            // 3. Aguardar estabilização do sistema
            delay(500)
            
        } catch (e: Exception) {
            Log.e("AudioEffects", "❌ Falha no reset de emergência", e)
            
            // 4. Last resort: recreate everything
            tryRecoverEqualizer()
        }
    }
    
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
    

} 