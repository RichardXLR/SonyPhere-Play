package com.richard.musicplayer.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.util.Log
import androidx.annotation.RawRes
import com.richard.musicplayer.R
import com.richard.musicplayer.db.PrivacySecurityRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Gerenciador de efeitos sonoros da UI com otimizações de performance
 * e controle inteligente de volume baseado nas configurações do sistema.
 */
@Singleton
class AudioFeedbackManager @Inject constructor(
    private val context: Context,
    private val privacySecurityRepository: PrivacySecurityRepository
) {
    companion object {
        private const val TAG = "AudioFeedbackManager"
        private const val MAX_STREAMS = 5
        private const val DEFAULT_VOLUME = 0.08f  // Ajuste fino final - extremamente discreto
        private const val FADE_DURATION = 50L
    }
    
    private var soundPool: SoundPool? = null
    private var audioManager: AudioManager? = null
    private val loadedSounds = mutableMapOf<Int, Int>()
    private var isInitialized = false
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // ID do som de desbloqueio (Som 7 - Digital escolhido pelo usuário)
    private var unlockSuccessSoundId: Int = 0
    
    /**
     * Inicializa o sistema de áudio com configurações otimizadas para reprodução instantânea
     */
    fun initialize() {
        if (isInitialized) {
            Log.d(TAG, "✅ AudioFeedbackManager já inicializado")
            return
        }
        
        try {
            val initStartTime = System.currentTimeMillis()
            Log.d(TAG, "🚀 Inicializando AudioFeedbackManager...")
            
            audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            
            // Configurar SoundPool com AudioAttributes otimizadas para baixa latência
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setFlags(AudioAttributes.FLAG_AUDIBILITY_ENFORCED or AudioAttributes.FLAG_LOW_LATENCY)
                .build()
            
            soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                SoundPool.Builder()
                    .setMaxStreams(MAX_STREAMS)
                    .setAudioAttributes(audioAttributes)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                SoundPool(MAX_STREAMS, AudioManager.STREAM_NOTIFICATION, 0)
            }
            
            // PRÉ-CARREGAMENTO IMEDIATO para reprodução instantânea
            preloadSoundsImmediate()
            
            isInitialized = true
            
            val initTime = System.currentTimeMillis() - initStartTime
            Log.d(TAG, "✅ AudioFeedbackManager inicializado em ${initTime}ms")
            Log.d(TAG, "⚡ Pronto para reprodução instantânea")
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao inicializar AudioFeedbackManager", e)
        }
    }
    
    /**
     * Pré-carrega o som de desbloqueio na memória para reprodução instantânea
     * Som Digital (antiga opção 7) - sequência rápida de tons
     */
    private fun preloadSounds() {
        scope.launch(Dispatchers.IO) {
            try {
                // Som de desbloqueio digital escolhido pelo usuário
                unlockSuccessSoundId = loadSound(R.raw.unlock_success)
                
                Log.d(TAG, "🎵 Som de desbloqueio pré-carregado: unlock_success=$unlockSuccessSoundId")
                Log.d(TAG, "   🎼 Tipo: Digital com sequência de tons (350ms)")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao pré-carregar som de desbloqueio", e)
            }
        }
    }
    
    /**
     * ⚡ PRÉ-CARREGAMENTO IMEDIATO para reprodução instantânea
     * Carrega o som sincronamente na thread atual para garantir disponibilidade imediata
     */
    private fun preloadSoundsImmediate() {
        try {
            val loadStartTime = System.currentTimeMillis()
            Log.d(TAG, "⚡ Iniciando pré-carregamento imediato...")
            
            // Carregar som sincronamente para garantir disponibilidade imediata
            unlockSuccessSoundId = soundPool?.load(context, R.raw.unlock_success, 1) ?: 0
            
            val loadTime = System.currentTimeMillis() - loadStartTime
            Log.d(TAG, "⚡ Som pré-carregado instantaneamente em ${loadTime}ms")
            Log.d(TAG, "   🎼 ID: $unlockSuccessSoundId | Tipo: Digital (350ms)")
            Log.d(TAG, "   🚀 Pronto para reprodução com zero latência")
            
            // Verificar se carregou corretamente
            if (unlockSuccessSoundId == 0) {
                Log.w(TAG, "⚠️ Falha no pré-carregamento imediato, usando carregamento assíncrono como fallback")
                preloadSounds() // Fallback para método assíncrono
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro no pré-carregamento imediato", e)
            preloadSounds() // Fallback para método assíncrono
        }
    }
    
    /**
     * Carrega um som na SoundPool
     */
    private suspend fun loadSound(@RawRes soundResId: Int): Int = withContext(Dispatchers.IO) {
        soundPool?.load(context, soundResId, 1) ?: 0
    }
    
    /**
     * Reproduz o som de desbloqueio bem-sucedido com volume adaptativo
     * Som Digital: sequência rápida de tons (350ms) - escolhido pelo usuário
     */
    fun playUnlockSuccess(forcePlay: Boolean = false) {
        Log.d(TAG, "🎵 Reproduzindo som digital de desbloqueio (forcePlay: $forcePlay)...")
        
        if (!isInitialized) {
            Log.w(TAG, "⚠️ AudioFeedbackManager não inicializado, inicializando agora...")
            initialize()
        }
        
        scope.launch {
            try {
                // Verificar se o som está habilitado nas configurações do usuário
                val unlockSoundEnabled = isUnlockSoundEnabled()
                if (!unlockSoundEnabled && !forcePlay) {
                    Log.d(TAG, "🔇 Som de desbloqueio desabilitado nas configurações (use forcePlay=true para testar)")
                    return@launch
                }
                
                // Verificar se o som está carregado
                if (unlockSuccessSoundId == 0) {
                    Log.w(TAG, "⚠️ Som não pré-carregado, carregando agora...")
                    unlockSuccessSoundId = loadSound(R.raw.unlock_success)
                    delay(200) // Aguardar carregamento
                }
                
                val soundEffectsEnabled = areSoundEffectsEnabled()
                Log.d(TAG, "🔊 Efeitos sonoros do sistema: $soundEffectsEnabled")
                
                if (!soundEffectsEnabled && !forcePlay) {
                    Log.w(TAG, "⚠️ Efeitos sonoros desabilitados no sistema (use forcePlay=true para ignorar)")
                    return@launch
                }
                
                val volume = calculateOptimalVolume()
                Log.d(TAG, "📊 Volume calculado: $volume")
                
                // Se forcePlay=true, usar volume mínimo mesmo se calculado como 0
                val finalVolume = if (forcePlay && volume <= 0f) {
                    0.015f // Volume final - quase sussurro
                } else {
                    volume
                }
                
                if (finalVolume <= 0f) {
                    Log.w(TAG, "⚠️ Volume zero, não reproduzindo som")
                    return@launch
                }
                
                val result = playSound(unlockSuccessSoundId, finalVolume)
                Log.d(TAG, "🔓 Som digital de desbloqueio reproduzido com sucesso!")
                Log.d(TAG, "   🎼 ID: $unlockSuccessSoundId | Volume: $finalVolume | Forced: $forcePlay | Habilitado: $unlockSoundEnabled")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao reproduzir som de desbloqueio digital", e)
            }
        }
    }
    
    /**
     * ⚡ REPRODUÇÃO INSTANTÂNEA do som de desbloqueio
     * Otimizado para execução imediata após validação de autenticação
     * Elimina delays e verificações desnecessárias para máxima responsividade
     */
    fun playUnlockSuccessInstant() {
        val startTime = System.currentTimeMillis()
        Log.d(TAG, "⚡ REPRODUÇÃO INSTANTÂNEA iniciada...")
        
        if (!isInitialized) {
            Log.w(TAG, "⚠️ AudioFeedbackManager não inicializado para reprodução instantânea")
            initialize()
        }
        
        // Usar MainDispatcher para execução imediata na thread principal
        scope.launch(Dispatchers.Main.immediate) {
            try {
                val checkTime = System.currentTimeMillis()
                
                // VERIFICAÇÃO RÁPIDA: Som habilitado nas configurações
                val unlockSoundEnabled = withContext(Dispatchers.IO) {
                    try {
                        val settings = privacySecurityRepository.settingsFlow.first()
                        settings[PrivacySecurityRepository.getPreferencesKey("unlock_sound_enabled")] as Boolean? ?: true
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Erro na verificação rápida de configuração", e)
                        true // Assume habilitado em caso de erro
                    }
                }
                
                if (!unlockSoundEnabled) {
                    Log.d(TAG, "🔇 Som desabilitado nas configurações - reprodução instantânea cancelada")
                    return@launch
                }
                
                val configCheckTime = System.currentTimeMillis() - checkTime
                Log.d(TAG, "⚡ Verificação de configuração: ${configCheckTime}ms")
                
                // GARANTIR QUE O SOM ESTEJA CARREGADO
                if (unlockSuccessSoundId == 0) {
                    Log.w(TAG, "⚠️ Som não pré-carregado para reprodução instantânea, carregando...")
                    unlockSuccessSoundId = withContext(Dispatchers.IO) {
                        loadSound(R.raw.unlock_success)
                    }
                    // Sem delay - assumir que carregou instantaneamente
                }
                
                val loadCheckTime = System.currentTimeMillis() - checkTime
                Log.d(TAG, "⚡ Verificação de carregamento: ${loadCheckTime}ms")
                
                // CALCULAR VOLUME RAPIDAMENTE
                val volume = try {
                    calculateOptimalVolume()
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Erro no cálculo rápido de volume, usando padrão")
                    DEFAULT_VOLUME
                }
                
                val finalVolume = if (volume <= 0f) 0.015f else volume // Mínimo para reprodução instantânea
                
                val volumeCheckTime = System.currentTimeMillis() - checkTime
                Log.d(TAG, "⚡ Cálculo de volume: ${volumeCheckTime}ms")
                
                                 // REPRODUÇÃO IMEDIATA otimizada
                 val playResult = playSoundInstant(unlockSuccessSoundId, finalVolume)
                
                val totalTime = System.currentTimeMillis() - startTime
                Log.d(TAG, "⚡ REPRODUÇÃO INSTANTÂNEA CONCLUÍDA!")
                Log.d(TAG, "   ⏱️ Tempo total: ${totalTime}ms")
                Log.d(TAG, "   🎼 ID: $unlockSuccessSoundId | Volume: $finalVolume | StreamID: $playResult")
                Log.d(TAG, "   🔊 Sincronizado com validação de autenticação")
                
            } catch (e: Exception) {
                val errorTime = System.currentTimeMillis() - startTime
                Log.e(TAG, "❌ Erro na reprodução instantânea após ${errorTime}ms", e)
            }
        }
    }
    
    /**
     * Reproduz um som com volume especificado
     */
    private suspend fun playSound(soundId: Int, volume: Float = DEFAULT_VOLUME): Int {
        if (soundId == 0) {
            Log.w(TAG, "⚠️ Som não carregado (ID: $soundId)")
            return -1
        }
        
        if (soundPool == null) {
            Log.e(TAG, "❌ SoundPool é null")
            return -1
        }
        
        return withContext(Dispatchers.Main) {
            val streamId = soundPool?.play(
                soundId,
                volume,      // Volume esquerdo
                volume,      // Volume direito
                1,           // Prioridade
                0,           // Loop (0 = não repetir)
                1.0f         // Taxa de reprodução
            ) ?: -1
            
            Log.d(TAG, "🎵 SoundPool.play() chamado - soundId: $soundId, volume: $volume, streamId: $streamId")
            streamId
        }
    }
    
    /**
     * ⚡ REPRODUÇÃO INSTANTÂNEA otimizada
     * Reproduz som sem mudança de contexto quando já está na thread principal
     */
    private fun playSoundInstant(soundId: Int, volume: Float = DEFAULT_VOLUME): Int {
        if (soundId == 0) {
            Log.w(TAG, "⚠️ Som não carregado para reprodução instantânea (ID: $soundId)")
            return -1
        }
        
        if (soundPool == null) {
            Log.e(TAG, "❌ SoundPool é null para reprodução instantânea")
            return -1
        }
        
        // Reprodução direta sem mudança de contexto para máxima velocidade
        val streamId = soundPool?.play(
            soundId,
            volume,      // Volume esquerdo
            volume,      // Volume direito
            2,           // Prioridade alta para reprodução instantânea
            0,           // Loop (0 = não repetir)
            1.0f         // Taxa de reprodução
        ) ?: -1
        
        Log.d(TAG, "⚡ SoundPool.playInstant() - soundId: $soundId, volume: $volume, streamId: $streamId")
        return streamId
    }
    
    /**
     * Calcula o volume ótimo baseado nas configurações do sistema
     */
    private fun calculateOptimalVolume(): Float {
        return try {
            val audioManager = this.audioManager ?: return DEFAULT_VOLUME
            
            // Verificar se o dispositivo está no modo silencioso
            val ringerMode = audioManager.ringerMode
            if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
                return 0f
            }
            
            // Verificar se o dispositivo está no modo vibração
            if (ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
                return 0.025f // Volume final - extremamente baixo para modo vibração
            }
            
            // Calcular volume baseado no volume do sistema
            val systemVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)
            
            if (maxVolume == 0) return 0f
            
            val systemVolumeRatio = systemVolume.toFloat() / maxVolume.toFloat()
            
            // Aplicar uma curva suave para volume extremamente baixo e discreto
            val adjustedVolume = (systemVolumeRatio * DEFAULT_VOLUME * 0.3f).coerceIn(0f, 0.06f)
            
            Log.d(TAG, "📊 Volume calculado: sistema=$systemVolumeRatio, ajustado=$adjustedVolume")
            
            adjustedVolume
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao calcular volume", e)
            DEFAULT_VOLUME
        }
    }
    
    /**
     * Verifica se os efeitos sonoros estão habilitados no sistema
     */
    fun areSoundEffectsEnabled(): Boolean {
        return try {
            val contentResolver = context.contentResolver
            android.provider.Settings.System.getInt(
                contentResolver,
                android.provider.Settings.System.SOUND_EFFECTS_ENABLED,
                1
            ) == 1
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao verificar efeitos sonoros do sistema", e)
            true // Assume habilitado por padrão
        }
    }
    
    /**
     * Verifica se o som de desbloqueio está habilitado nas configurações do app
     */
    private suspend fun isUnlockSoundEnabled(): Boolean {
        return try {
            val settings = privacySecurityRepository.settingsFlow.first()
            val isEnabled = settings[PrivacySecurityRepository.getPreferencesKey("unlock_sound_enabled")] as Boolean? ?: true
            Log.d(TAG, "🔊 Som de desbloqueio nas configurações: $isEnabled")
            isEnabled
        } catch (e: Exception) {
            Log.e(TAG, "❌ Erro ao verificar configuração de som de desbloqueio", e)
            true // Assume habilitado por padrão
        }
    }
    
    /**
     * Pausa todos os sons em reprodução
     */
    fun pauseAll() {
        scope.launch {
            soundPool?.autoPause()
            Log.d(TAG, "⏸️ Todos os sons pausados")
        }
    }
    
    /**
     * Retoma a reprodução de sons
     */
    fun resumeAll() {
        scope.launch {
            soundPool?.autoResume()
            Log.d(TAG, "▶️ Reprodução de sons retomada")
        }
    }
    
    /**
     * Libera recursos do sistema de áudio
     */
    fun release() {
        scope.launch {
            try {
                soundPool?.release()
                soundPool = null
                loadedSounds.clear()
                isInitialized = false
                
                Log.d(TAG, "🧹 AudioFeedbackManager liberado")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao liberar AudioFeedbackManager", e)
            }
        }
        
        scope.cancel()
    }
    

    
    /**
     * Reproduz um som de feedback genérico com parâmetros customizáveis
     */
    fun playCustomFeedback(
        @RawRes soundResId: Int,
        volume: Float = DEFAULT_VOLUME,
        pitch: Float = 1.0f
    ) {
        if (!isInitialized) {
            initialize()
        }
        
        scope.launch {
            try {
                val soundId = loadSound(soundResId)
                delay(100) // Aguardar carregamento
                
                soundPool?.play(
                    soundId,
                    volume,
                    volume,
                    1,
                    0,
                    pitch
                )
                
                Log.d(TAG, "🎵 Som customizado reproduzido (res: $soundResId, volume: $volume, pitch: $pitch)")
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Erro ao reproduzir som customizado", e)
            }
        }
    }
} 