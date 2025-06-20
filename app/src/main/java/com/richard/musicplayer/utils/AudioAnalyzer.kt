package com.richard.musicplayer.utils

import android.media.audiofx.Visualizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * Advanced audio analyzer for visualizations and effects
 */
@Singleton
class AudioAnalyzer @Inject constructor() {
    
    private var visualizer: Visualizer? = null
    
    private val _waveformData = MutableStateFlow(ByteArray(0))
    val waveformData: StateFlow<ByteArray> = _waveformData.asStateFlow()
    
    private val _fftData = MutableStateFlow(ByteArray(0))
    val fftData: StateFlow<ByteArray> = _fftData.asStateFlow()
    
    private val _amplitudeData = MutableStateFlow(0f)
    val amplitudeData: StateFlow<Float> = _amplitudeData.asStateFlow()
    
    private val _frequencyBands = MutableStateFlow(FloatArray(0))
    val frequencyBands: StateFlow<FloatArray> = _frequencyBands.asStateFlow()
    
    fun startAnalyzing(audioSessionId: Int) {
        try {
            stopAnalyzing()
            
            visualizer = Visualizer(audioSessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]
                
                setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(
                        visualizer: Visualizer?,
                        waveform: ByteArray?,
                        samplingRate: Int
                    ) {
                        waveform?.let {
                            _waveformData.value = it
                            _amplitudeData.value = calculateAmplitude(it)
                        }
                    }
                    
                    override fun onFftDataCapture(
                        visualizer: Visualizer?,
                        fft: ByteArray?,
                        samplingRate: Int
                    ) {
                        fft?.let {
                            _fftData.value = it
                            _frequencyBands.value = calculateFrequencyBands(it)
                        }
                    }
                }, Visualizer.getMaxCaptureRate() / 2, true, true)
                
                enabled = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun stopAnalyzing() {
        visualizer?.apply {
            enabled = false
            release()
        }
        visualizer = null
    }
    
    private fun calculateAmplitude(waveform: ByteArray): Float {
        var sum = 0.0
        for (byte in waveform) {
            sum += abs(byte.toInt() - 128)
        }
        return (sum / waveform.size).toFloat() / 128f
    }
    
    private fun calculateFrequencyBands(fft: ByteArray): FloatArray {
        val bands = FloatArray(32) // 32 frequency bands
        val bandSize = fft.size / bands.size / 2
        
        for (i in bands.indices) {
            var magnitude = 0.0
            for (j in 0 until bandSize) {
                val index = i * bandSize + j
                if (index * 2 + 1 < fft.size) {
                    val real = fft[index * 2].toDouble()
                    val imag = fft[index * 2 + 1].toDouble()
                    magnitude += sqrt(real * real + imag * imag)
                }
            }
            
            // Convert to decibels and normalize
            val db = 20 * log10(magnitude / bandSize + 1)
            bands[i] = (db / 100).coerceIn(0.0, 1.0).toFloat()
        }
        
        return bands
    }
    
    /**
     * Get dominant frequency
     */
    fun getDominantFrequency(samplingRate: Int): Float {
        val fft = _fftData.value
        if (fft.isEmpty()) return 0f
        
        var maxMagnitude = 0.0
        var dominantIndex = 0
        
        for (i in 0 until fft.size / 2) {
            val real = fft[i * 2].toDouble()
            val imag = fft[i * 2 + 1].toDouble()
            val magnitude = sqrt(real * real + imag * imag)
            
            if (magnitude > maxMagnitude) {
                maxMagnitude = magnitude
                dominantIndex = i
            }
        }
        
        return dominantIndex * samplingRate.toFloat() / fft.size
    }
    
    /**
     * Detect beat
     */
    fun detectBeat(threshold: Float = 0.7f): Boolean {
        return _amplitudeData.value > threshold
    }
} 